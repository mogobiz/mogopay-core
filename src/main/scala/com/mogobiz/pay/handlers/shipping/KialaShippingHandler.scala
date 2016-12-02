/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.shipping

import java.util.UUID

import com.mogobiz.pay.codes.MogopayConstant
import com.mogobiz.pay.common.{Cart, Shipping, ShippingWithQuantity}
import com.mogobiz.pay.model.{ShippingAddress, ShippingData, ShippingDataList}
import org.json4s.JValue
import com.mogobiz.pay.config.MogopayHandlers.handlers._

object KialaShippingHandler {
  val KIALA_PRICE           = 400
  val KIALA_SHIPPING_PREFIX = "KIALA_"
}

class KialaShippingHandler extends ShippingHandler {

  import KialaShippingHandler._

  override def computePrice(shippingAddress: ShippingAddress, cart: Cart): ShippingDataList = {
    cart.shopCarts.find(_.shopId == MogopayConstant.SHOP_MOGOBIZ).map { mogobizShop =>

      val shippingContent = extractShippingContent(mogobizShop)

      def computePrice(list: List[ShippingWithQuantity]): Option[(Long, Long)] = {
        if (list.isEmpty) None
        else {
          val prixFixeAndKiala = computePrice(list.tail).getOrElse((0L, 0L))
          val elt = list.head;
          if (elt.shipping.free) Some(prixFixeAndKiala)
          else if (elt.shipping.amount > 0)
            Some(convertStorePrice(elt.shipping.amount, cart) * elt.quantity + prixFixeAndKiala._1, prixFixeAndKiala._2)
          else Some(prixFixeAndKiala._1, rateHandler.convert(KIALA_PRICE, "EUR", cart.rate.code).getOrElse(0L))
        }
      }

      computePrice(shippingContent).map { prixFixeAndKiala =>
        cart.shippingRulePrice.map { shippingPriceRule =>
          val price = convertStorePrice(shippingPriceRule, cart)
          ShippingDataList(None, List(
            createShippingData(shippingAddress.address,
              KIALA_SHIPPING_PREFIX + UUID.randomUUID().toString,
              UUID.randomUUID().toString,
              "KIALA",
              "KIALA",
              "KIALA",
              price,
              cart.rate.code)))
        } getOrElse {
          ShippingDataList(None, List(
            createShippingData(shippingAddress.address,
              KIALA_SHIPPING_PREFIX + UUID.randomUUID().toString,
              UUID.randomUUID().toString,
              "KIALA",
              "KIALA",
              "KIALA",
              prixFixeAndKiala._1 + prixFixeAndKiala._2,
              cart.rate.code)))
        }
      } getOrElse (ShippingDataList(None, Nil))
    } getOrElse (ShippingDataList(None, Nil))
  }

  override def isValidShipmentId(shippingPrice: ShippingData): Boolean =
    shippingPrice.shipmentId.startsWith(KIALA_SHIPPING_PREFIX)

  override def confirmShipmentId(shippingPrice: ShippingData): ShippingData = shippingPrice.copy(confirm = true)
}
