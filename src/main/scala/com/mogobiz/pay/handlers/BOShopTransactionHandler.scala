/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import com.mogobiz.es.EsClient
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.model.Mogopay.BOShopTransaction
import com.mogobiz.pay.sql.BOShopTransactionDAO
import com.sksamuel.elastic4s.http.ElasticDsl._

class BOShopTransactionHandler {
  def find(uuid: String): Option[BOShopTransaction] = {
    EsClient.load[BOShopTransaction](Settings.Mogopay.EsIndex, uuid)
  }

  /*
  def findByGroupTxUUID(uuid: String): Seq[BOTransaction] = {
    val req = search in Settings.Mogopay.EsIndex -> "BOTransaction" postFilter termQuery("groupTransactionUUID", uuid) from 0 size EsClient.MAX_SIZE
    EsClient.searchAll[BOTransaction](req)
  }

  def findByShipmentId(shipmentId: String): Option[BOTransaction] = {
    val req = search in Settings.Mogopay.EsIndex -> "BOTransaction" query matchQuery("shippingData.shipmentId",
                                                                                     shipmentId)
    EsClient.search[BOTransaction](req)
  }

  def findOtherGroupBOTx(uuid: String): Seq[BOTransaction] = {
    find(uuid)
      .flatMap(_.groupTransactionUUID)
      .map(findByGroupTxUUID)
      .getOrElse(Seq())
      .filter(_.customer.isDefined)
      .filter(_.customer.get.uuid != uuid)
  }

  def findAllGroupTransactions(): Seq[BOTransaction] = {
    val req = search in Settings.Mogopay.EsIndex -> "BOTransaction" postFilter {
      existsFilter("groupTransactionUUID")
    } from 0 size EsClient.MAX_SIZE
    EsClient.searchAll[BOTransaction](req)
  }*/

  def findByTransactionUuid(
      transactionUuid: String): List[BOShopTransaction] = {
    val query = search(Settings.Mogopay.EsIndex -> "BOShopTransaction") query {
      boolQuery().must(termQuery("transactionUUID", transactionUuid))
    }
    EsClient.searchAll[BOShopTransaction](query).toList
  }

  def findByShopIdAndTransactionUuid(
      shopId: String,
      transactionUuid: String): Option[BOShopTransaction] = {
    val query = search(Settings.Mogopay.EsIndex -> "BOShopTransaction") query {
      boolQuery().must(termQuery("shopId", shopId),
                       termQuery("transactionUUID", transactionUuid))
    }
    val list = EsClient.searchAll[BOShopTransaction](query).toList
    list.headOption
  }

  def create(transaction: BOShopTransaction): String = {
    val refresh = true
    BOShopTransactionDAO.create(transaction)
    EsClient.index(Settings.Mogopay.EsIndex, transaction, refresh)
  }

  def update(transaction: BOShopTransaction): Boolean = {
    val refresh = true
    val updateResult = BOShopTransactionDAO.update(transaction)
    EsClient.update[BOShopTransaction](Settings.Mogopay.EsIndex,
                                       transaction,
                                       upsert = false,
                                       refresh = refresh)
  }
}
