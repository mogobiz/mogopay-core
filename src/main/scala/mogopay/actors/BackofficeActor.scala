package mogopay.actors

import akka.actor.Actor
import mogopay.config.HandlersConfig._

object BackofficeActor {
  case class ListCustomers(merchantId: String, page: Int, max: Int)
  case class ListTransactionLogs(transactionId: String)
  case class ListTransactions(term: Either[String, String], startDate: Option[Long], endDate: Option[Long],
                              amount: Option[Int], transactionUuid: Option[String])
  case class GetTransaction(uuid: String)
}

class BackofficeActor extends Actor {
  import BackofficeActor._

  def receive: Receive = {
    case ListCustomers(merchantId, page, max) => sender ! backofficeHandler.listCustomers(merchantId, page, max)
    case ListTransactionLogs(transactionId) => sender ! backofficeHandler.listTransactionLogs(transactionId)
    case ListTransactions(term, startDate, endDate, amount, transactionUuid) =>
      sender ! backofficeHandler.listTransactions(term, startDate, endDate, amount, transactionUuid)
    case GetTransaction(uuid) => sender ! backofficeHandler.getTransaction(uuid)
  }
}