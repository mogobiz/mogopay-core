package com.mogobiz.pay.model

/**
 * Created by yoannbaudy on 01/12/2014.
 */
object ParamRequest {

  case class TransactionInit(merchant_secret: String, transaction_amount: Long, currency_code: String, currency_rate: Double, extra: Option[String])

  case class ListShippingPriceParam(currency_code: String, transaction_extra: String)

  case class SelectShippingPriceParam(currency_code: String, transaction_extra: String, provider: String, service: String, rate_type: String)

}

