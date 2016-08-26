/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import com.google.i18n.phonenumbers.{PhoneNumberUtil, NumberParseException}
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.mogobiz.pay.exceptions.Exceptions.InvalidPhoneNumberException
import com.mogobiz.pay.model.Telephone
import com.mogobiz.pay.model.TelephoneStatus.TelephoneStatus

class TelephoneHandler {
  lazy val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

  def buildTelephone(number: String, countryCode: String, status: TelephoneStatus): Telephone = {
    try {
      val phoneNumber = phoneUtil.parse(number, countryCode)
      Telephone(phone = phoneUtil.format(phoneNumber, PhoneNumberFormat.INTERNATIONAL),
                lphone = phoneUtil.format(phoneNumber, PhoneNumberFormat.NATIONAL),
                isoCode = countryCode,
                pinCode3 = Some("000"),
                status = status)
    } catch {
      case e: NumberParseException => throw InvalidPhoneNumberException(e.toString)
      case e: Throwable            => throw e
    }
  }
}
