package com.mogobiz.pay.sql

import java.util.Date

case class BOAccount(id: Long, uuid: java.util.UUID, extra: String, email: String, company: String,
                      dateCreated: Date, lastUpdated: Date)