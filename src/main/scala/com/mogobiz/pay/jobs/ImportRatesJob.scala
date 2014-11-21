package com.mogobiz.pay.jobs

import akka.actor.{Actor, ActorSystem, Props}
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.settings.Settings
import scala.concurrent.duration._
import scala.util.control.NonFatal

object ImportRatesJob {
  def start(system: ActorSystem) {
    import system.dispatcher

    if (Settings.Jobs.Interval.importRates > 0) {
      system.scheduler.schedule(
        initialDelay = Settings.Jobs.Delay.importRates seconds,
        interval = Settings.Jobs.Interval.importRates seconds,
        receiver = system.actorOf(Props[ImportRatesJob]),
        message = "")
    }
  }
}

/**
 * Periodically import countries into the database
 */
class ImportRatesJob extends Actor {
  def receive = {
    case _ => try {
      println(" == ImportRatesJob: start.")
      val rates = Settings.Import.RatesFile
      rateImportHandler.importRates(rates)
      println(" == ImportRatesJob: end.")
    } catch {
      case NonFatal(e) =>
        e.printStackTrace()
        println(" == ImportRatesJob: files missing, skipping…")
    }
  }
}

object RunImportRatesJob extends App {
  ImportRatesJob.start(ActorSystem())
}