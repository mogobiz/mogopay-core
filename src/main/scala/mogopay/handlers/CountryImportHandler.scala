package mogopay.handlers

import java.io.File
import com.sksamuel.elastic4s.ElasticDsl._
import mogopay.config._
import mogopay.config.HandlersConfig._
import mogopay.es.EsClient
import mogopay.model.Mogopay._
import org.elasticsearch.index.query.TermQueryBuilder

class CountryImportHandler {
  private def findCountryAdmin(code: String, level: Int): Option[CountryAdmin] = {
    val req = search in Settings.DB.INDEX -> "CountryAdmin" filter {
      and(
        termFilter("code", code),
        termFilter("level", level)
      )
    }
    EsClient.search[CountryAdmin](req)
  }

  def importCountries(countriesFile: File, currenciesFile: File) {
    assert(countriesFile.exists(), s"${countriesFile.getAbsolutePath} does not exist.")
    assert(currenciesFile.exists(), s"${currenciesFile.getAbsolutePath} does not exist.")

    val req = search in Settings.DB.INDEX -> "Country" aggs {
      aggregation max "agg" field "lastUpdated"
    }
    EsClient.search[Country](req) map (_.lastUpdated.getTime) orElse Some(countriesFile.lastModified) map { lastUpdated =>
      if (lastUpdated >= countriesFile.lastModified) {
        EsClient.client.client
          .prepareDeleteByQuery(Settings.DB.INDEX)
          .setQuery(new TermQueryBuilder("_type", "Country"))
          .execute
          .actionGet

        val currencyMap: Map[String, String] =
          scala.io.Source.fromFile(currenciesFile, "utf-16").getLines().map {
            case line if line.trim().length() > 0 =>
              val field = line.trim.split('\t')
              Some((field(0).trim, field(1).trim))
            case _ => None
          }.collect { case Some(x) => x}.toMap[String, String]

        scala.io.Source.fromFile(countriesFile, "utf-8").getLines().foreach { line =>
          val field = line.split('\t')
          val code: String = field(0)
          val name: String = field(4)
          val postalCodeRegex: String = field(14)
          val phoneCode: String = field(12)
          val currencyCode: String = field(10)
          val currencyName: String = field(11)

          if (Settings.ImportCountries.codes.contains(code) || Settings.ImportCountries.codes.isEmpty) {
            val country = countryHandler.findByCode(code)
            if (country.isEmpty) {
              val newCountry = Country(uuid = java.util.UUID.randomUUID().toString,
                code = code,
                name = name,
                shipping = true,
                billing = true,
                zipCodeRegex = Option(postalCodeRegex),
                currencyCode = Option(currencyCode),
                currencyName = Option(currencyName),
                currencyNumericCode = Option(currencyMap(currencyCode)),
                phoneCode = Option(phoneCode))
              EsClient.index(newCountry, true)
            }
          }
        }
      }
    }
  }

  def importAdmins1(admins1File: File) {
    assert(admins1File.exists(), s"${admins1File.getAbsolutePath} does not exist.")

    val list: scala.collection.mutable.MutableList[String] = scala.collection.mutable.MutableList()

    scala.io.Source.fromFile(admins1File, "utf-8").getLines().foreach { line =>
      val field = line.split('\t')
      val code = field(0)
      val name = field(2)
      val countryCode = code.split("\\.")(0)

      if (Settings.ImportCountries.codes.contains(countryCode) || Settings.ImportCountries.codes.isEmpty) {
        val localAdmin1 = new File(s"${admins1File.getParent}/${countryCode.toLowerCase}/${admins1File.getName}")
        if (!list.contains(countryCode)) {
          if (localAdmin1.exists()) {
            list += countryCode
            try {
              val cls = this.getClass.getClassLoader.loadClass(countryCode + ".Import")
              cls.getMethod("importAdmin1", classOf[File]).invoke(cls.newInstance(), localAdmin1)
            } catch {
              case _: Throwable =>
            }
          } else {
            val countryAdmin = findCountryAdmin(code, 1)
            if (countryAdmin.isEmpty) {
              val country = countryHandler.findByCode(countryCode)
              country map { country =>
                val newCountryAdmin = CountryAdmin(java.util.UUID.randomUUID().toString,
                  Option(code),
                  Option(name),
                  1,
                  CountryRef(country.uuid, country.code),
                  None,
                  None)
                EsClient.index(newCountryAdmin, true)
              }
            }
          }
        }
      }
    }
  }

