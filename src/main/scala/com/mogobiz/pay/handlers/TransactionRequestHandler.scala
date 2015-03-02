package com.mogobiz.pay.handlers

import java.util.Calendar

import com.sksamuel.elastic4s.ElasticDsl.{delete => del, _}
import com.mogobiz.pay.settings.Settings
import com.mogobiz.es.EsClient
import com.mogobiz.pay.model.Mogopay.TransactionRequest

class TransactionRequestHandler {
  def save(txRequest: TransactionRequest, refresh: Boolean = false) =
    EsClient.index(Settings.Mogopay.EsIndex, txRequest, refresh)

  def update(txRequest: TransactionRequest) = EsClient.update(Settings.Mogopay.EsIndex, txRequest, true, false)

  def find(uuid: String) = EsClient.load[TransactionRequest](Settings.Mogopay.EsIndex, uuid)

  def delete(uuid: String, refresh: Boolean = false) =
    EsClient.delete[TransactionRequest](Settings.Mogopay.EsIndex, uuid, refresh)

  def recycle() = {
    val cal = Calendar.getInstance()
    cal.add(Calendar.MILLISECOND, -1 * Settings.TransactionRequestDuration)
    val xMillisAgo = cal.getTime

    val req = del from Settings.Mogopay.EsIndex -> "TransactionRequest" where {
      range("dateCreated") from 0 to xMillisAgo.getTime
    }
    EsClient().execute(req)
  }
}
