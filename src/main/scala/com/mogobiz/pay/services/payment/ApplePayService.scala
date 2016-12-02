/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.services.payment

import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers.handlers._
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
      startPayment
    }
  }

  lazy val startPayment = path("start" / Segment) { xtoken =>
    complete(StatusCodes.OK)
    /*
    get {
      parameterMap { params =>
        val session = SessionESDirectives.load(xtoken).get
        handleCall(applePayHandler.startPayment(session.sessionData), (data: Either[String, Uri]) => {
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
}
