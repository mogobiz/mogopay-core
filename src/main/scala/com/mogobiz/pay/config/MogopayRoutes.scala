package com.mogobiz.pay.config

import java.io.File
import java.net.UnknownHostException

import akka.actor.{Actor, ActorLogging, Props}
import com.gettyimages.spray.swagger.SwaggerHttpService
import com.mogobiz.auth.services._
import com.mogobiz.pay.boot.DBInitializer
import com.mogobiz.pay.exceptions.Exceptions.MogopayException
import com.mogobiz.pay.implicits.Implicits
import com.mogobiz.pay.services._
import com.mogobiz.pay.services.payment._
import com.mogobiz.pay.settings.Settings
import com.mogobiz.system.MogobizSystem
import com.wordnik.swagger.model.ApiInfo
import spray.http.StatusCodes._
import spray.http.{HttpEntity, StatusCode, _}
import spray.routing.{Directives, _}
import spray.util.LoggingContext
import com.mogobiz.system.RoutedHttpService

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

trait MogopayRoutes extends Directives {
  this: MogobizSystem =>

  private implicit val _ = system.dispatcher

  private val adminIndex = new File(new File(new File(s"${Settings.ResourcesPath}", "admin"), "html"), "index.html")

  def bootstrap() = DBInitializer(true)

  def routes =
    logRequestResponse(showRequest _) {
      pathPrefix("static" / "admin") {
        pathEndOrSingleSlash {
          //compressResponse() {
          redirect(s"${Settings.Mogopay.BaseEndPoint}/static/admin/html/index.html", StatusCodes.PermanentRedirect)
          //}
        }
      } ~
        pathPrefix("static") {
          //compressResponse() {
          if (Settings.IsResourcesLocal) {
            getFromResourceDirectory("static")
          }
          else {
            getFromBrowseableDirectory(Settings.ResourcesPath)
          }
          //}
        } ~
        pathPrefix(("api" / "pay") | "pay") {
          new AccountService().route ~
            new AccountServiceJsonless().route ~
            new BackofficeService().route ~
            new CountryService().route ~
            new RateService().route ~
            new TransactionService().route ~
            new SampleService().route ~
            new TwitterService().route ~
            new LinkedInService().route ~
            new GoogleService().route ~
            new FacebookService().route ~
            new GithubService().route ~
            new SystempayService().route ~
            new PayPalService().route ~
            new PayboxService().route ~
            new PaylineService().route ~
            new MogopayService().route ~
            new SipsService().route ~
            new UserService().route ~
            new PdfService().route
        }
    }

  def routesServices = system.actorOf(Props(new RoutedHttpService(routes)))

}

trait DefaultComplete {
  this: Directives =>
  def handleCall[T](call: => T, handler: T => Route): Route = {
    Try(call) match {
      case Success(res) => handler(res)
      case Failure(t) => completeException(t)
    }
  }

  def completeException(t: Throwable) : Route = {
    import Implicits._
    t match {
      case (ex: MogopayException) =>
        ex.printStackTrace()
        complete(ex.code -> Map('type -> ex.getClass.getSimpleName, 'error -> ex.toString))
      case (ex: UnknownHostException) =>
        ex.printStackTrace()
        complete(StatusCodes.NotFound -> Map('type -> ex.getClass.getSimpleName, 'error -> ex.toString))
      case (_) =>
        t.printStackTrace()
        complete(StatusCodes.InternalServerError -> Map('type -> t.getClass.getSimpleName, 'error -> t.toString))
    }
  }

  def handleComplete[T](call: Try[Try[T]], handler: T => Route): Route = {
    call match {
      case Failure(t) => completeException(t)
      case Success(res) =>
        res match {
          case Success(id) => handler(id)
          case Failure(t) => completeException(t)
        }
    }
  }
}
