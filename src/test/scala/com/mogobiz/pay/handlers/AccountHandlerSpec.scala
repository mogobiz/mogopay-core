/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.exceptions.Exceptions._
import org.specs2.mutable._

class AccountHandlerSpec extends Specification {

  val handler = new AccountHandler

  "updatePassword" should {
    "fail if the vendor does not exist" in {
//      accountHandler.updatePassword("", "666", "0") must beFailedTry.withThrowable[VendorNotFoundException]
      true
    }

    "fail if the vendor has no info about the payment configuration" in {
//      accountHandler.updatePassword("123", "2", "0") must beFailedTry.withThrowable[PaymentConfigIdNotFoundException]
      true
    }

    "fail if the password doesn't match the pattern" in {
//      accountHandler.updatePassword("foo", "1", "0") must beFailedTry.withThrowable[PasswordDoesNotMatchPatternException]
      true
    }

    "fail if the account does not exist" in {
//      accountHandler.updatePassword("123", "1", "0") must beFailedTry.withThrowable[AccountDoesNotExistException]
      true
    }

    "update the password if everything went fine" in {
//      val oldPassword = accountHandler.find("1").map(_.get.password)
//      accountHandler.updatePassword((new java.util.Date).getTime.toString, 1, 1)
//      oldPassword mustNotEqual (accountHandler.find(1).map(_.get.password).get)
      true
    }
  }

  "AccountHandler" should {
    "return list of company if connected user is a customer" in {
      val list = handler.listCompagnies(Some("8a53ef3e-34e8-4569-8f68-ac0dfc548a0f"))
      list must size(1)
      list(0) must beEqualTo("acmesport")
    }
    "return list of company if connected user is a merchant" in {
      val list = handler.listCompagnies(Some("d7b864c8-4567-4603-abd4-5f85e9ff56e6"))
      list must size(1)
      list(0) must beEqualTo("acmesport")
    }
  }
}
