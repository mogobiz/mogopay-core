package com.mogobiz.pay.handlers


import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.es.EsClient
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.pay.settings.{Settings, Mapping}
import org.specs2.mutable._
import org.specs2.specification.BeforeExample

class TransactionSequenceHandlerSpec extends Specification with BeforeExample {
  sequential

  def before = {
    Mapping.clear
    Mapping.set
  }

  "getNextTransactionId" should {
    "generate a new ID if there is none" in {
      val id = transactionSequenceHandler.nextTransactionId("123123")
      id must_== 1
    }

    "increment the ID" in {
      val vendorId = "789"

      val txReq = TransactionSequence(vendorId, 1)
      boTransactionHandler.save(txReq, true)

      val id = transactionSequenceHandler.nextTransactionId(vendorId)
      id must_== 2
    }
  }
}