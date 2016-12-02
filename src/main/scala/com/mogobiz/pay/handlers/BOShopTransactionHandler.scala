/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import com.mogobiz.es.EsClient
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.model._
import com.mogobiz.pay.sql.BOShopTransactionDAO
import com.sksamuel.elastic4s.ElasticDsl.{termFilter, search => searchES, and}

class BOShopTransactionHandler {
  def find(uuid: String): Option[BOShopTransaction] = {
    EsClient.load[BOShopTransaction](Settings.Mogopay.EsIndex, uuid)
  }

  /*
  def findByGroupTxUUID(uuid: String): Seq[BOTransaction] = {
    val req = search in Settings.Mogopay.EsIndex -> "BOTransaction" postFilter termFilter("groupTransactionUUID", uuid) from 0 size EsClient.MAX_SIZE
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

  def findByShopIdAndTransactionUuid(shopId: String, transactionUuid: String): Option[BOShopTransaction] = {
    val query = searchES in Settings.Mogopay.EsIndex ->"BOShopTransaction" postFilter and(
      termFilter("shopId", shopId),
      termFilter("transactionUUID", transactionUuid)
    )
    val list = EsClient.searchAll[BOShopTransaction](query).toList
    if (list.isEmpty) None
    else Some(list.head)
  }

  def save(transaction: BOShopTransaction, refresh: Boolean = false) = {
    BOShopTransactionDAO.upsert(transaction)
    EsClient.index(Settings.Mogopay.EsIndex, transaction, refresh)
  }
/*
  def update(transaction: BOTransaction, refresh: Boolean): Boolean = {
    val updateResult = BOTransactionDAO.update(transaction)
    if (updateResult == 0) {
      throw new TransactionNotFoundException(transaction.transactionUUID)
    }
    EsClient.update[BOTransaction](Settings.Mogopay.EsIndex, transaction, false, refresh)
  }
  */
}
