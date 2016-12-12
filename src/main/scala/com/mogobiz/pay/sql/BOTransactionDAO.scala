/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.sql

import java.util.{Date, UUID}

import com.mogobiz.json.JacksonConverter
import com.mogobiz.pay.model
import Sql.BOTransaction
import scalikejdbc._

object BOTransactionDAO extends SQLSyntaxSupport[BOTransaction] with BOService {
  override val tableName = "b_o_transaction"

  //  implicit val uuidTypeBinder: TypeBinder[UUID] = new TypeBinder[UUID] {
  //    def apply(rs: ResultSet, label: String): UUID = UUID.fromString(rs.getString(label))
  //    def apply(rs: ResultSet, index: Int): UUID = UUID.fromString(rs.getString(index))
  //  }

  def apply(rn: ResultName[BOTransaction])(rs: WrappedResultSet): BOTransaction =
    BOTransaction(rs.get(rn.id),
                  UUID.fromString(rs.get(rn.uuid)),
                  rs.get(rn.extra),
                  rs.date(rn.dateCreated),
                  rs.date(rn.lastUpdated))

  def create(transaction: model.BOTransaction): BOTransaction = {
    DB localTx { implicit session =>
      val newBoCart = new BOTransaction(newId(),
        UUID.fromString(transaction.uuid),
        JacksonConverter.serialize(transaction),
        new Date,
        new Date)

      applyUpdate {
        insert
          .into(BOTransactionDAO)
          .namedValues(
            BOTransactionDAO.column.id -> newBoCart.id,
            BOTransactionDAO.column.uuid -> newBoCart.uuid.toString,
            BOTransactionDAO.column.extra -> newBoCart.extra,
            BOTransactionDAO.column.dateCreated -> newBoCart.dateCreated,
            BOTransactionDAO.column.lastUpdated -> newBoCart.lastUpdated
          )
      }
      newBoCart
    }
  }

  def update(transaction: model.BOTransaction): Int = {
    DB localTx { implicit session =>
      applyUpdate {
        QueryDSL
          .update(BOTransactionDAO)
          .set(
              BOTransactionDAO.column.extra       -> JacksonConverter.serialize(transaction),
              BOTransactionDAO.column.lastUpdated -> new Date
          )
          .where
          .eq(BOTransactionDAO.column.uuid, transaction.uuid)
      }
    }
  }
}