  def importAdmins2(admins2File: File) {
    assert(admins2File.exists(), s"${admins2File.getAbsolutePath} does not exist.")

    val list: scala.collection.mutable.MutableList[String] = scala.collection.mutable.MutableList()

    scala.io.Source.fromFile(admins2File, "utf-8").getLines().foreach { line =>
      val field = line.split('\t')
      val code: String = field(0)
      val name: String = field(2)
      val countryCode: String = code.split("\\.")(0)

      if (Settings.ImportCountries.codes.contains(countryCode) || Settings.ImportCountries.codes.isEmpty) {
        val localAdmin2 = new File(s"${admins2File.getParent}/${countryCode.toLowerCase}/${admins2File.getName}")
        if (!list.contains(countryCode)) {
          if (localAdmin2.exists()) {
            list += countryCode
            try {
              val cls = this.getClass.getClassLoader.loadClass(countryCode + ".Import")
              cls.getMethod("importAdmin2", classOf[File]).invoke(cls.newInstance(), localAdmin2)
            } catch {
              case _: Throwable =>
            }
          } else {
            val countryAdmin2 = findCountryAdmin(code, 2)
            if (countryAdmin2.isEmpty) {
              val admin1Code = countryCode + "." + code.split("\\.")(1)
              val countryAdmin1 = findCountryAdmin(admin1Code, 1)
              countryAdmin1 map { countryAdmin1 =>
                var newName = name.replace("Departement ", "")
                if (newName.startsWith("de ") || newName.startsWith("du "))
                  newName = newName.substring(3)
                if (newName.startsWith("des "))
                  newName = newName.substring(4)
                if (newName.startsWith("la ") || newName.startsWith("le "))
                  newName = newName.substring(3)
                if (newName.startsWith("l'") || newName.startsWith("d'"))
                  newName = newName.substring(2)

                val countryAdmin2ToIndex = CountryAdmin(uuid = java.util.UUID.randomUUID().toString,
                  code = Option(code),
                  name = Option(name),
                  level = 2,
                  country = countryAdmin1.country,
                  parentCountryAdmin1 = Some(CountryAdminRef(countryAdmin1.uuid, countryAdmin1.code.getOrElse(""))),
                  parentCountryAdmin2 = None)
                EsClient.index(countryAdmin2ToIndex, true)
              }
            }
          }
        }
      }
    }
  }


  def importCities(citiesFile: File) {
    assert(citiesFile.exists(), s"${citiesFile.getAbsolutePath} does not exist.")

    val list: scala.collection.mutable.MutableList[String] = scala.collection.mutable.MutableList()

    scala.io.Source.fromFile(citiesFile, "utf-8").getLines().foreach { line =>
      val field = line.split('\t')

      val cityCode = field(2)
      val countryCode = field(8)
      val a1code = if (field(10).length() == 1) "0" + field(10) else field(10)
      val a2code = if (field(11).length() == 1) "0" + field(11) else field(11)

      if (Settings.ImportCountries.codes.contains(countryCode) || Settings.ImportCountries.codes.isEmpty) {
        val localCities = new File(s"${citiesFile.getParent}/${countryCode.toLowerCase}/${citiesFile.getName}")
        if (!list.contains(countryCode)) {
          if (localCities.exists()) {
            list += countryCode
            try {
              val cls = this.getClass.getClassLoader.loadClass(countryCode + ".Import")
              cls.getMethod("importCities", classOf[File]).invoke(cls.newInstance(), localCities)
            } catch {
              case _: Throwable =>
            }
          } else {
            val cityFullCode = s"$countryCode.$a1code.$a2code.$cityCode"
            val findCityReq = search in Settings.DB.INDEX -> "CountryAdmin" filter {
              and(
                termFilter("code" -> cityFullCode),
                termFilter("level" -> 3)
              )
            }
            val city = EsClient.search[CountryAdmin](findCityReq)

            if (city.isEmpty) {
              val admin2Code = s"$countryCode.$a1code.$a2code"
              val findAdmin2Req = search in Settings.DB.INDEX -> "CountryAdmin" filter {
                and(
                  termFilter("code" -> admin2Code),
                  termFilter("level" -> 2)
                )
              }

              EsClient.search[CountryAdmin](findAdmin2Req) map { admin2: CountryAdmin =>
                val city = CountryAdmin(java.util.UUID.randomUUID().toString,
                  Option(cityFullCode),
                  Option(cityCode),
                  3,
                  admin2.country,
                  admin2.parentCountryAdmin1,
                  Some(CountryAdminRef(admin2.uuid, admin2.code.getOrElse(""))))
                EsClient.index(city, false)
              }
            }
          }
        }
      }
    }
  }
}

object Main extends App {
  println("Start...\n")
  EsClient.client.client.prepareDeleteByQuery(Settings.DB.INDEX).setQuery(new TermQueryBuilder("_type", "Country")).execute.actionGet
  EsClient.client.client.prepareDeleteByQuery(Settings.DB.INDEX).setQuery(new TermQueryBuilder("_type", "CountryAdmin")).execute.actionGet
  EsClient.client.client.prepareDeleteByQuery(Settings.DB.INDEX).setQuery(new TermQueryBuilder("_type", "Account")).execute.actionGet
  EsClient.client.client.prepareDeleteByQuery(Settings.DB.INDEX).setQuery(new TermQueryBuilder("_type", "BOTransaction")).execute.actionGet

  countryImportHandler.importCountries(Settings.Import.countriesFile, Settings.Import.currenciesFile)
  countryImportHandler.importAdmins1(Settings.Import.admins1File)
  countryImportHandler.importAdmins2(Settings.Import.admins2File)
  countryImportHandler.importCities(Settings.Import.citiesFile)
}
