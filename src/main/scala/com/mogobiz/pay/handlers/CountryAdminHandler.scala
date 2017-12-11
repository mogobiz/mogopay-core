/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import com.mogobiz.es.EsClient
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.model._
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.searches.queries.QueryDefinition

class CountryAdminHandler {
  def getAdmin1ByCode(countryCode: String, admin1Code: String): Option[CountryAdmin] = {
    val req = search(Settings.Mogopay.EsIndex -> "CountryAdmin") query {
      boolQuery().must(
          termQuery("level"        -> 1),
          termQuery("country.code" -> countryCode),
          termQuery("code"         -> admin1Code)
      )
    } size Int.MaxValue
    EsClient.search[CountryAdmin](req)
  }

  def getAdmin2ByCode(countryCode: String, admin2Code: String): Option[CountryAdmin] = {
    val req = search(Settings.Mogopay.EsIndex -> "CountryAdmin") query {
      boolQuery().must(
          termQuery("level"        -> 2),
          termQuery("country.code" -> countryCode),
          termQuery("code"         -> admin2Code)
      )
    } size Int.MaxValue
    EsClient.search[CountryAdmin](req)
  }

  def admins1(countryCode: String): Seq[CountryAdmin] = {
    val req = search(Settings.Mogopay.EsIndex -> "CountryAdmin") query {
      boolQuery().must(
          termQuery("level"        -> 1),
          termQuery("country.code" -> countryCode)
      )
    } size Int.MaxValue
    EsClient.searchAll[CountryAdmin](req) sortBy (_.name)
  }

  def admins2(admin1Code: String): Seq[CountryAdmin] = {
    val req = search(Settings.Mogopay.EsIndex -> "CountryAdmin") query {
      boolQuery().must(
          termQuery("level"                    -> 2),
          termQuery("parentCountryAdmin1.code" -> admin1Code)
      )
    } size Int.MaxValue
    EsClient.searchAll[CountryAdmin](req) sortBy (_.name)
  }

  def cities(countryCode: Option[String] = None,
             admin1Code: Option[String] = None,
             admin2Code: Option[String] = None,
             name: Option[String] = None) = {
    val filters: Seq[Option[QueryDefinition]] = List(
        Option(termQuery("level"                                    -> 3)),
        name.map(name => regexQuery("name"                          -> (".*" + name.toLowerCase + ".*"))),
        countryCode.map(code => termQuery("country.code"            -> code)),
        admin1Code.map(code => termQuery("parentCountryAdmin1.code" -> code)),
        admin2Code.map(code => termQuery("parentCountryAdmin2.code" -> code))
    )
    val req = search(Settings.Mogopay.EsIndex -> "CountryAdmin") query {
      boolQuery().must(filters.flatten: _*)
    } size Int.MaxValue
    EsClient.searchAll[CountryAdmin](req) sortBy (_.name)
  }
}
