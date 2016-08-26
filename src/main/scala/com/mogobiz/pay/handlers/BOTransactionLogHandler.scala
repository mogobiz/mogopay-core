/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import com.mogobiz.es.EsClient
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.model._
import com.mogobiz.pay.sql.BOTransactionLogDAO

class BOTransactionLogHandler {
  def save(transactionLog: BOTransactionLog, refresh: Boolean = false): String = {
    BOTransactionLogDAO.upsert(transactionLog, false)
    EsClient.index(Settings.Mogopay.EsIndex, transactionLog, refresh)
  }
}
