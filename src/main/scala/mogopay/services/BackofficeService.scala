package mogopay.services

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import mogopay.actors.BackofficeActor._
import mogopay.config.Implicits._
import mogopay.services.Util._
import mogopay.model.Mogopay.{BOTransaction, BOTransactionLog, Account}
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

  lazy val listCustomers = getPath("listCustomers") {
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

  lazy val listTransactionLogs = getPath("listTransanctionLogs") {
    session { session =>
      parameters('transactionId) { transactionId =>
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

  lazy val listTransactions = getPath("listTransanctions") {
    session { session =>
      val params = parameters('startDate.as[Long]?, 'endDate.as[Long]?, 'amount.as[Int]?, 'transactionUuid?)
      params { (startDate, endDate, amount, transaction) =>
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

  lazy val getTransaction = path("getTransaction" / JavaUUID) { uuid =>
    get {
      session { session =>
        complete {
          (backofficeActor ? GetTransaction(uuid.toString)).mapTo[Option[BOTransaction]]
        }
      }
    }
  }

  private def withSession(response: Session => spray.routing.StandardRoute) = {
    session { session =>
      session.sessionData.accountId match {
        case Some(id: String) => response(session)
        case _ => complete {
          StatusCodes.Unauthorized ->
            Map('error -> "ID missing or incorrect. The user is probably not logged in.")
        }
      }
    }
  }
}
