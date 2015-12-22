/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.jobs

import akka.actor.{ Actor, ActorSystem, Props }
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.config.Settings
import scala.concurrent.duration._
import scala.util.control.NonFatal

object ImportCountriesJob {
  def start(system: ActorSystem) {
    import system.dispatcher

    if (Settings.Jobs.Interval.ImportCountries > 0) {
      system.scheduler.schedule(
        initialDelay = Settings.Jobs.Delay.ImportCountries seconds,
        interval = Settings.Jobs.Interval.ImportCountries seconds,
        receiver = system.actorOf(Props[ImportCountriesJob]),
        message = "")
    }
  }
}

/**
 * Periodically import countries into the database
 */
class ImportCountriesJob extends Actor {
  def receive = {
    case _ => try {
      println(" == ImportCountriesJob: start.")

      val currencies = Settings.Import.CurrenciesFile
      val countries = Settings.Import.CountriesFile
      val admins1 = Settings.Import.Admins1File
      val admins2 = Settings.Import.Admins2File
      val cities = Settings.Import.CitiesFile

      if (countryImportHandler.importCountries(countries, currencies)) {
        countryImportHandler.importAdmins1(admins1)
        countryImportHandler.importAdmins2(admins2)
        countryImportHandler.importCities(cities)
        println(" == ImportCountriesJob: done.")
      } else {
        println(" == ImportCountriesJob: skipped.")
      }
    } catch {
      case NonFatal(e) =>
        e.printStackTrace()
        println(" == ImportCountriesJob: files missing, skipping…")
    }
  }
}

object RunImportCountriesJob extends App {
  ImportCountriesJob.start(ActorSystem())
}