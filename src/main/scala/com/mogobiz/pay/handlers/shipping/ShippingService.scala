package com.mogobiz.pay.handlers.shipping

import com.mogobiz.pay.model.Mogopay._
import org.json4s.JValue

case class ShippingPrice(provider: String, service: String, rateType: String, price: Long,
                         currencyCode: String, currencyFractionDigits: Int)

trait ShippingService {
  def calculatePrice(shippingAddress: ShippingAddress, currencyCode: String, cart: JValue): Seq[ShippingPrice]
}