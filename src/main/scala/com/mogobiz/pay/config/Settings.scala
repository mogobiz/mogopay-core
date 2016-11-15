/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.config

import java.io.{File, StringReader}
import java.security.{KeyFactory, PublicKey}
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec

import com.mogobiz.utils.EmailHandler.MailConfig
import com.typesafe.config.{Config, ConfigFactory}
import org.bouncycastle.util.io.pem.PemReader
import scalikejdbc.config._

import scala.util.Try

object Settings {
  private val config = ConfigFactory.load("mogopay").withFallback(ConfigFactory.load("default-mogopay"))

  val Env = Environment.withName(if (config hasPath "dialect") config getString "dialect" else "DEV")

  val Demo = config.getBoolean("demo")

  val ResourcesPath           = config.getString("resources.path")
  val TemplateImagesUrl       = config.getString("templates.imagesUrl")
  val TemplatesPath           = config.getString("templates.path")
  val IsResourcesLocal        = ResourcesPath.isEmpty
  val isResourcesPathAbsolute = ResourcesPath.contains('/')

  val CakeClass = config.getString("mogopay.handlers.class")

  val AccountValidateMerchantPhone       = config.getBoolean("account.validate.merchantphone")
  val AccountValidateMerchantEmail       = config.getBoolean("account.validate.merchantemail")
  val AccountValidateCustomerPhone       = config.getBoolean("account.validate.customerphone")
  val AccountValidateCustomerEmail       = config.getBoolean("account.validate.customeremail")
  val AccountValidateMerchantEmails      = config.getString("account.validate.merchant.emails")
  val AccountValidateMerchantDefault     = config.getString("account.validate.merchant.default")
  val AccountValidatePasswordMaxattempts = config.getInt("account.validate.password.maxattempts")
  val AccountRecycleDuration             = config.getInt("account.recycle.duration")
  val TransactionRequestDuration         = config.getInt("transaction.request.duration")
  val TransactionDuration                = config.getInt("transaction.duration")
  val MaxQueryResults                    = config.getInt("maxQueryResults")
  val sharedCustomers                    = config.getBoolean("account.validate.customershared")
  val EmailTemplatesDir                  = "emailtemplates/"
  val SecretKeysDir                      = "secretkeys/"
  val EmailSenderAddress                 = "noreply@mogobiz.com"
  val EmailSenderName                    = "noreply@mogobiz.com"
  val ImagesPath                         = "/static/images/"

  object ImportCountries {
    val codes = config.getStringList("import.countries.codes")
  }

  object Clickatell {
    val user     = ""
    val password = ""
    val apiId    = ""
    val sender   = ""
    val sessionIdUrl =
      "http://api.clickatell.com/http/auth?api_id=%s&user=%s&password=%s".format(apiId, user, password)
    val sendFlashUrlPattern =
      "http://api.clickatell.com/http/sendmsg?session_id=%s&req_feat=544&to=%S&from=%s&msg_type=SMS_FLASH&text=%s"
  }

  object Mail {

    object Smtp {
      val Host                     = config.getString("mail.smtp.host")
      val Port                     = config.getInt("mail.smtp.port")
      val SslPort                  = config.getInt("mail.smtp.sslport")
      val Username                 = config.getString("mail.smtp.username")
      val Password                 = config.getString("mail.smtp.password")
      val IsSSLEnabled             = config.getBoolean("mail.smtp.ssl")
      val IsSSLCheckServerIdentity = config.getBoolean("mail.smtp.checkserveridentity")
      val IsStartTLSEnabled        = config.getBoolean("mail.smtp.starttls")
      implicit val MailSettings = MailConfig(
          host = Host,
          port = Port,
          sslPort = SslPort,
          username = Username,
          password = Password,
          sslEnabled = IsSSLEnabled,
          sslCheckServerIdentity = IsSSLCheckServerIdentity,
          startTLSEnabled = IsStartTLSEnabled
      )
    }

    val MaxAge = config.getInt("mail.confirmation.maxage")
  }

  object RSA {
    def privateKey = getKey("private")

    def publicKey = getKey("public")

    import java.io.{FileInputStream, InputStream}

    private def getKey(whichOne: String): InputStream = {
      if (Env == Environment.PROD) {
        new FileInputStream(whichOne + ".key")
      } else {
        getClass.getClassLoader.getResource("secretkeys/" + whichOne + ".key").openStream()
      }
    }
  }

  object Import {
    val rootDir = new File(config.getString("import.countries.dir"))

    private def path(fileName: String) = new File(rootDir, fileName)

    val RatesFile      = path("rates.txt")
    val CurrenciesFile = path("currencies.txt")
    val CountriesFile  = path("countries.txt")
    val Admins1File    = path("admins1.txt")
    val Admins2File    = path("admins2.txt")
    val CitiesFile     = path("cities.txt")
  }

  object Transaction {
    val MaxInactiveInterval = 300
  }

