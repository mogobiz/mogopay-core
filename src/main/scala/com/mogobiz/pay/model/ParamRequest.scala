/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.model

/**
  */
object ParamRequest {
  case class TransactionInit(merchantSecret: String,
                             transactionAmount: Long,
                             returnUrl: Option[String],
                             groupPaymentExpirationDate: Option[Long],
                             groupPaymentRefundPercentage: Option[Int])

  case class ListShippingPriceParam(cartProvider: String, cartKeys: String)

  case class SelectShippingPriceParam(currency: String, shopsAndshippingDataIds: List[String])
}
