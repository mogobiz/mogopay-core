package mogopay.handlers

import com.sksamuel.elastic4s.ElasticDsl._
import mogopay.config.Settings
import mogopay.es.EsClient
import mogopay.model.Mogopay._
import org.joda.time.{DateTimeComparator, DateTime}

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
      EsClient.index(TransactionSequence(vendorId, vendorId, 1L))
      1L
    }
  }
}
