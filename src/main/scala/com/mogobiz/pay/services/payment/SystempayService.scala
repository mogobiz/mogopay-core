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

class SystempayService extends Directives with DefaultComplete {

  val route = {
    pathPrefix("systempay") {
      startPayment ~
        done ~
        callback ~
        threeDSCallback
    }
  }

  lazy val startPayment = path("start" / Segment) {
    xtoken =>

      import Implicits._

      get {
        parameterMap {
          params =>
            val session = SessionESDirectives.load(xtoken).get
            handleCall(systempayHandler.startPayment(session.sessionData),
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

  lazy val done = path("done") {

    import Implicits._

    get {
      session {
        session =>
          println("done:" + session.sessionData.uuid)
          parameterMap {
            params =>
              handleCall(systempayHandler.done(session.sessionData, params),
                (data: Uri) =>
                  setSession(session) {
                    redirect(data, StatusCodes.TemporaryRedirect)
                  }
              )
          }
      }
    }
  }


  lazy val callback = path("callback") {
    get {
      parameterMap {
        params =>

          import Implicits._

          handleCall(systempayHandler.callbackPayment(params),
            (pr: PaymentResult) => complete(StatusCodes.OK, pr))
      }
    }
  }

  lazy val threeDSCallback = path("3ds-callback" / Segment) {
    xtoken =>
      post {
        entity(as[FormData]) {
          formData =>

            import Implicits.MogopaySession

            val session = SessionESDirectives.load(xtoken).get

            import Implicits._

            handleCall(systempayHandler.threeDSCallback(session.sessionData, formData.fields.toMap),
              (u: Uri) => redirect(u, StatusCodes.TemporaryRedirect))
        }
      }
  }
}
