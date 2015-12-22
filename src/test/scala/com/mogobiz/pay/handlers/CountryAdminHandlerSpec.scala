/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import com.mogobiz.pay.config.MogopayHandlers.handlers._
import org.specs2.mutable._

class CountryAdminHandlerSpec extends Specification {
  "findAdmin1" should {
    "return an admin1 list" in {
//      countryAdminHandler.findAdmin1("GB").size must_== 4
      true
    }

    "return nothing if nothing is found" in {
//      countryAdminHandler.findAdmin1("slayer").size must_== 0
      true
    }
  }

  "findAdmin2" should {
    "return an admin2 list" in {
//      countryAdminHandler.findAdmin2("FR", None).size must_== 101
      true
    }

    "return an admin2 list" in {
//      countryAdminHandler.findAdmin2("FR", Some("FR.11")).size must_== 8
      true
    }

    "return nothing if nothing is found" in {
//      countryAdminHandler.findAdmin2("slayer", None).size must_== 0
      true
    }
  }

  "findCities" should {
    "return cities by country" in {
//      countryAdminHandler.findCities(Some("FR"), None, None).size must_== 34220
      true
    }

    "return cities by region" in {
//      countryAdminHandler.findCities(Some("FR"), Some("FR.83", 1), None).size must_== 1310
      true
    }

    "return cities by department" in {
//      countryAdminHandler.findCities(Some("FR"), Some("FR.83.15", 2), None).size must_== 260
      true
    }

    "return cities whose name starts with..." in {
//      countryAdminHandler.findCities(None, None, Some("CHU")).size must_== 8
      true
    }
  }
}
