/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.shipping

import com.mogobiz.pay.common.{Cart, ShippingWithQuantity, ShopCart}
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.model._
import com.typesafe.scalalogging.StrictLogging

import scala.collection.Seq

trait ShippingHandler extends StrictLogging {
  // return a list of shipping price. Each shipping price is related to a level of service, for example : Same day delivery, 3 days, ...
  def computePrice(shippingAddress: ShippingAddress, cart: Cart): ShippingDataList

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

  def extractShippingContent(cart: ShopCart): List[ShippingWithQuantity] = {
    cart.cartItems.flatMap { cartItem =>
      cartItem.shipping.flatMap { shipping =>
        Some(ShippingWithQuantity(cartItem.quantity, shipping))
      }
    }
  }

  def createShippingData(shippingAddress: AccountAddress,
                         shipmentId: String,
                         rateId: String,
                         provider: String,
                         service: String,
                         rateType: String,
                         price: Long,
                         currencyCode: String): ShippingData = {
    var rate: Option[Rate] = rateHandler.findByCurrencyCode(currencyCode)
    ShippingData(shippingAddress,
                 shipmentId,
                 rateId,
                 provider,
                 service,
                 rateType,
                 price,
                 currencyCode,
                 if (rate.isDefined) rate.get.currencyFractionDigits else 2)
  }
}

object ShippingHandler {
  val servicesList: List[ShippingHandler] =
    if (!Settings.Shipping.Kiala.enable) List(easyPostHandler)
    else List(kialaShippingHandler, easyPostHandler)

  def computePrice(address: ShippingAddress, cart: Cart): ShippingDataList = {
    val list = servicesList.map { service =>
      service.computePrice(address, cart)
    }
    mergeShippingDataList(list)
  }

  def mergeShippingDataList(list: List[ShippingDataList]) : ShippingDataList = {
    if (list.isEmpty) ShippingDataList(None, Nil)
    else {
      val shippingData1 = list.head
      val shippingData2 = mergeShippingDataList(list.tail)
      ShippingDataList(mergeError(shippingData1.error, shippingData2.error), shippingData1.shippingPrices ::: shippingData2.shippingPrices)
    }
  }

  def mergeError(error1: Option[ShippingPriceError.ShippingPriceError], error2: Option[ShippingPriceError.ShippingPriceError]) = (error1, error2) match {
    case (None, None) => None
    case (Some(er), None) => Some(er)
    case (None, Some(er)) => Some(er)
    case (Some(er1), Some(er2)) => {
      if (getErrorValue(er1) <= getErrorValue(er2)) Some(er1)
      else Some(er2)
    }
  }

  def getErrorValue(er: ShippingPriceError.ShippingPriceError) = er match {
    case ShippingPriceError.INTERNATIONAL_SHIPPING_NOT_ALLOWED => 1
    case ShippingPriceError.SHIPPING_ZONE_NOT_ALLOWED => 2
    case ShippingPriceError.SHIPPING_TYPE_NOT_ALLOWED => 3
    case _ => 99
  }

  def confirmShippingPrice(shippingPrice: Option[ShippingData]): Option[ShippingData] = {
    shippingPrice.map { shippingPrice =>
      val serviceOpt = servicesList.find {
        _.isValidShipmentId(shippingPrice)
      }
      serviceOpt.map { service =>
        service.confirmShipmentId(shippingPrice)
      }
    }.getOrElse(None)
  }
}

