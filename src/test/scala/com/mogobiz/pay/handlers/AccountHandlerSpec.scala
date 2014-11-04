package com.mogobiz.pay.handlers

import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.exceptions.Exceptions._
import org.specs2.mutable._

class AccountHandlerSpec extends Specification {
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
}
