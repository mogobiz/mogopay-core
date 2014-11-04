package com.mogobiz.pay.services

import akka.actor.ActorRef
import com.mogobiz.pay.actors.CountryActor.{Admins1, Admins2, CountriesForShipping, _}
import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.implicits.Implicits
import Implicits._
import com.mogobiz.pay.handlers.PhoneVerification
import com.mogobiz.pay.model.Mogopay.{Rate, CountryAdmin}
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
      onComplete((actor ? CountriesForShipping).mapTo[Try[Seq[Country]]]) { call =>
        handleComplete(call, (countries: Seq[Country]) => complete(StatusCodes.OK -> countries))
      }
    }
  }

  lazy val countriesForBilling = get {
    path("countries-for-billing") {
      onComplete((actor ? CountriesForBilling).mapTo[Try[Seq[Country]]]) { call =>
        handleComplete(call, (countries: Seq[Country]) => complete(StatusCodes.OK -> countries))
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
  lazy val admins1 = path("admins1" / Segment) { countryCode =>
    get {
      onComplete((actor ? Admins1(countryCode)).mapTo[Try[Seq[CountryAdmin]]]) { call =>
        handleComplete(call, (admins: Seq[CountryAdmin]) => complete(StatusCodes.OK -> admins))
      }
    }
  }

  lazy val cities = path("cities") {
    get {
      val params = parameters('country.?, 'parent_admin1_code.?, 'parent_admin2_code.?, 'name.?)
      params { (c, a1, a2, name) =>
        onComplete((actor ? Cities(c, a1, a2, name)).mapTo[Try[Seq[CountryAdmin]]]) { call =>
          handleComplete(call, (admins: Seq[CountryAdmin]) => complete(StatusCodes.OK -> admins))
        }
      }
    }
  }

  lazy val admins2 = path("admins2" / Segment) { admin1 =>
    get {
      onComplete((actor ? Admins2(admin1)).mapTo[Try[Seq[CountryAdmin]]]) { call =>
        handleComplete(call, (admins: Seq[CountryAdmin]) => complete(StatusCodes.OK -> admins))
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
