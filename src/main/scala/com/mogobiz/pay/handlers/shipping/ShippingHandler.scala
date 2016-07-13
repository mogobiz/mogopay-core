/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.shipping

import com.mogobiz.pay.common.{ Cart, ShippingWithQuantity }
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.model.Mogopay._
import com.typesafe.scalalogging.StrictLogging

import scala.collection.Seq

trait ShippingHandler extends StrictLogging {
  // return a list of shipping price. Each shipping price is related to a level of service, for example : Same day delivery, 3 days, ...
  def computePrice(shippingAddress: ShippingAddress, cart: Cart): Seq[ShippingData]

  def isValidShipmentId(shippingPrice: ShippingData): Boolean

  def confirmShipmentId(shippingPrice: ShippingData): ShippingData

  //def confirmPrice()

  def convertStorePrice(price: Long, cart: Cart): Long = {
    var rate: Option[Rate] = rateHandler.findByCurrencyCode(cart.rate.code)
    val currencyFractionDigits: Integer = rate.map {
      _.currencyFractionDigits
    }.getOrElse(2)
    (price * rate.map {
      _.currencyRate
    }.getOrElse(0.01) * Math.pow(10, currencyFractionDigits.doubleValue())).asInstanceOf[Long]
  }

  def extractShippingContent(cart: Cart): List[ShippingWithQuantity] = {
    cart.cartItems.map { cartItem =>
      if (!cartItem.externalCodes.isEmpty) None
      else {
        cartItem.shipping.map { shipping =>
          if (shipping.isDefine) Some(ShippingWithQuantity(cartItem.quantity, shipping))
          else None
        }.flatten
      }
    }.flatten.toList
  }

  def createShippingData(shippingAddress: AccountAddress, shipmentId: String, rateId: String, provider: String, service: String, rateType: String, price: Long, currencyCode: String): ShippingData = {
    var rate: Option[Rate] = rateHandler.findByCurrencyCode(currencyCode)
    ShippingData(shippingAddress, shipmentId, rateId, provider, service, rateType, price, currencyCode, if (rate.isDefined) rate.get.currencyFractionDigits else 2)
  }
}

object ShippingHandler {
  val servicesList: Seq[ShippingHandler] = if (!Settings.Shipping.Kiala.enable) Seq(noShippingHandler, easyPostHandler)
  else Seq(noShippingHandler, kialaShippingHandler, easyPostHandler)

  def computePrice(address: ShippingAddress, cart: Cart): Seq[ShippingData] = {
    servicesList.flatMap {
      service =>
        service.computePrice(address, cart)
    }
  }

  def confirmShippingPrice(shippingCart: Option[SelectShippingCart]): Option[ShippingData] = {
    shippingCart.map { shippingCart =>
      val serviceOpt = servicesList.find {
        _.isValidShipmentId(shippingCart.shippingPrices)
      }
      serviceOpt.map { service =>
        service.confirmShipmentId(shippingCart.shippingPrices)
      }
    }.getOrElse(None)
  }
}

