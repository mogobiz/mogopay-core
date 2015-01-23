package com.mogobiz.pay.sql

import java.sql.ResultSet
import java.util.UUID

import scalikejdbc._

object BOAccountDAO extends SQLSyntaxSupport[BOAccount] with BOService {
  implicit val uuidTypeBinder: TypeBinder[UUID] = new TypeBinder[UUID] {
    def apply(rs: ResultSet, label: String): UUID = UUID.fromString(rs.getString(label))
    def apply(rs: ResultSet, index: Int): UUID = UUID.fromString(rs.getString(index))
  }

  def apply(rn: ResultName[BOAccount])(rs: WrappedResultSet): BOAccount = BOAccount(
    rs.get(rn.id),
    rs.get[UUID](rn.uuid),
    rs.get(rn.extra),
    rs.get(rn.email),
    rs.get(rn.company))

  def create(uuid: java.util.UUID, extra: String, email: String, company: String)(implicit session: DBSession): BOAccount = {
    val newBoCart = new BOAccount(newId(), uuid, extra, email, company)

    applyUpdate {
      insert.into(BOAccountDAO).namedValues(
        BOAccountDAO.column.id      -> newBoCart.id,
        BOAccountDAO.column.uuid    -> newBoCart.uuid,
        BOAccountDAO.column.extra   -> newBoCart.extra,
        BOAccountDAO.column.email   -> newBoCart.email,
        BOAccountDAO.column.company -> newBoCart.company
      )
    }

    newBoCart
  }
}

