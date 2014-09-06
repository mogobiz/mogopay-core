package mogopay.services

import akka.actor.ActorRef
import mogopay.actors.SystempayActor._
import mogopay.model.Mogopay._
import mogopay.services.Util._
import mogopay.session.{SessionESDirectives}
import mogopay.session.SessionESDirectives._
import spray.http.HttpHeaders.`Content-Type`
import spray.http._
import spray.routing.Directives

import scala.concurrent.ExecutionContext
import scala.util._

class SystempayService(actor: ActorRef)(implicit executionContext: ExecutionContext) extends Directives {

  import akka.pattern.ask
  import akka.util.Timeout

  import scala.concurrent.duration._

  implicit val timeout = Timeout(10.seconds)

  val route = {
    pathPrefix("systempay") {
      startPayment ~
        done ~
        callback ~
        threeDSCallback
    }
  }

  lazy val startPayment = path("start-payment" / Segment) { xtoken =>
    import mogopay.config.Implicits._
    get {
      parameterMap { params =>
        val session = SessionESDirectives.load(xtoken).get
          println("start-payment:" + session.sessionData.uuid)
          val message = StartPayment(session.sessionData)
          onComplete((actor ? message).mapTo[Try[Either[String, Uri]]]) {
            case Failure(t) => complete(StatusCodes.InternalServerError)
            case Success(r) =>
              r match {
                case Failure(t) =>
                  println(t)
                  complete(toHTTPResponse(t), Map('error -> t.toString))
                case Success(data) =>
                  setSession(session) {
                    data match {
                      case Left(content) =>
                        println(content)
                        complete(HttpResponse(entity = content).withHeaders(List(`Content-Type`(MediaTypes.`text/html`))))
                      case Right(url) =>
                        println(url)
                        redirect(url, StatusCodes.TemporaryRedirect)
                    }
                  }
              }
        }
      }
    }
  }

  lazy val done = path("done") {
    import mogopay.config.Implicits._
    get {
      session { session =>
        println("done:" + session.sessionData.uuid)
        parameterMap { params =>
          val message = Done(session.sessionData, params)
          onComplete((actor ? message).mapTo[Try[Uri]]) {
            case Failure(t) => complete(StatusCodes.InternalServerError)
            case Success(r) =>
              setSession(session) {
                r match {
                  case Failure(t) => complete(toHTTPResponse(t), Map('error -> t.toString))
                  case Success(x) => redirect(x, StatusCodes.TemporaryRedirect)
                }
              }
          }
        }
      }
    }
  }


  lazy val callback = path("callback") {
    import mogopay.config.Implicits._
    get {
      parameterMap {
        params =>
          val message = HandleResponse(params)
          onComplete((actor ? message).mapTo[Try[PaymentResult]]) {
            case Failure(t) => complete(StatusCodes.InternalServerError)
            case Success(r) =>
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
        import mogopay.config.Implicits.MogopaySession
        val session = SessionESDirectives.load(xtoken).get
        val message = ThreeDSCallback(session.sessionData, formData.fields.toMap)
        onComplete((actor ? message).mapTo[Try[Uri]]) {
          case Failure(t) => complete(StatusCodes.InternalServerError)
          case Success(data) =>
            setSession(session) {
              import mogopay.config.Implicits._
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
