package mogopay.handlers

import mogopay.config.Settings
import mogopay.config.HandlersConfig._
import mogopay.exceptions.Exceptions.{CreditCardDoesNotExistException, AccountDoesNotExistError}
import mogopay.model.Mogopay._
import mogopay.es.EsClient
import com.sksamuel.elastic4s.ElasticDsl._

import scala.util._

class CreditCardHandler {
  def delete(accountId: String, cardId: String): Try[Unit] = {
    accountHandler.load(accountId).map { account: Account =>
      account.creditCards.find(_.uuid == cardId).map { card =>
        val newCards = account.creditCards.diff(Seq(card))
        EsClient.index(account.copy(creditCards = newCards))
        Success()
      } getOrElse {
        Failure(new CreditCardDoesNotExistException)
      }
    }.getOrElse(Failure(new AccountDoesNotExistError))
  }

  def findByAccount(accountId: String): Seq[CreditCard] = {
    accountHandler.load(accountId).map(_.creditCards).getOrElse(List())
  }
}