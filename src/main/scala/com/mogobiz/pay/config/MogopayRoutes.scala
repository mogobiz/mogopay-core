/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.config

import java.io.File
import java.net.UnknownHostException

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import com.mogobiz.auth.services._
import com.mogobiz.pay.boot.DBInitializer
import com.mogobiz.pay.exceptions.Exceptions.{MogopayException, MogopayMessagelessException}
import com.mogobiz.pay.services._
import com.mogobiz.pay.services.payment._
import com.mogobiz.system.MogobizSystem

import scala.util.{Failure, Success, Try}

trait MogopayRoutes extends Directives {
  this: MogobizSystem =>

  private implicit val _ = system.dispatcher

  private val adminIndex = new File(new File(new File(s"${Settings.ResourcesPath}", "admin"), "html"), "index.html")

  def bootstrap() = {
    com.mogobiz.session.boot.DBInitializer()
    com.mogobiz.notify.boot.DBInitializer()
    com.mogobiz.pay.jobs.ImportRatesJob.start(system)
    com.mogobiz.pay.jobs.ImportCountriesJob.start(system)
    com.mogobiz.pay.jobs.CleanAccountsJob.start(system)
    DBInitializer(Settings.Demo)
  }

  def routes =
    logRequestResult("com.mogobiz.pay.config.RestAll") {
      path("static" / "admin") {
        //compressResponse() {
        redirect(s"${Settings.Mogopay.BaseEndPoint}/static/admin/html/index.html", StatusCodes.PermanentRedirect)
        //}
      } ~
        pathPrefix("static") {
          //compressResponse() {
          if (Settings.IsResourcesLocal) {
            getFromResourceDirectory("static")
          } else if (Settings.isResourcesPathAbsolute) {
            getFromBrowseableDirectory(Settings.ResourcesPath)
          } else {
            getFromResourceDirectory(Settings.ResourcesPath)
          }
          //}
        } ~
        pathPrefix(("api" / "pay") | "pay") {
          new AccountService().route ~
            new AccountServiceJsonless().route ~
            new AuthorizeNetService().route ~
            new BackofficeService().route ~
            new CountryService().route ~
            new RateService().route ~
            new TransactionService().route ~
            new TwitterService().route ~
            new LinkedInService().route ~
            new GoogleService().route ~
            new FacebookService().route ~
            new GithubService().route ~
            new SystempayService().route ~
            new PayPalService().route ~
            new ApplePayService().route ~
            new PayboxService().route ~
            new PaylineService().route ~
            new MogopayService().route ~
            new SipsService().route ~
            new UserService().route ~
            new PdfService().route
        }
    }
}

trait DefaultComplete {
  this: Directives =>
  def handleCall[T](call: => T, handler: T => Route): Route = {
    Try(call) match {
      case Success(res) => handler(res)
      case Failure(t) => completeException(t)
    }
  }

  def completeException(t: Throwable): Route = {
    t match {
      case (ex: MogopayException) =>
        if (ex.printTrace) ex.printStackTrace()
        complete(ex.code -> Map('type -> ex.getClass.getSimpleName, 'error -> ex.getMessage))
      case (ex: MogopayMessagelessException) =>
        if (ex.printTrace) ex.printStackTrace()
        complete(ex.code -> Map('type -> ex.getClass.getSimpleName, 'error -> ex.getMessage))
      case (ex: UnknownHostException) =>
        ex.printStackTrace()
        complete(StatusCodes.NotFound -> Map('type -> ex.getClass.getSimpleName, 'error -> ex.getMessage))
      case (_) =>
        t.printStackTrace()
        complete(StatusCodes.InternalServerError -> Map('type -> t.getClass.getSimpleName, 'error -> t.getMessage))
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
