package com.mogobiz.pay

import akka.io.IO
import com.mogobiz.pay.actors.BootedMogopaySystem
import com.mogobiz.pay.config.{MogopayActors, MogopayRoutes}
import com.mogobiz.pay.settings.Settings
import spray.can.Http

/**
 * Created by hayssams on 04/11/14.
 */
object Rest extends App with BootedMogopaySystem with MogopayActors with MogopayRoutes {
  com.mogobiz.pay.jobs.ImportCountriesJob.start(system)
  com.mogobiz.pay.jobs.CleanAccountsJob.start(system)
  com.mogobiz.pay.jobs.CleanTransactionRequestsJob.start(system)

  IO(Http)(system) ! Http.Bind(routesServices, Settings.ServerListen, port = Settings.ServerPort)
}
