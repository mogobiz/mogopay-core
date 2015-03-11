package com.mogobiz.pay.sql

object Sql {
  import java.util.Date

  case class BOAccount(id: Long, uuid: java.util.UUID, extra: String, email: String, company: String,
                       dateCreated: Date, lastUpdated: Date)
  case class BOTransaction(id: Long, uuid: java.util.UUID, extra: String, dateCreated: Date, lastUpdated: Date)
}
