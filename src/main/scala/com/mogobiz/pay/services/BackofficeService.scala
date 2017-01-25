/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.services

import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.implicits.Implicits
import Implicits._
import com.mogobiz.pay.model.{Account, BOShopTransaction, BOTransaction, BOTransactionLog}
import com.mogobiz.session.SessionESDirectives._
import spray.http.StatusCodes
import spray.routing.Directives

class BackofficeService extends Directives with DefaultComplete {

  val route = pathPrefix("backoffice") {
    listCustomers ~
    listTransactionLogs ~
    listTransactions ~
    getTransaction ~
    listShopTransaction
  }

  lazy val listCustomers = path("customers") {
    get {
      parameters('page.as[Int], 'max.as[Int]) { (page, max) =>
        session { session =>
          handleCall(backofficeHandler.listCustomers(session.sessionData, page, max),
                     (accounts: Seq[Account]) => complete(StatusCodes.OK -> accounts))
        }
      }
    }
  }

  lazy val listTransactionLogs = path("transactions" / Segment / "logs") { transactionId =>
    get {
      session { session =>
        handleCall(backofficeHandler.listTransactionLogs(transactionId),
                   (trans: Seq[BOTransactionLog]) => complete(StatusCodes.OK -> trans))
      }
    }
  }

  lazy val listTransactions = path("transactions") {
    get {
      session { session =>
        val params = parameters('email ?,
                                'start_date.as[String] ?,
                                'start_time.as[String] ?,
                                'end_date.as[String] ?,
                                'end_time.as[String] ?,
                                'amount.as[Int] ?,
                                'transaction_uuid ?,
                                'transaction_status.?,
                                'delivery_status.?)
        params {
          (email, startDate, startTime, endDate, endTime, amount, transaction, transactionStatus, deliveryStatus) =>
            handleCall(backofficeHandler.listTransactions(session.sessionData,
                                                          email.filter(_.trim.nonEmpty),
                                                          startDate,
                                                          startTime,
                                                          endDate,
                                                          endTime,
                                                          amount,
                                                          transaction.filter(_.trim.nonEmpty),
                                                          transactionStatus.filter(_.trim.nonEmpty),
                                                          deliveryStatus.filter(_.trim.nonEmpty)),
                       (trans: Seq[BOTransaction]) => complete(StatusCodes.OK -> trans))
        }
      }
    }
  }

  lazy val getTransaction = path("transaction" / JavaUUID) { uuid =>
    get {
      session { session =>
        handleCall(backofficeHandler.getTransaction(uuid.toString),
                   (trans: Option[BOTransaction]) => complete(StatusCodes.OK -> trans))
      }
    }
  }

  lazy val listShopTransaction = path("transactions" / Segment / "shopTransactions") { transactionId =>
    get {
      session { session =>
        handleCall(backofficeHandler.listShopTransactions(transactionId),
          (r: List[BOShopTransaction]) => complete(StatusCodes.OK -> r))
      }
    }
  }

}
