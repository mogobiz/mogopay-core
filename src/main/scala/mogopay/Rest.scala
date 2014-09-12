package mogopay

import mogopay.services.MogopayRoutes
import mogopay.actors.{BootedMogopaySystem, MogopayActors}
import akka.io.IO
import spray.can.Http

object Rest extends App with BootedMogopaySystem with MogopayActors with MogopayRoutes {
  mogopay.jobs.ImportCountriesJob.start(system)
  mogopay.jobs.CleanAccountsJob.start(system)
  mogopay.jobs.CleanTransactionRequestsJob.start(system)

  IO(Http)(system) ! Http.Bind(routesServices, "0.0.0.0", port = 8080)
}