package com.mogobiz.pay.services.payment

import akka.actor.ActorRef
import com.mogobiz.pay.actors.PaylineActor._
import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.implicits.Implicits
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.pay.services.Util._
import com.mogobiz.session.SessionESDirectives
import com.mogobiz.session.SessionESDirectives._
import spray.http.HttpHeaders.`Content-Type`
import spray.http._
import spray.routing.Directives

import scala.concurrent.ExecutionContext
import scala.util._

class PaylineService(actor: ActorRef)(implicit executionContext: ExecutionContext) extends Directives with DefaultComplete {

  import akka.pattern.ask
  import akka.util.Timeout

  import scala.concurrent.duration._

  implicit val timeout = Timeout(10.seconds)

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
        println("start:" + session.sessionData.uuid)
        val message = StartPayment(session.sessionData)
        onComplete((actor ? message).mapTo[Try[Either[String, Uri]]]) { call =>
          handleComplete(call,
            (data: Either[String, Uri]) =>
              setSession(session) {
                data match {
                  case Left(content) =>
                    complete(HttpResponse(entity = content).withHeaders(List(`Content-Type`(MediaTypes.`text/html`))))
                  case Right(url) =>
                    println(url)
                    redirect(url, StatusCodes.TemporaryRedirect)
                }
              }
          )
        }
      }
    }
  }

  lazy val done = path("done" / Segment) { xtoken =>
    import Implicits._
    get {
      parameterMap { params =>
        val session = SessionESDirectives.load(xtoken).get
        println("done:" + session.sessionData.uuid)
        val message = Done(session.sessionData, params)
        onComplete((actor ? message).mapTo[Try[Uri]]) { call =>
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


  lazy val callback = path("callback" / Segment) { xtoken =>
    import Implicits._
    get {
      parameterMap { params =>
        val session = SessionESDirectives.load(xtoken).get
        val message = CallbackPayment(session.sessionData, params)
        onComplete((actor ? message).mapTo[Try[PaymentResult]]) { call =>
          handleComplete(call, (pr: PaymentResult) => complete(StatusCodes.OK, pr))
        }
      }
    }
  }

  lazy val threeDSCallback = path("3ds-callback" / Segment) { xtoken =>
    post {
      entity(as[FormData]) { formData =>
        import Implicits._
        val session = SessionESDirectives.load(xtoken).get
        val message = ThreeDSCallback(session.sessionData, formData.fields.toMap)
        onComplete((actor ? message).mapTo[Try[Uri]]) {call =>
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