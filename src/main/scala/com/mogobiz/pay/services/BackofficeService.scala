/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.services

import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.exceptions.Exceptions.{ NotAuthentifiedException, InvalidContextException }
import com.mogobiz.pay.implicits.Implicits
import Implicits._
import com.mogobiz.pay.model.Mogopay.{ SessionData, BOTransaction, Account, BOTransactionLog }
import com.mogobiz.session.SessionESDirectives._
import spray.http.StatusCodes
import spray.routing.{ RejectionHandler, Rejection, Directives }

class BackofficeService extends Directives with DefaultComplete {

  case object MogopayAuthenticationRejection extends Rejection

  implicit val mogopayAuthenticationRejectionHandler = RejectionHandler {
    case MogopayAuthenticationRejection :: _ =>
      //complete(StatusCodes.Unauthorized, "Not logged in")
      complete(StatusCodes.Unauthorized -> Map('type -> "NotAuthentifiedException", 'error -> "Not logged in"))
  }

  /*
  val route = pathPrefix("backoffice") {
    customers ~ transactions
  }*/
  val route = customers ~ transactions

  lazy val customers = pathPrefix("customers") {
    path(JavaUUID / "transactions") { uuid =>
      handleCall(transactionHandler.searchByCustomer(uuid.toString),
        (res: Seq[BOTransaction]) => complete(res))
    } ~
      get {
        parameters('page.as[Int], 'max.as[Int]) { (page, max) =>
          session { session =>
            handleCall(backofficeHandler.listCustomers(session.sessionData, page, max),
              (accounts: Seq[Account]) => complete(StatusCodes.OK -> accounts)
            )
          }
        }
      }
  }

  /**
   * TODO çà : http://spray.io/documentation/1.2.2/spray-routing/key-concepts/rejections/
   */
  def assertAuthenticated(sessionData: SessionData) = {
    println("assertAuthenticated")
    if (!sessionData.authenticated) {
      reject(MogopayAuthenticationRejection)
      //completeException(new NotAuthentifiedException("Not logged in"))
      //complete(StatusCodes.Unauthorized -> Map('type -> "NotAuthentifiedException", 'error -> "Not logged in"))
    }
  }

  lazy val transactions = pathPrefix("transactions") {
    session { session =>
      pathPrefix(Segment) { transactionId =>
        path("logs") {
          get {
            handleCall(
              backofficeHandler.listTransactionLogs(transactionId),
              (trans: Seq[BOTransactionLog]) => complete(StatusCodes.OK -> trans)
            )
          }
        } ~ pathEnd {
          get {
            handleCall(backofficeHandler.getTransaction(transactionId),
              (trans: Option[BOTransaction]) => complete(StatusCodes.OK -> trans))
          }
        }
      } ~ pathEnd {
        get {

          val params = parameters('email ?,
            'start_date.as[String] ?, 'start_time.as[String] ?,
            'end_date.as[String] ?, 'end_time.as[String] ?,
            'amount.as[Int] ?, 'transaction_uuid ?, 'transaction_status.?, 'delivery_status.?)
          params {
            (email, startDate, startTime, endDate, endTime, amount, transaction, transactionStatus, deliveryStatus) =>
              handleCall(backofficeHandler.listTransactions(session.sessionData,
                email.filter(_.trim.nonEmpty),
                startDate, startTime,
                endDate, endTime,
                amount,
                transaction.filter(_.trim.nonEmpty),
                transactionStatus.filter(_.trim.nonEmpty),
                deliveryStatus.filter(_.trim.nonEmpty)),
                (trans: Seq[BOTransaction]) => complete(StatusCodes.OK -> trans))
          }
        }
      }
    }
  }

}
