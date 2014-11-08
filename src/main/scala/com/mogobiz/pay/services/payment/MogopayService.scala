package com.mogobiz.pay.services.payment

import akka.actor.ActorRef
import com.mogobiz.pay.actors.MogopayActor._
import com.mogobiz.pay.config.DefaultComplete
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

class MogopayService(actor: ActorRef)(implicit executionContext: ExecutionContext) extends Directives with DefaultComplete {

  import akka.pattern.ask
  import akka.util.Timeout

  import scala.concurrent.duration._

  implicit val timeout = Timeout(40.seconds)

  val route = {
    pathPrefix("mogopay") {
      authenticate ~
        startPayment
    }
  }

  lazy val authenticate = path("authenticate" / Segment) { xtoken =>
    get {
      val session = SessionESDirectives.load(xtoken).get
      onComplete((actor ? Authenticate(session.sessionData)).mapTo[Try[Either[String, Uri]]]) { call =>
        handleComplete(call,
          (data: Either[String, Uri]) =>
            setSession(session) {
              data match {
                case Left(content) =>
                  complete(HttpResponse(entity = content).withHeaders(List(`Content-Type`(MediaTypes.`text/html`))))
                case Right(url) =>
                  println(url)
                  redirect(url, StatusCodes.TemporaryRedirect)
              }
            }
        )
      }
    }
  }

  lazy val startPayment = path("start" / Segment) { xtoken =>
    get {
      val session = SessionESDirectives.load(xtoken).get

      onComplete((actor ? StartPayment(session.sessionData)).mapTo[Try[Either[String, Uri]]]) { call =>
        handleComplete(call,
          (data: Either[String, Uri]) =>
            setSession(session) {
              data match {
                case Left(content) =>
                  println(content)
                  complete(HttpResponse(entity = content).withHeaders(List(`Content-Type`(MediaTypes.`text/html`))))
                case Right(url) =>
                  redirect(url, StatusCodes.TemporaryRedirect)
              }
            }
        )
      }
    }
  }
}