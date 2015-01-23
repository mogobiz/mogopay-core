package com.mogobiz.pay.sql

import java.sql.ResultSet
import java.util.{Date, UUID}

import scalikejdbc._

object BOAccountDAO extends SQLSyntaxSupport[BOAccount] with BOService {
  override val tableName = "b_o_account"

  implicit val uuidTypeBinder: TypeBinder[UUID] = new TypeBinder[UUID] {
    def apply(rs: ResultSet, label: String): UUID = UUID.fromString(rs.getString(label))
    def apply(rs: ResultSet, index: Int): UUID = UUID.fromString(rs.getString(index))
  }

  def apply(rn: ResultName[BOAccount])(rs: WrappedResultSet): BOAccount = BOAccount(
    rs.get(rn.id),
    rs.get[UUID](rn.uuid),
    rs.get(rn.extra),
    rs.get(rn.email),
    rs.get(rn.company),
    rs.date(rn.dateCreated),
    rs.date(rn.lastUpdated))

  def create(uuid: java.util.UUID, extra: String, email: String, company: String)(implicit session: DBSession): BOAccount = {
    val newBoCart = new BOAccount(newId(), uuid, extra, email, company, new Date, new Date)

    applyUpdate {
      insert.into(BOAccountDAO).namedValues(
        BOAccountDAO.column.id          -> newBoCart.id,
        BOAccountDAO.column.uuid        -> newBoCart.uuid,
        BOAccountDAO.column.extra       -> newBoCart.extra,
        BOAccountDAO.column.email       -> newBoCart.email,
        BOAccountDAO.column.company     -> newBoCart.company,
        BOAccountDAO.column.dateCreated -> newBoCart.dateCreated,
        BOAccountDAO.column.lastUpdated -> newBoCart.lastUpdated
      )
    }

    newBoCart
  }
}

