package mogopay.services

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import mogopay.actors.BackofficeActor._
import mogopay.config.Implicits._
import mogopay.model.Mogopay.{BOTransaction, Account, BOTransactionLog}
import mogopay.session.Session
import mogopay.session.SessionESDirectives._
import spray.http.StatusCodes
import spray.routing.Directives

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class BackofficeService(backofficeActor: ActorRef)(implicit executionContext: ExecutionContext) extends Directives {
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
          complete {
            if (session.contains("isMerchant")) {
              val message = ListCustomers(session("accountId").toString, page, max)
              (backofficeActor ? message).mapTo[Seq[Account]]
            } else {
              StatusCodes.Unauthorized -> ""
            }
          }
        }
      }
    }
  }

  lazy val listTransactionLogs = path("transactions" / Segment / "logs") { transactionId =>
    get {
      session { session =>
        complete {
          if (session.contains("isMerchant")) {
            (backofficeActor ? ListTransactionLogs(transactionId)).mapTo[Seq[BOTransactionLog]]
          } else {
            StatusCodes.Unauthorized -> Map()
          }
        }
      }
    }
  }

  lazy val listTransactions = path("transactions") {
    get {
      session {
        session =>
          val params = parameters('start_date.as[Long] ?, 'end_date.as[Long] ?, 'amount.as[Int] ?, 'transaction_uuid ?)
          params {
            (startDate, endDate, amount, transaction) =>
              complete {
                if (session.contains("accountId")) {
                  val term = if (session.contains("isMerchant") && session("isMerchant") == true)
                    Left(session("accountId").toString)
                  else
                    Right(session("email").toString)

                  val message = ListTransactions(term, startDate, endDate, amount, transaction)
                  (backofficeActor ? message).mapTo[Seq[BOTransaction]]
                } else {
                  StatusCodes.Unauthorized ->
                    Map('error -> "Account ID missing or incorrect. The user is probably not logged in.")
                }
              }
          }
      }
    }
  }

  lazy val getTransaction = path("transaction" / JavaUUID) { uuid =>
      get {
        session {
          session =>
            complete {
              (backofficeActor ? GetTransaction(uuid.toString)).mapTo[Option[BOTransaction]]
            }
        }
      }
  }

}
