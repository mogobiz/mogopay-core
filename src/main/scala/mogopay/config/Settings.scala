package mogopay.config

import com.typesafe.config.ConfigFactory
import java.io.File

object Settings {
  private val config = ConfigFactory.load()

  val environment = if (System.getenv.containsKey("MOGOPAY_PROD")) {
    Environment.PROD
  } else {
    Environment.DEV
  }
  val ServerListen = config.getString("spray.interface")
  val ServerPort = config.getInt("spray.port")


  val AccountValidateMerchantPhone = config.getBoolean("account.validate.merchantphone")
  val AccountValidateMerchantEmail = config.getBoolean("account.validate.merchantemail")
  val AccountValidateCustomerPhone = config.getBoolean("account.validate.customerphone")
  val AccountValidateCustomerEmail = config.getBoolean("account.validate.customeremail")
  val AccountValidateMerchantEmails = config.getString("account.validate.merchant.emails")
  val AccountValidateMerchantDefault = config.getString("account.validate.merchant.default")
  val AccountValidateCustomershared = config.getBoolean("account.validate.customershared")
  val AccountValidatePasswordMaxattempts = config.getInt("account.validate.password.maxattempts")
  val RecycleAccountDuration = config.getInt("recycleAccount.duration")
  val TransactionRequestDuration = config.getInt("transactionRequest.duration")
  val TransactionDuration = config.getInt("transaction.duration")
  val SelectForUpdate = config.getString("db.select.forupdate")
  val MaxQueryResults = config.getInt("maxQueryResults")
  val sharedCustomers = false
  val EmailTemplatesDir = "emailtemplates/"
  val SecretKeysDir = "secretkeys/"
  val EmailSenderAddress = "noreply@mogobiz.com"
  val EmailSenderName = "noreply@mogobiz.com"
  val applicationUIURL = "mogopay.ui/"
  val MogopayEndPoint = config getString "mogopay.endpoint"

  val Interface = config getString "spray.interface"
  val Port = config getInt "spray.port"
  val ApplicationSecret = config getString "session.application.secret"
  val SessionFolder = new File(config getString "session.folder")
  val SessionCookieName = config getString "session.cookie.name"
  val SessionMaxAge = config getLong "session.maxage"
  val RememberCookieName = config getString "session.remember.cookie.name"
  val RememberCookieMaxAge = config getLong "session.remember.cookie.maxage"


  val TwitterCallback = config.getString("oauth.twitter.callback")
  val TwitterConsumerKey = config.getString("oauth.twitter.consumer.key")
  val TwitterConsumerSecret = config.getString("oauth.twitter.consumer.secret")
  val TwitterScope = config.getString("oauth.twitter.scope")
  val TwitterResourceUrl = config.getString("oauth.twitter.resource.url")

  val LinkedInCallback = config.getString("oauth.linkedin.callback")
  val LinkedInConsumerKey = config.getString("oauth.linkedin.consumer.key")
  val LinkedInConsumerSecret = config.getString("oauth.linkedin.consumer.secret")
  val LinkedInScope = config.getString("oauth.linkedin.scope")
  val LinkedInbResourceUrl = config.getString("oauth.linkedin.resource.url")

  val GoogleCallback = config.getString("oauth.google.callback")
  val GoogleConsumerKey = config.getString("oauth.google.consumer.key")
  val GoogleConsumerSecret = config.getString("oauth.google.consumer.secret")
  val GoogleScope = config.getString("oauth.google.scope")
  val GoogleResourceUrl = config.getString("oauth.google.resource.url")

  val FacebookCallback = config.getString("oauth.facebook.callback")
  val FacebookConsumerKey = config.getString("oauth.facebook.consumer.key")
  val FacebookConsumerSecret = config.getString("oauth.facebook.consumer.secret")
  val FacebookScope = config.getString("oauth.facebook.scope")
  val FacebookResourceUrl = config.getString("oauth.facebook.resource.url")

  val GithubCallback = config.getString("oauth.github.callback")
  val GithubConsumerKey = config.getString("oauth.github.consumer.key")
  val GithubConsumerSecret = config.getString("oauth.github.consumer.secret")
  val GithubScope = config.getString("oauth.github.scope")
  val GithubResourceUrl = config.getString("oauth.github.resource.url")

