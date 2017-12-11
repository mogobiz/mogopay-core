/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import java.text.NumberFormat
import java.util.{Currency, Locale}

import com.mogobiz.es.EsClient
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.model.Rate
import com.sksamuel.elastic4s.http.ElasticDsl._

class RateHandler {
  def list = EsClient.searchAll[Rate](search(Settings.Mogopay.EsIndex -> "Rate") from 0 size EsClient.MaxSize)

  def format(amount: Long, currency: String, country: String): Option[String] =
    format(amount.toFloat, currency, country)

  def format(amount: Float, currency: String, country: String): Option[String] = {
    findByCurrencyCode(currency) map { rate =>
      val locale = new Locale(country)

      val numberFormat = NumberFormat.getCurrencyInstance(locale)
      numberFormat.setCurrency(Currency.getInstance(currency))

      numberFormat.format(amount * rate.currencyRate)
    }
  }

  def findByCurrencyCode(currency: String): Option[Rate] = {
    import java.util.Calendar

    val req = search(Settings.Mogopay.EsIndex -> "Rate") query {
      boolQuery().must(termQuery("currencyCode", currency),
                       rangeQuery("activationDate") gt 0 lte Calendar.getInstance.getTime.getTime)
    } sortByFieldDesc "activationDate"
    EsClient.search[Rate](req)
  }

  def convert(amount: Long, originCurrency: String, destinationCurrency: String): Option[Long] = {
    if (originCurrency == destinationCurrency) {
      Option(amount)
    } else {
      (findByCurrencyCode(originCurrency), findByCurrencyCode(destinationCurrency)) match {
        case (Some(src), Some(dst)) =>
          Option(
              amount /
                (src.currencyRate * Math.pow(10, src.currencyFractionDigits.toDouble)) *
                (dst.currencyRate * Math.pow(10, dst.currencyFractionDigits.toDouble))) map (_.toLong)
        case _ => None
      }
    }
  }
}
