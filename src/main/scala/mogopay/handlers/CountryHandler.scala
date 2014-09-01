package mogopay.handlers

import com.sksamuel.elastic4s.ElasticDsl._
import mogopay.config.Settings
import mogopay.es.EsClient
import mogopay.model.Mogopay._

class CountryHandler {
  def findCountriesForShipping() = findCountriesByBool("shipping")

  def findCountriesForBilling() = findCountriesByBool("billing")

  private def findCountriesByBool(predicate: String): Seq[Country] = {
    val req = search in Settings.DB.INDEX -> "Country" filter termFilter(predicate -> true)
    EsClient.searchAll[Country](req) sortBy(_.name)
  }

  def findByCode(code: String): Option[Country] = {
    val req = search in Settings.DB.INDEX types "Country" filter termFilter("code", code)
    EsClient.search[Country](req)
  }
}