  object Payline {
    val LanguageCode   = config.getString("payline.languageCode")
    val SecurityMode   = config.getString("payline.securityMode")
    val Version        = config.getString("payline.version")
    val DirectEndPoint = config.getString("payline.directendpoint")
    val WebEndPoint    = config.getString("payline.webendpoint")
  }

  object PayPal {
    val UrlExpresschout = config.getString("paypal.urlExpresschout")
    val UrlNvpApi       = config.getString("paypal.urlNvpApi")
    val Version         = config.getString("paypal.version")
  }

  object Paybox {
    val MPIEndPoint    = config.getString("paybox.mpiendpoint")
    val SystemEndPoint = config.getString("paybox.systemendpoint")
    val DirectEndPoint = config.getString("paybox.directendpoint")
    val PemFileName    = config.getString("paybox.pemfile")
    val PBXPorteur     = config.getString("paybox.pbxporteur")
    val PublicKey: PublicKey = {
      val pemreader          = new PemReader(new StringReader(scala.io.Source.fromFile(Settings.Paybox.PemFileName).mkString))
      val x509EncodedKeySpec = new X509EncodedKeySpec(pemreader.readPemObject().getContent)
      val kf                 = KeyFactory.getInstance("RSA")
      kf.generatePublic(x509EncodedKeySpec)
    }
  }

  object Systempay {
    val Version = config.getString("systempay.version")
    val Url     = config.getString("systempay.url")
  }

  object Sips {
    val CertifDir = config.getString("sips.certif.dir")
    val PathFile  = config.getString("sips.pathfile")
  }

  object Shipping {

    object Kiala {
      val enable = config.getBoolean("shipping.kiala.enable")
    }

    object EasyPost {
      val ApiKey = config.getString("shipping.easyPost.apiKey")
      val UpsCostCenter =
        if (config.hasPath("shipping.easyPost.ups.costCenter")) config.getString("shipping.easyPost.ups.costCenter")
        else ""
    }

  }

  object ApplePay {

    import net.authorize.{Environment => ANetEnv}

    val token = Try(Option(config.getString(s"applepay.$Env.token"))).getOrElse(None)
    val env   = if (Env == Environment.DEV) ANetEnv.SANDBOX else ANetEnv.PRODUCTION
  }

  object AuthorizeNet {
    val formAction = config.getString(s"authorizenet.$Env.formaction")
  }

  object Jobs {

    object Interval {
      val CleanAccounts   = config.getInt("jobs.cron.recycleaccount")
      val ImportCountries = config.getInt("jobs.cron.importcountries")
      val ImportRates     = config.getInt("jobs.cron.importrates")
      val Refund          = config.getInt("jobs.cron.refund")
    }

    object Delay {
      val CleanAccounts   = config.getInt("jobs.delay.recycleaccount")
      val ImportCountries = config.getInt("jobs.delay.importcountries")
      val ImportRates     = config.getInt("jobs.delay.importrates")
      val Refund          = config.getInt("jobs.delay.refund")
    }

  }

  object Mogopay {
    val EsIndex        = config.getString("mogopay.esindex")
    val Secret         = config getString "mogopay.secret"
    val CardSecret     = config getString "mogopay.card.secret"
    val Protocol       = config getString "mogopay.protocol"
    val Host           = config getString "mogopay.host"
    val Port           = config getInt "mogopay.port"
    val Anonymous      = config getBoolean "mogopay.anonymous"
    val BaseEndPoint   = s"$Protocol://$Host:$Port"
    val EndPointNoPort = s"$Protocol://$Host/api/pay/" // For authorize (does not support port in url)
    val EndPoint       = s"${BaseEndPoint}/api/pay/"
    val isHTTPS        = Protocol.equalsIgnoreCase("https")

    require(Secret.nonEmpty, "mogopay.secret must be non-empty")
    require(EndPoint.endsWith("/"), "applicationAPIURL must end with a '/'.")
  }

  val GoogleAPIKey = config.getString("mogopay.googleApi.key")
  //"AIzaSyCwKQwcdpCHgs5t-tXxaAlPshpu4HsYKbU"
  val EnableGeoLocation = config.getBoolean("mogopay.googleApi.enable")

  val NextVal       = config getString s"$Env.db.default.nextval"
  val DerbySequence = config getString s"$Env.db.default.sequence"
  MogopayDBsWithEnv(Env.toString).setupAll()

  require(ImagesPath.endsWith("/"), "applicationUIURL must end with a '/'.")
}

trait MogopayTypesafeConfig extends TypesafeConfig {
  lazy val config: Config = ConfigFactory.load("mogopay").withFallback(ConfigFactory.load("default-mogopay"))
}

case class MogopayDBsWithEnv(envValue: String)
    extends DBs
    with TypesafeConfigReader
    with MogopayTypesafeConfig
    with EnvPrefix {
  override val env = Option(envValue)
}

object Environment extends Enumeration {
  type Environment = Value
  val DEV  = Value(1)
  val PROD = Value(2)
}
