package mogopay.handlers

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.FilterDefinition
import mogopay.config.Settings
import mogopay.es.EsClient
import mogopay.model.Mogopay._

class CountryAdminHandler {
  def admins1(countryCode: String): Seq[CountryAdmin] = {
    val req = search in Settings.ElasticSearch.Index -> "CountryAdmin" filter and(
      termFilter("level" -> 1),
      termFilter("country.code" -> countryCode)
    )
    EsClient.searchAll[CountryAdmin](req) sortBy (_.name)
  }

  def admins2(admin1Code: String): Seq[CountryAdmin] = {
    val req = search in Settings.ElasticSearch.Index -> "CountryAdmin" filter and(
      termFilter("level" -> 2),
      termFilter("parentCountryAdmin1.code" -> admin1Code)
    )
    EsClient.searchAll[CountryAdmin](req) sortBy (_.name)
  }

  def cities(countryCode: Option[String] = None,
             admin1Code: Option[String] = None,
             admin2Code: Option[String] = None,
             name: Option[String] = None) = {
    val filters: Seq[Option[FilterDefinition]] = List(
      Option(termFilter("level" -> 3)),
      name.map(name => regexFilter("name" -> (".*" + name.toLowerCase + ".*"))),
      countryCode.map(code => termFilter("country.code" -> code)),
      admin1Code.map(code => termFilter("parentCountryAdmin1.code" -> code)),
      admin2Code.map(code => termFilter("parentCountryAdmin2.code" -> code))
    )
    val req = search in Settings.ElasticSearch.Index -> "CountryAdmin" filter and(filters.flatten: _*)
    EsClient.searchAll[CountryAdmin](req) sortBy (_.name)
  }
}
