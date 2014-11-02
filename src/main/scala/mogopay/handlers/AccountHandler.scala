package mogopay.handlers

import java.io.File
import java.security.MessageDigest
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.{Calendar, Date, UUID}

import com.atosorigin.services.cad.common.util.FileParamReader
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.sksamuel.elastic4s.ElasticDsl._
import mogopay.actors.AccountActor._
import mogopay.codes.MogopayConstant
import mogopay.config.HandlersConfig._
import mogopay.config._
import mogopay.es.EsClient
import mogopay.exceptions.Exceptions._
import mogopay.handlers.Token.{Token, TokenType}
import mogopay.handlers.Token.TokenType.TokenType
import mogopay.util.GlobalUtil._
import mogopay.model.Mogopay.TokenValidity.TokenValidity
import mogopay.model.Mogopay._
import mogopay.session.Session
import mogopay.util.SymmetricCrypt
import org.apache.shiro.crypto.hash.Sha256Hash
import org.json4s.jackson.Serialization.write
import org.json4s.jackson.Serialization.read

import scala.util._
import scala.util.control.NonFatal
import scala.util.parsing.json.JSON

class LoginException(msg: String) extends Exception(msg)

class AccountHandler {

  implicit val formats = new org.json4s.DefaultFormats {}

  def update(account: Account) = EsClient.update(account, true, false)

  def findByEmail(email: String): Option[Account] = {
    val req = search in Settings.ElasticSearch.Index types "Account" limit 1 from 0 filter {
      termFilter("email", email)
    }
    EsClient.search[Account](req)
  }

  def isPatternValid(pattern: String): Boolean = {
    try {
      java.util.regex.Pattern.compile(pattern)
      true
    } catch {
      case NonFatal(_) => false
    }
  }

  private def buildFindAccountRequest(email: String, merchantId: Option[String]): SearchDefinition = {
    if (!Settings.sharedCustomers && merchantId.nonEmpty) {
      search in Settings.ElasticSearch.Index -> "Account" limit 1 from 0 filter {
        and(
          termFilter("email", email),
          termFilter("owner", merchantId.get)
        )
      }
    } else {
      search in Settings.ElasticSearch.Index types "Account" filter {
        and(
          termFilter("email", email),
          missingFilter("owner") existence true includeNull true
        )
      }
    }
  }

  def alreadyExistEmail(email: String, merchantId: Option[String]): Boolean = {
    val req = buildFindAccountRequest(email, merchantId)
    val res = EsClient().execute(req)
    res.getHits.totalHits() == 1
  }

  //
  //  def verifAccountId(id: String): Boolean = {
  //    EsClient.load[Account](id).isDefined
  //  }

  def login(email: String, password: String, merchantId: Option[String], isCustomer: Boolean): Account = {
    val lowerCaseEmail = email.toLowerCase
    val userAccountRequest =
      if (isCustomer) {
        val merchantReq = if (merchantId.isDefined) {
          search in Settings.ElasticSearch.Index -> "Account" limit 1 from 0 filter {
            and(
              termFilter("uuid", merchantId.get),
              missingFilter("owner") existence true includeNull true
            )
          }
        } else {
          search in Settings.ElasticSearch.Index -> "Account" limit 1 from 0 filter {
            and(
              termFilter("email", Settings.AccountValidateMerchantDefault),
              missingFilter("owner") existence true includeNull true
            )
          }
        }

        val merchant = EsClient.search[Account](merchantReq).getOrElse(throw VendorNotFoundException(""))
        val isMerchant = merchant.roles.contains(RoleName.MERCHANT)
        if (!isMerchant) {
          throw InvalidMerchantAccountException("")
        }
        if (merchant.status == AccountStatus.INACTIVE) {
          throw InactiveMerchantException("")
        }
        search in Settings.ElasticSearch.Index -> "Account" limit 1 from 0 filter {
          and(
            termFilter("email", lowerCaseEmail),
            termFilter("owner", merchant.uuid)
          )
        }
      } else {
        search in Settings.ElasticSearch.Index -> "Account" limit 1 from 0 filter {
          and(
            termFilter("email", lowerCaseEmail),
            missingFilter("owner") existence true includeNull true
          )
        }
      }

    EsClient.search[Account](userAccountRequest).map { userAccount =>
      if (userAccount.loginFailedCount > MogopayConstant.MaxAttempts)
        throw TooManyLoginAttemptsException(s"${userAccount.email}")
      else if (userAccount.status == AccountStatus.INACTIVE)
        throw InactiveAccountException(s"${userAccount.email}")
      else if (userAccount.password != new Sha256Hash(password).toString) {
        EsClient.update(userAccount.copy(loginFailedCount = userAccount.loginFailedCount + 1), false, true)
        throw InvalidPasswordErrorException(s"${userAccount.email}")
      } else {
        val userAccountToIndex = userAccount.copy(loginFailedCount = 0, lastLogin = Some(Calendar.getInstance().getTime))
        EsClient.update(userAccountToIndex, false, true)
        userAccountToIndex.copy(password = "")
      }
    }.getOrElse(throw AccountDoesNotExistException(""))
  }

