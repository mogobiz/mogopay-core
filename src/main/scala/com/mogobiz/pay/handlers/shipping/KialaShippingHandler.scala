package com.mogobiz.pay.handlers.shipping

import com.mogobiz.pay.model.Mogopay.{Rate, ShippingAddress}
import org.json4s.JValue
import org.json4s.JsonAST._
import com.mogobiz.pay.config.MogopayHandlers._

class KialaShippingHandler extends ShippingService {

  val KIALA_PRICE = 400

  override def calculatePrice(shippingAddress: ShippingAddress, currencyCode: String,
                              cart: JValue): Seq[ShippingPrice] = {


    val shippingContent : List[(Boolean, BigInt)] = for {
      JObject(shipping) <- cart \ "cartItemVOs" \ "shipping"
      JField("free", JBool(free))  <- shipping
      JField("amount", JInt(amount))  <- shipping
    } yield (free, amount)


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
    else {
      var rate : Option[Rate] = rateHandler.findByCurrencyCode(currencyCode)
      Seq(ShippingPrice("KIALA", "KIALA", "KIALA", calculatePrice(shippingContent), currencyCode, if (rate.isDefined) rate.get.currencyFractionDigits else 2))
    }
  }
}
