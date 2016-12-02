/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.services.payment

import akka.actor.ActorRef
import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.implicits.Implicits
import com.mogobiz.session.SessionESDirectives
import com.mogobiz.session.SessionESDirectives._
import com.typesafe.scalalogging.StrictLogging
import spray.http.HttpHeaders.`Content-Type`
import spray.http._
import spray.routing._

import scala.util._

class PayboxService extends Directives with DefaultComplete with StrictLogging {

  val route = {
    pathPrefix("paybox") {
      startPayment ~
      done ~
      callbackPayment ~
      callback3DSecureCheck ~
      done3DSecureCheck
    }
  }

  lazy val startPayment = path("start" / Segment) { (xtoken) =>
    complete(StatusCodes.OK)
    /*
    import Implicits._
    get {
      parameterMap { params =>
        val session = SessionESDirectives.load(xtoken).get
        handleCall(payboxHandler.startPayment(session.sessionData), (data: Either[String, Uri]) =>
              setSession(session) {
            data match {
              case Left(content) =>
                complete(HttpResponse(entity = content).withHeaders(List(`Content-Type`(MediaTypes.`text/html`))))
              case Right(url) =>
                logger.debug(url.toString())
                redirect(url, StatusCodes.TemporaryRedirect)
            }
        })
      }
    }*/
  }

  def queryString: Directive1[String] = extract(_.request.uri.toString())

  lazy val done = path("done" / Segment) { xtoken =>
    complete(StatusCodes.OK)
/*
    import Implicits._
    get {
      parameterMap { params =>
        queryString { uri =>
          val session = SessionESDirectives.load(xtoken).get
          handleCall(payboxHandler.donePayment(session.sessionData, params, uri), (data: Uri) =>
                setSession(session) {
              redirect(data, StatusCodes.TemporaryRedirect)
          })
        }
      }
    }*/
  }

  lazy val callbackPayment = path("callback" / Segment) { xtoken =>
    complete(StatusCodes.OK)
/*
    import Implicits.MogopaySession

    get {
      parameterMap { params =>
        queryString { uri =>
          val session = SessionESDirectives.load(xtoken).get
          handleCall(payboxHandler.callbackPayment(session.sessionData, params, uri),
                     (_: Unit) => complete(StatusCodes.OK))
        }
      }
    }*/
  }

  lazy val done3DSecureCheck = path("done-3ds" / Segment) { xtoken =>
    complete(StatusCodes.OK)
/*
    post {
      entity(as[FormData]) { formData =>
        import Implicits._
        val session = SessionESDirectives.load(xtoken).get
        handleCall(payboxHandler.done3DSecureCheck(session.sessionData, formData.fields.toMap), (data: Uri) =>
              setSession(session) {
            redirect(data, StatusCodes.TemporaryRedirect)
        })
      }
    }*/
  }

  lazy val callback3DSecureCheck = path("callback-3ds" / Segment) { xtoken =>
    complete(StatusCodes.OK)
/*
    import Implicits.MogopaySession
    get {
      parameterMap { params =>
        val session = SessionESDirectives.load(xtoken).get
        handleCall(payboxHandler.callback3DSecureCheck(session.sessionData, params),
                   (_: Unit) => complete(StatusCodes.OK))
      }
    }*/
  }
}
