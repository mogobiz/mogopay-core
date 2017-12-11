/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import java.util.Calendar

import com.mogobiz.es.EsClient
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.model.TransactionRequest
import com.sksamuel.elastic4s.http.ElasticDsl._

class TransactionRequestHandler {
  def save(txRequest: TransactionRequest, refresh: Boolean = false) =
    EsClient.index(Settings.Mogopay.EsIndex, txRequest, refresh)

  def update(txRequest: TransactionRequest) =
    EsClient.update[TransactionRequest](Settings.Mogopay.EsIndex, txRequest, true, false)

  def find(uuid: String) = EsClient.load[TransactionRequest](Settings.Mogopay.EsIndex, uuid)

  def findByGroupTxUUID(uuid: String): Seq[TransactionRequest] = {
    val req = search(Settings.Mogopay.EsIndex -> "TransactionRequest") query
        termQuery("groupTransactionUUID", uuid) from 0 size EsClient.MaxSize
    EsClient.searchAll[TransactionRequest](req)
  }

  def delete(uuid: String, refresh: Boolean = false) =
    EsClient.delete[TransactionRequest](Settings.Mogopay.EsIndex, uuid, refresh)

  def recycle() = {
    val cal = Calendar.getInstance()
    cal.add(Calendar.MILLISECOND, -1 * Settings.TransactionRequestDuration * 60 * 1000)
    val xMillisAgo = cal.getTime

    val req = deleteIn(Settings.Mogopay.EsIndex -> "TransactionRequest")
      .by(rangeQuery("dateCreated") gte 0 lte xMillisAgo.getTime)
    EsClient().execute(req)
  }
}
