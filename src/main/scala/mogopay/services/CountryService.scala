package mogopay.services

import akka.actor.ActorRef
import mogopay.actors.CountryActor.{Admins1, Admins2, CountriesForShipping, _}
import mogopay.config.Implicits._
import mogopay.handlers.PhoneVerification
import mogopay.model.Mogopay.CountryAdmin
import mogopay.services.Util._
import spray.routing.Directives

import scala.concurrent.ExecutionContext

class CountryService(country: ActorRef)(implicit executionContext: ExecutionContext) extends Directives {

  import akka.pattern.ask
  import akka.util.Timeout

import scala.concurrent.duration._

  implicit val timeout = Timeout(2.seconds)

  val route = {
    pathPrefix("country") {
      countriesForShipping ~
        countriesForBilling ~
        countryPath ~
        admins1 ~
        admins2 ~
        cities ~
        checkPhoneNumber
    }
  }

  lazy val countriesForShipping = path("countries-for-shipping") {
    get {
      complete {
        (country ? CountriesForShipping).mapTo[List[Country]]
      }
    }
  }

  lazy val countriesForBilling = getPath("countries-for-billing") {
    complete {
      (country ? CountriesForBilling).mapTo[List[Country]]
    }
  }

  lazy val countryPath = path("country" / Segment) { code =>
    get {
      complete {
        (country ? Country(code)).mapTo[Option[Country]]
      }
    }
  }
  lazy val admins1 = path(Segment / "admins1") { countryCode =>
    get {
          complete {
            (country ? Admins1(countryCode)).mapTo[List[CountryAdmin]]
          }
    }
  }

  lazy val cities = path("cities") {
    get {
      val params = parameters('country.?, 'parent_admin1_code.?, 'parent_admin1_code.?, 'city.?)
      params { (c, a1, a2, city) =>
        val cities = Cities(c, a1, a2, city)
        complete {
          (country ? cities).mapTo[Seq[CountryAdmin]]
        }
      }
    }
  }

  lazy val admins2 = path(Segment / "admins2") { countryCode =>
    get {
      parameters('admin1.?) { admin1 =>
          complete {
            (country ? Admins2(countryCode, admin1)).mapTo[Seq[CountryAdmin]]
          }
      }
    }
  }

  lazy val checkPhoneNumber = path(Segment / "check-phone-number" / Segment) { (countryCode, phone) =>
    get {
        complete {
          (country ? CheckPhoneNumber(phone, countryCode)).mapTo[PhoneVerification]
        }
    }
  }
}
