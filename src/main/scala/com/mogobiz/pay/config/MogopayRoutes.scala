package com.mogobiz.pay.config

import java.io.File
import java.net.UnknownHostException

import akka.actor.{Actor, ActorLogging, Props}
import com.mogobiz.auth.services._
import com.mogobiz.pay.boot.DBInitializer
import com.mogobiz.pay.exceptions.Exceptions.MogopayException
import com.mogobiz.pay.implicits.Implicits
import com.mogobiz.pay.services._
import com.mogobiz.pay.services.payment._
import com.mogobiz.pay.settings.Settings
import com.mogobiz.system.MogobizSystem
import spray.http.StatusCodes._
import spray.http.{HttpEntity, StatusCode, _}
import spray.routing.{Directives, _}
import spray.util.LoggingContext
import com.mogobiz.system.RoutedHttpService

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

trait MogopayRoutes extends Directives {
  this: MogopayActors with MogobizSystem =>

  private implicit val _ = system.dispatcher

  private val adminIndex = new File(new File(new File(s"${Settings.ResourcesPath}", "admin"), "html"), "index.html")

  def bootstrap() = DBInitializer(true)

  def routes =
    logRequestResponse(showRequest _) {
      pathPrefix("static" / "admin") {
        pathEndOrSingleSlash {
          compressResponse() {
            redirect(s"${Settings.Mogopay.BaseEndPoint}/static/admin/html/index.html", StatusCodes.PermanentRedirect)
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
            new UserService(userActor).route ~
            new PdfService(pdfActor).route
        }
    }

  def routesServices = system.actorOf(Props(new RoutedHttpService(routes)))
}

trait DefaultComplete {
  this: Directives =>
  def handleComplete[T](call: Try[Try[T]], handler: T => Route): Route = {
    import Implicits._
    call match {
      case Failure(t) => t.printStackTrace(); complete(StatusCodes.InternalServerError -> Map('type -> t.getClass.getSimpleName, 'error -> t.toString))
      case Success(res) =>
        res match {
          case Success(id) => handler(id)
          case Failure(t: MogopayException) => t.printStackTrace(); complete(t.code -> Map('type -> t.getClass.getSimpleName, 'error -> t.toString))
          case Failure(t: UnknownHostException) => t.printStackTrace(); complete(StatusCodes.NotFound -> Map('type -> t.getClass.getSimpleName, 'error -> t.toString))
          case Failure(t) => t.printStackTrace(); complete(StatusCodes.InternalServerError -> Map('type -> t.getClass.getSimpleName, 'error -> t.toString))
        }
    }
  }
}
