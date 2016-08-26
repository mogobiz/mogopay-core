/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import com.mogobiz.es.EsClient
import com.mogobiz.pay.config.{Settings, Mapping}
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.model._
import com.mogobiz.pay.config.Settings
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
      EsClient.index(Settings.Mogopay.EsIndex, txReq, true)

      val id = transactionSequenceHandler.nextTransactionId(vendorId)
      id must_== 2
    }
  }
}