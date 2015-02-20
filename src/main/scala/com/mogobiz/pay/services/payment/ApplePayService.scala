package com.mogobiz.pay.services.payment

import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.implicits.Implicits
import com.mogobiz.pay.implicits.Implicits._
import com.mogobiz.session.SessionESDirectives
import com.mogobiz.session.SessionESDirectives._
import spray.http.HttpHeaders.`Content-Type`
import spray.http.{HttpResponse, MediaTypes, StatusCodes, Uri}
import spray.routing.Directives

import scala.util._

class ApplePayService extends Directives with DefaultComplete {

  val route = {
    pathPrefix("applepay") {
      startPayment ~
        fail ~
        success
    }
  }

  lazy val startPayment = path("start" / Segment) { xtoken =>
    get {
      parameterMap { params =>
        val session = SessionESDirectives.load(xtoken).get
        handleCall(applePayHandler.startPayment(session.sessionData),
          (data: Either[String, Uri]) => {
            setSession(session) {
              data match {
                case Left(content) =>
                  complete(HttpResponse(entity = content).withHeaders(List(`Content-Type`(MediaTypes.`text/html`))))
                case Right(url) =>
                  redirect(url, StatusCodes.TemporaryRedirect)
              }
            }
          })
      }
    }
  }

  lazy val fail = path("fail") {
    get {
      parameters("token") {
        token =>
          session {
            session =>
              handleCall(applePayHandler.fail(session.sessionData, token),
                (url: Uri) => {
                  setSession(session) {
                    redirect(url, StatusCodes.TemporaryRedirect)
                  }
                })
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
              handleCall(applePayHandler.success(session.sessionData, token),
                (url: Uri) => {
                  setSession(session) {
                    redirect(url, StatusCodes.TemporaryRedirect)
                  }
                })
          }
      }
    }
  }
}
