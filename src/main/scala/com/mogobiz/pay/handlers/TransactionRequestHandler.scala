package com.mogobiz.pay.handlers

import java.util.Calendar

import com.sksamuel.elastic4s.ElasticDsl._
import com.mogobiz.pay.settings.Settings
import com.mogobiz.es.EsClient
import com.mogobiz.pay.model.Mogopay.TransactionRequest
import com.mogobiz.es.{Settings => esSettings}

class TransactionRequestHandler {
  def save(txRequest: TransactionRequest) = EsClient.index(txRequest)

  def update(txRequest: TransactionRequest) = EsClient.update(txRequest, true, false)

  def find(uuid: String) = EsClient.load[TransactionRequest](uuid)

  def recycle() = {
    val cal = Calendar.getInstance()
    cal.add(Calendar.MILLISECOND, -1 * Settings.TransactionRequestDuration)
    val xMillisAgo = cal.getTime

    val req = delete from esSettings.ElasticSearch.Index -> "TransactionRequest" where {
      range("dateCreated") from 0 to xMillisAgo.getTime
    }
    EsClient.client.execute(req)
  }
}
