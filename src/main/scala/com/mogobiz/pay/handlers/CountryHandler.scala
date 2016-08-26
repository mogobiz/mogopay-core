/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.{PhoneNumberFormat, PhoneNumberType}
import com.mogobiz.pay.config.Settings
import com.sksamuel.elastic4s.ElasticDsl._
import com.mogobiz.es.EsClient
import com.mogobiz.pay.model._

case class PhoneVerification(isValid: Boolean,
                             nationalFormat: Option[String] = None,
                             internationalFormat: Option[String] = None,
                             phoneType: Option[PhoneNumberType] = None)

class CountryHandler {
  def findCountriesForShipping(): Seq[Country] = {
    // (Integer.MAX_VALUE / 2) because Lucene says us `Caused by: java.lang.IllegalArgumentException: maxSize must be <= 2147483391; got: 2147483647`
    val req = search in Settings.Mogopay.EsIndex -> "Country" postFilter termFilter("shipping" -> true) size (Integer.MAX_VALUE / 2)
    EsClient.searchAll[Country](req) sortBy (_.name)
  }

  def findCountriesForBilling(): Seq[Country] = {
    val req = search in Settings.Mogopay.EsIndex -> "Country" postFilter termFilter("billing" -> true) size (Integer.MAX_VALUE / 2)
    EsClient.searchAll[Country](req) sortBy (_.name)
  }

  def findByCode(code: String): Option[Country] = {
    val req = search in Settings.Mogopay.EsIndex types "Country" postFilter termFilter("code", code) size (Integer.MAX_VALUE / 2)
    EsClient.search[Country](req)
  }

  def checkPhoneNumber(phone: String, country: String): PhoneVerification = {
    val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

    try {
      val phoneNumber = phoneUtil.parse(phone, country)
      if (!phoneUtil.isValidNumberForRegion(phoneNumber, country)) {
        PhoneVerification(isValid = false)
      } else {
        PhoneVerification(isValid = true,
                          Some(phoneUtil.format(phoneNumber, PhoneNumberFormat.NATIONAL)),
                          Some(phoneUtil.format(phoneNumber, PhoneNumberFormat.INTERNATIONAL)),
                          Some(phoneUtil.getNumberType(phoneNumber)))
      }
    } catch {
      case _: Throwable => PhoneVerification(isValid = false)
    }
  }
}
