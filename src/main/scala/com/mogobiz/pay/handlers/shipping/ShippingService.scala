package com.mogobiz.pay.handlers.shipping

import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.model.Mogopay._
import org.json4s.JValue
import org.json4s.JsonAST.{JInt, JBool, JField, JObject}

case class ShippingPrice(provider: String, service: String, rateType: String, price: Long,
                         currencyCode: String, currencyFractionDigits: Int)

trait ShippingService {
  def calculatePrice(shippingAddress: ShippingAddress, currencyCode: String, cart: JValue): Seq[ShippingPrice]

  def extractChippingContent(cart: JValue) : List[(Boolean, BigInt)] = {
    for {
      JObject(shipping) <- cart \ "cartItemVOs" \ "shipping"
      JField("free", JBool(free))  <- shipping
      JField("amount", JInt(amount))  <- shipping
    } yield (free, amount)
  }

  def createShippingPrice(provider: String, service: String, rateType: String, price: Long, currencyCode: String) : ShippingPrice = {
    var rate : Option[Rate] = rateHandler.findByCurrencyCode(currencyCode)
    ShippingPrice(provider, service, rateType, price, currencyCode, if (rate.isDefined) rate.get.currencyFractionDigits else 2)
  }
}