  //  def requestPasswordChange(email:String, merchantId:Option[String], isCustomer:Boolean) : Boolean = {
  //    val res = buildFindAccountRequest(email, merchantId, isCustomer)
  //    val account = EsClient.search[Account](res)
  //    account map { account =>
  //
  //    }
  //  }

  //
  //  boolean requestPasswordChange(String email, Long vendorId, String changePasswordURL, boolean isCustomer) {
  //    if (!shared && isCustomer && vendorId == null)  {
  //      throw new IllegalArgumentException("invalid request")
  //    }
  //    if (email) {
  //      List<Account> accounts = Account.withCriteria {
  //        eq("email", email)
  //        if (isCustomer) {
  //          owner { eq ("id", vendorId) }
  //        }
  //        else {
  //          isNull("owner")
  //        }
  //      }
  //      if (accounts?.size() > 0) {
  //        Account account = accounts.get(0)
  //        emailConfirmationService.maxAge = Holders.config.emailConfirmation.maxAge
  //        emailConfirmationService.sendConfirmation(
  //          id:account.id.toString() +","+(changePasswordURL ? changePasswordURL : ""),
  //        model:[email:email, merchantId:vendorId, accountId: account.id, changePasswordURL:changePasswordURL, callbackPrefix:isCustomer ? account.owner.paymentConfig.callbackPrefix : Holders.config.grails.serverURL],
  //        view:'/mailtemplates/change_password',
  //        to:email,
  //        event:'lost',
  //        subject:"Update MOGOPAY password request !")
  //        return true
  //      }
  //      else {
  //        // email does not exists redirect user to login page
  //        return false
  //      }
  //    }
  //    else {
  //      return false
  //    }
  //  }
  //

  /**
   * For debugging purposes. Works only for merchant.com - so no risk :)
   * @return
   */
  def id(seller: String): String = {
    val email = seller + "@merchant.com"
    this.findByEmail(email).map(_.uuid).getOrElse(throw InvalidEmailException(s"$email"))
  }

  /**
   * For debugging purposes. Works only for merchant.com - so no risk :)
   * @return
   */
  def secret(seller: String): String = {
    val email = seller + "@merchant.com"
    this.findByEmail(email).map(_.secret).getOrElse(throw InvalidEmailException(s"$email"))
  }


  def generateLostPasswordToken(email: String, merchantSecret: String): String = {
    val findBySecretReq = search in Settings.ElasticSearch.Index -> "Account" limit 1 from 0 filter {
      termFilter("secret", merchantSecret)
    }
    val merchantAccount = EsClient.search[Account](findBySecretReq)
    merchantAccount map { merchantAccount =>
      val req = buildFindAccountRequest(email, Some(merchantAccount.uuid))
      val account = EsClient.search[Account](req)
      account map { account =>
        import org.joda.time.DateTime
        val date = new DateTime().plusSeconds(Settings.Mail.MaxAge)
        SymmetricCrypt.encrypt(s"$email;${date.toDate.getTime}", Settings.ApplicationSecret, "AES")
      } getOrElse (throw new AccountDoesNotExistException(s"$email"))
    } getOrElse (throw new AccountDoesNotExistException("Invalid merchant secret"))
  }

  //
  //
  //  def listCustomers(merchantId: String, pageNumber: Int): Array[Account] = {
  //    val req = search in Settings.DB.INDEX types "Account" filter {
  //      termFilter("owner", merchantId)
  //    }
  //
  //    val res = EsClient().execute(req)
  //    res.getHits.getHits.map { hit =>
  //      JacksonConverter.deserialize[Account](hit.source().toString)
  //    }
  //  }
  //
  //  def findByNames(firstName: String, lastName: String): Future[Option[Account]] =
  //    DAO.findBy("firstName" -> firstName, "lastName" -> lastName)

  def load(uuid: String): Option[Account] = {
    EsClient.load[Account](uuid)
  }

  def findBySecret(secret: String): Option[Account] = {
    val req = search in Settings.ElasticSearch.Index -> "Account" filter termFilter("secret", secret)
    EsClient.search[Account](req)
  }

  //
  //  def findWithoutOwner(id: Long): Future[Option[Account]] =
  //    DAO.findBy("uuid" -> id, "owner" -> "null")
  //
  //  def findByEmail(email: String): Future[Option[Account]] =
  //    DAO.findBy("email" -> email)
  //
  //  def findByEmailAndOwnerIsNull(email: String): Future[Option[Account]] = {
  //    DAO.findBy("email" -> email, "owner" -> "null")
  //  }
  //
  def save(account: Account): Try[Unit] = findByEmail(account.email) match {
    case Some(_) => Failure(AccountWithSameEmailAddressAlreadyExistsError(s"${account.email}"))
    case None => EsClient.index(account); Success()
  }

  def getMerchant(merchantId: String): Option[Account] = {
    val merchant = EsClient.load[Account](merchantId)
    merchant flatMap { merchant =>
      if (merchant.owner.isEmpty) Some(merchant) else None
    }
  }

  type UserInfo = Option[Map[Symbol, Option[String]]]

