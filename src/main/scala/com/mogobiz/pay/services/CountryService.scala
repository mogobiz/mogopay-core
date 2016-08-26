/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.services

import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.implicits.Implicits
import Implicits._
import com.mogobiz.pay.handlers.PhoneVerification
import com.mogobiz.pay.model.{Country, Rate, CountryAdmin}
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

  lazy val countryPath =
    get {
      path("country" / Segment / "admin1" / Segment) { (countryCode, admin1Code) =>
        handleCall(countryAdminHandler.getAdmin1ByCode(countryCode, admin1Code),
                   (country: Option[CountryAdmin]) => complete(StatusCodes.OK -> country))
      }
    } ~
      get {
        path("country" / Segment / "admin2" / Segment) { (countryCode, admin2Code) =>
          handleCall(countryAdminHandler.getAdmin2ByCode(countryCode, admin2Code),
                     (country: Option[CountryAdmin]) => complete(StatusCodes.OK -> country))
        }
      } ~
      get {
        path("country" / Segment) { countryCode =>
          handleCall(countryHandler.findByCode(countryCode),
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

  lazy val checkPhoneNumber = path(Segment / "check-phone-number" / Segment) { (countryCode, phone) =>
    get {
      handleCall(countryHandler.checkPhoneNumber(phone, countryCode),
                 (phoneVerif: PhoneVerification) => complete(StatusCodes.OK -> phoneVerif))
    }
  }
}
