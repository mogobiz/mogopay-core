package com.mogobiz.pay.sql

import java.sql.ResultSet
import java.util.{Date, UUID}

import com.mogobiz.json.JacksonConverter
import com.mogobiz.pay.model
import scalikejdbc._

object BOTransactionDAO extends SQLSyntaxSupport[BOTransaction] with BOService {
  override val tableName = "b_o_transaction"

//  implicit val uuidTypeBinder: TypeBinder[UUID] = new TypeBinder[UUID] {
//    def apply(rs: ResultSet, label: String): UUID = UUID.fromString(rs.getString(label))
//    def apply(rs: ResultSet, index: Int): UUID = UUID.fromString(rs.getString(index))
//  }

  def apply(rn: ResultName[BOTransaction])(rs: WrappedResultSet): BOTransaction = BOTransaction(
    rs.get(rn.id),
    UUID.fromString(rs.get(rn.uuid)),
    rs.get(rn.extra),
    rs.date(rn.dateCreated),
    rs.date(rn.lastUpdated))

  def create(transaction: model.Mogopay.BOTransaction)(implicit session: DBSession): BOTransaction = {
    val newBoCart = new BOTransaction(newId(), UUID.fromString(transaction.uuid), JacksonConverter.serialize(transaction),
      new Date, new Date)

    applyUpdate {
      insert.into(BOTransactionDAO).namedValues(
        BOTransactionDAO.column.id          -> newBoCart.id,
        BOTransactionDAO.column.uuid        -> newBoCart.uuid.toString,
        BOTransactionDAO.column.extra       -> newBoCart.extra,
        BOTransactionDAO.column.dateCreated -> newBoCart.dateCreated,
        BOTransactionDAO.column.lastUpdated -> newBoCart.lastUpdated
      )
    }

    newBoCart
  }

  def upsert(transaction: model.Mogopay.BOTransaction): Unit = {
    DB localTx { implicit session =>
      val updateResult = update(transaction)
      if (updateResult == 0) create(transaction)
    }
  }

  def update(transaction: model.Mogopay.BOTransaction): Int = {
    DB localTx { implicit session =>
      applyUpdate {
        QueryDSL.update(BOTransactionDAO).set(
          BOTransactionDAO.column.extra -> JacksonConverter.serialize(transaction),
          BOTransactionDAO.column.lastUpdated -> new Date
        ).where.eq(BOTransactionDAO.column.uuid, transaction.uuid)
      }
    }
  }
}
