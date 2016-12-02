/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.services.payment

import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.implicits.Implicits
import com.mogobiz.pay.model._
import com.mogobiz.session.SessionESDirectives
import com.mogobiz.session.SessionESDirectives._
import com.typesafe.scalalogging.StrictLogging
import spray.http.HttpHeaders.`Content-Type`
import spray.http._
import spray.routing.Directives

import scala.util._

class SystempayService extends Directives with DefaultComplete with StrictLogging {

  val route = {
    pathPrefix("systempay") {
      startPayment ~
      done ~
      callback ~
      threeDSCallback
    }
  }

  lazy val startPayment = path("start" / Segment) { xtoken =>
    complete(StatusCodes.OK)
/*
    import Implicits._

    get {
      parameterMap { params =>
        val session = SessionESDirectives.load(xtoken).get
        handleCall(systempayHandler.startPayment(session.sessionData), (data: Either[String, Uri]) =>
              setSession(session) {
            data match {
              case Left(content) =>
                complete(HttpResponse(entity = content).withHeaders(List(`Content-Type`(MediaTypes.`text/html`))))
              case Right(url) =>
                redirect(url, StatusCodes.TemporaryRedirect)
            }
        })
      }
    }*/
  }

  lazy val done = path("done" / Segment) { xtoken =>
    complete(StatusCodes.OK)
/*
    import Implicits._
    get {
      logger.debug("done:" + xtoken)
      parameterMap { params =>
        val session = SessionESDirectives.load(xtoken).get
        handleCall(systempayHandler.done(session.sessionData, params), (data: Uri) =>
              setSession(session) {
            redirect(data, StatusCodes.TemporaryRedirect)
        })
      }
    }*/
  }

  lazy val callback = path("callback" / Segment) { xtoken =>
    complete(StatusCodes.OK)
/*
    get {
      parameterMap { params =>
        import Implicits._
        val session = SessionESDirectives.load(xtoken).get
        handleCall(systempayHandler.callbackPayment(session.sessionData, params),
                   (pr: PaymentResult) => complete(StatusCodes.OK, pr))
      }
    }*/
  }

  lazy val threeDSCallback = path("3ds-callback" / Segment) { xtoken =>
    complete(StatusCodes.OK)
/*
    post {
      entity(as[FormData]) { formData =>
        import Implicits.MogopaySession

        val session = SessionESDirectives.load(xtoken).get

        import Implicits._

        handleCall(systempayHandler.threeDSCallback(session.sessionData, formData.fields.toMap),
                   (u: Uri) => redirect(u, StatusCodes.TemporaryRedirect))
      }
    }*/
  }
}
