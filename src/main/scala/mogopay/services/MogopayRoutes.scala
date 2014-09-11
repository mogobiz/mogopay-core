package mogopay.services

import mogoauth.services._
import mogopay.model.Mogopay.SessionData
import mogopay.services.payment._
import spray.routing._
import akka.actor.{ActorLogging, Actor, Props}
import mogopay.actors.{MogopaySystem, MogopayActors}
import scala.util.control.NonFatal
import spray.http.StatusCodes._
import spray.http.{HttpEntity, StatusCode}
import spray.util.LoggingContext
import spray.routing.Directives
import mogopay.session.SessionESDirectives._

trait MogopayRoutes extends Directives {
  this: MogopayActors with MogopaySystem =>

  private implicit val _ = system.dispatcher

  val routes = pathPrefix("pay") {
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
