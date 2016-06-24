/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.exceptions.Exceptions.{ CreditCardDoesNotExistException, AccountDoesNotExistException }
import com.mogobiz.pay.model.{ AccountChange }
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.utils.GlobalUtil
import scalikejdbc.DBSession

class CreditCardHandler {
  def delete(accountId: String, cardId: String): Unit = {
    accountHandler.load(accountId).map { account: Account =>
      account.creditCards.find(_.uuid == cardId).map { card =>
        val transactionalBlock = { implicit session: DBSession =>
          val newCards = account.creditCards.diff(Seq(card))
          accountHandler.update(account.copy(creditCards = newCards))
        }
        val successBlock = { result: AccountChange =>
          accountHandler.notifyESChanges(result, false)
        }
        GlobalUtil.runInTransaction(transactionalBlock, successBlock)
      } getOrElse {
        throw CreditCardDoesNotExistException("")
      }
    }.getOrElse(throw AccountDoesNotExistException(""))
  }

  def findByAccount(accountId: String): Seq[CreditCard] = {
    accountHandler.load(accountId).map(_.creditCards).getOrElse(List())
  }
}