/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.services.payment

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.implicits.Implicits._
import com.mogobiz.session.SessionESDirectives
import com.mogobiz.session.SessionESDirectives._
import com.typesafe.scalalogging.StrictLogging

import scala.util._

class MogopayService
    extends Directives
    with DefaultComplete
    with StrictLogging {

  val route = {
    pathPrefix("mogopay") {
      authenticate ~
        startPayment
    }
  }

  lazy val authenticate = path("authenticate" / Segment) { xtoken =>
    complete(StatusCodes.OK)
  /*
    get {
      val session = SessionESDirectives.load(xtoken).get
      handleCall(mogopayHandler.authenticate(session.sessionData), (data: Either[String, Uri]) =>
            setSession(session) {
          data match {
            case Left(content) =>
              complete(HttpResponse(entity = content).withHeaders(List(`Content-Type`(MediaTypes.`text/html`))))
            case Right(url) =>
              logger.debug(url.toString())
              redirect(url, StatusCodes.TemporaryRedirect)
          }
      })
    }*/
  }

  lazy val startPayment = path("start" / Segment) { xtoken =>
    complete(StatusCodes.OK)
  /*
    get {
      val session = SessionESDirectives.load(xtoken).get
      handleCall(mogopayHandler.startPayment(session.sessionData), (data: Either[String, Uri]) =>
            setSession(session) {
          data match {
            case Left(content) =>
              logger.debug(content)
              complete(HttpResponse(entity = content).withHeaders(List(`Content-Type`(MediaTypes.`text/html`))))
            case Right(url) =>
              redirect(url, StatusCodes.TemporaryRedirect)
          }
      })
    }*/
  }
}
