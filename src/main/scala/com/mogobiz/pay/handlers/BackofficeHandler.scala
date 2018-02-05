/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import java.text.SimpleDateFormat
import java.util.Date

import com.mogobiz.es.EsClient
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.exceptions.Exceptions.InvalidContextException
import com.mogobiz.pay.model.Mogopay._
import com.sksamuel.elastic4s.http.ElasticDsl.{termQuery, _}
import com.sksamuel.elastic4s.searches.sort.FieldSortDefinition

class BackofficeHandler {

  def listCustomers(sessionData: SessionData,
                    page: Int,
                    max: Int): Seq[Account] = {
    if (!sessionData.isMerchant)
      throw InvalidContextException("User not a merchant")
    val merchantId = sessionData.accountId.getOrElse(
      throw InvalidContextException("No logged merchant found"))
    val req = search(Settings.Mogopay.EsIndex -> "Account") query {
      boolQuery().must(termQuery("owner", merchantId))
    } start page * max limit max
    EsClient.searchAll[Account](req)
  }

  def listTransactionLogs(transactionId: String): Seq[BOTransactionLog] = {
    val req = search(Settings.Mogopay.EsIndex -> "BOTransactionLog") query boolQuery()
      .must(termQuery("transactionUuid", transactionId)) sortBy (
      FieldSortDefinition("transactionShopUuid").desc(),
      FieldSortDefinition("dateCreated").desc()
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
    def parseDateAndTime(date: Option[String],
                         time: Option[String]): Option[Date] = date.map { d =>
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
    val parsedEndDatetime = parseDateAndTime(endDate, endTime)

    val accountFilter = if (sessionData.isMerchant) {
      val accountId = sessionData.accountId.getOrElse(
        throw InvalidContextException("No logged user found"))
      List(Some(termQuery("vendor.uuid", accountId)),
           email.map(termQuery("email", _))).flatten
    } else {
      val accountEmail = sessionData.email.getOrElse(
        throw InvalidContextException("No logged user found"))
      List(termQuery("email", accountEmail))
    }

    val filters = accountFilter ++
      transactionUUID.map(uuid => termQuery("transactionUUID", uuid)) ++
      amount.map(x => termQuery("amount", x)) ++
      transactionStatus.map(x => termQuery("status", x)) ++
      deliveryStatus.map(x => termQuery("delivery.status", x))

    val req = search(Settings.Mogopay.EsIndex -> "BOTransaction") postFilter {
      boolQuery().must(filters: _*)
    } query {
      rangeQuery("transactionDate") gte parsedStartDatetime
        .map(_.getTime)
        .getOrElse(0L) lt parsedEndDatetime
        .map(_.getTime)
        .getOrElse(Long.MaxValue)
    } sortByFieldDesc "transactionDate" start 0 limit Settings.MaxQueryResults

    EsClient.searchAll[BOTransaction](req)
  }

  def getTransaction(uuid: String): Option[BOTransaction] =
    EsClient.search[BOTransaction](
      search(Settings.Mogopay.EsIndex -> "BOTransaction") query {
        termQuery("uuid", uuid)
      })

  def listShopTransactions(transactionId: String): List[BOShopTransaction] = {
    boShopTransactionHandler.findByTransactionUuid(transactionId)
  }

}
