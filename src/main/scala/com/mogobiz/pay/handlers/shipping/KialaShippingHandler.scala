package com.mogobiz.pay.handlers.shipping

import java.util.UUID

import com.mogobiz.pay.model.Mogopay.ShippingAddress
import org.json4s.JValue
import com.mogobiz.pay.config.MogopayHandlers._

class KialaShippingHandler extends ShippingService {

  val KIALA_PRICE = 400

  override def calculatePrice(shippingAddress: ShippingAddress, currencyCode: String,
                              cart: JValue): Seq[ShippingPrice] = {


    val shippingContent : List[(Boolean, BigInt)] = extractChippingContent(cart)

    def calculatePrice(list: List[(Boolean, BigInt)]) : Long = {
      if (list == Nil) 0
      else {
        val elt = list.head;
        if (elt._1) calculatePrice(list.tail)
        else if (elt._2 > 0) elt._2.longValue() + calculatePrice(list.tail)
        else rateHandler.convert(KIALA_PRICE, "EUR", currencyCode).getOrElse(0)
      }
    }

    if (shippingContent == Nil) Seq()
    else Seq(createShippingPrice(UUID.randomUUID().toString, UUID.randomUUID().toString, "KIALA", "KIALA", "KIALA", calculatePrice(shippingContent), currencyCode))
  }
}
