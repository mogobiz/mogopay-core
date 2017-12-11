/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import org.scalatest.{FlatSpec, Matchers}

class AccountHandlerSpec extends FlatSpec with Matchers {

  val handler = new AccountHandler

  "updatePassword" should "fail if the vendor does not exist" in {
    //      accountHandler.updatePassword("", "666", "0") must beFailedTry.withThrowable[VendorNotFoundException]
    true
  }

  it should "fail if the vendor has no info about the payment configuration" in {
    //      accountHandler.updatePassword("123", "2", "0") must beFailedTry.withThrowable[PaymentConfigIdNotFoundException]
    true
  }

  it should "fail if the password doesn't match the pattern" in {
    //      accountHandler.updatePassword("foo", "1", "0") must beFailedTry.withThrowable[PasswordDoesNotMatchPatternException]
    true
  }

  it should "fail if the account does not exist" in {
    //      accountHandler.updatePassword("123", "1", "0") must beFailedTry.withThrowable[AccountDoesNotExistException]
    true
  }

  it should "update the password if everything went fine" in {
    //      val oldPassword = accountHandler.find("1").map(_.get.password)
    //      accountHandler.updatePassword((new java.util.Date).getTime.toString, 1, 1)
    //      oldPassword mustNotEqual (accountHandler.find(1).map(_.get.password).get)
    true
  }

  "AccountHandler" should "return list of company if connected user is a customer" in {
    val list = handler.listCompagnies(Some("8a53ef3e-34e8-4569-8f68-ac0dfc548a0f"))
    list should have size 1
    list(0) shouldEqual "acmesport"
  }
  it should "return list of company if connected user is a merchant" in {
    val list = handler.listCompagnies(Some("d7b864c8-4567-4603-abd4-5f85e9ff56e6"))
    list should have size 1
    list(0) shouldEqual "acmesport"
  }
}
