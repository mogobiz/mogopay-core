package com.mogobiz.pay.sql

import java.util.Date

case class BOTransaction(id: Long, uuid: java.util.UUID, extra: String, dateCreated: Date, lastUpdated: Date)