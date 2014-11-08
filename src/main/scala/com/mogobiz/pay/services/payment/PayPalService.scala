package com.mogobiz.pay.services.payment

import akka.actor.ActorRef
import com.mogobiz.pay.actors.PayPalActor._
import com.mogobiz.pay.implicits.Implicits
import Implicits._
import com.mogobiz.pay.services.Util._
import com.mogobiz.session.SessionESDirectives
import com.mogobiz.session.SessionESDirectives._
import spray.http.HttpHeaders.`Content-Type`
import spray.http.{MediaTypes, HttpResponse, StatusCodes, Uri}
import spray.routing.Directives

import scala.concurrent.ExecutionContext
import scala.util._

class PayPalService(actor: ActorRef)(implicit executionContext: ExecutionContext) extends Directives {

  import akka.pattern.ask
  import akka.util.Timeout

import scala.concurrent.duration._

  implicit val timeout = Timeout(40.seconds)

  val route = {
    pathPrefix("paypal") {
      startPayment ~
        fail ~
        success
    }
  }

  lazy val startPayment = path("start" / Segment) { xtoken =>
    get {
      parameterMap { params =>
        val session = SessionESDirectives.load(xtoken).get
        val message = StartPayment(session.sessionData)
        onComplete((actor ? message).mapTo[Try[Either[String, Uri]]]) {
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
                      println(content)
                      complete(HttpResponse(entity = content).withHeaders(List(`Content-Type`(MediaTypes.`text/html`))))
                    case Right(url) =>
                      redirect(url, StatusCodes.TemporaryRedirect)
                  }
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