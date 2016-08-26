/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.shipping

import java.util.UUID

import com.mogobiz.pay.common.Cart
import com.mogobiz.pay.model.{ShippingAddress, ShippingData}
import org.json4s._

/**
  */
class NoShippingHandler extends ShippingHandler {

  val NO_SHIPPING_PREFIX = "NONE_"

  override def computePrice(shippingAddress: ShippingAddress, cart: Cart): Seq[ShippingData] = {

    val shippingContent = extractShippingContent(cart)

    // aucun produit ne nécessite de livraison
    if (shippingContent.isEmpty)
      Seq(
          createShippingData(shippingAddress.address,
                             NO_SHIPPING_PREFIX + UUID.randomUUID().toString(),
                             UUID.randomUUID().toString(),
                             "NONE",
                             "NONE",
                             "NONE",
                             0,
                             cart.rate.code))
    else Seq()
  }

  override def isValidShipmentId(shippingPrice: ShippingData): Boolean =
    shippingPrice.shipmentId.startsWith(NO_SHIPPING_PREFIX)

  override def confirmShipmentId(shippingPrice: ShippingData): ShippingData = shippingPrice.copy(confirm = true)
}
