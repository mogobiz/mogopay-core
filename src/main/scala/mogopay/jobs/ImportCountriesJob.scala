package mogopay.jobs

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import mogopay.config.HandlersConfig._
import mogopay.config.{Environment, Settings}

object ImportCountriesJob {
  def start(system: ActorSystem) {
    val scheduler = QuartzSchedulerExtension(system)
    val job = system.actorOf(Props[ImportCountriesJob])
    scheduler.schedule("ImportCountries", job, "")
  }
}

/**
 * Periodically import countries into the database
 * In prodution mode, imported files are renamed to the current date/time
 */
class ImportCountriesJob extends Actor {
  def receive = {
    case _ =>
      val currencies = Settings.Import.currenciesFile
      val countries = Settings.Import.countriesFile
      val admins1 = Settings.Import.admins1File
      val admins2 = Settings.Import.admins2File
      val cities = Settings.Import.citiesFile

      countryImportHandler.importCountries(countries, currencies)
      countryImportHandler.importAdmins1(admins1)
      countryImportHandler.importAdmins2(admins2)
      countryImportHandler.importCities(cities)

      cities.renameTo(new File(cities.getAbsolutePath + "." + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())))

      if (Settings.environment == Environment.PROD) {
        val now = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
        countries.renameTo(new File(countries.getAbsolutePath + "." + now))
        admins1.renameTo(new File(admins1.getAbsolutePath + "." + now))
        admins2.renameTo(new File(admins2.getAbsolutePath + "." + now))
      }
  }
}