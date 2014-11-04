package com.mogobiz.pay.services

import com.mogobiz.pay.actors.RateActor._
import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.implicits.Implicits
import Implicits._
import com.mogobiz.pay.model.Mogopay.Rate
import spray.http.StatusCodes
import spray.routing.Directives

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Try

class RateService(rateActor: ActorRef)(implicit executionContext: ExecutionContext) extends Directives with DefaultComplete {
  implicit val timeout = Timeout(10.seconds)

  val route = pathPrefix("rate") {
    list ~
      format
  }

  lazy val list = path("list") {
    get {
      onComplete((rateActor ? ListRates).mapTo[Try[Seq[Rate]]]) { call =>
        handleComplete(call, (rates: Seq[Rate]) => complete(StatusCodes.OK -> rates))
      }
    }
  }

  lazy val format = path("format") {
    get {
      parameters('amount.as[Long], 'currency, 'country) { (amount, currency, country) =>
        onComplete((rateActor ? Format(amount, currency, country)).mapTo[Try[Option[String]]]) { call =>
          handleComplete(call, (res: Option[String]) => complete(StatusCodes.OK -> res))
        }
      }
    }
  }
}
