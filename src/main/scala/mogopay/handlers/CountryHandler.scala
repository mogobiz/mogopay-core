package mogopay.handlers

import com.sksamuel.elastic4s.ElasticDsl._
import mogopay.config.Settings
import mogopay.es.EsClient
import mogopay.model.Mogopay._

class CountryHandler {
  def findCountriesForShipping(): Seq[Country] = {
    val req = search in Settings.ElasticSearch.Index -> "Country" filter termFilter("shipping" -> true)
    EsClient.searchAll[Country](req) sortBy (_.name)
  }

  def findCountriesForBilling(): Seq[Country] = {
    val req = search in Settings.ElasticSearch.Index -> "Country" filter termFilter("billing" -> true)
    EsClient.searchAll[Country](req) sortBy (_.name)
  }


  def findByCode(code: String): Option[Country] = {
    val req = search in Settings.ElasticSearch.Index types "Country" filter termFilter("code", code)
    EsClient.search[Country](req)
  }
}