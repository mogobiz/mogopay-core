/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import java.util.Calendar

import com.mogobiz.pay.config.Settings
import com.sksamuel.elastic4s.ElasticDsl.{delete => del, _}
import com.mogobiz.es.EsClient
import com.mogobiz.pay.model.TransactionRequest

class TransactionRequestHandler {
  def save(txRequest: TransactionRequest, refresh: Boolean = false) =
    EsClient.index(Settings.Mogopay.EsIndex, txRequest, refresh)

  def update(txRequest: TransactionRequest) =
    EsClient.update[TransactionRequest](Settings.Mogopay.EsIndex, txRequest, true, false)

  def find(uuid: String) = EsClient.load[TransactionRequest](Settings.Mogopay.EsIndex, uuid)

  def findByGroupTxUUID(uuid: String): Seq[TransactionRequest] = {
    val req = search in Settings.Mogopay.EsIndex -> "TransactionRequest" postFilter termFilter(
          "groupTransactionUUID",
          uuid) from 0 size EsClient.MAX_SIZE
    EsClient.searchAll[TransactionRequest](req)
  }

  def delete(uuid: String, refresh: Boolean = false) =
    EsClient.delete[TransactionRequest](Settings.Mogopay.EsIndex, uuid, refresh)

  def recycle() = {
    val cal = Calendar.getInstance()
    cal.add(Calendar.MILLISECOND, -1 * Settings.TransactionRequestDuration * 60 * 1000)
    val xMillisAgo = cal.getTime

    val req = del from Settings.Mogopay.EsIndex -> "TransactionRequest" where {
      range("dateCreated") from 0 to xMillisAgo.getTime
    }
    import EsClient.secureRequest
    EsClient().execute(secureRequest(req))
  }
}
