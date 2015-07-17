/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.shipping

import com.mogobiz.pay.common.{Cart, Shipping}
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.model.Mogopay._

import scala.collection.Seq

case class ShippingPrice(shipmentId: String, rateId: String, provider: String, service: String, rateType: String, price: Long,
                         currencyCode: String, currencyFractionDigits: Int)

trait ShippingService {
  def calculatePrice(shippingAddress: ShippingAddress, cart: Cart): Seq[ShippingPrice]

  def isManageShipmentId(shippingPrice: ShippingPrice) : Boolean

  def confirmShipmentId(shippingPrice: ShippingPrice) : Long

  //def confirmPrice()

  def extractShippingContent(cart: Cart) : List[Shipping] = {
    (for {
      cartItem <- cart.cartItems
      shipping  <- cartItem.shipping
    } yield shipping).toList
  }

  def createShippingPrice(shipmentId: String, rateId: String, provider: String, service: String, rateType: String, price: Long, currencyCode: String) : ShippingPrice = {
    var rate : Option[Rate] = rateHandler.findByCurrencyCode(currencyCode)
    ShippingPrice(shipmentId, rateId, provider, service, rateType, price, currencyCode, if (rate.isDefined) rate.get.currencyFractionDigits else 2)
  }
}


object ShippingService {
  val servicesList: Seq[ShippingService] = Seq(noShippingHandler, kialaShippingHandler, easyPostHander)

  def calculatePrice(address: ShippingAddress, cart: Cart): Seq[ShippingPrice] = {
    servicesList.flatMap {
      service =>
        service.calculatePrice(address, cart)
    }
  }

  def confirmShippingPrice(shippingPriceOpt: Option[ShippingPrice]) : Long = {
    shippingPriceOpt.map { shippingPrice =>
      val serviceOpt = servicesList.find { _.isManageShipmentId(shippingPrice) }
      serviceOpt.map { service =>
        service.confirmShipmentId(shippingPrice)
      }.getOrElse(0L)
    }.getOrElse(0L)
  }
}