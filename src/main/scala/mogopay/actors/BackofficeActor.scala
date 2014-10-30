package mogopay.actors

import akka.actor.Actor
import mogopay.config.HandlersConfig._
import mogopay.model.Mogopay.SessionData

import scala.util.Try

object BackofficeActor {

  case class ListCustomers(sessionData : SessionData, page: Int, max: Int)

  case class ListTransactionLogs(transactionId: String)

  case class ListTransactions(sessionData : SessionData, email: Option[String],
                              startDate: Option[String], startTime: Option[String],
                              endDate: Option[String], endTime: Option[String],
                              amount: Option[Int], transactionUuid: Option[String])

  case class GetTransaction(uuid: String)

}

class BackofficeActor extends Actor {

  import BackofficeActor._

  def receive: Receive = {
    case ListCustomers(merchantId, page, max) => sender ! Try(backofficeHandler.listCustomers(merchantId, page, max))
    case ListTransactionLogs(transactionId) => sender ! Try(backofficeHandler.listTransactionLogs(transactionId))
    case ListTransactions(sessionData, email, startDate, startTime, endDate, endTime, amount, transactionUuid) => {
      sender ! Try(backofficeHandler.listTransactions(sessionData,
        email,
        startDate,
        startTime,
        endDate,
        endTime,
        amount,
        transactionUuid))
    }
    case GetTransaction(uuid) => sender ! Try(backofficeHandler.getTransaction(uuid))
  }
}