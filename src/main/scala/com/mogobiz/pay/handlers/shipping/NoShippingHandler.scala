package com.mogobiz.pay.handlers.shipping

import com.mogobiz.pay.model.Mogopay.ShippingAddress
import org.json4s._

/**
 * Created by yoannbaudy on 16/02/2015.
 */
class NoShippingHandler extends ShippingService {

  override def calculatePrice(shippingAddress: ShippingAddress, currencyCode: String,
                              cart: JValue): Seq[ShippingPrice] = {


    val shippingContent : List[(Boolean, BigInt)] = extractChippingContent(cart)

    // aucun produit ne nécessite de livraison
    if (shippingContent == Nil) Seq(createShippingPrice("NONE", "NONE", "NONE", 0, currencyCode))
    else Seq()
  }

}
