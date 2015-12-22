/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.shipping

import com.mogobiz.pay.common.{ ShippingWithQuantity, Cart, Shipping }
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.model.Mogopay._

import scala.collection.Seq

case class ShippingPrice(shipmentId: String, rateId: String, provider: String, service: String, rateType: String, price: Long,
  currencyCode: String, currencyFractionDigits: Int, confirm: Boolean = false)

trait ShippingService {
  def calculatePrice(shippingAddress: ShippingAddress, cart: Cart): Seq[ShippingPrice]

  def isManageShipmentId(shippingPrice: ShippingPrice): Boolean

  def confirmShipmentId(shippingPrice: ShippingPrice): ShippingPrice

  //def confirmPrice()

  def convertStorePrice(price: Long, cart: Cart): Long = {
    var rate: Option[Rate] = rateHandler.findByCurrencyCode(cart.rate.code)
    val currencyFractionDigits: Integer = rate.map { _.currencyFractionDigits }.getOrElse(2)
    (price * rate.map { _.currencyRate }.getOrElse(0.01) * Math.pow(10, currencyFractionDigits.doubleValue())).asInstanceOf[Long]
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

  def createShippingPrice(shipmentId: String, rateId: String, provider: String, service: String, rateType: String, price: Long, currencyCode: String): ShippingPrice = {
    var rate: Option[Rate] = rateHandler.findByCurrencyCode(currencyCode)
    ShippingPrice(shipmentId, rateId, provider, service, rateType, price, currencyCode, if (rate.isDefined) rate.get.currencyFractionDigits else 2)
  }
}

object ShippingService {
  val servicesList: Seq[ShippingService] = if (!Settings.Shipping.Kiala.enable) Seq(noShippingHandler, easyPostHander)
  else Seq(noShippingHandler, kialaShippingHandler, easyPostHander)

  def calculatePrice(address: ShippingAddress, cart: Cart): Seq[ShippingPrice] = {
    servicesList.flatMap {
      service =>
        service.calculatePrice(address, cart)
    }
  }

  def confirmShippingPrice(shippingPriceOpt: Option[ShippingPrice]): Option[ShippingPrice] = {
    shippingPriceOpt.map { shippingPrice =>
      val serviceOpt = servicesList.find {
        _.isManageShipmentId(shippingPrice)
      }
      serviceOpt.map { service =>
        service.confirmShipmentId(shippingPrice)
      }
    }.getOrElse(None)
  }
}