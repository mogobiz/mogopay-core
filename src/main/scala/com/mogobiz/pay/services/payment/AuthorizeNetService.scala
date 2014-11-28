package com.mogobiz.pay.services.payment

import com.mogobiz.pay.actors.AuthorizeNetActor._
import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.implicits.Implicits
import com.mogobiz.session.SessionESDirectives
import com.mogobiz.session.SessionESDirectives._
import spray.http.HttpHeaders.`Content-Type`
import spray.http._
import spray.routing._

import scala.concurrent.ExecutionContext
import scala.util._

class AuthorizeNetService(implicit executionContext: ExecutionContext) extends Directives with DefaultComplete {

  import akka.pattern.ask
  import akka.util.Timeout

  import scala.concurrent.duration._

  implicit val timeout = Timeout(40.seconds)

  val route = {
    pathPrefix("authorizenet") {
      startPayment ~
      done ~
//      callbackPayment ~
//      callback3DSecureCheck ~
//      done3DSecureCheck ~
      relay
    }
  }

  lazy val startPayment = path("start" / Segment) { xtoken =>
    import Implicits._
    get {
      parameterMap { params =>
        val session = SessionESDirectives.load(xtoken).get
        handleCall(authorizeNetHandler.startPayment(session.sessionData),
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

  def queryString: Directive1[String] = extract(_.request.uri.toString())

  lazy val done = path("done") {
    import Implicits._
    get {
      session { session =>
        parameterMap { params =>
          queryString { uri =>
            handleCall(authorizeNetHandler.donePayment(session.sessionData, params, uri),
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

  lazy val relay = path("relay") {
    post {
      entity(as[FormData]) { formData =>
        handleCall(authorizeNetHandler.relay(formData.fields.toMap),
          (_: Any) => complete(StatusCodes.OK)
        )
      }
    }
  }
}