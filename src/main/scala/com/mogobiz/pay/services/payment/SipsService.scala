/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.services.payment

import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.implicits.Implicits
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.session.SessionESDirectives
import com.mogobiz.session.SessionESDirectives._
import spray.http.HttpHeaders.`Content-Type`
import spray.http._
import spray.routing.Directives

import scala.util._

class SipsService extends Directives with DefaultComplete {

  val route = {
    pathPrefix("sips") {
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
        println("start:" + session.sessionData.uuid)
        handleCall(sipsHandler.startPayment(session.sessionData),
          (data: Either[String, Uri]) =>
            setSession(session) {
              data match {
                case Left(content) =>
                  complete(HttpResponse(entity = content).withHeaders(List(`Content-Type`(MediaTypes.`text/html`))))
                case Right(url) =>
                  redirect(url, StatusCodes.TemporaryRedirect)
              }
            }
        )
      }
    }
  }

  lazy val done = path("done" / Segment) { xtoken =>
    post {
      entity(as[FormData]) { formData =>
        import Implicits._
        val session = SessionESDirectives.load(xtoken).get
        handleCall(sipsHandler.done(session.sessionData, formData.fields.toMap),
          (data: Uri) =>
            setSession(session) {
              redirect(data, StatusCodes.TemporaryRedirect)
            }
        )
      }
    } ~
      get {
        import Implicits._
        parameterMap { params =>
          val session = SessionESDirectives.load(xtoken).get
          handleCall(sipsHandler.done(session.sessionData, params),
            (data: Uri) =>
              setSession(session) {
                redirect(data, StatusCodes.TemporaryRedirect)
              }
          )
        }
      }
  }

  lazy val callback = path("callback" / Segment / Segment) { (vendorUuid, xtoken) =>
    post {
      entity(as[FormData]) { formData =>
        import Implicits._
        val session = SessionESDirectives.load(xtoken).get
        handleCall(sipsHandler.callbackPayment(session.sessionData, formData.fields.toMap, vendorUuid),
          (pr: PaymentResult) => complete(StatusCodes.OK, pr))
      }
    }
  }

  lazy val threeDSCallback = path("3ds-callback" / Segment) { xtoken =>
    post {
      entity(as[FormData]) { formData =>
        val session = SessionESDirectives.load(xtoken).get
        import Implicits._
        handleCall(sipsHandler.threeDSCallback(session.sessionData, formData.fields.toMap),
          (data: Uri) =>
            setSession(session) {
              redirect(data, StatusCodes.TemporaryRedirect)
            }
        )
      }
    }
  }
}
