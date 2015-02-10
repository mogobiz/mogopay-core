package com.mogobiz.pay.services

import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.implicits.Implicits
import Implicits._
import com.mogobiz.pay.handlers.PhoneVerification
import com.mogobiz.pay.model.Mogopay.{Country, Rate, CountryAdmin}
import spray.http.StatusCodes
import spray.routing.Directives

class CountryService extends Directives with DefaultComplete {

  val route = {
    pathPrefix("country") {
      countriesForShipping ~
        countriesForBilling ~
        countryPath ~
        admins1 ~
        admins2 ~
        cities ~
        checkPhoneNumber
    }
  }

  lazy val countriesForShipping = path("countries-for-shipping") {
    get {
      dynamic {
        handleCall(countryHandler.findCountriesForShipping(),
          (countries: Seq[Country]) => complete(StatusCodes.OK -> countries))
      }
    }
  }

  lazy val countriesForBilling = get {
    path("countries-for-billing") {
      dynamic {
        handleCall(countryHandler.findCountriesForBilling(),
          (countries: Seq[Country]) => complete(StatusCodes.OK -> countries))
      }
    }
  }

  lazy val countryPath = path("country" / Segment) { code =>
    get {
      handleCall(countryHandler.findByCode(code),
        (country: Option[Country]) => complete(StatusCodes.OK -> country))
    }
  }

  lazy val admins1 = path("admins1" / Segment) { countryCode =>
    get {
      handleCall(countryAdminHandler.admins1(countryCode),
        (admins: Seq[CountryAdmin]) => complete(StatusCodes.OK -> admins))
    }
  }

  lazy val cities = path("cities") {
    get {
        val params = parameters('country.?, 'parent_admin1_code.?, 'parent_admin2_code.?, 'name.?)
        params { (c, a1, a2, name) =>
          handleCall(countryAdminHandler.cities(c, a1, a2, name),
            (admins: Seq[CountryAdmin]) => complete(StatusCodes.OK -> admins))
        }
    }
  }

  lazy val admins2 = path("admins2" / Segment) { admin1 =>
    get {
      handleCall(countryAdminHandler.admins2(admin1),
        (admins: Seq[CountryAdmin]) => complete(StatusCodes.OK -> admins))
    }
  }

  lazy val checkPhoneNumber = path(Segment / "check-phone-number" / Segment) {
    (countryCode, phone) =>
      get {
        handleCall(countryHandler.checkPhoneNumber(phone, countryCode),
          (phoneVerif: PhoneVerification) => complete(StatusCodes.OK -> phoneVerif))
      }
  }
}
