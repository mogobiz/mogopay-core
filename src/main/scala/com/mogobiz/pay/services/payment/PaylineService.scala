/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.services.payment

import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.handlers.payment.FormRedirection
import com.mogobiz.pay.implicits.Implicits
import com.mogobiz.session.SessionESDirectives
import com.mogobiz.session.SessionESDirectives._
import com.typesafe.scalalogging.StrictLogging
import spray.http.HttpHeaders.`Content-Type`
import spray.http._
import spray.routing.Directives

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
        handleCall(paylineHandler.startPayment(session.sessionData), (data: Either[FormRedirection, Uri]) =>
              setSession(session) {
            data match {
              case Left(content) =>
                complete(HttpResponse(entity = content.html).withHeaders(List(`Content-Type`(MediaTypes.`text/html`))))
              case Right(url) =>
                logger.debug(url.toString())
                redirect(url, StatusCodes.TemporaryRedirect)
            }
        })
      }
    }
  }

  lazy val done = path("done" / Segment / Segment) { (xtoken, boShopTransaction) =>
    get {
      parameterMap { params =>
        import Implicits._
        val session = SessionESDirectives.load(xtoken)
        handleCall(paylineHandler.done(boShopTransaction, params), (data: Uri) =>
          session.map { session =>
            setSession(session) {
              redirect(data, StatusCodes.TemporaryRedirect)
            }
          }.getOrElse {
            redirect(data, StatusCodes.TemporaryRedirect)
          }
        )
      }
    }
  }

  lazy val callback = path("callback" / Segment / Segment) { (xtoken, boShopTransaction) =>
    get {
      parameterMap { params =>
        import Implicits._
        val session = SessionESDirectives.load(xtoken)
        handleCall(paylineHandler.callbackPayment(boShopTransaction, params), (pr: Unit) =>
          session.map { session =>
            setSession(session) {
              complete(StatusCodes.OK)
            }
          }.getOrElse {
            complete(StatusCodes.OK)
          }
        )
      }
    }
  }

  lazy val threeDSCallback = path("3ds-callback" / Segment / Segment) { (xtoken, boTransactionUuid) =>
    post {
      entity(as[FormData]) { formData =>
        import Implicits._
        val session = SessionESDirectives.load(xtoken)
        val sessionData = session.map{_.sessionData}
        handleCall(paylineHandler.threeDSCallback(sessionData, boTransactionUuid, formData.fields.toMap), (data: Uri) =>
          session.map { session =>
            setSession(session) {
              redirect(data, StatusCodes.TemporaryRedirect)
            }
          }.getOrElse {
            redirect(data, StatusCodes.TemporaryRedirect)
          }
        )
      }
    }
  }
}
