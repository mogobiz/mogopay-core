package com.mogobiz.pay.jobs

import akka.actor.{Actor, ActorSystem, Props}
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.settings.Settings
import scala.concurrent.duration._
import scala.util.control.NonFatal

object ImportCountriesJob {
  def start(system: ActorSystem) {
    import system.dispatcher

    if (Settings.Jobs.Interval.importCountries > 0) {
      system.scheduler.schedule(
        initialDelay = Settings.Jobs.Delay.importCountries seconds,
        interval     = Settings.Jobs.Interval.importCountries seconds,
        receiver     = system.actorOf(Props[ImportCountriesJob]),
        message      = "")
    }
  }
}

/**
 * Periodically import countries into the database
 * In prodution mode, imported files are renamed to the current date/time
 */
class ImportCountriesJob extends Actor {
  def receive = {
    case _ => try {
      println(" == ImportCountriesJob: start.")

      val currencies = Settings.Import.currenciesFile
      val countries = Settings.Import.countriesFile
      val admins1 = Settings.Import.admins1File
      val admins2 = Settings.Import.admins2File
      val cities = Settings.Import.citiesFile

      countryImportHandler.importCountries(countries, currencies)
      countryImportHandler.importAdmins1(admins1)
      countryImportHandler.importAdmins2(admins2)
      countryImportHandler.importCities(cities)

//      val now = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
//      cities.renameTo(new File(cities.getAbsolutePath + "." + now))
//      countries.renameTo(new File(countries.getAbsolutePath + "." + now))
//      admins1.renameTo(new File(admins1.getAbsolutePath + "." + now))
//      admins2.renameTo(new File(admins2.getAbsolutePath + "." + now))
//
      println(" == ImportCountriesJob: done.")
    } catch {
      case NonFatal(_) => println(" == ImportCountriesJob: files missing, skipping…")
    }
  }
}

object RunImportCountriesJob extends App {
  ImportCountriesJob.start(ActorSystem())
}