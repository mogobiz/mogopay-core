/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import com.mogobiz.es.EsClient
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.config.{Mapping, Settings}
import com.mogobiz.pay.model._
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class TransactionSequenceHandlerSpec extends FlatSpec with Matchers with BeforeAndAfter {

  before {
    Mapping.clear
    Mapping.set
  }

  "getNextTransactionId" should "generate a new ID if there is none" in {
    val id = transactionSequenceHandler.nextTransactionId("123123")
    id should be
    1L
  }

  it should "increment the ID" in {
    val vendorId = "789"

    val txReq = TransactionSequence(vendorId, 1)
    EsClient.index(Settings.Mogopay.EsIndex, txReq, true)

    val id = transactionSequenceHandler.nextTransactionId(vendorId)
    id should be
    2L
  }
}