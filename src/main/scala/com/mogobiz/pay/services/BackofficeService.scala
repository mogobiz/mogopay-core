package com.mogobiz.pay.services

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.mogobiz.pay.actors.BackofficeActor._
import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.implicits.Implicits
import Implicits._
import com.mogobiz.pay.model.Mogopay.{BOTransaction, Account, BOTransactionLog}
import com.mogobiz.session.SessionESDirectives._
import spray.http.StatusCodes
import spray.routing.Directives

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Try

class BackofficeService(backofficeActor: ActorRef)(implicit executionContext: ExecutionContext) extends Directives with DefaultComplete {
  implicit val timeout = Timeout(10.seconds)

  val route = pathPrefix("backoffice") {
    listCustomers ~
    listTransactionLogs ~
    listTransactions ~
    getTransaction
  }

  lazy val listCustomers = path("customers") {
    get {
      parameters('page.as[Int], 'max.as[Int]) { (page, max) =>
        session { session =>
          onComplete((backofficeActor ? ListCustomers(session.sessionData, page, max)).mapTo[Try[Seq[Account]]]) { call =>
            handleComplete(call, (accounts: Seq[Account]) => complete(StatusCodes.OK -> accounts)
            )
          }
        }
      }
    }
  }

  lazy val listTransactionLogs = path("transactions" / Segment / "logs") { transactionId =>
    get {
      session { session =>
        onComplete((backofficeActor ? ListTransactionLogs(transactionId)).mapTo[Try[Seq[BOTransactionLog]]]) { call =>
          handleComplete(call, (trans: Seq[BOTransactionLog]) => complete(StatusCodes.OK -> trans)
          )
        }
      }
    }
  }

  lazy val listTransactions = path("transactions") {
    get {
      session {
        session =>
          val params = parameters('email ?,
            'start_date.as[String] ?, 'start_time.as[String] ?,
            'end_date.as[String] ?, 'end_time.as[String] ?,
            'amount.as[Int] ?, 'transaction_uuid ?)
          params {
            (email, startDate, startTime, endDate, endTime, amount, transaction) =>
              onComplete((backofficeActor ? ListTransactions(session.sessionData,
                email.filter(_.trim.nonEmpty),
                startDate, startTime,
                endDate, endTime,
                amount,
                transaction.filter(_.trim.nonEmpty))).mapTo[Try[Seq[BOTransactionLog]]]) { call =>
                handleComplete(call, (trans: Seq[BOTransactionLog]) => complete(StatusCodes.OK -> trans))
              }
          }
      }
    }
  }

  lazy val getTransaction = path("transaction" / JavaUUID) { uuid =>
    get {
      session {
        session =>
          onComplete((backofficeActor ? GetTransaction(uuid.toString)).mapTo[Try[Option[BOTransactionLog]]]) { call =>
            handleComplete(call, (trans: Option[BOTransactionLog]) => complete(StatusCodes.OK -> trans))
          }
      }
    }
  }
}
