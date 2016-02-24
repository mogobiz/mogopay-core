/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.services

import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.implicits.Implicits
import Implicits._
import com.mogobiz.pay.handlers.PhoneVerification
import com.mogobiz.pay.model.Mogopay.{ Country, CountryAdmin }
import spray.http.StatusCodes
import spray.routing.Directives

class CountryService extends Directives with DefaultComplete {

  val route = {
    pathPrefix("countries") {
      countriesForShipping ~
        countriesForBilling ~
        countryPath
    }
  }

  lazy val countriesForShipping = path("for-shipping") {
    get {
      dynamic {
        handleCall(countryHandler.findCountriesForShipping(),
          (countries: Seq[Country]) => complete(StatusCodes.OK -> countries))
      }
    }
  }

  lazy val countriesForBilling = path("for-billing") {
    get {
      dynamic {
        handleCall(countryHandler.findCountriesForBilling(),
          (countries: Seq[Country]) => complete(StatusCodes.OK -> countries))
      }
    }
  }

  lazy val countryPath = pathPrefix(Segment) { countryCode =>
    path("check-phone-number") {
      post {
        entity(as[String]) { phone =>
          handleCall(countryHandler.checkPhoneNumber(phone, countryCode),
            (phoneVerif: PhoneVerification) => complete(StatusCodes.OK -> phoneVerif))
        }
      }
    } ~
      path("cities") {
        get {
          parameters('name.?, 'parent_admin1_code.?, 'parent_admin2_code.?) {
            (name, a1, a2) =>
              handleCall(countryAdminHandler.cities(Some(countryCode), a1, a2, name),
                (admins: Seq[CountryAdmin]) => complete(StatusCodes.OK -> admins))
          }
        }
      } ~
      pathPrefix(("admin1" | "states")) {
        pathPrefix(Segment) {
          admin1Code =>
            path(("admin2" | "regions")) {
              get {
                handleCall(countryAdminHandler.admins2(admin1Code),
                  (admins: Seq[CountryAdmin]) => complete(StatusCodes.OK -> admins))
              }
            } ~ pathEnd {
              get {
                handleCall(countryAdminHandler.getAdmin1ByCode(countryCode, admin1Code),
                  (country: Option[CountryAdmin]) => complete(StatusCodes.OK -> country))
              }
            }
        } ~
          pathEnd {
            get {
              handleCall(countryAdminHandler.admins1(countryCode),
                (admins: Seq[CountryAdmin]) => complete(StatusCodes.OK -> admins))
            }
          }
      } ~
      pathPrefix(("admin2" | "regions") / Segment) { admin2Code =>
        get {
          handleCall(countryAdminHandler.getAdmin2ByCode(countryCode, admin2Code),
            (country: Option[CountryAdmin]) => complete(StatusCodes.OK -> country))
        }
      } ~
      pathEnd {
        get {
          handleCall(countryHandler.findByCode(countryCode),
            (country: Option[Country]) => complete(StatusCodes.OK -> country))
        }
      }
  }
}
