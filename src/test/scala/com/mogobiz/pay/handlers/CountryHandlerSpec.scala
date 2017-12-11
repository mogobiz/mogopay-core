/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import org.scalatest.{FlatSpec, Matchers}

class CountryHandlerSpec extends FlatSpec with Matchers {
  "countriesForShipping" should "return a list of countries we can ship to" in {
    //      countryHandler.findCountriesForShipping().size must_== 3
    true
  }

  "countriesForBilling" should "return a list of countries we can ship to" in {
    //      countryHandler.findCountriesForBilling().size must_== 3
    true
  }

  "findByCode" should "return the country name corresponding to a code" in {
    //      countryHandler.findByCode("FR") must beSome
    //      countryHandler.findByCode("FR").get.name must_== "France"
    true
  }

  it should "return None if no country with the specified code exists" in {
    //      countryHandler.findByCode("slayer") must_== None
    true
  }
}
