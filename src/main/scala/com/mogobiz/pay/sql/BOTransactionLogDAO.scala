package com.mogobiz.pay.sql

import java.util.{Date, UUID}

import com.mogobiz.json.JacksonConverter
import com.mogobiz.pay.model
import com.mogobiz.pay.sql.Sql.BOTransactionLog
import scalikejdbc._

object BOTransactionLogDAO extends SQLSyntaxSupport[BOTransactionLog] with BOService {
  override val tableName = "b_o_transaction_log"

  //  implicit val uuidTypeBinder: TypeBinder[UUID] = new TypeBinder[UUID] {
  //    def apply(rs: ResultSet, label: String): UUID = UUID.fromString(rs.getString(label))
  //    def apply(rs: ResultSet, index: Int): UUID = UUID.fromString(rs.getString(index))
  //  }

  def apply(rn: ResultName[model.Mogopay.BOTransactionLog])(rs: WrappedResultSet): BOTransactionLog = BOTransactionLog(
    rs.get(rn.id),
    UUID.fromString(rs.get(rn.uuid)),
    rs.get(rn.extra),
    rs.date(rn.dateCreated),
    rs.date(rn.lastUpdated))

  def create(transactionLog: model.Mogopay.BOTransactionLog)(implicit session: DBSession): BOTransactionLog = {
    val newBOTransactionLog = new BOTransactionLog(newId(), UUID.fromString(transactionLog.uuid), JacksonConverter.serialize(transactionLog),
      new Date, new Date)

    applyUpdate {
      insert.into(BOTransactionLogDAO).namedValues(
        BOTransactionLogDAO.column.id -> newBOTransactionLog.id,
        BOTransactionLogDAO.column.uuid -> newBOTransactionLog.uuid.toString,
        BOTransactionLogDAO.column.extra -> newBOTransactionLog.extra,
        BOTransactionLogDAO.column.dateCreated -> newBOTransactionLog.dateCreated,
        BOTransactionLogDAO.column.lastUpdated -> newBOTransactionLog.lastUpdated
      )
    }
    BOTransactionLog
  }

  def upsert(transactionLog: model.Mogopay.BOTransactionLog): Unit = {
    DB localTx { implicit session =>
      val updateResult = update(transactionLog)
      if (updateResult == 0) create(transactionLog)
    }
  }

  def update(transactionLog: model.Mogopay.BOTransactionLog): Int = {
    DB localTx { implicit session =>
      applyUpdate {
        QueryDSL.update(BOTransactionLogDAO).set(
          BOTransactionLogDAO.column.extra -> JacksonConverter.serialize(transactionLog),
          BOTransactionLogDAO.column.lastUpdated -> new Date
        ).where.eq(BOTransactionLogDAO.column.uuid, transactionLog.uuid)
      }
    }
  }
}
