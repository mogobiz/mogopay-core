/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.services.payment

import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.implicits.Implicits
import com.mogobiz.session.SessionESDirectives
import com.mogobiz.session.SessionESDirectives._
import spray.http.HttpHeaders.`Content-Type`
import spray.http._
import spray.routing._

import scala.concurrent.ExecutionContext
import scala.util._

class AuthorizeNetService(implicit executionContext: ExecutionContext) extends Directives with DefaultComplete {

  import akka.util.Timeout

  import scala.concurrent.duration._

  implicit val timeout = Timeout(40.seconds)

  val route = {
    pathPrefix("authorizenet") {
      startPayment ~
      cancel ~
      done ~
      relay
      //      callbackPayment ~
      //      callback3DSecureCheck ~
      //      done3DSecureCheck ~
      //      cancel
    }
  }

  lazy val startPayment = path("start" / Segment) { xtoken =>
    complete(StatusCodes.OK)
/*
    import Implicits._
    get {
      parameterMap { params =>
        val session = SessionESDirectives.load(xtoken).get
        handleCall(authorizeNetHandler.startPayment(session.sessionData), (data: Either[String, Uri]) =>
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

  def queryString: Directive1[String] = extract(_.request.uri.toString())

  /*
  lazy val callbackPayment = path("callback" / Segment) {
    xtoken =>

      import Implicits.MogopaySession

      get {
        parameterMap { params =>
          queryString { uri =>
            val session = SessionESDirectives.load(xtoken).get
            onComplete((actor ? CallbackPayment(session.sessionData, params, uri)).mapTo[Try[Unit]]) { call =>
              handleComplete(call, (_: Unit) => complete(StatusCodes.OK))
            }
          }
        }
      }
  }
   */

  /*
  lazy val done3DSecureCheck = path("done-3ds") {
    post {
      entity(as[FormData]) {
        formData =>
          session {
            session =>

              import Implicits._

              onComplete((actor ? Done3DSecureCheck(session.sessionData, formData.fields.toMap)).mapTo[Try[Uri]]) { call =>
                handleComplete(call,
                  (data: Uri) =>
                    setSession(session) {
                      redirect(data, StatusCodes.TemporaryRedirect)
                    }
                )
              }
          }
      }
    }
  }
   */

  /*
  lazy val callback3DSecureCheck = path("callback-3ds" / Segment) {
    xtoken =>

      import Implicits.MogopaySession

      get {
        parameterMap {
          params =>
            val session = SessionESDirectives.load(xtoken).get
            onComplete((actor ? Callback3DSecureCheck(session.sessionData, params)).mapTo[Try[Unit]]) { call =>
              handleComplete(call, (_: Unit) => complete(StatusCodes.OK))
            }
        }
      }
  }
   */

  lazy val relay = path("relay" / Segment) { xtoken =>
    complete(StatusCodes.OK)
/*
    post {
      entity(as[FormData]) { formData =>
        import Implicits._
        val session = SessionESDirectives.load(xtoken).get
        handleCall(authorizeNetHandler.relay(session.sessionData, formData.fields.toMap), (form: String) =>
              respondWithMediaType(MediaTypes.`text/html`) {
            complete {
              new HttpResponse(StatusCodes.OK, HttpEntity(form))
            }
        })
      }
    }*/
  }

  lazy val cancel = path("cancel" / Segment) { xtoken =>
    complete(StatusCodes.OK)
/*
    import Implicits._
    get {
      val session = SessionESDirectives.load(xtoken).get
      parameterMap { params =>
        handleCall(authorizeNetHandler.cancel(session.sessionData),
                   (uri: Uri) => redirect(uri, StatusCodes.TemporaryRedirect))
      }
    }*/
  }

  lazy val done = path("done" / Segment) { xtoken =>
    complete(StatusCodes.OK)
/*
    import Implicits._
    get {
      parameterMap { params =>
        val session = SessionESDirectives.load(xtoken).get
        handleCall(authorizeNetHandler.done(session.sessionData, params),
                   (uri: Uri) => redirect(uri, StatusCodes.TemporaryRedirect))
      }
    }*/
  }
}