  def checkTokenValidity(token: String): (TokenValidity, UserInfo) = {
    import org.joda.time._

    val uncryptedToken = SymmetricCrypt.decrypt(token, Settings.ApplicationSecret, "AES").split(";")
    val (email, date) = (uncryptedToken(0), DateTime.parse(uncryptedToken(1)))

    val account = findByEmail(email)
    val result: (TokenValidity, UserInfo) = if (account == None) {
      (TokenValidity.INVALID, None)
    } else if (Seconds.secondsBetween(DateTime.now, date).getSeconds < 0) {
      (TokenValidity.EXPIRED, None)
    } else {
      (TokenValidity.VALID, Some(Map(
        'id -> Some(account.get.uuid),
        'email -> Some(account.get.email),
        'firstName -> account.get.firstName,
        'lastName -> account.get.lastName
      )))
    }

    Settings.RSA.privateKey.close()
    result
  }

  def updatePassword(password: String, vendorId: String, accountId: String): Unit = {
    def `match`(pattern: String, password: String): Boolean = {
      if (pattern.length == 0) {
        true
      } else {
        password.matches(pattern)
      }
    }

    val account = accountHandler.load(accountId).getOrElse(throw AccountDoesNotExistException(""))
    val merchant = getMerchant(vendorId).getOrElse(throw VendorNotFoundException(s"$vendorId"))
    val paymentConfig = merchant.paymentConfig.getOrElse(throw PaymentConfigNotFoundException(""))
    val pattern = paymentConfig.passwordPattern.getOrElse(throw PasswordPatternNotFoundException(""))
    val matching: Boolean = `match`(pattern, password)

    if (!matching) throw PasswordDoesNotMatchPatternException("")
    update(account.copy(password = new Sha256Hash(password).toHex))
  }

  def updateLostPassword(password: String, token: String): Unit = {
    val clearText = SymmetricCrypt.decrypt(token, Settings.ApplicationSecret, "AES").split(";")
    val email = clearText(0)
    val date = clearText(1).toLong
    val now = new Date().getTime
    if (now > date) {
      throw TokenExpiredException(s"$now > $date")
    }
    else {
      val account = this.findByEmail(email).getOrElse(throw InvalidEmailException(s"$email"))
      update(account.copy(password = new Sha256Hash(password).toHex))
    }
  }

  /*
  def verify(userEmail: String, merchantSecret: String,
             mogopayToken: String): Try[Either[String, String]] = {
    val time = System.currentTimeMillis()

    val account: Try[Account] = findBySecret(merchantSecret) match {
      case None    => Failure(new AccountDoesNotExistException)
      case Some(x) => Success(x)
    }

    val merchant: Try[Unit] = account match {
      case Failure(t)                  => Failure(t)
      case Success(m) if isMerchant(m) => Success(())
      case _                           => Failure(new NotAVendorAccountException)
    }

    merchant match {
      case Failure(t) => Failure(t)
      case Success(_) =>
        val decodedData = RSA.decrypt(mogopayToken, Settings.RSA.privateKey)

        val parts = decodedData.split(";")
        val (email, errorCode) = (parts(0), parts(3))

        if (errorCode == MogopayConstant.Success) {
          val startTime: Long = parts(1).toLong
          if (time - startTime > 60000) {
            Success(Left(MogopayConstant.VerifyTimeout))
          } else if (email != userEmail) {
            Success(Left(MogopayConstant.VerifyInvalidEmail))
          } else {
            Success(Right(MogopayConstant.Success))
          }
        } else {
          Success(Left(errorCode))
        }
    }
  }
  */

  def isMerchant(account: Account) = account.roles.contains(RoleName.MERCHANT)

  //  def isMerchant(uuid: String): Try[Boolean] =
  //    find(uuid).map { acc =>
  //      Success(acc.roles.contains(RoleName.MERCHANT))
  //    }.getOrElse(Failure(new AccountDoesNotExistException))

  /**
   * Generates, saves and sends a pin code
   */
  def generateAndSendPincode3(uuid: String): Unit = {

    val acc = accountHandler.load(uuid) getOrElse (throw AccountDoesNotExistException(""))
    val phoneNumber: Telephone =
      (for {
        addr <- acc.address
        tel <- addr.telephone
      } yield tel) getOrElse (throw NoPhoneNumberFoundException(""))

    val plainTextPinCode = UtilHandler.generatePincode3()
    val md = MessageDigest.getInstance("SHA-256")
    md.update(plainTextPinCode.getBytes("UTF-8"))
    val pinCode3 = new String(md.digest(), "UTF-8")

    val newTelephone = phoneNumber.copy(status = TelephoneStatus.WAITING_ENROLLMENT, pinCode3 = Some(pinCode3))
    EsClient.index(acc.copy(address = acc.address.map(_.copy(telephone = Option(newTelephone)))))

    def message = "Your 3 digits code is: " + plainTextPinCode
    smsHandler.sendSms(message, phoneNumber.phone)
  }

