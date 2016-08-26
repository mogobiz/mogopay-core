/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.exceptions.Exceptions.{CreditCardDoesNotExistException, AccountDoesNotExistException}
import com.mogobiz.pay.model._

class CreditCardHandler {
  def delete(accountId: String, cardId: String): Unit = {
    accountHandler
      .load(accountId)
      .map { account: Account =>
        account.creditCards.find(_.uuid == cardId).map { card =>
          val newCards = account.creditCards.diff(Seq(card))
          accountHandler.update(account.copy(creditCards = newCards), false)
        } getOrElse {
          throw CreditCardDoesNotExistException("")
        }
      }
      .getOrElse(throw AccountDoesNotExistException(""))
  }

  def findByAccount(accountId: String): Seq[CreditCard] = {
    accountHandler.load(accountId).map(_.creditCards).getOrElse(List())
  }
}
