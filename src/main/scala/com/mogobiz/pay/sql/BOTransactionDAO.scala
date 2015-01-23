package com.mogobiz.pay.sql

import java.sql.ResultSet
import java.util.{Date, UUID}

import scalikejdbc._

object BOTransactionDAO extends SQLSyntaxSupport[BOTransaction] with BOService {
  override val tableName = "b_o_transaction"

  implicit val uuidTypeBinder: TypeBinder[UUID] = new TypeBinder[UUID] {
    def apply(rs: ResultSet, label: String): UUID = UUID.fromString(rs.getString(label))
    def apply(rs: ResultSet, index: Int): UUID = UUID.fromString(rs.getString(index))
  }

  def apply(rn: ResultName[BOTransaction])(rs: WrappedResultSet): BOTransaction = BOTransaction(
    rs.get(rn.id),
    rs.get[UUID](rn.uuid),
    rs.get(rn.extra),
    rs.date(rn.dateCreated),
    rs.date(rn.lastUpdated))

  def create(uuid: java.util.UUID, extra: String, email: String, company: String)(implicit session: DBSession): BOTransaction = {
    val newBoCart = new BOTransaction(newId(), uuid, extra, new Date, new Date)

    applyUpdate {
      insert.into(BOTransactionDAO).namedValues(
        BOTransactionDAO.column.id          -> newBoCart.id,
        BOTransactionDAO.column.uuid        -> newBoCart.uuid,
        BOTransactionDAO.column.extra       -> newBoCart.extra,
        BOTransactionDAO.column.dateCreated -> newBoCart.dateCreated,
        BOTransactionDAO.column.lastUpdated -> newBoCart.lastUpdated
      )
    }

    newBoCart
  }
}

