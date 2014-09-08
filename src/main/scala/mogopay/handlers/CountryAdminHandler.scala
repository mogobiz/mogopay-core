package mogopay.handlers

import mogopay.model.Mogopay._
import scala.concurrent.Future

class CountryAdminHandler {
  def admins1(countryCode: String) = {
    val query: Seq[(String, Any)] = Seq("level" -> 1, "country.code" -> countryCode)
    //    DAO.findAllBy[CountryAdmin](query: _*) map (_.sortBy(_.name))
  }

  def admins2(countryCode: String, admin1Code: Option[String] = None) = {
    val query = Seq("level" -> 2, "country.code" -> countryCode) ++
      admin1Code.map(c => Seq("parentCountryAdmin1" -> c)).getOrElse(Nil)
    //    DAO.findAllBy[CountryAdmin](query: _*) map (_.sortBy(_.name))
  }

  def cities(countryCode: Option[String] = None,
             admin1Code: Option[String] = None,
             admin2Code: Option[String] = None,
             name: Option[String] = None) = {
    val query = Seq("level" -> Some(3),
      "country.code" -> countryCode,
      "parentCountryAdmin1" -> admin1Code,
      "parentCountryAdmin2" -> admin2Code,
      "name" -> name) collect { case (k, Some(v)) => (k, v)}
    //    DAO.findAllBy[CountryAdmin](query: _*) map (_.sortBy(_.name))
  }
}
