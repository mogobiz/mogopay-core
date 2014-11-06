package com.mogobiz.pay.handlers

import com.mogobiz.es.EsClient
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.pay.settings.Settings

class BOTransactionLogHandler {
  def save(transactionLog: BOTransactionLog) = EsClient.index(Settings.Mogopay.EsIndex, transactionLog, false)
}
