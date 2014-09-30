package mogopay.services.payment

import akka.actor.ActorRef
import mogopay.actors.PayboxActor.{CallbackPayment, Done3DSecureCheck, _}
import mogopay.services.DefaultComplete
import mogopay.session.SessionESDirectives
import mogopay.session.SessionESDirectives._
import spray.http.HttpHeaders.`Content-Type`
import spray.http._
import spray.routing._

import scala.concurrent.ExecutionContext
import scala.util._

class PayboxService(actor: ActorRef)(implicit executionContext: ExecutionContext) extends Directives with DefaultComplete {

  import akka.pattern.ask
  import akka.util.Timeout

import scala.concurrent.duration._

  implicit val timeout = Timeout(10.seconds)

  val route = {
    pathPrefix("paybox") {
      startPayment ~
        done ~
        callbackPayment ~
        callback3DSecureCheck ~
        done3DSecureCheck
    }
  }

  lazy val startPayment = path("start" / Segment) { xtoken =>
    import mogopay.config.Implicits._
    get {
      parameterMap { params =>
        val session = SessionESDirectives.load(xtoken).get
        println("start:" + xtoken)
        onComplete((actor ? StartPayment(session.sessionData)).mapTo[Try[Either[String, Uri]]]) { call =>
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


  def queryString: Directive1[String] = extract(_.request.uri.toString())

  lazy val done = path("done") {
    import mogopay.config.Implicits._
    get {
      session { session =>
        parameterMap { params =>
          queryString { uri =>
            onComplete((actor ? Done(session.sessionData, params, uri)).mapTo[Try[Uri]]) { call =>
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
  }

  lazy val callbackPayment = path("callback" / Segment) {
    xtoken =>

      import mogopay.config.Implicits.MogopaySession

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

  lazy val done3DSecureCheck = path("done-3ds") {
    post {
      entity(as[FormData]) {
        formData =>
          session {
            session =>

              import mogopay.config.Implicits._

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

  lazy val callback3DSecureCheck = path("callback-3ds" / Segment) {
    xtoken =>

      import mogopay.config.Implicits.MogopaySession

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
}
