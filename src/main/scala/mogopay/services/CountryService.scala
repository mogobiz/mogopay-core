package mogopay.services

import akka.actor.ActorRef
import mogopay.actors.CountryActor._
import mogopay.actors.CountryActor.Admins1
import mogopay.actors.CountryActor.Admins2
import mogopay.actors.CountryActor.CountriesForShipping
import mogopay.services.Util._
import scala.concurrent.ExecutionContext
import spray.routing.Directives
import mogopay.model.Mogopay.CountryAdmin
import mogopay.handlers.PhoneVerification

import mogopay.config.Implicits._

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

  lazy val countriesForShipping = path("countriesForShipping") {
    get {
      complete {
          (country ? CountriesForShipping).mapTo[List[Country]]
      }
    }
  }

  lazy val countriesForBilling = getPath("countriesForBilling") {
    complete {
      (country ? CountriesForBilling).mapTo[List[Country]]
    }
  }

  lazy val countryPath = getPath("country") {
    parameters('code).as(Country) { c =>
      complete { (country ? c).mapTo[Option[Country]] }
    }
  }

  lazy val admins1 = path("admins1") {
    get {
      parameter('country).as(Admins1) {
        admins1: Admins1 =>
          complete { (country ? admins1).mapTo[List[CountryAdmin]] }
      }
    }
  }

  lazy val cities = path("cities") {
    get {
      val params = parameters('country.?, 'parentAdmin1Code.?, 'parentAdmin1Code.?, 'city.?)
      params { (c, a1, a2, city) =>
        val cities = Cities(c, a1, a2, city)
        complete { (country ? cities).mapTo[Seq[CountryAdmin]] }
      }
    }
  }

  lazy val admins2 = path("admins2") {
    get {
      parameters('country, 'parentAdmin1Code.?).as(Admins2) {
        admins2: Admins2 =>
          complete { (country ? admins2).mapTo[Seq[CountryAdmin]] }
      }
    }
  }

  lazy val checkPhoneNumber = path("checkPhoneNumber") {
    get {
      parameters('phone, 'country).as(CheckPhoneNumber) { checkPhoneNumber =>
          complete { (country ? checkPhoneNumber).mapTo[PhoneVerification] }
      }
    }
  }
}
