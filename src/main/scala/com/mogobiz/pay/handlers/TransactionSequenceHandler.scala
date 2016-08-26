/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import java.text.SimpleDateFormat
import java.util.Date

import com.mogobiz.pay.config.{Environment, Settings}
import com.sksamuel.elastic4s.ElasticDsl._
import com.mogobiz.es.EsClient
import com.mogobiz.pay.model._
import org.joda.time.{DateTime, DateTimeComparator}

import scala.util._

class TransactionSequenceHandler {
  def findByVendorId(uuid: String): Option[TransactionSequence] = {
    val req = search in Settings.Mogopay.EsIndex -> "TransactionSequence" postFilter termFilter("vendorId" -> uuid)
    EsClient.search[TransactionSequence](req)
  }

  // TODO Maybe we should check what the payment provider is
  def nextTransactionId(vendorId: String): Long = {
    val maybeRes = EsClient.loadWithVersion[TransactionSequence](Settings.Mogopay.EsIndex, vendorId)
    maybeRes map {
      case (seq, version) =>
        val newTxId =
          if (DateTimeComparator.getDateOnlyInstance.compare(new DateTime(seq.lastUpdated), new DateTime()) == 0) {
            seq.transactionId + 1
          } else {
            1L
          }

        val tryUpdate = Try {
          EsClient.update[TransactionSequence](Settings.Mogopay.EsIndex, seq.copy(transactionId = newTxId), version)
          newTxId
        }

        tryUpdate match {
          case Success(id) => id
          case Failure(_)  => nextTransactionId(vendorId)
        }
    } getOrElse {
      // TODO should not be done here. It should be at vendor creation time
      val seq =
        if (Settings.Env == Environment.DEV)
          new SimpleDateFormat("HHmmss").format(new Date()).toLong
        else
          1L
      EsClient.index(Settings.Mogopay.EsIndex, TransactionSequence(vendorId, seq), false)
      seq
    }
  }
}
