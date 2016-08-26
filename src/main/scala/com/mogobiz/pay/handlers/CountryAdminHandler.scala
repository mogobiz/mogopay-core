/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import com.mogobiz.pay.config.Settings
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.FilterDefinition
import com.mogobiz.es.EsClient
import com.mogobiz.pay.model._

class CountryAdminHandler {
  def getAdmin1ByCode(countryCode: String, admin1Code: String): Option[CountryAdmin] = {
    val req = search in Settings.Mogopay.EsIndex -> "CountryAdmin" postFilter and(
          termFilter("level"        -> 1),
          termFilter("country.code" -> countryCode),
          termFilter("code"         -> admin1Code)
      ) size Integer.MAX_VALUE
    EsClient.search[CountryAdmin](req)
  }

  def getAdmin2ByCode(countryCode: String, admin2Code: String): Option[CountryAdmin] = {
    val req = search in Settings.Mogopay.EsIndex -> "CountryAdmin" postFilter and(
          termFilter("level"        -> 2),
          termFilter("country.code" -> countryCode),
          termFilter("code"         -> admin2Code)
      ) size Integer.MAX_VALUE
    EsClient.search[CountryAdmin](req)
  }

  def admins1(countryCode: String): Seq[CountryAdmin] = {
    val req = search in Settings.Mogopay.EsIndex -> "CountryAdmin" postFilter and(
          termFilter("level"        -> 1),
          termFilter("country.code" -> countryCode)
      ) size Integer.MAX_VALUE
    EsClient.searchAll[CountryAdmin](req) sortBy (_.name)
  }

  def admins2(admin1Code: String): Seq[CountryAdmin] = {
    val req = search in Settings.Mogopay.EsIndex -> "CountryAdmin" postFilter and(
          termFilter("level"                    -> 2),
          termFilter("parentCountryAdmin1.code" -> admin1Code)
      ) size Integer.MAX_VALUE
    EsClient.searchAll[CountryAdmin](req) sortBy (_.name)
  }

  def cities(countryCode: Option[String] = None,
             admin1Code: Option[String] = None,
             admin2Code: Option[String] = None,
             name: Option[String] = None) = {
    val filters: Seq[Option[FilterDefinition]] = List(
        Option(termFilter("level"                                    -> 3)),
        name.map(name => regexFilter("name"                          -> (".*" + name.toLowerCase + ".*"))),
        countryCode.map(code => termFilter("country.code"            -> code)),
        admin1Code.map(code => termFilter("parentCountryAdmin1.code" -> code)),
        admin2Code.map(code => termFilter("parentCountryAdmin2.code" -> code))
    )
    val req = search in Settings.Mogopay.EsIndex -> "CountryAdmin" postFilter and(filters.flatten: _*) size Integer.MAX_VALUE
    EsClient.searchAll[CountryAdmin](req) sortBy (_.name)
  }
}
