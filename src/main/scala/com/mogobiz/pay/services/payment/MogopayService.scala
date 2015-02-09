package com.mogobiz.pay.services.payment

import akka.actor.ActorRef
import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.implicits.Implicits
import Implicits._
import com.mogobiz.session.SessionESDirectives
import com.mogobiz.session.SessionESDirectives._
import spray.http.HttpHeaders.`Content-Type`
import spray.http.{MediaTypes, HttpResponse, StatusCodes, Uri}
import spray.routing.Directives

import scala.util._

class MogopayService extends Directives with DefaultComplete {

  val route = {
    pathPrefix("mogopay") {
      authenticate ~
        startPayment
    }
  }

  lazy val authenticate = path("authenticate" / Segment) { xtoken =>
    get {
      val session = SessionESDirectives.load(xtoken).get
      handleCall(mogopayHandler.authenticate(session.sessionData),
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

  lazy val startPayment = path("start" / Segment) { xtoken =>
    get {
      val session = SessionESDirectives.load(xtoken).get
      handleCall(mogopayHandler.startPayment(session.sessionData),
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