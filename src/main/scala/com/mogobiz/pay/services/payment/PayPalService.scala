/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.services.payment

import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.implicits.Implicits
import Implicits._
import com.mogobiz.session.SessionESDirectives
import com.mogobiz.session.SessionESDirectives._
import spray.http.HttpHeaders.`Content-Type`
import spray.http.{MediaTypes, HttpResponse, StatusCodes, Uri}
import spray.routing.Directives

import scala.util._

class PayPalService extends Directives with DefaultComplete {

  val route = {
    pathPrefix("paypal") {
      startPayment ~
      fail ~
      success
    }
  }

  lazy val startPayment = path("start" / Segment) { xtoken =>
    complete(StatusCodes.OK)
/*
    get {
      parameterMap { params =>
        val session = SessionESDirectives.load(xtoken).get
        handleCall(payPalHandler.startPayment(session.sessionData), (data: Either[String, Uri]) => {
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
    }*/
  }

  lazy val fail = path("fail" / Segment) { xtoken =>
    complete(StatusCodes.OK)
/*
    get {
      parameters("token") { token =>
        val session = SessionESDirectives.load(xtoken).get
        handleCall(payPalHandler.fail(session.sessionData, token), (url: Uri) => {
          setSession(session) {
            redirect(url, StatusCodes.TemporaryRedirect)
          }
        })
      }
    }*/
  }

  lazy val success = path("success" / Segment) { xtoken =>
    complete(StatusCodes.OK)
/*
    get {
      parameters("token") { token =>
        val session = SessionESDirectives.load(xtoken).get
        handleCall(payPalHandler.success(session.sessionData, token), (url: Uri) => {
          setSession(session) {
            redirect(url, StatusCodes.TemporaryRedirect)
          }
        })
      }
    }*/
  }
}
