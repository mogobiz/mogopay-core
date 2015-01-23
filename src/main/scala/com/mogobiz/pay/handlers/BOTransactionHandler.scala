package com.mogobiz.pay.handlers

import com.mogobiz.es.EsClient
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.pay.settings.Settings

class BOTransactionHandler {
  def findByUUID(uuid: String): Option[BOTransaction] = {
    EsClient.load[BOTransaction](Settings.Mogopay.EsIndex, uuid) // todo
  }

  def save(transaction: BOTransaction, refresh: Boolean = false) = {
    EsClient.index(Settings.Mogopay.EsIndex, transaction, refresh) // todo
  }
}
