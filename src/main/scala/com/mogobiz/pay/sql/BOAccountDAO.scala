/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.sql

import java.util.{ Date, UUID }

import com.mogobiz.json.JacksonConverter
import com.mogobiz.pay.model.Mogopay.Account
import Sql.BOAccount
import scalikejdbc._

object BOAccountDAO extends SQLSyntaxSupport[BOAccount] with BOService {
  override val tableName = "b_o_account"

  //  implicit val uuidTypeBinder: TypeBinder[UUID] = new TypeBinder[UUID] {
  //    def apply(rs: ResultSet, label: String): UUID = UUID.fromString(rs.getString(label))
  //    def apply(rs: ResultSet, index: Int): UUID = UUID.fromString(rs.getString(index))
  //  }

  def apply(rn: ResultName[BOAccount])(rs: WrappedResultSet): BOAccount = BOAccount(
    rs.get(rn.id),
    UUID.fromString(rs.get(rn.uuid)),
    rs.get(rn.extra),
    rs.get(rn.email),
    rs.get(rn.company),
    rs.date(rn.dateCreated),
    rs.date(rn.lastUpdated))

  def load(uuid: String)(implicit session: DBSession): Option[BOAccount] = {
    val t = BOAccountDAO.syntax("t")
    withSQL {
      select.from(BOAccountDAO as t).where.eq(t.uuid, uuid)
    }.map(BOAccountDAO(t.resultName)).single().apply()
  }

  def create(account: Account)(implicit session: DBSession): Unit = {
    val newBoAccount = new BOAccount(newId(), UUID.fromString(account.uuid), JacksonConverter.serialize(account),
      account.email, account.company.orNull, new Date, new Date)

    applyUpdate {
      insert.into(BOAccountDAO).namedValues(
        BOAccountDAO.column.id -> newBoAccount.id,
        BOAccountDAO.column.uuid -> newBoAccount.uuid.toString,
        BOAccountDAO.column.extra -> newBoAccount.extra,
        BOAccountDAO.column.email -> newBoAccount.email,
        BOAccountDAO.column.company -> newBoAccount.company,
        BOAccountDAO.column.dateCreated -> new java.sql.Timestamp(newBoAccount.dateCreated.getTime()),
        BOAccountDAO.column.lastUpdated -> new java.sql.Timestamp(newBoAccount.lastUpdated.getTime())
      )
    }
  }

  def update(account: Account)(implicit session: DBSession): Int = {
    applyUpdate {
      QueryDSL.update(BOAccountDAO).set(
        BOAccountDAO.column.extra -> JacksonConverter.serialize(account),
        BOAccountDAO.column.email -> account.email,
        BOAccountDAO.column.company -> account.company.orNull,
        BOAccountDAO.column.lastUpdated -> new Date
      ).where.eq(BOAccountDAO.column.uuid, account.uuid)
    }
  }

  def delete(id: String)(implicit session: DBSession): Unit =
    withSQL {
      QueryDSL.delete.from(BOAccountDAO).where.eq(BOAccountDAO.column.uuid, id)
    }.update().apply()

}

