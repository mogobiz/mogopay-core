package com.mogobiz.pay.handlers

import com.mogobiz.pay.config.{Settings, Mapping}
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.es.EsClient
import com.mogobiz.pay.model.Mogopay.{Account, BOTransaction}
import com.mogobiz.pay.config.Settings
import org.specs2.mutable._

class TransactionHandlerSpec extends Specification with Before {
  def before = {
    Mapping.clear
    Mapping.set

    val acc = Account("xyz", "", None, None, "", None, None, None, None, None, null, 0, 0L, 0L, None, None, None, None, Nil, None, None, Nil, "", Nil, None)
    val tx = BOTransaction("123", null, null, None, 0L, null, null, null, null, null, false, null, null, null,
      null, null, null, null, Some(acc), null, null)
    boTransactionHandler.save(tx, true)
  }

  "searchByCustomer" should {
    "find an existent result" in {
      val maybeTransaction = EsClient.load[BOTransaction](Settings.Mogopay.EsIndex, "123")
      transactionHandler.searchByCustomer("xyz").size must_== 1
    }

    "not find a non-existent result" in {
      transactionHandler.searchByCustomer("0") must beEmpty
    }
  }
}