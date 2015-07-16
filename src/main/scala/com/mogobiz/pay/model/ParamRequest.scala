/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.model

/**
 * Created by yoannbaudy on 01/12/2014.
 */
object ParamRequest {
  case class TransactionInit(merchantSecret: String, transactionAmount: Long,
                             returnUrl: Option[String],
                             groupPaymentExpirationDate: Option[Long], groupPaymentRefundPercentage: Option[Int])

  case class ListShippingPriceParam(cartProvider: String, cartKeys: String)

  case class SelectShippingPriceParam(shipmentId: String, rateId: String)
}

