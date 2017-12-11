/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import java.io.File

import com.mogobiz.es.EsClient
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.model._
import com.mogobiz.utils.GlobalUtil._
import com.sksamuel.elastic4s.http.ElasticDsl._

class RateImportHandler {
  def importRates(ratesFile: File): Unit = {
    assert(ratesFile.exists(), s"${ratesFile.getAbsolutePath} does not exist.")
    val format = new java.text.SimpleDateFormat("yyyy-MM-dd")

    val req = search(Settings.Mogopay.EsIndex -> "Rate") query matchAllQuery() aggregations {
      maxAggregation("agg") field "lastUpdated"
    }

    EsClient.search[Rate](req) map (_.lastUpdated.getTime) orElse Some(ratesFile.lastModified) foreach { lastUpdated =>
      if (lastUpdated <= ratesFile.lastModified) {
        EsClient.delIndex(Settings.Mogopay.EsIndex -> "Rate")
        val rates = scala.io.Source.fromFile(ratesFile, "utf-8").getLines().flatMap {
          case line if line.trim().length() > 0 =>
            val field          = line.trim.split('\t')
            val code           = field(0).trim
            val rate           = field(1).toDouble
            val fractionDigits = field(2).toInt
            val activationDate = format.parse(field(3).trim)
            Some(Rate(newUUID, code, activationDate, rate, fractionDigits))
          case _ => None
        }
        rates.foreach { rate =>
          println("indexing")
          EsClient.index(Settings.Mogopay.EsIndex, rate, true)
        }
      }
    }
  }
}

object RateImportMain extends App {
  println("Start...\n")
  EsClient.delIndex(Settings.Mogopay.EsIndex -> "Rate")
  rateImportHandler.importRates(Settings.Import.RatesFile)
}
