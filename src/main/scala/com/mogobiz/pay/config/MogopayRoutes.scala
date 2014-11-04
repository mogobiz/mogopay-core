package com.mogobiz.pay.config

import java.io.File
import java.net.UnknownHostException

import akka.actor.{Actor, ActorLogging, Props}
import akka.event.Logging._
import com.mogobiz.auth.services._
import com.mogobiz.pay.actors.MogopaySystem
import com.mogobiz.pay.exceptions.Exceptions.MogopayException
import com.mogobiz.pay.implicits.Implicits
import com.mogobiz.pay.services._
import com.mogobiz.pay.services.payment._
import com.mogobiz.pay.settings.Settings
import spray.http.StatusCodes._
import spray.http.{HttpEntity, StatusCode, _}
import spray.routing.directives.LogEntry
import spray.routing.{Directives, _}
import spray.util.LoggingContext

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

trait MogopayRoutes extends Directives {
  this: MogopayActors with MogopaySystem =>

  private implicit val _ = system.dispatcher

  def showRequest(request: HttpRequest): HttpResponsePart ⇒ Option[LogEntry] = {
    case HttpResponse(s, _, _, _) ⇒ Some(LogEntry(s"${s.intValue}: ${request.uri}", InfoLevel))
    case ChunkedResponseStart(HttpResponse(OK, _, _, _)) ⇒ Some(LogEntry(" 200 (chunked): ${request.uri}", InfoLevel))
    case _ ⇒ None
  }

  private val adminIndex = new File(new File(new File(s"${Settings.ResourcesPath}", "admin"), "html"), "index.html")

  val routes =
    logRequestResponse(showRequest _) {
      pathPrefix("static" / "admin") {
        pathEndOrSingleSlash {
          compressResponse() {
            redirect(s"${Settings.ApplicationEndPoint}/static/admin/html/index.html", StatusCodes.PermanentRedirect)
          }
        }
      } ~
        pathPrefix("static") {
          compressResponse() {
            if (Settings.IsResourcesLocal) {
              getFromResourceDirectory("static")
            }
            else {
              getFromBrowseableDirectory(Settings.ResourcesPath)
            }
          }
        } ~
        pathPrefix("pay") {
          new AccountService(accountActor).route ~
            new AccountServiceJsonless(accountActor).route ~
            new BackofficeService(backofficeActor).route ~
            new CountryService(countryActor).route ~
            new RateService(rateActor).route ~
            new TransactionService(transactionActor).route ~
            new SampleService().route ~
            new TwitterService().route ~
            new LinkedInService().route ~
            new GoogleService().route ~
            new FacebookService().route ~
            new GithubService().route ~
            new SystempayService(systempayActor).route ~
            new PayPalService(payPalActor).route ~
            new PayboxService(payboxActor).route ~
            new PaylineService(paylineActor).route ~
            new MogopayService(mogopayActor).route ~
            new SipsService(sipsActor).route ~
            new UserService(userActor).route
        }
    }

  val routesServices = system.actorOf(Props(new RoutedHttpService(routes)))
}

/**
 * @param responseStatus
 * @param response
 */
case class ErrorResponseException(responseStatus: StatusCode, response: Option[HttpEntity]) extends Exception

/**
 * Allows you to construct Spray ``HttpService`` from a concatenation of routes; and wires in the error handler.
 * It also logs all internal server errors using ``SprayActorLogging``.
 *
 * @param route the (concatenated) route
 */
class RoutedHttpService(route: Route) extends Actor with HttpService with ActorLogging {

  implicit def actorRefFactory = context

  implicit val handler = ExceptionHandler {
    case NonFatal(ErrorResponseException(statusCode, entity)) => ctx =>
      ctx.complete(statusCode, entity)

    case NonFatal(e) => ctx => {
      log.error(e, InternalServerError.defaultMessage)
      ctx.complete(InternalServerError)
    }
  }

  def receive: Receive =
    runRoute(route)(handler, RejectionHandler.Default, context, RoutingSettings.default, LoggingContext.fromActorRefFactory)
}

trait DefaultComplete {
  this: Directives =>
  def handleComplete[T](call: Try[Try[T]], handler: T => Route): Route = {
    import Implicits._
    call match {
      case Failure(t) => t.printStackTrace(); complete(StatusCodes.InternalServerError -> Map('error -> t.toString))
      case Success(res) =>
        res match {
          case Success(id) => handler(id)
          case Failure(t: MogopayException) => t.printStackTrace(); complete(t.code -> Map('type -> t.getClass.getSimpleName, 'error -> t.toString))
          case Failure(t: UnknownHostException) => t.printStackTrace(); complete(StatusCodes.NotFound -> Map('error -> t.toString))
          case Failure(t) => t.printStackTrace(); complete(StatusCodes.InternalServerError -> Map('error -> t.toString))
        }
    }
  }
}
