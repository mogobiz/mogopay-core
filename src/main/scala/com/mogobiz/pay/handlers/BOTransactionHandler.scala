package com.mogobiz.pay.handlers

import java.util.UUID

import com.mogobiz.es.EsClient
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.exceptions.Exceptions.BOTransactionNotFoundException
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.pay.sql.BOTransactionDAO
import com.sksamuel.elastic4s.ElasticDsl._

class BOTransactionHandler {
  def find(uuid: String): Option[BOTransaction] = {
    EsClient.load[BOTransaction](Settings.Mogopay.EsIndex, uuid)
  }

  def findByGroupTxUUID(uuid: String): Seq[BOTransaction] = {
    val req = search in Settings.Mogopay.EsIndex -> "BOTransaction" postFilter termFilter("groupTransactionUUID", uuid)
    EsClient.searchAll[BOTransaction](req)
  }

  def findOtherGroupBOTx(uuid: String): Seq[BOTransaction] = {
    find(uuid)
      .flatMap(_.groupTransactionUUID)
      .map(findByGroupTxUUID)
      .getOrElse(Seq())
      .filter(_.customer.isDefined)
      .filter(_.customer.get.uuid != uuid)
  }

  def save(transaction: BOTransaction, refresh: Boolean = false) = {
    BOTransactionDAO.upsert(transaction)
    EsClient.index(Settings.Mogopay.EsIndex, transaction, refresh)
  }

  def update(transaction: BOTransaction, refresh: Boolean): Boolean = {
    val updateResult = BOTransactionDAO.update(transaction)
    if (updateResult == 0) {
      throw new BOTransactionNotFoundException("")
    }
    EsClient.update[BOTransaction](Settings.Mogopay.EsIndex, transaction, false, refresh)
  }
}
