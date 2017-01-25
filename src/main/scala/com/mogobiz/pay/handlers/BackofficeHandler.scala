/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import java.text.SimpleDateFormat
import java.util.Date

import com.mogobiz.pay.config.Settings
import com.sksamuel.elastic4s.ElasticDsl._
import com.mogobiz.es.EsClient
import com.mogobiz.pay.exceptions.Exceptions.InvalidContextException
import com.mogobiz.pay.model._
import org.elasticsearch.search.sort.SortOrder._
import com.mogobiz.pay.config.MogopayHandlers.handlers._

class BackofficeHandler {

  def listCustomers(sessionData: SessionData, page: Int, max: Int): Seq[Account] = {
    if (!sessionData.isMerchant)
      throw InvalidContextException("User not a merchant")
    val merchantId = sessionData.accountId.getOrElse(throw InvalidContextException("No logged merchant found"))
    val req = search in Settings.Mogopay.EsIndex -> "Account" postFilter {
      termFilter("owner", merchantId)
    } start page * max limit max
    EsClient.searchAll[Account](req)
  }

  def listTransactionLogs(transactionId: String): Seq[BOTransactionLog] = {
    val req = search in Settings.Mogopay.EsIndex -> "BOTransactionLog" query must(matchQuery("transactionUuid", transactionId)) sort (
      by field "transactionShopUuid" order DESC,
      by field "dateCreated" order DESC
    ) start 0 limit Settings.MaxQueryResults
    EsClient.searchAll[BOTransactionLog](req)
  }

  def listTransactions(sessionData: SessionData,
                       email: Option[String],
                       startDate: Option[String],
                       startTime: Option[String],
                       endDate: Option[String],
                       endTime: Option[String],
                       amount: Option[Int],
                       transactionUUID: Option[String],
                       transactionStatus: Option[String],
                       deliveryStatus: Option[String]): Seq[BOTransaction] = {
    def parseDateAndTime(date: Option[String], time: Option[String]): Option[Date] = date.map { d =>
      val date = new SimpleDateFormat("yyyy-MM-dd").parse(d)
      time match {
        case None => date
        case Some(t) =>
          date.setHours(t.split(':')(0).toInt)
          date.setMinutes(t.split(':')(1).toInt)
          date
      }
    }

    val parsedStartDatetime = parseDateAndTime(startDate, startTime)
    val parsedEndDatetime   = parseDateAndTime(endDate, endTime)

    val accountFilter = if (sessionData.isMerchant) {
      val accountId = sessionData.accountId.getOrElse(throw InvalidContextException("No logged user found"))
      List(Some(termFilter("vendor.uuid", accountId)), email.map(termFilter("email", _))).flatten
    } else {
      val accountEmail = sessionData.email.getOrElse(throw InvalidContextException("No logged user found"))
      List(termFilter("email", accountEmail))
    }

    val filters = accountFilter ++
        transactionUUID.map(uuid => termFilter("transactionUUID", uuid)) ++
        amount.map(x => termFilter("amount", x)) ++
        transactionStatus.map(x => termFilter("status", x)) ++
        deliveryStatus.map(x => termFilter("delivery.status", x))

    val req = search in Settings.Mogopay.EsIndex -> "BOTransaction" postFilter {
      and(filters: _*)
    } query {
      rangeQuery("transactionDate") from parsedStartDatetime.map(_.getTime).orNull to parsedEndDatetime
        .map(_.getTime)
        .orNull
    } sort {
      field sort "transactionDate" order DESC
    } start 0 limit Settings.MaxQueryResults

    EsClient.searchAll[BOTransaction](req)
  }

  def getTransaction(uuid: String): Option[BOTransaction] =
    EsClient.search[BOTransaction](search in Settings.Mogopay.EsIndex -> "BOTransaction" postFilter {
      termFilter("uuid", uuid)
    })

  def listShopTransactions(transactionId: String): List[BOShopTransaction] = {
    boShopTransactionHandler.findByTransactionUuid(transactionId)
  }

}
