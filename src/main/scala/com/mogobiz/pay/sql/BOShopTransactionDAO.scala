/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.sql

import java.util.{Date, UUID}

import com.mogobiz.json.JacksonConverter
import com.mogobiz.pay.model
import Sql.BOShopTransaction
import scalikejdbc._

object BOShopTransactionDAO extends SQLSyntaxSupport[BOShopTransaction] with BOService {
  override val tableName = "b_o_shop_transaction"

  def create(transaction: model.BOShopTransaction): BOShopTransaction = {
    DB localTx { implicit session =>
      val newBo = new BOShopTransaction(newId(),
        UUID.fromString(transaction.uuid),
        JacksonConverter.serialize(transaction),
        new Date,
        new Date)

      applyUpdate {
        insert
          .into(BOShopTransactionDAO)
          .namedValues(
            BOShopTransactionDAO.column.id -> newBo.id,
            BOShopTransactionDAO.column.uuid -> newBo.uuid.toString,
            BOShopTransactionDAO.column.extra -> newBo.extra,
            BOShopTransactionDAO.column.dateCreated -> newBo.dateCreated,
            BOShopTransactionDAO.column.lastUpdated -> newBo.lastUpdated
          )
      }

      newBo
    }
  }

  def update(transaction: model.BOShopTransaction): Int = {
    DB localTx { implicit session =>
      applyUpdate {
        QueryDSL
          .update(BOShopTransactionDAO)
          .set(
            BOShopTransactionDAO.column.extra       -> JacksonConverter.serialize(transaction),
            BOShopTransactionDAO.column.lastUpdated -> new Date
          )
          .where
          .eq(BOShopTransactionDAO.column.uuid, transaction.uuid)
      }
    }
  }
}
