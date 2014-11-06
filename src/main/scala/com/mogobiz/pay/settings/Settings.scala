package com.mogobiz.pay.settings

import java.io.File

import com.typesafe.config.ConfigFactory

object Settings {
  private val config = ConfigFactory.load("mogopay")

  val Env = if (System.getenv.containsKey("PRODUCTION")) {
    Environment.PROD
  } else {
    Environment.DEV
  }

  val ResourcesPath = config.getString("resources.path")
  val IsResourcesLocal = ResourcesPath.isEmpty

  val AccountValidateMerchantPhone = config.getBoolean("account.validate.merchantphone")
  val AccountValidateMerchantEmail = config.getBoolean("account.validate.merchantemail")
  val AccountValidateCustomerPhone = config.getBoolean("account.validate.customerphone")
  val AccountValidateCustomerEmail = config.getBoolean("account.validate.customeremail")
  val AccountValidateMerchantEmails = config.getString("account.validate.merchant.emails")
  val AccountValidateMerchantDefault = config.getString("account.validate.merchant.default")
  val AccountValidateCustomershared = config.getBoolean("account.validate.customershared")
  val AccountValidatePasswordMaxattempts = config.getInt("account.validate.password.maxattempts")
  val AccountRecycleDuration = config.getInt("account.recycle.duration")
  val TransactionRequestDuration = config.getInt("transaction.request.duration")
  val TransactionDuration = config.getInt("transaction.duration")
  val SelectForUpdate = config.getString("db.select.forupdate")
  val MaxQueryResults = config.getInt("maxQueryResults")
  val sharedCustomers = false
  val EmailTemplatesDir = "emailtemplates/"
  val SecretKeysDir = "secretkeys/"
  val EmailSenderAddress = "noreply@mogobiz.com"
  val EmailSenderName = "noreply@mogobiz.com"
  val ImagesPath = "/static/images/"

  object ImportCountries {
    val codes = config.getStringList("import.countries.codes")
  }

  object Clickatell {
    val user = ""
    val password = ""
    val apiId = ""
    val sender = ""
    val sessionIdUrl = "http://api.clickatell.com/http/auth?api_id=%s&user=%s&password=%s".format(apiId, user, password)
    val sendFlashUrlPattern = "http://api.clickatell.com/http/sendmsg?session_id=%s&req_feat=544&to=%S&from=%s&msg_type=SMS_FLASH&text=%s"
  }

  object Mail {

    object Smtp {
      val Host = config.getString("mail.smtp.host")
      val Port = config.getInt("mail.smtp.port")
      val Username = config.getString("mail.smtp.username")
      val Password = config.getString("mail.smtp.password")
      val IsSSLEnabled = config.getBoolean("mail.smtp.ssl")
      val IsSSLCheckServerIdentity = config.getBoolean("mail.smtp.checkserveridentity")
      val IsStartTLSEnabled = config.getBoolean("mail.smtp.starttls")
    }

    val MaxAge = config.getInt("mail.confirmation.maxAge")
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
    private def path(fileName: String) = s"""${config.getString("import.countries.dir")}$fileName.txt"""

    lazy val currenciesFile = new File(path("currencies"))
    lazy val countriesFile = new File(path("countries"))
    lazy val admins1File = new File(path("admins1"))
    lazy val admins2File = new File(path("admins2"))
    lazy val citiesFile = new File(path("cities"))
  }

  object Transaction {
    val MaxInactiveInterval = 300
  }

  object Payline {
    val PaymentAction = config.getString("payline.paymentAction")
    val PaymentMode = config.getString("payline.paymentMode")
    val LanguageCode = config.getString("payline.languageCode")
    val SecurityMode = config.getString("payline.securityMode")
    val Version = config.getString("payline.version")
    val DirectEndPoint = config.getString("payline.directendpoint")
    val WebEndPoint = config.getString("payline.webendpoint")
  }

  object PayPal {
    val UrlExpresschout = config.getString("paypal.urlExpresschout")
    val UrlNvpApi = config.getString("paypal.urlNvpApi")
    val Version = config.getString("paypal.version")
  }

  object Paybox {
    val MPIEndPoint = config.getString("paybox.mpiendpoint")
    val SystemEndPoint = config.getString("paybox.systemendpoint")
    val DirectEndPoint = config.getString("paybox.directendpoint")
    val PemFileName = config.getString("paybox.pemfile")
    val PBXPorteur = config.getString("paybox.pbxporteur")
    val publicKey: String = {
      scala.io.Source.fromFile(Settings.Paybox.PemFileName).mkString
    }

  }

  object Systempay {
    val Version = config.getString("systempay.version")
    val Url = config.getString("systempay.url")
  }

  object Sips {
    val CertifDir = config.getString("sips.certif.dir")
    val PathFile = config.getString("sips.pathfile")
  }

  object Jobs {

    object Interval {
      val cleanTransactionRequests = config.getInt("jobs.cron.transactionrequest")
      val cleanAccounts = config.getInt("jobs.cron.recycleaccount")
      val importCountries = config.getInt("jobs.cron.importcountries")
    }

    object Delay {
      val cleanTransactionRequests = config.getInt("jobs.delay.transactionrequest")
      val cleanAccounts = config.getInt("jobs.delay.recycleaccount")
      val importCountries = config.getInt("jobs.delay.importcountries")
    }

  }
  object Mogopay {
    val EsIndex = config.getString("mogopay.esindex")
    val Secret = config getString "mogopay.secret"
    val Protocol = config getString "mogopay.protocol"
    val Host = config getString "mogopay.host"
    val Port = config getInt "mogopay.port"
    val BaseEndPoint = s"$Protocol://$Host:$Port"
    val EndPoint = s"${BaseEndPoint}/pay/"
  }

  require(Mogopay.Secret.nonEmpty, "mogopay.secret must be non-empty")
  require(ImagesPath.endsWith("/"), "applicationUIURL must end with a '/'.")
  require(Mogopay.EndPoint.endsWith("/"), "applicationAPIURL must end with a '/'.")
}

object Environment extends Enumeration {
  type Environment = Value
  val DEV = Value(1)
  val PROD = Value(2)
}
