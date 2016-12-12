/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import java.util.{Locale, Date, UUID}

import com.mogobiz.pay.common.CartRate
import com.mogobiz.pay.config.{Settings, Mapping}
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.es.EsClient
import com.mogobiz.pay.handlers.payment.{BOTransactionJsonTransform, SubmitParams}
import com.mogobiz.pay.model._
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
    boTransactionHandler.create(tx)
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


  "BOTransactionJsonTransform" should {

    "transform BOTransaction AsJValue" in {

      val boPaymentData = new BOPaymentData(PaymentType.CREDIT_CARD,
        CBPaymentProvider.NONE,
        None,
        None,
        None,
        None,
        None
      )

      val transaction = new BOTransaction(UUID.randomUUID().toString,
        UUID.randomUUID().toString,
        None,
        None,
        100,
        "00001",
        Some(new Date()),
        10000,
        new CartRate("EUR", 950),
        TransactionStatus.COMPLETED,
        Some(new Date()),
        boPaymentData,
        false,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        Nil
      )
      val result = BOTransactionJsonTransform.transformAsJValue(transaction, Locale.ENGLISH)
      result must not beNull
    }
  }
}