package mogopay.services

import mogopay.actors.RateActor._
import mogopay.config.Implicits._
import mogopay.model.Mogopay.Rate
import spray.routing.Directives

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class RateService(rateActor: ActorRef)(implicit executionContext: ExecutionContext) extends Directives {
  implicit val timeout = Timeout(10.seconds)

  val route = pathPrefix("rate") {
    list ~
    format
  }

  lazy val list = path("list") {
    get {
      complete {
        (rateActor ? ListRates).mapTo[Seq[Rate]]
      }
    }
  }

  lazy val format = path("format") {
    get {
      parameters('amount.as[Long], 'currency, 'country) { (amount, currency, country) =>
        complete {
          (rateActor ? Format(amount, currency, country)).mapTo[Option[String]]
        }
      }
    }
  }
}
