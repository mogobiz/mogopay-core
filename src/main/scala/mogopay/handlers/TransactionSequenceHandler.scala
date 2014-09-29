package mogopay.handlers

import java.text.SimpleDateFormat
import java.util.Date

import com.sksamuel.elastic4s.ElasticDsl._
import mogopay.config.{Environment, Settings}
import mogopay.es.EsClient
import mogopay.model.Mogopay._
import org.joda.time.{DateTime, DateTimeComparator}

import scala.util._

class TransactionSequenceHandler {
  def findByVendorId(uuid: String): Option[TransactionSequence] = {
    val req = search in Settings.ElasticSearch.Index -> "TransactionSequence" filter termFilter("vendorId" -> uuid)
    EsClient.search[TransactionSequence](req)
  }

  // TODO Maybe we should check what the payment provider is
  def nextTransactionId(vendorId: String): Long = {
    val maybeRes = EsClient.loadWithVersion[TransactionSequence](vendorId)
    maybeRes map { case (seq, version) =>
      val newTxId =
        if (DateTimeComparator.getDateOnlyInstance.compare(new DateTime(seq.lastUpdated), new DateTime()) == 0) {
          seq.transactionId + 1
        } else {
          1L
        }

      val tryUpdate = Try {
        EsClient.update[TransactionSequence](seq.copy(transactionId = newTxId), version)
        newTxId
      }

      tryUpdate match {
        case Success(id) => id
        case Failure(_) => nextTransactionId(vendorId)
      }
    } getOrElse {
      // TODO should not be done here. It should be at vendor creation time
      val seq = if (Settings.Env == Environment.DEV)
        new SimpleDateFormat("HHmmss").format(new Date()).toLong
      else
        1L
      EsClient.index(TransactionSequence(vendorId, seq))
      seq
    }
  }
}
