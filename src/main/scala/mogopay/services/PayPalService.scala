package mogopay.services

import akka.actor.ActorRef
import mogopay.actors.PayPalActor._
import mogopay.config.Implicits._
import mogopay.services.Util._
import mogopay.session.SessionESDirectives
import mogopay.session.SessionESDirectives._
import spray.http.{Uri, StatusCodes}
import spray.routing.Directives

import scala.concurrent.{ExecutionContext}
import scala.util._

class PayPalService(actor: ActorRef)(implicit executionContext: ExecutionContext) extends Directives {

  import akka.pattern.ask
  import akka.util.Timeout

  import scala.concurrent.duration._

  implicit val timeout = Timeout(10.seconds)

  val route = {
    pathPrefix("paypal") {
      startPayment ~
        fail ~
        success
    }
  }

  lazy val startPayment = path("start-payment" / Segment) { xtoken =>
    get {
      parameterMap { params =>
          val session = SessionESDirectives.load(xtoken).get
          val message = StartPayment(session.sessionData, params)
          onComplete((actor ? message).mapTo[Try[Uri]]) {
            case Failure(t) => complete(StatusCodes.InternalServerError)
            case Success(r) =>
              setSession(session) {
                r match {
                  case Success(url) => redirect(url, StatusCodes.TemporaryRedirect)
                  case Failure(t) => complete(toHTTPResponse(t), Map('error -> t.toString))
                }
              }
        }
      }
    }
  }

  lazy val fail = path("fail") {
    get {
      parameters("token") {
        token =>
          session {
            session =>
              onComplete((actor ? Fail(session.sessionData, token)).mapTo[Try[Uri]]) {
                case Failure(t) => complete(StatusCodes.InternalServerError)
                case Success(r) =>
                  setSession(session) {
                    r match {
                      case Failure(e) => complete(toHTTPResponse(e), Map())
                      case Success(url) => redirect(url, StatusCodes.TemporaryRedirect)
                    }
                  }
              }
          }
      }
    }
  }

  lazy val success = path("success") {
    get {
      parameters("token") {
        token =>
          session {
            session =>
              onComplete((actor ? SuccessPP(session.sessionData, token)).mapTo[Try[Uri]]) {
                case Failure(t) => complete(StatusCodes.InternalServerError)
                case Success(r) =>
                  setSession(session) {
                    r match {
                      case Failure(e) => complete(toHTTPResponse(e), Map())
                      case Success(url) => redirect(url, StatusCodes.TemporaryRedirect)
                    }
                  }
              }
          }
      }
    }
  }
}