  /*
  def generateNewEmailCode(uuid: String): Try[Unit] = accountHandler.find(uuid) map { account =>
    def buildMessage(uri: String): String = {
      val path: String = Settings.EmailTemplatesDir
      val content = getClass.getClassLoader.getResource(path + "confirm_merchant")
      scala.io.Source.fromFile(new File(content.toURI)).getLines()
        .mkString
        .replace("${uri}", uri)
    }

    import mogopay.handlers.EmailHandler._

    val uri = ???
    send a new Mail(
      from = (Settings.EmailSenderAddress, Settings.EmailSenderName),
      to = account.email,
      subject = "Please confirm your MOGOPAY merchant account",
      message = buildMessage(uri)
    )

    Success(())
  } getOrElse Failure(new AccountDoesNotExistException)
  */

  object Emailing {

    import mogopay.util._

    object EmailType extends Enumeration {
      type EmailType = Value
      val Signup = Value(0)
      val BypassLogin = Value(1)
    }

    /*
    private def generateAndSaveEmailingToken(accountId: String, tokenType: EmailType): String = {
      val timestamp: Long = (new java.util.Date).getTime
      val clearToken: String = tokenType.id + "-" + timestamp + "-" + accountId
      val token = RSA.encrypt(clearToken, publicKey)
      find(accountId).map {
        _.map { acc =>
          //          DAO.update(acc.copy(emailingToken = Some(token)))
        }
      }
      token
    }
    */

    /*
    def sendSignupConfirmationEmail(accountId: String) = {
      def buildEmailBody(uri: String): String = {
        val path: String = Settings.EmailTemplatesDir
        val content = getClass.getClassLoader.getResource(path + "confirm_merchant")
        scala.io.Source.fromFile(new File(content.toURI)).getLines()
          .mkString
          .replace("${uri}", uri)
      }

      val token = generateAndSaveEmailingToken(accountId, Signup)
      accountHandler.find(accountId) match {
        case None => Failure(new AccountDoesNotExistException)
        case Some(account) => Success(send a new Mail(
          from = (Settings.EmailSenderAddress, Settings.EmailSenderName),
          to = account.email,
          subject = "Mogopay signup confirmation",
          message = buildEmailBody(Settings.applicationUIURL + "confirmSignup?token=" + token)
        ))
      }
    }
    */

    def confirmSignup(token: String): Boolean = {
      if (!token.contains("-")) {
        throw InvalidTokenException("Invalid token.")
      } else {
        val splitToken = SymmetricCrypt.decrypt(token, Settings.ApplicationSecret, "AES").split("-")
        val timestamp = splitToken(1).toLong
        val accountId = splitToken(2)

        if (EmailType(Integer.parseInt(splitToken(0))) != EmailType.Signup) {
          false
        } else {
          val signupDate = new org.joda.time.DateTime(timestamp).getMillis
          val currentDate = new org.joda.time.DateTime().getMillis
          if (currentDate - signupDate > Settings.Mail.MaxAge) {
            false
          } else {
            load(accountId).map { acc =>
              EsClient.index(acc.copy(status = AccountStatus.ACTIVE))
            }
            true
          }
        }
      }
    }

    /*
    def sendLoginBypassEmail(accountId: String) = {
      def buildEmailBody(url: String) = {
        val path: String = Settings.EmailTemplatesDir
        val content = getClass.getClassLoader.getResource(path + "passwordchange")
        scala.io.Source.fromFile(new File(content.toURI)).getLines()
          .mkString
          .replace("${uri}", url)
      }

      val token = generateAndSaveEmailingToken(accountId, BypassLogin)
      accountHandler.find(accountId) match {
        case None => Failure(new AccountDoesNotExistException)
        case Some(account) => Success(send a new Mail(
          from = (Settings.EmailSenderAddress, Settings.EmailSenderName),
          to = account.email,
          subject = "Mogopay signup confirmation",
          message = buildEmailBody(Settings.applicationUIURL + "bypassLogin?token=" + token)
        ))
      }
    }
    */

    def bypassLogin(token: String, session: Session): Option[Session] = {
      val splitToken = SymmetricCrypt.decrypt(token, Settings.ApplicationSecret, "AES").split("-")
      val timestamp = splitToken(1).toLong
      val accountId = splitToken(2).toLong

      if (EmailType(Integer.parseInt(splitToken(0))) != EmailType.BypassLogin) {
        None
      } else {
        val signupDate = new org.joda.time.DateTime(timestamp).getMillis
        val currentDate = new org.joda.time.DateTime().getMillis
        if (currentDate - signupDate > Settings.Mail.MaxAge) {
          None
        } else {
          Some(session += "accountId" -> accountId)
        }
      }
    }
  }

  def generateNewSecret(accountId: String): Option[String] = load(accountId).map {
    acc =>
      val secret = UUID.randomUUID().toString
      update(acc.copy(secret = secret))
      secret
  }

  def addCreditCard(accountId: String, ccId: Option[String], holder: String,
                    number: Option[String], expiryDate: String, ccType: String): CreditCard = ccId match {
    case None => createCard(accountId, holder, number.get, expiryDate, ccType)
    case Some(cardId) => updateCard(accountId, cardId, holder, expiryDate, ccType)
  }

