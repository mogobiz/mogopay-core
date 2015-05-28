package com.mogobiz.pay.model

/**
 * Created by yoannbaudy on 01/12/2014.
 */
object ParamRequest {
  case class TransactionInit(merchantSecret: String, transactionAmount: Long,
                             currencyCode: String, currencyRate: Double,
                             extra: Option[String], returnUrl: Option[String],
                             groupPaymentExpirationDate: Option[Long], groupPaymentRefundPercentage: Option[Int])

  case class ListShippingPriceParam(currency_code: String, transaction_extra: String)

  case class SelectShippingPriceParam(currency_code: String, transaction_extra: String, provider: String, service: String, rate_type: String)
}

