package com.mogobiz.pay.handlers.shipping

import java.util.UUID

import com.mogobiz.pay.common.{Shipping, Cart}
import com.mogobiz.pay.model.Mogopay.ShippingAddress
import org.json4s.JValue
import com.mogobiz.pay.config.MogopayHandlers._

class KialaShippingHandler extends ShippingService {

  val KIALA_PRICE = 400

  override def calculatePrice(shippingAddress: ShippingAddress, cart: Cart): Seq[ShippingPrice] = {

    val shippingContent = extractShippingContent(cart)

    def calculatePrice(list: List[Shipping]) : Long = {
      if (list.isEmpty) 0
      else {
        val elt = list.head;
        if (elt.free) calculatePrice(list.tail)
        else if (elt.amount > 0) elt.amount + calculatePrice(list.tail)
        else rateHandler.convert(KIALA_PRICE, "EUR", cart.currencyCode).getOrElse(0)
      }
    }

    if (shippingContent == Nil) Seq()
    else Seq(createShippingPrice(UUID.randomUUID().toString, UUID.randomUUID().toString, "KIALA", "KIALA", "KIALA", calculatePrice(shippingContent), cart.currencyCode))
  }
}
