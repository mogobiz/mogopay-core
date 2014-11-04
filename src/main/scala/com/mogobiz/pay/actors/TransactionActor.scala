package com.mogobiz.pay.actors

import akka.actor.Actor
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.handlers.shipping.ShippingPrice
import com.mogobiz.pay.model.Mogopay.SessionData
import com.mogobiz.session.Session

import scala.util.Try

object TransactionActor {

  case class Init(merchantSecret: String, amount: Long, code: String,
                  rate: Double, extra: Option[String]) {
    require(!merchantSecret.isEmpty, "Merchant request cannot be empty")
  }

  case class SearchByCustomer(uuid: String)

  case class GetShippingPrices(currencyCode: String, extra: String, accountId: String)

  case class GetShippingPrice(shippingPrices: Seq[ShippingPrice], provider: String, service: String, rateType: String)

  case class Verify(secret: String, amount: Option[Long], transactionUUID: String)

  case class SubmitParams(successURL: Option[String], errorURL: Option[String], cardinfoURL: Option[String], authURL: Option[String],
                          cvvURL: Option[String],transactionUUID: Option[String], amount: Option[Long], merchantId: Option[String],
                          transactionType: Option[String], customerCVV: Option[String], ccNum: Option[String],
                          customerEmail: Option[String], customerPassword: Option[String],
                          transactionDescription: Option[String],
                          ccMonth: Option[String], ccYear: Option[String], ccType: Option[String]) {
    def toMap = Map(
      "_successURL" -> "_successURL",
      "_errorURL" -> "_errorURL",
      "cardinfoURL" -> "cardinfoURL",
      "cvvURL" -> "cvvURL",
      "_transactionUUID" -> "_transactionUUID",
      "_amount" -> "_amount",
      "merchantId" -> "merchantId",
      "_transactionType" -> "_transactionType",
      "customerCVV" -> "customerCVV",
      "ccNum" -> "ccNum",
      "customerEmail" -> "customerEmail",
      "customerPassword" -> "customerPassword",
      "transactionDescription" -> "transactionDescription",
      "ccMonth" -> "ccMonth",
      "ccYear" -> "ccYear",
      "ccType" -> "ccType"
    )
  }

  case class Submit(sessionData: SessionData, params: SubmitParams, actionName: Option[String], csrfToken: Option[String])

}

class TransactionActor extends Actor {

  import TransactionActor._

  def receive = {
    case Init(merchantRequest, amount, code, rate, extra) =>
      sender ! Try(transactionHandler.init(merchantRequest, amount, code, rate, extra))
    case SearchByCustomer(uuid) => sender ! Try(transactionHandler.searchByCustomer(uuid))
    case GetShippingPrices(c, e, id) => sender ! Try(transactionHandler.shippingPrices(c, e, id))
    case GetShippingPrice(sps, p, s, rt) => sender ! Try(transactionHandler.shippingPrice(sps, p, s, rt))
    case Verify(s, a, uuid) => sender ! Try(transactionHandler.verify(s, a, uuid))
    case submit: Submit => sender ! Try(transactionHandler.submit(submit))
  }
}
