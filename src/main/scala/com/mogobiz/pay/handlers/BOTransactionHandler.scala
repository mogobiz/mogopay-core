package com.mogobiz.pay.handlers

import com.mogobiz.es.EsClient
import com.mogobiz.pay.exceptions.Exceptions.BOTransactionNotFoundException
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.pay.settings.Settings
import com.mogobiz.pay.sql.BOTransactionDAO

class BOTransactionHandler {
  def find(uuid: String): Option[BOTransaction] = {
    EsClient.load[BOTransaction](Settings.Mogopay.EsIndex, uuid)
  }

  def save(transaction: BOTransaction, refresh: Boolean = false) = {
    BOTransactionDAO.upsert(transaction)
    EsClient.index(Settings.Mogopay.EsIndex, transaction, refresh)
  }

  def update(transaction: BOTransaction, refresh: Boolean): Boolean = {
    val updateResult = BOTransactionDAO.update(transaction)
    if (updateResult == 0) {
      throw new BOTransactionNotFoundException("")
    }
    EsClient.update[BOTransaction](Settings.Mogopay.EsIndex, transaction, false, refresh)
  }
}
