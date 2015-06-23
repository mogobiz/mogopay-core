package com.mogobiz.pay.handlers.shipping

import java.util.UUID

import com.mogobiz.pay.common.Cart
import com.mogobiz.pay.model.Mogopay.ShippingAddress
import org.json4s._

/**
 * Created by yoannbaudy on 16/02/2015.
 */
class NoShippingHandler extends ShippingService {

  override def calculatePrice(shippingAddress: ShippingAddress, cart: Cart): Seq[ShippingPrice] = {

    val shippingContent = extractShippingContent(cart)

    // aucun produit ne nécessite de livraison
    if (shippingContent.isEmpty) Seq(createShippingPrice(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "NONE", "NONE", "NONE", 0, cart.currencyCode))
    else Seq()
  }

}
