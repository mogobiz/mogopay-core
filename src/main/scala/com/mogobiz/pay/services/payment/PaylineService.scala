/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.services.payment

import akka.actor.ActorRef
import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.implicits.Implicits
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.session.SessionESDirectives
import com.mogobiz.session.SessionESDirectives._
import com.typesafe.scalalogging.StrictLogging
import spray.http.HttpHeaders.`Content-Type`
import spray.http._
import spray.routing.Directives

import scala.concurrent.ExecutionContext
import scala.util._

class PaylineService extends Directives with DefaultComplete with StrictLogging {

  val route = {
    pathPrefix("payline") {
      startPayment ~
        done ~
        callback ~
        threeDSCallback
    }
  }

  lazy val startPayment = path("start" / Segment) { xtoken =>
    import Implicits._
    get {
      parameterMap { params =>
        val session = SessionESDirectives.load(xtoken).get
        handleCall(paylineHandler.startPayment(session.sessionData),
          (data: Either[String, Uri]) =>
            setSession(session) {
              data match {
                case Left(content) =>
                  complete(HttpResponse(entity = content).withHeaders(List(`Content-Type`(MediaTypes.`text/html`))))
                case Right(url) =>
                  logger.debug(url.toString())
                  redirect(url, StatusCodes.TemporaryRedirect)
              }
            }
        )
      }
    }
  }

  lazy val done = path("done" / Segment) { xtoken =>
    import Implicits._
    get {
      parameterMap { params =>
        val session = SessionESDirectives.load(xtoken).get
        handleCall(paylineHandler.done(session.sessionData, params),
          (data: Uri) =>
            setSession(session) {
              redirect(data, StatusCodes.TemporaryRedirect)
            }
        )
      }
    }
  }

  lazy val callback = path("callback" / Segment) { xtoken =>
    import Implicits._
    get {
      parameterMap { params =>
        val session = SessionESDirectives.load(xtoken).get
        handleCall(paylineHandler.callbackPayment(session.sessionData, params),
          (pr: PaymentResult) => complete(StatusCodes.OK, pr))
      }
    }
  }

  lazy val threeDSCallback = path("3ds-callback" / Segment) { xtoken =>
    post {
      entity(as[FormData]) { formData =>
        import Implicits._
        val session = SessionESDirectives.load(xtoken).get
        handleCall(paylineHandler.threeDSCallback(session.sessionData, formData.fields.toMap),
          (data: Uri) =>
            setSession(session) {
              redirect(data, StatusCodes.TemporaryRedirect)
            }
        )
      }
    }
  }
}
