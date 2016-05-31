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
    (for {
      cartItem <- cart.cartItems
      shipping <- cartItem.shipping
    } yield ShippingWithQuantity(cartItem.quantity, shipping)).flatMap { shippingWithQuantity: ShippingWithQuantity =>
      val shipping = shippingWithQuantity.shipping
      if (shipping.height == 0 || shipping.width == 0 || shipping.weight == 0 || shipping.weightUnit == null || shipping.weightUnit.isEmpty
        || shipping.linearUnit == null || shipping.linearUnit.isEmpty)
        None
      else
        Some(shippingWithQuantity)
    } toList
  }

  def createShippingData(shippingAddress: AccountAddress, shipmentId: String, rateId: String, provider: String, service: String, rateType: String, price: Long, currencyCode: String): ShippingData = {
    var rate: Option[Rate] = rateHandler.findByCurrencyCode(currencyCode)
    ShippingData(shippingAddress, shipmentId, rateId, provider, service, rateType, price, currencyCode, if (rate.isDefined) rate.get.currencyFractionDigits else 2)
  }
}

object ShippingHandler {
  val servicesList: Seq[ShippingHandler] = if (!Settings.Shipping.Kiala.enable) Seq(noShippingHandler, easyPostHander)
  else Seq(noShippingHandler, kialaShippingHandler, easyPostHander)

  def computePrice(address: ShippingAddress, cart: Cart): Seq[ShippingData] = {
    servicesList.flatMap {
      service =>
        service.computePrice(address, cart)
    }
  }

  def confirmShippingPrice(shippingDataOpt: Option[ShippingData]): Option[ShippingData] = {
    shippingDataOpt.map { shippingPrice =>
      val serviceOpt = servicesList.find {
        _.isValidShipmentId(shippingPrice)
      }
      serviceOpt.map { service =>
        service.confirmShipmentId(shippingPrice)
      }
    }.getOrElse(None)
  }
}

