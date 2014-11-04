package com.mogobiz.pay.handlers

import com.mogobiz.es.EsClient
import com.mogobiz.pay.model.Mogopay._

class BOTransactionLogHandler {
  def save(transactionLog: BOTransactionLog) = EsClient.index(transactionLog, false)
}
