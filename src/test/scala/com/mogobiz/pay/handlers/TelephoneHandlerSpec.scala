/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import org.scalatest.{FlatSpec, Matchers}

class TelephoneHandlerSpecextends extends FlatSpec with Matchers {

  "checkPhoneNumber" should "fail if the phone number is invalid" in {
    //      val verification: PhoneVerification = telephoneHandler.checkPhoneNumber("FR", "invalid")
    //      verification.isValid must beFalse
    //      verification.nationalFormat must_== None
    //      verification.internationalFormat must_== None
    //      verification.phoneType must_== None
    true
  }

  it should "return true if the phone number is valid" in {
    //      telephoneHandler.checkPhoneNumber("FR", "0123456789").isValid must beTrue
    true
  }

  it should "return the elements of the phone number if it is valid" in {
    //      val verification = telephoneHandler.checkPhoneNumber("FR", "0123456789")
    //      verification.nationalFormat must_== Some("01 23 45 67 89")
    //      verification.internationalFormat must_== Some("+33 1 23 45 67 89")
    //      verification.phoneType must_== Some(PhoneNumberType.FIXED_LINE)
    true
  }
}