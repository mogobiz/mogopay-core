/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.shipping

import java.util.UUID

import com.mogobiz.pay.common.{Shipping, Cart}
import com.mogobiz.pay.model.Mogopay.ShippingAddress
import org.json4s.JValue
import com.mogobiz.pay.config.MogopayHandlers._

class KialaShippingHandler extends ShippingService {

  val KIALA_PRICE = 400
  val KIALA_SHIPPING_PREFIX = "KIALA_"

  override def calculatePrice(shippingAddress: ShippingAddress, cart: Cart): Seq[ShippingPrice] = {

    val shippingContent = extractShippingContent(cart)

    def calculatePrice(list: List[Shipping]) : (Long, Long) = {
      if (list.isEmpty) (0, 0)
      else {
        val prixFixeAndKiala = calculatePrice(list.tail)
        val elt = list.head;
        if (elt.free) prixFixeAndKiala
        else if (elt.amount > 0) (elt.amount + prixFixeAndKiala._1, prixFixeAndKiala._2)
        else (prixFixeAndKiala._1, rateHandler.convert(KIALA_PRICE, "EUR", cart.rate.code).getOrElse(0))
      }
    }

    if (shippingContent == Nil) Seq()
    else {
      val prixFixeAndKiala = calculatePrice(shippingContent)
      val prix = prixFixeAndKiala._1 + prixFixeAndKiala._2
      Seq(createShippingPrice(KIALA_SHIPPING_PREFIX + UUID.randomUUID().toString, UUID.randomUUID().toString, "KIALA", "KIALA", "KIALA", prix, cart.rate.code))
    }
  }

  override def isManageShipmentId(shippingPrice: ShippingPrice): Boolean = shippingPrice.shipmentId.startsWith(KIALA_SHIPPING_PREFIX)

  override def confirmShipmentId(shippingPrice: ShippingPrice): Long = shippingPrice.price
}