  private def updateCard(accountId: String, ccId: String, holder: String,
                         expiryDate: String, ccType: String): CreditCard = {
    val account: Account = EsClient.load[Account](accountId) getOrElse (throw AccountDoesNotExistException(""))

    val card = account.creditCards.find(_.uuid == ccId).getOrElse(throw CreditCardDoesNotExistException(""))

    val newCard = card.copy(
      holder = holder,
      expiryDate = new Timestamp(new SimpleDateFormat("MM/yyyy").parse(expiryDate).getTime),
      cardType = CreditCardType.withName(ccType)
    )

    val newCards = account.creditCards.filter(_.uuid != ccId) :+ newCard
    EsClient.index(account.copy(creditCards = newCards))

    newCard
  }

  private def createCard(accountId: String, holder: String, number: String,
                         expiryDate: String, ccType: String): CreditCard = {
    val account = load(accountId) getOrElse (throw AccountDoesNotExistException(""))

    val (hiddenN, cryptedN) =
      if (!UtilHandler.checkLuhn(number)) {
        throw InvalidCardNumberException("")
      } else {
        (UtilHandler.hideCardNumber(number, "X"), SymmetricCrypt.encrypt(number, Settings.ApplicationSecret, "AES"))
      }

    val expiryTime = new Timestamp(new SimpleDateFormat("MM/yyyy").parse(expiryDate).getTime)

    val newCard = CreditCard(uuid = java.util.UUID.randomUUID().toString,
      number = cryptedN,
      holder = holder,
      expiryDate = expiryTime,
      cardType = CreditCardType.withName(ccType),
      account = accountId,
      hiddenNumber = hiddenN)
    val newCards = account.creditCards :+ newCard
    EsClient.index(account.copy(creditCards = newCards))
    newCard.copy(number = UtilHandler.hideCardNumber(newCard.number, "X"))
  }

  def getBillingAddress(accountId: String): Option[AccountAddress] = load(accountId).flatMap(_.address)

  def getShippingAddresses(accountId: String): Seq[ShippingAddress] =
    load(accountId) map (_.shippingAddresses) getOrElse Nil

  def getShippingAddress(accountId: String): Option[ShippingAddress] =
    getShippingAddresses(accountId: String) find (_.active)

  def assignBillingAddress(accountId: String, address: AddressToAssignFromGetParams): Unit = {
    load(accountId).map {
      account =>
        val newAddress = account.address match {
          case None => address.getAddress
          case Some(addr) => addr.copy(road = address.road,
            road2 = address.road2,
            city = address.city,
            zipCode = address.zipCode,
            extra = address.extra,
            civility = address.civility.map(Civility.withName),
            firstName = address.firstName,
            lastName = address.lastName,
            country = address.country,
            admin1 = address.admin1,
            admin2 = address.admin2)
        }
        EsClient.index(account.copy(address = Some(newAddress)))
    } getOrElse (throw AccountDoesNotExistException(s"$accountId"))
  }

  def deleteShippingAddress(accountId: String, addressId: String): Unit =
    for {
      account <- load(accountId)
      address <- account.shippingAddresses.find(_.uuid == addressId)
    } yield {
      val newAddresses = account.shippingAddresses diff List(address)
      val newAccount = account.copy(shippingAddresses = newAddresses)
      update(newAccount)
    }

  def addShippingAddress(accountId: String, address: AddressToAddFromGetParams): Unit =
    load(accountId).map {
      account =>
        val shippAddr = ShippingAddress(java.util.UUID.randomUUID().toString, active = true, address.getAddress)
        val newAddrs = account.shippingAddresses.map(_.copy(active = false)) :+ shippAddr
        EsClient.index(account.copy(shippingAddresses = newAddrs))
        shippAddr
    } getOrElse (throw AccountDoesNotExistException(s"$accountId"))

  def updateShippingAddress(accountId: String, address: AddressToUpdateFromGetParams): Unit =
    load(accountId).map {
      account =>
        account.shippingAddresses.find(_.uuid == address.id) map {
          addr =>
            val newAddrs = account.shippingAddresses.filterNot(_ == addr) :+ addr.copy(
              address = addr.address.copy(
                road = address.road,
                road2 = address.road2,
                city = address.city,
                zipCode = address.zipCode,
                extra = address.extra,
                civility = address.civility.map(Civility.withName),
                firstName = address.firstName,
                lastName = address.lastName,
                country = address.country,
                admin1 = address.admin1,
                admin2 = address.admin2))
            EsClient.index(account.copy(shippingAddresses = newAddrs))
        }
    } getOrElse (throw AccountDoesNotExistException(s"$accountId"))

  type ActiveCountryState = Map[Symbol, Option[String]]

  def getActiveCountryState(accountId: String): Option[ActiveCountryState] = {
    load(accountId).map {
      account =>
        val addr = account.shippingAddresses.find(_.active).map(_.address).orElse(account.address)
        addr.map {
          addr =>
            Map('countryCode -> addr.country, 'stateCode -> addr.admin1)
        }
    } getOrElse (throw AccountDoesNotExistException(s"$accountId"))
  }

