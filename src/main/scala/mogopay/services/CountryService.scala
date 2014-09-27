package mogopay.services

import akka.actor.ActorRef
import mogopay.actors.CountryActor.{Admins1, Admins2, CountriesForShipping, _}
import mogopay.config.Implicits._
import mogopay.handlers.PhoneVerification
import mogopay.model.Mogopay.{Rate, CountryAdmin}
import spray.http.StatusCodes
import spray.routing.Directives

import scala.concurrent.ExecutionContext
import scala.util.Try

class CountryService(actor: ActorRef)(implicit executionContext: ExecutionContext) extends Directives with DefaultComplete {

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
      onComplete((actor ? CountriesForShipping).mapTo[Try[List[Country]]]) { call =>
        handleComplete(call, (countries: List[Country]) => complete(StatusCodes.OK -> countries))
      }
    }
  }

  lazy val countriesForBilling = get {
    path("countries-for-billing") {
      onComplete((actor ? CountriesForBilling).mapTo[Try[List[Country]]]) { call =>
        handleComplete(call, (countries: List[Country]) => complete(StatusCodes.OK -> countries))
      }
    }
  }

  lazy val countryPath = path("country" / Segment) { code =>
    get {
      onComplete((actor ? Country(code)).mapTo[Try[Option[Country]]]) { call =>
        handleComplete(call, (country: Option[Country]) => complete(StatusCodes.OK -> country))
      }
    }
  }
  lazy val admins1 = path(Segment / "admins1") { countryCode =>
    get {
      onComplete((actor ? Admins1(countryCode)).mapTo[Try[List[CountryAdmin]]]) { call =>
        handleComplete(call, (admins: List[CountryAdmin]) => complete(StatusCodes.OK -> admins))
      }
    }
  }

  lazy val cities = path("cities") {
    get {
      val params = parameters('country.?, 'parent_admin1_code.?, 'parent_admin1_code.?, 'city.?)
      params { (c, a1, a2, city) =>
        onComplete((actor ? Cities(c, a1, a2, city)).mapTo[Try[Seq[CountryAdmin]]]) { call =>
          handleComplete(call, (admins: Seq[CountryAdmin]) => complete(StatusCodes.OK -> admins))
        }
      }
    }
  }

  lazy val admins2 = path(Segment / "admins2") { countryCode =>
    get {
      parameters('admin1.?) { admin1 =>
        onComplete((actor ? Admins2(countryCode, admin1)).mapTo[Try[Seq[CountryAdmin]]]) { call =>
          handleComplete(call, (admins: Seq[CountryAdmin]) => complete(StatusCodes.OK -> admins))
        }
      }
    }
  }

  lazy val checkPhoneNumber = path(Segment / "check-phone-number" / Segment) { (countryCode, phone) =>
    get {
      onComplete((actor ? CheckPhoneNumber(phone, countryCode)).mapTo[Try[PhoneVerification]]) { call =>
        handleComplete(call, (phoneVerif: PhoneVerification) => complete(StatusCodes.OK -> phoneVerif))
      }
    }
  }
}
