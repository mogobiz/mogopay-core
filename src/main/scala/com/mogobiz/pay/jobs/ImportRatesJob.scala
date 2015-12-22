/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.jobs

import akka.actor.{ Actor, ActorSystem, Props }
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.config.Settings
import scala.concurrent.duration._
import scala.util.control.NonFatal

object ImportRatesJob {
  def start(system: ActorSystem) {
    import system.dispatcher

    if (Settings.Jobs.Interval.ImportRates > 0) {
      system.scheduler.schedule(
        initialDelay = Settings.Jobs.Delay.ImportRates seconds,
        interval = Settings.Jobs.Interval.ImportRates seconds,
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