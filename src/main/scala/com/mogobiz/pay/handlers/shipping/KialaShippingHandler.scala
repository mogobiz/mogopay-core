/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.shipping

import java.util.UUID

import com.mogobiz.pay.common.{ShippingWithQuantity, Shipping, Cart}
import com.mogobiz.pay.model.Mogopay.ShippingAddress
import org.json4s.JValue
import com.mogobiz.pay.config.MogopayHandlers._

class KialaShippingHandler extends ShippingService {

  val KIALA_PRICE = 400
  val KIALA_SHIPPING_PREFIX = "KIALA_"

  def computeFixPrice(price: Long, currencyCode: String) : Seq[ShippingPrice]= {
    Seq(createShippingPrice(KIALA_SHIPPING_PREFIX + UUID.randomUUID().toString, UUID.randomUUID().toString, "KIALA", "KIALA", "KIALA", price, currencyCode))
  }

  override def calculatePrice(shippingAddress: ShippingAddress, cart: Cart): Seq[ShippingPrice] = {

    val shippingContent = extractShippingContent(cart)

    def calculatePrice(list: List[ShippingWithQuantity]) : Option[(Long, Long)] = {
      if (list.isEmpty) None
      else {
        val prixFixeAndKiala = calculatePrice(list.tail).getOrElse((0L, 0L))
        val elt = list.head;
        if (elt.shipping.free) Some(prixFixeAndKiala)
        else if (elt.shipping.amount > 0) Some(convertStorePrice(elt.shipping.amount, cart) * elt.quantity + prixFixeAndKiala._1, prixFixeAndKiala._2)
        else Some(prixFixeAndKiala._1, rateHandler.convert(KIALA_PRICE, "EUR", cart.rate.code).getOrElse(0L))
      }
    }

    calculatePrice(shippingContent).map { prixFixeAndKiala =>
      cart.shippingRulePrice.map { shippingPriceRule =>
        computeFixPrice(convertStorePrice(shippingPriceRule, cart), cart.rate.code)
      }.getOrElse(computeFixPrice(prixFixeAndKiala._1 + prixFixeAndKiala._2, cart.rate.code))
    }.getOrElse(Seq())
  }

  override def isManageShipmentId(shippingPrice: ShippingPrice): Boolean = shippingPrice.shipmentId.startsWith(KIALA_SHIPPING_PREFIX)

  override def confirmShipmentId(shippingPrice: ShippingPrice): ShippingPrice = shippingPrice.copy(confirm = true)
}