  def selectShippingAddress(accountId: String, addrId: String): Unit =
    load(accountId).map {
      account =>
        val newAddrs = account.shippingAddresses.map {
          addr => addr.copy(active = addr.uuid == addrId)
        }
        EsClient.index(account.copy(shippingAddresses = newAddrs))
    } getOrElse (throw AccountDoesNotExistException(s"$accountId"))

  def profileInfo(accountId: String): Map[Symbol, Any] = load(accountId).map {
    account =>
      val cards = creditCardHandler.findByAccount(accountId)
      val countries = countryHandler.findCountriesForBilling()

      val paymentConfig = account.paymentConfig
      val paypalParam = paymentConfig.map(_.paypalParam).flatten
      val kwixoParam = paymentConfig.map(_.kwixoParam).flatten

      val basePaymentProviderParam: Option[Map[String, String]] = paymentConfig.map(_.cbParam).flatten
        .map(JSON.parseFull).flatten
        .map(_.asInstanceOf[Map[String, String]])
      val cbParam = basePaymentProviderParam.map {
        ppp =>
          val dir = new File(Settings.Sips.CertifDir, account.uuid)
          dir.mkdirs
          val targetFile = new File(dir, "certif.fr." + ppp.get("sipsMerchantId"))

          if (targetFile.exists) {
            val certificateData = scala.io.Source.fromFile(targetFile).getLines().mkString
            ppp ++ "sipsMerchantCertificateData" -> certificateData
          } else {
            ppp
          }
      }

      Map('account -> account.copy(password = ""),
        'cards -> cards,
        'countries -> countries,
        'cbParam -> cbParam,
        'paypalParam -> paypalParam.map(JSON.parseFull).flatten,
        'kwixoParam -> kwixoParam.map(JSON.parseFull).flatten,
        'emailField -> paymentConfig.map(_.emailField),
        'passwordField -> paymentConfig.map(_.passwordField),
        'callbackPrefix -> paymentConfig.map(_.callbackPrefix),
        'passwordPattern -> paymentConfig.map(_.passwordPattern),
        'isMerchant -> account.owner.isEmpty)
  } getOrElse (throw AccountDoesNotExistException(s"$accountId"))

  def find(uuid: String) = EsClient.load[Account](uuid)

  def updateProfile(profile: UpdateProfile): Unit = {
    if (!profile.isMerchant && profile.vendor.isEmpty) Failure(new VendorNotProvidedError(""))
    else find(profile.id) match {
      case None => Failure(new AccountAddressDoesNotExistException(""))
      case Some(account) =>
        lazy val address = account.address.map {
          address =>
            val telephone = Telephone("", profile.lphone, "", None, TelephoneStatus.WAITING_ENROLLMENT)
            address.copy(
              telephone = Some(telephone),
              road = profile.billingAddress.road,
              road2 = profile.billingAddress.road2,
              city = profile.billingAddress.city,
              zipCode = profile.billingAddress.zipCode,
              country = profile.billingAddress.country,
              admin1 = profile.billingAddress.admin1,
              admin2 = profile.billingAddress.admin2
            )
        }

        lazy val civility = Civility.withName(profile.civility)

        lazy val birthDate = Some(new SimpleDateFormat("yyyy-MM-dd").parse(profile.birthDate))

        lazy val password = profile.password.map {
          case (p1, p2) =>
            if (p1 != p2) throw PasswordsDoNotMatchError("*****")
            else new Sha256Hash(p1).toHex
        } getOrElse account.password

        val cbProvider = CBPaymentProvider.withName(profile.cbProvider)
        val cbParam = profile.cbParam

        val updateCBParam = if (cbProvider == CBPaymentProvider.SIPS) {
          var params = cbParam.asInstanceOf[SIPSParams]
          val dir = new File(Settings.Sips.CertifDir, account.uuid)

          dir.mkdirs()

          if (params.sipsMerchantParcomFileName.isDefined && params.sipsMerchantParcomFileName != Some("")) {
            val parcomTargetFile = new File(dir, "parcom." + params.sipsMerchantId)
            if (params.sipsMerchantParcomFileContent.getOrElse("").length > 0 && params.sipsMerchantParcomFileName.getOrElse("").length > 0) {
              parcomTargetFile.delete()
              scala.tools.nsc.io.File(parcomTargetFile.getAbsolutePath).writeAll(params.sipsMerchantParcomFileContent.get)
            }
          } else {
            println("NO PARCOM")
            try {
              val oldSIPSMerchantParcomFileContent: Option[String] = (for {
                pc <- account.paymentConfig
                cbp <- pc.cbParam
              } yield read[SIPSParams](cbp).sipsMerchantParcomFileContent).flatten
              val oldSIPSMerchantParcomFileName: Option[String] = (for {
                pc <- account.paymentConfig
                cbp <- pc.cbParam
              } yield read[SIPSParams](cbp).sipsMerchantParcomFileName).flatten

              params = params.copy(
                sipsMerchantParcomFileContent = oldSIPSMerchantParcomFileContent,
                sipsMerchantParcomFileName = oldSIPSMerchantParcomFileName
              )
            } catch {
              case _: Throwable => params
            }
          }

          if (params.sipsMerchantCertificateFileName.isDefined && params.sipsMerchantCertificateFileName != Some("")) {
            val certificateTargetFile = new File(dir, "certif." + params.sipsMerchantCountry + "." + params.sipsMerchantId)
            if (params.sipsMerchantCertificateFileContent.getOrElse("").length > 0 && params.sipsMerchantCertificateFileName.getOrElse("").length > 0) {
              certificateTargetFile.delete()
              scala.tools.nsc.io.File(certificateTargetFile.getAbsolutePath).writeAll(params.sipsMerchantCertificateFileContent.get)
            }

            val targetFile = new File(dir, "pathfile")
            val isJSP = params.sipsMerchantCertificateFileContent.map(_.indexOf("!jsp") > 0).getOrElse(false) ||
              (targetFile.exists() && (new FileParamReader(targetFile.getAbsolutePath)).getParam("F_CTYPE") == "jsp")
            targetFile.delete()
            scala.tools.nsc.io.File(targetFile.getAbsolutePath).writeAll(
              s"""
             |D_LOGO!${Settings.MogopayEndPoint}${Settings.ImagesPath}sips/logo/!
             |F_DEFAULT!${Settings.Sips.CertifDir}${File.separator}parmcom.defaut!
             |F_PARAM!${new File(dir, "parcom").getAbsolutePath}!
             |F_CERTIFICATE!${new File(dir, "certif").getAbsolutePath}!
             |${if (isJSP) "F_CTYPE!jsp!" else ""}"
           """.stripMargin.trim
            )

            if (isJSP)
              certificateTargetFile.renameTo(new File(certificateTargetFile.getAbsolutePath + ".jsp"))
          } else {
            println("NO CERTIF")
            try {
              val oldSIPSMerchantCertificateFileContent: Option[String] = (for {
                pc <- account.paymentConfig
                cbp <- pc.cbParam
              } yield read[SIPSParams](cbp).sipsMerchantCertificateFileContent).flatten
              val oldSIPSMerchantCertificateFileName: Option[String] = (for {
                pc <- account.paymentConfig
                cbp <- pc.cbParam
              } yield read[SIPSParams](cbp).sipsMerchantCertificateFileName).flatten

              params = params.copy(
                sipsMerchantCertificateFileContent = oldSIPSMerchantCertificateFileContent,
                sipsMerchantCertificateFileName = oldSIPSMerchantCertificateFileName
              )
            } catch {
              case _: Throwable => params
            }
          }

          params
        } else {
          cbParam
        }

        val paymentConfig = PaymentConfig(
          paymentMethod = CBPaymentMethod.withName(profile.paymentMethod),
          cbProvider = CBPaymentProvider.withName(profile.cbProvider),
          kwixoParam = profile.kwixoParam.kwixoParams,
          paypalParam = Some(write(caseClassToMap(profile.payPalParam))),
          cbParam = Some(write(updateCBParam)),
          emailField = if (profile.emailField == "") "user_email" else profile.emailField,
          passwordField = if (profile.passwordField == "") "user_password" else profile.passwordField,
          pwdEmailContent = profile.passwordContent,
          pwdEmailSubject = profile.passwordSubject,
          callbackPrefix = profile.callbackPrefix,
          passwordPattern = profile.passwordPattern)

        val newAccount = account.copy(
          password = password,
          company = Some(profile.company),
          website = Some(profile.website),
          civility = Some(civility),
          firstName = Some(profile.firstName),
          lastName = Some(profile.lastName),
          birthDate = birthDate,
          address = address,
          paymentConfig = Some(paymentConfig)
        )

        val validateMerchantPhone: Boolean = false
        val validateCustomerPhone: Boolean = false

        if ((profile.isMerchant && validateMerchantPhone) || (!profile.isMerchant && validateCustomerPhone)) {
          val lphone: Option[String] = newAccount.address.flatMap(_.telephone.map(_.lphone))
          val oldLPhone: Option[String] = account.address.flatMap(_.telephone.map(_.lphone))
          if (newAccount.address.flatMap(_.telephone) != None && (oldLPhone == None || oldLPhone != lphone)) {
            generateAndSendPincode3(newAccount.uuid)
          }
        }
        update(newAccount)
    }
  }

  def recycle() {
    val req = select in Settings.ElasticSearch.Index -> "Account" filter {
      and(
        termFilter("status", AccountStatus.WAITING_ENROLLMENT),
        rangeFilter("waitingEmailSince") from 0 to (System.currentTimeMillis() - Settings.RecycleAccountDuration)
      )
    }
    EsClient.searchAllRaw(req) map (_.getId) foreach (EsClient.delete[Account](_, false))
  }

  def enroll(accountId: String, lPhone: String, pinCode: String): Try[Unit] = {
    load(accountId).map {
      user =>
        if (user.address.map(_.telephone.map(_.lphone)).flatten.nonEmpty &&
          user.address.map(_.telephone.map(_.lphone)).flatten != Some(lPhone)) {
          val newTel = user.address.get.telephone.get.copy(lphone = lPhone)
          val newAddr = user.address.get.copy(telephone = Some(newTel))
          val newUser = user.copy(address = Some(newAddr))
          EsClient.index(newUser)
          Success(())
        } else {
          load(accountId).map {
            account =>
              val encryptedCode = new Sha256Hash(pinCode).toHex
              if (account.address.map(_.telephone.map(_.pinCode3)).flatten == Some(encryptedCode) &&
                account.address.map(_.telephone.map(_.status)).flatten == Some(TelephoneStatus.WAITING_ENROLLMENT)) {
                val newTel = Telephone("", lPhone, "", None, TelephoneStatus.ACTIVE)
                EsClient.index(account.copy(address = account.address.map(_.copy(telephone = Some(newTel)))))
                Success(())
              } else {
                Failure(new MogopayError(MogopayConstant.InvalidPhonePincode3))
              }
          }.getOrElse(Failure(new AccountDoesNotExistException("")))
        }
    } getOrElse Failure(new AccountDoesNotExistException(""))
  }

  def signup(signup: Signup): (Token, Account) = {
    if (!signup.isMerchant && signup.vendor.isEmpty) {
      throw VendorNotProvidedError("Vendor cannot be null")
    } else {
      val req = search in Settings.ElasticSearch.Index -> "Account" filter termFilter("email", signup.email)
      if (EsClient.search[Account](req).isDefined)
        throw new AccountWithSameEmailAddressAlreadyExistsError("")

      val birthdate = new SimpleDateFormat("yyyy-MM-dd").parse(signup.birthDate)
      val civility = Civility.withName(signup.civility)

      if (signup.password.isEmpty)
        throw NoPasswordProvidedError("****")

      if (signup.password != signup.password2)
        throw PasswordsDoNotMatchError("****")

      val password = new Sha256Hash(signup.password).toHex

      val countryCode = signup.address.country.getOrElse(throw InvalidInputException(s"Country not found. ${signup.address.country}"))

      val country: Country = countryHandler.findByCode(countryCode) getOrElse (throw CountryDoesNotExistException(s"$countryCode"))

      def address(country: Country): AccountAddress = {
        val phoneStatus = if ((signup.isMerchant && Settings.AccountValidateMerchantPhone) ||
          (!signup.isMerchant && Settings.AccountValidateCustomerPhone)) {
          TelephoneStatus.WAITING_ENROLLMENT
        } else {
          TelephoneStatus.ACTIVE
        }

        val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
        val phoneNumber = phoneUtil.parse(signup.lphone, country.code)

        val tel = Telephone(
          phone = phoneUtil.format(phoneNumber, PhoneNumberFormat.INTERNATIONAL),
          lphone = phoneUtil.format(phoneNumber, PhoneNumberFormat.NATIONAL),
          isoCode = country.code,
          pinCode3 = Some("000"),
          status = phoneStatus)

        signup.address.copy(telephone = Some(tel))
      }

      val addr = address(country)
      val accountId = newUUID

      val accountStatus = if ((signup.isMerchant && Settings.AccountValidateMerchantEmail) ||
        (!signup.isMerchant && Settings.AccountValidateCustomerEmail)) {
        AccountStatus.WAITING_ENROLLMENT
      } else {
        AccountStatus.ACTIVE
      }

      val token = if (accountStatus == AccountStatus.ACTIVE) ""
      else Token.generateToken(accountId, TokenType.Signup)

      val account = Account(
        uuid = accountId,
        email = signup.email,
        password = password,
        civility = Some(civility),
        firstName = Some(signup.firstName),
        lastName = Some(signup.lastName),
        birthDate = Some(birthdate),
        status = accountStatus,
        secret = if (signup.isMerchant) newUUID else "",
        owner = if (signup.isMerchant) None else signup.vendor,
        address = Some(addr),
        waitingPhoneSince = System.currentTimeMillis(),
        waitingEmailSince = System.currentTimeMillis(),
        country = Some(country),
        roles = List(if (signup.isMerchant) RoleName.MERCHANT else RoleName.CUSTOMER),
        company = signup.company,
        website = signup.website,
        emailingToken = Some(token)
      )

      if (signup.isMerchant)
        transactionSequenceHandler.nextTransactionId(account.uuid)

      EsClient.index(account)

      (token, account)
    }
  }
}

object Token {
  type Token = String

  object TokenType extends Enumeration {
    type TokenType = Value
    val Signup = Value(0)
    val BypassLogin = Value(1)
  }

  def generateAndSaveToken(accountId: String, tokenType: TokenType): Option[String] = {
    val timestamp: Long = (new java.util.Date).getTime
    val clearToken: String = tokenType.id + "-" + timestamp + "-" + accountId
    val token = SymmetricCrypt.encrypt(clearToken, Settings.ApplicationSecret, "AES")
    accountHandler.find(accountId).map { account =>
      accountHandler.save(account.copy(emailingToken = Some(token)))
      token
    }
  }

  def generateToken(accountId: String, tokenType: TokenType): String = {
    val timestamp: Long = (new java.util.Date).getTime
    val clearToken: String = tokenType.id + "-" + timestamp + "-" + accountId
    SymmetricCrypt.encrypt(clearToken, Settings.ApplicationSecret, "AES")
  }
}
