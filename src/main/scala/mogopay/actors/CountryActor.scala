package mogopay.actors

import akka.actor.Actor
import mogopay.config.HandlersConfig._

import scala.util.Try

object CountryActor {

  case object CountriesForShipping

  case object CountriesForBilling

  case class Admins1(country: String)

  case class Admins2(country: String, admin1: Option[String])

  case class Cities(country: Option[String],
                    parentAdmin1Code: Option[String],
                    parentAdmin2Code: Option[String],
                    city: Option[String])

  case class CheckPhoneNumber(phone: String, country: String)

  case class Country(code: String)

}

class CountryActor extends Actor {

  import CountryActor._

  def receive: Receive = {
    case CountriesForShipping => {
      sender ! Try(countryHandler.findCountriesForShipping)
    }
    case CountriesForBilling => {
      sender ! Try(countryHandler.findCountriesForBilling)
    }
    case Country(code) => {
      sender ! Try(countryHandler.findByCode(code))
    }
    case Admins1(country) => {
      sender ! Try(countryAdminHandler.admins1(country))
    }
    case Admins2(country, parentAdmin) => {
      sender ! Try(countryAdminHandler.admins2(country, parentAdmin))
    }
    case Cities(country, parentAdmin1Code, parentAdmin2Code, cityName) => {
      sender ! Try(countryAdminHandler.cities(country, parentAdmin1Code, parentAdmin2Code, cityName))
    }
    //    case CheckPhoneNumber(phone, country) => {
    //      sender ! telephoneHandler.checkPhoneNumber(phone, country)
    //    }
  }
}
