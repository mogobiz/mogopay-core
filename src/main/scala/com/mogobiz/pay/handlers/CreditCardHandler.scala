package com.mogobiz.pay.handlers

import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.exceptions.Exceptions.{CreditCardDoesNotExistException, AccountDoesNotExistException}
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.es.EsClient
import com.mogobiz.pay.settings.Settings

class CreditCardHandler {
  def delete(accountId: String, cardId: String): Unit = {
    accountHandler.find(accountId).map { account: Account =>
      account.creditCards.find(_.uuid == cardId).map { card =>
        val newCards = account.creditCards.diff(Seq(card))
        accountHandler.save(account.copy(creditCards = newCards), false)
      } getOrElse {
        throw CreditCardDoesNotExistException("")
      }
    }.getOrElse(throw AccountDoesNotExistException(""))
  }

  def findByAccount(accountId: String): Seq[CreditCard] = {
    accountHandler.find(accountId).map(_.creditCards).getOrElse(List())
  }
}