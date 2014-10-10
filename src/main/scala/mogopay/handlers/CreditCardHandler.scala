package mogopay.handlers

import mogopay.config.HandlersConfig._
import mogopay.exceptions.Exceptions.{CreditCardDoesNotExistException, AccountDoesNotExistException}
import mogopay.model.Mogopay._
import mogopay.es.EsClient

class CreditCardHandler {
  def delete(accountId: String, cardId: String): Unit = {
    accountHandler.load(accountId).map { account: Account =>
      account.creditCards.find(_.uuid == cardId).map { card =>
        val newCards = account.creditCards.diff(Seq(card))
        EsClient.index(account.copy(creditCards = newCards))
      } getOrElse {
        throw CreditCardDoesNotExistException("")
      }
    }.getOrElse(throw AccountDoesNotExistException(""))
  }

  def findByAccount(accountId: String): Seq[CreditCard] = {
    accountHandler.load(accountId).map(_.creditCards).getOrElse(List())
  }
}