package com.mogobiz.pay.handlers

import com.mogobiz.es.EsClient
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.pay.settings.Settings

class BOTransactionHandler {
  def find(uuid: String): Option[BOTransaction] = { // todo
    EsClient.load[BOTransaction](Settings.Mogopay.EsIndex, uuid)
  }

  def save(transaction: BOTransaction, refresh: Boolean = false) = { // todo
    EsClient.index(Settings.Mogopay.EsIndex, transaction, refresh)
  }

  def update(transaction: BOTransaction, upsert: Boolean, refresh: Boolean): Boolean = { //todo
    EsClient.update[BOTransaction](Settings.Mogopay.EsIndex, transaction, true, false)
  }
}
