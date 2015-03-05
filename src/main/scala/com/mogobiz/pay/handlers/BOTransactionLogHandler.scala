package com.mogobiz.pay.handlers

import com.mogobiz.es.EsClient
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.model.Mogopay._

class BOTransactionLogHandler {
  def save(transactionLog: BOTransactionLog, refresh: Boolean = false) =
    EsClient.index(Settings.Mogopay.EsIndex, transactionLog, refresh)
}
