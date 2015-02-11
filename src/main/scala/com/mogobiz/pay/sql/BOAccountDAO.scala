package com.mogobiz.pay.sql

import java.sql.ResultSet
import java.util.{Date, UUID}

import com.mogobiz.json.JacksonConverter
import com.mogobiz.pay.model.Mogopay.Account
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

  def create(account: Account)(implicit session: DBSession): Unit = {
    val newBoCart = new BOAccount(newId(), UUID.fromString(account.uuid), JacksonConverter.serialize(account),
      account.email, account.company.orNull, new Date, new Date)

    applyUpdate {
      insert.into(BOAccountDAO).namedValues(
        BOAccountDAO.column.id          -> newBoCart.id,
        BOAccountDAO.column.uuid        -> newBoCart.uuid.toString,
        BOAccountDAO.column.extra       -> newBoCart.extra,
        BOAccountDAO.column.email       -> newBoCart.email,
        BOAccountDAO.column.company     -> newBoCart.company,
        BOAccountDAO.column.dateCreated -> new java.sql.Timestamp(newBoCart.dateCreated.getTime()),
        BOAccountDAO.column.lastUpdated -> new java.sql.Timestamp(newBoCart.lastUpdated.getTime())
      )
    }
  }

  def upsert(account: Account): Unit = {
    DB localTx { implicit session =>
      val updateResult = update(account)
      if (updateResult == 0) create(account)
    }
  }

  def update(account: Account): Int = {
    DB localTx { implicit session =>
      applyUpdate {
        QueryDSL.update(BOAccountDAO).set(
          BOAccountDAO.column.extra       -> JacksonConverter.serialize(account),
          BOAccountDAO.column.email       -> account.email,
          BOAccountDAO.column.company     -> account.company.orNull,
          BOAccountDAO.column.lastUpdated -> new Date
        ).where.eq(BOAccountDAO.column.uuid, account.uuid)
      }
    }
  }

  def delete(id: String): Unit = DB localTx { implicit session =>
    withSQL {
      QueryDSL.delete.from(BOAccountDAO).where.eq(BOAccountDAO.column.uuid, id)
    }.update().apply()
  }
}

