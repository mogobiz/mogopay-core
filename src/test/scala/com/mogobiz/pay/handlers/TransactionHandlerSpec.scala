package com.mogobiz.pay.handlers

import com.mogobiz.pay.config.{Settings, Mapping}
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.es.EsClient
import com.mogobiz.pay.handlers.payment.SubmitParams
import com.mogobiz.pay.model.Mogopay.{Account, BOTransaction}
import com.mogobiz.pay.config.Settings
import org.specs2.mutable._

class TransactionHandlerSpec extends Specification with Before {
  def before = {
    Mapping.clear
    Mapping.set

    val acc = Account("xyz", "", None, None, "", None, None, None, None, None, null, 0, 0L, 0L, None, None, None, None, Nil, None, None, Nil, "", Nil, None)
//<<<<<<< HEAD
//    val tx = BOTransaction("123", null, None, null, None, 0L, null, null, null, null, null, false, null, null, null,
//      null, null, null, null, null, null, Some(acc), null, null)
//=======
    val tx = BOTransaction("123", null, None, None, -1, null, None, 0L, null, null, null, null, false, null, null, null,
      null, null, null, null, null, null, Some(acc), null, null)
//    val tx = BOTransaction("123", null, None, None, 100, null, None, 0L, null, null, null, null, null, false, null, null, null,
//      null, null, null, null, Some(acc), null, null)
//>>>>>>> Add RefundJob (and fix many small things)
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

  //  "SubmitParam" in {
  //    "payers" should {
  //      "correctly parse the list payers" in {
  //        val payers = "foo@bar.com:100;a@b.c:1"
  //        val params = SubmitParams(null, null, null, null, null, null, -1L, null, null, null, null, null, null, null,
  //          null, null, null, null, null, Some(payers), None, None, None, None)
  //        params.payers.size must_==           2
  //        params.payers("foo@bar.com") must_== 100L
  //        params.payers("a@b.c") must_==       1L
  //      }
  //    }
  //  }
}