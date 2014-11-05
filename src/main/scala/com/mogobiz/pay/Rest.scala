package com.mogobiz.pay

import akka.io.IO
import com.mogobiz.pay.config.{MogopayActors, MogopayRoutes}
import com.mogobiz.pay.settings.Settings
import com.mogobiz.system.BootedMogobizSystem
import spray.can.Http

object Rest extends App with BootedMogobizSystem with MogopayActors with MogopayRoutes {
  com.mogobiz.pay.jobs.ImportCountriesJob.start(system)
  com.mogobiz.pay.jobs.CleanAccountsJob.start(system)
  com.mogobiz.pay.jobs.CleanTransactionRequestsJob.start(system)

  IO(Http)(system) ! Http.Bind(routesServices, interface = Settings.ServerListen, port = Settings.ServerPort)
}