  val YahooCallback = config.getString("oauth.yahoo.callback")
  val YahooConsumerKey = config.getString("oauth.yahoo.consumer.key")
  val YahooConsumerSecret = config.getString("oauth.yahoo.consumer.secret")
  val YahooScope = config.getString("oauth.yahoo.scope")
  val YahooResourceUrl = config.getString("oauth.yahoo.resource.url")

  val GcmApiKey = config.getString("notification.gcm.key")

  val ApnsKeystore = config.getString("notification.apns.keystore.name")
  val ApnsPassword = config.getString("notification.apns.password")
  val ApnsKeystoreType = config.getString("notification.apns.keystore.type")
  val ApnsHost = config.getString("notification.apns.host")
  val ApnsPort = config.getString("notification.apns.port")
  val ApnsTokenSize = config.getInt("notification.apns.token.size")


  val ESDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ"

  object ImportCountries {
    val codes = Seq("FR", "GB", "SN")
  }

  object clickatell {
    val user = ""
    val password = ""
    val apiId = ""
    val sender = ""
    val sessionIdUrl = "http://api.clickatell.com/http/auth?api_id=%s&user=%s&password=%s".format(apiId, user, password)
    val sendFlashUrlPattern = "http://api.clickatell.com/http/sendmsg?session_id=%s&req_feat=544&to=%S&from=%s&msg_type=SMS_FLASH&text=%s"
  }

  object Emailing {

    object SMTP {
      val Hostname = "localhost"
      val Port = 25
      val Username = ""
      val Password = ""
      val IsSSLEnabled = false
    }

    val MaxAge = 24 * 3600
  }

  object RSA {
    def privateKey = getKey("private")

    def publicKey = getKey("public")

    import java.io.InputStream
    import java.io.FileInputStream

    private def getKey(whichOne: String): InputStream = {
      if (environment == Environment.PROD) {
        new FileInputStream(whichOne + ".key")
      } else {
        getClass.getClassLoader.getResource("secretkeys/" + whichOne + ".key").openStream()
      }
    }
  }

  object DB {
    val ES_HOST = "localhost"
    val ES_HTTP_PORT = 9200
    val ES_PORT = 9300
    val INDEX = "mogopay"
    val ES_CLUSTER = "elasticsearch"
    val ES_FULL_URL = ES_HOST + ":" + ES_HTTP_PORT
  }

  object Import {
    private def path(fileName: String) = s"/data/mogopay/import/countries/$fileName.txt"

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
    val Version = config.getString("payline.xversion")
    val DirectEndPoint = config.getString("payline.directendpoint")
    val WebEndPoint = config.getString("payline.webendpoint")
  }

  object PayPal {
    val UrlExpresschout = config.getString("paypal.urlExpresschout")
    val UrlNvpApi = config.getString("paypal.urlNvpApi")
    val Version = config.getString("paypal.xversion")
  }

  object Paybox {
    val MPIEndPoint = config.getString("paybox.mpiendpoint")
    val SystemEndPoint = config.getString("paybox.systemendpoint")
    val DirectEndPoint = config.getString("paybox.directendpoint")
    val PEMFile = config.getString("paybox.pemfile")
    val PBXPorteur = config.getString("paybox.pbxporteur")

  }

  object Systempay {
    val Version = "V2"
    val Url = "https://paiement.systempay.fr/vads-payment/"
  }

  object Sips {
    val CertifDir = config.getString("sips.certif.dir")
    val PathFile = config.getString("sips.pathfile")

  }

  require(ApplicationSecret.nonEmpty, "application.secret must be non-empty")
  require(SessionCookieName.nonEmpty, "session.cookie.name must be non-empty")
  require(RememberCookieName.nonEmpty, "session.remember.cookie.name must be non-empty")
  require(Interface.nonEmpty, "interface must be non-empty")
  require(0 < Port && Port < 65536, "illegal port")
  require(applicationUIURL.endsWith("/"), "applicationUIURL must end with a '/'.")
  require(MogopayEndPoint.endsWith("/"), "applicationAPIURL must end with a '/'.")
  require(Sips.CertifDir.endsWith("/"), "SipsCertifDir must end with a '/'.")
}

object Environment extends Enumeration {
  type Environment = Value
  val DEV = Value(1)
  val PROD = Value(2)
}

import Environment._
