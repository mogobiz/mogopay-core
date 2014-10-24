package mogopay.handlers

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.{PhoneNumberFormat, PhoneNumberType}
import com.sksamuel.elastic4s.ElasticDsl._
import mogopay.config.Settings
import mogopay.es.EsClient
import mogopay.model.Mogopay._

case class PhoneVerification(isValid: Boolean,
                             nationalFormat: Option[String] = None,
                             internationalFormat: Option[String] = None,
                             phoneType: Option[PhoneNumberType] = None)

class CountryHandler {
  def findCountriesForShipping(): Seq[Country] = {
    val req = search in Settings.ElasticSearch.Index -> "Country" filter termFilter("shipping" -> true) size Integer.MAX_VALUE
    EsClient.searchAll[Country](req) sortBy (_.name)
  }

  def findCountriesForBilling(): Seq[Country] = {
    val req = search in Settings.ElasticSearch.Index -> "Country" filter termFilter("billing" -> true) size Integer.MAX_VALUE
    EsClient.searchAll[Country](req) sortBy (_.name)
  }


  def findByCode(code: String): Option[Country] = {
    val req = search in Settings.ElasticSearch.Index types "Country" filter termFilter("code", code) size Integer.MAX_VALUE
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