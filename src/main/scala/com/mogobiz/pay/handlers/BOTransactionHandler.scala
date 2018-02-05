/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import com.mogobiz.es.EsClient
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.model.Mogopay.BOTransaction
import com.mogobiz.pay.sql.BOTransactionDAO
import com.sksamuel.elastic4s.http.ElasticDsl._

class BOTransactionHandler {
  def find(uuid: String): Option[BOTransaction] = {
    EsClient.load[BOTransaction](Settings.Mogopay.EsIndex, uuid)
  }

  def findByShipmentId(shipmentId: String): Option[BOTransaction] = {
    val req = search(Settings.Mogopay.EsIndex -> "BOTransaction") query {
      boolQuery().must(termQuery("shippingData.shipmentId", shipmentId))
    }
    EsClient.search[BOTransaction](req)
  }

  def create(transaction: BOTransaction) = {
    val refresh = true
    BOTransactionDAO.create(transaction)
    EsClient.index(Settings.Mogopay.EsIndex, transaction, refresh)
  }

  def update(transaction: BOTransaction): Boolean = {
    val refresh = true
    val updateResult = BOTransactionDAO.update(transaction)
    EsClient.update[BOTransaction](Settings.Mogopay.EsIndex,
                                   transaction,
                                   false,
                                   refresh)
  }
}
