package com.mogobiz.pay.services.payment

import akka.actor.ActorRef
import com.mogobiz.pay.actors.SystempayActor._
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

class SystempayService(actor: ActorRef)(implicit executionContext: ExecutionContext) extends Directives with DefaultComplete {

  import akka.pattern.ask
  import akka.util.Timeout

  import scala.concurrent.duration._

  implicit val timeout = Timeout(40.seconds)

  val route = {
    pathPrefix("systempay") {
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

  lazy val done = path("done") {
    import Implicits._
    get {
      session { session =>
        println("done:" + session.sessionData.uuid)
        parameterMap { params =>
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
  }


  lazy val callback = path("callback") {
    get {
      parameterMap {
        params =>
          val message = CallbackPayment(params)
          onComplete((actor ? message).mapTo[Try[PaymentResult]]) {
            case Failure(t) => complete(StatusCodes.InternalServerError)
            case Success(r) =>
              import Implicits._
              r match {
                case Success(pr) => complete(StatusCodes.OK, pr)
                case Failure(t) => complete(toHTTPResponse(t), Map('error -> t.toString))
              }
          }
      }
    }
  }

  lazy val threeDSCallback = path("3ds-callback" / Segment) { xtoken =>
    post {
      entity(as[FormData]) { formData =>
        import Implicits.MogopaySession
        val session = SessionESDirectives.load(xtoken).get
        val message = ThreeDSCallback(session.sessionData, formData.fields.toMap)
        onComplete((actor ? message).mapTo[Try[Uri]]) {
          case Failure(t) => complete(StatusCodes.InternalServerError)
          case Success(data) =>
            import Implicits._
            setSession(session) {
              data match {
                case Success(u) => redirect(u, StatusCodes.TemporaryRedirect)
                case Failure(t) => complete(toHTTPResponse(t), Map('error -> t.toString))
              }
            }
        }
      }
    }
  }
}
