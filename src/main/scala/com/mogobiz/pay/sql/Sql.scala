/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.sql

object Sql {

  import java.util.Date

  case class BOAccount(id: Long,
                       uuid: java.util.UUID,
                       extra: String,
                       email: String,
                       company: String,
                       dateCreated: Date,
                       lastUpdated: Date)

  case class BOTransaction(id: Long, uuid: java.util.UUID, extra: String, dateCreated: Date, lastUpdated: Date)

  case class BOShopTransaction(id: Long, uuid: java.util.UUID, extra: String, dateCreated: Date, lastUpdated: Date)

  case class BOTransactionLog(id: Long, uuid: java.util.UUID, extra: String, dateCreated: Date, lastUpdated: Date)

}
