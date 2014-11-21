package com.mogobiz.pay.handlers

import java.io.File
import com.mogobiz.utils.GlobalUtil._
import com.sksamuel.elastic4s.ElasticDsl._
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.es.EsClient
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.pay.settings.Settings
import org.elasticsearch.index.query.TermQueryBuilder

class RateImportHandler {
  def importRates(ratesFile: File) {
    assert(ratesFile.exists(), s"${ratesFile.getAbsolutePath} does not exist.")
    val format = new java.text.SimpleDateFormat("yyyy-MM-dd")

    val req = search in Settings.Mogopay.EsIndex -> "Rate" aggs {
      aggregation max "agg" field "lastUpdated"
    }

    EsClient.search[Rate](req) map (_.lastUpdated.getTime) orElse Some(ratesFile.lastModified) map { lastUpdated =>
      if (lastUpdated <= ratesFile.lastModified) {
        EsClient.client.client
          .prepareDeleteByQuery(Settings.Mogopay.EsIndex)
          .setQuery(new TermQueryBuilder("_type", "Rate"))
          .execute
          .actionGet

        val rates =
          scala.io.Source.fromFile(ratesFile, "utf-8").getLines().flatMap {
            case line if line.trim().length() > 0 =>
              val field = line.trim.split('\t')
              val code = field(0).trim
              val rate = field(1).toDouble
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
  EsClient.client.client.prepareDeleteByQuery(Settings.Mogopay.EsIndex).setQuery(new TermQueryBuilder("_type", "Rate")).execute.actionGet
  rateImportHandler.importRates(Settings.Import.RatesFile)
}
