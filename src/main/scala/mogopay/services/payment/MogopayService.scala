package mogopay.services.payment

import akka.actor.ActorRef
import mogopay.actors.MogopayActor._
import mogopay.config.Implicits._
import mogopay.services.Util._
import mogopay.session.SessionESDirectives
import mogopay.session.SessionESDirectives._
import spray.http.HttpHeaders.`Content-Type`
import spray.http.{MediaTypes, HttpResponse, StatusCodes, Uri}
import spray.routing.Directives

import scala.concurrent.ExecutionContext
import scala.util._

class MogopayService(actor: ActorRef)(implicit executionContext: ExecutionContext) extends Directives {

  import akka.pattern.ask
  import akka.util.Timeout

  import scala.concurrent.duration._

  implicit val timeout = Timeout(10.seconds)

  val route = {
    pathPrefix("mogopay") {
      authenticate ~
        startPayment
    }
  }

  lazy val authenticate = path("authenticate" / Segment) { xtoken =>
    get {
      val session = SessionESDirectives.load(xtoken).get
      onComplete((actor ? Authenticate(session.sessionData)).mapTo[Try[Either[String, Uri]]]) {
        case Failure(t) => complete(StatusCodes.InternalServerError)
        case Success(r) =>
          r match {
            case Failure(t) =>
              println(t)
              complete(toHTTPResponse(t), Map('error -> t.toString))
            case Success(data) =>
              setSession(session) {
                data match {
                  case Left(content) =>
                    complete(HttpResponse(entity = content).withHeaders(List(`Content-Type`(MediaTypes.`text/html`))))
                  case Right(url) =>
                    println(url)
                    redirect(url, StatusCodes.TemporaryRedirect)
                }
              }
          }
      }
    }
  }

  lazy val startPayment = path("start-payment" / Segment) { xtoken =>
    get {
      val session = SessionESDirectives.load(xtoken).get

      onComplete((actor ? StartPayment(session.sessionData)).mapTo[Try[Uri]]) {
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