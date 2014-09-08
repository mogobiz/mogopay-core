package mogopay.handlers

import java.security.MessageDigest
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import java.io.File
import java.util.{Calendar, UUID}
import com.sksamuel.elastic4s.ElasticDsl._
import mogopay.actors.AccountActor._
import mogopay.codes.MogopayConstant
import mogopay.config._
import mogopay.config.HandlersConfig._
import mogopay.config.Implicits._
import mogopay.es.EsClient
import mogopay.exceptions.Exceptions._
import mogopay.handlers.UtilHandler._
import mogopay.model.Mogopay._
import mogopay.model.Mogopay.RoleName.RoleName
import mogopay.model.Mogopay.TokenValidity.TokenValidity
import mogopay.session.Session
import mogopay.util.{RSA, JacksonConverter}
import org.apache.shiro.crypto.hash.Sha256Hash
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.TermQueryBuilder
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util._
import com.atosorigin.services.cad.common.util.FileParamReader
import com.sksamuel.elastic4s.ElasticDsl._
import mogopay.util.GlobalUtil._

import scala.util.parsing.json.JSON

class LoginException(msg: String) extends Exception(msg)

class AccountHandler {
  import Token._

  def update(account: Account) = EsClient.update(account, true, false)

  def findByEmail(email: String): Option[Account] = {
    val req = search in Settings.DB.INDEX types "Account" limit 1 from 0 filter {
      termFilter("email", email)
    }
    EsClient.search[Account](req)
  }

  def isPatternValid(pattern: String): Boolean = {
    try {
      java.util.regex.Pattern.compile(pattern)
      true
    } catch {
      case _: Throwable => false
    }
  }

  private def buildFindAccountRequest(email: String, merchantId: Option[String]): SearchDefinition = {
    if (!Settings.sharedCustomers && merchantId.nonEmpty) {
      search in Settings.DB.INDEX -> "Account" limit 1 from 0 filter {
        and(
          termFilter("email", email),
          termFilter("owner", merchantId.get)
        )
      }
    } else {
      search in Settings.DB.INDEX types "Account" filter {
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

  def login(email: String, password: String, merchantId: Option[String], isCustomer: Boolean): Try[Account] = {
    val lowerCaseEmail = email.toLowerCase
    val tryUserAccountRequest =
      if (isCustomer) {
        val merchantReq = if (merchantId.isDefined) {
          search in Settings.DB.INDEX -> "Account" limit 1 from 0 filter {
            and(
              termFilter("uuid", merchantId.get),
              missingFilter("owner") existence true includeNull true
            )
          }
        } else {
          search in Settings.DB.INDEX -> "Account" limit 1 from 0 filter {
            and(
              termFilter("email", Settings.AccountValidateMerchantDefault),
              missingFilter("owner") existence true includeNull true
            )
          }
        }

        val tryMerchant = EsClient.search[Account](merchantReq).map(Success(_)).getOrElse(Failure(new VendorNotFoundException))
        tryMerchant.flatMap { merchant =>
          val isMerchant = merchant.roles.contains(RoleName.MERCHANT)
          if (!isMerchant) {
            Failure(new InvalidMerchantAccountException)
          }
          if (merchant.status == AccountStatus.INACTIVE) {
            Failure(new InactiveMerchantException)
          } else {
            Success(search in Settings.DB.INDEX -> "Account" limit 1 from 0 filter {
              and(
                termFilter("email", lowerCaseEmail),
                termFilter("owner", merchant.uuid)
              )
            })
          }
        }
      } else {
        Success(search in Settings.DB.INDEX -> "Account" limit 1 from 0 filter {
          and(
            termFilter("email", lowerCaseEmail),
            missingFilter("owner") existence true includeNull true
          )
        })
      }

    tryUserAccountRequest.flatMap { userAccountRequest =>
      EsClient.search[Account](userAccountRequest).map { userAccount =>
        if (userAccount.loginFailedCount > MogopayConstant.MaxAttempts)
          Failure(new TooManyLoginAttemptsException)
        else if (userAccount.status == AccountStatus.INACTIVE)
          Failure(new InactiveAccountException)
        else if (userAccount.password != new Sha256Hash(password).toString) {
          EsClient.update(userAccount.copy(loginFailedCount = userAccount.loginFailedCount + 1), false, true)
          Failure(new InvalidPasswordErrorException)
        } else {
          val userAccountToIndex = userAccount.copy(loginFailedCount = 0, lastLogin = Some(Calendar.getInstance().getTime))
          EsClient.update(userAccountToIndex, false, true)
          Success(userAccountToIndex.copy(password = ""))
        }
      }.getOrElse(Failure(new AccountDoesNotExistError))
    }
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
    this.findByEmail(email).map(_.uuid).getOrElse(throw new Exception(s"unknown $email"))
  }

  /**
   * For debugging purposes. Works only for merchant.com - so no risk :)
   * @return
   */
  def secret(seller: String): String = {
    val email = seller + "@merchant.com"
    this.findByEmail(email).map(_.secret).getOrElse(throw new Exception(s"unknown $email"))
  }

  //    def generateLostPasswordToken(email: String, merchantId: Option[String]): String = {
  //      import org.joda.time.DateTime
  //      val req = merchantId.map { merchantId =>
  //          val merchEsClient.load(merchantId)
  //      } getOrElse
  //      val futureResult = merchantId match {
  //        case Some(id) => DAO.findBy[Account]("owner" -> id)
  //        case None => DAO.findBy[Account]("email" -> "null")
  //      }
  //      futureResult map { r =>
  //        r.filter(_.email == email).map { a =>
  //          val date = new DateTime().plusSeconds(Settings.Emailing.MaxAge)
  //          Success(RSA.encrypt(a.email + ";" + date.toString(), Settings.RSA.publicKey))
  //        } getOrElse Failure(new AccountDoesNotExistException)
  //      }
  //    }

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
    val req = search in Settings.DB.INDEX -> "Account" filter termFilter("secret", secret)
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
    case Some(_) => Failure(new AccountWithSameEmailAddressAlreadyExistsError)
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

    val uncryptedToken = RSA.decrypt(token, Settings.RSA.privateKey).split(";")
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

  def updatePassword(password: String, vendorId: String, accountId: String): Try[Unit] = {
    def `match`(pattern: String, password: String): Boolean = {
      if (pattern.length == 0) {
        true
      } else {
        password.matches(pattern)
      }
    }

    val account = accountHandler.load(accountId)
    val merchant = getMerchant(vendorId)

    val paymentConfig: Try[Option[PaymentConfig]] = merchant match {
      case Some(c) => Success(c.paymentConfig)
      case None => Failure(new VendorNotFoundException)
    }

    val pattern: Try[Option[String]] = paymentConfig match {
      case Success(Some(pc)) => Success(pc.passwordPattern)
      case Success(None) => Failure(new PaymentConfigNotFoundException)
      case Failure(t) => Failure(t)
    }

    val matching: Try[Boolean] = pattern match {
      case Success(Some(p)) => Success(`match`(p, password))
      case Success(None) => Failure(new PasswordPatternNotFoundException)
      case Failure(t) => Failure(t)
    }

    matching match {
      case Failure(t) => Failure(t)
      case Success(false) => Failure(new PasswordDoesNotMatchPatternException)
      case Success(true) => account match {
        case None => Failure(new AccountDoesNotExistError)
        case Some(acc) =>
          update(acc.copy(password = new Sha256Hash(password).toHex))
          Success()
      }
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
  def generateAndSendPincode3(uuid: String): Try[Unit] = accountHandler.load(uuid) map { acc =>
    val phoneNumber: Try[Telephone] =
      (for {
        addr <- acc.address
        tel <- addr.telephone
      } yield Success(tel)) getOrElse Failure(new NoPhoneNumberFoundException)

    phoneNumber match {
      case Failure(t) => Failure(t)
      case Success(n) =>
        val plainTextPinCode = UtilHandler.generatePincode3()
        val md = MessageDigest.getInstance("SHA-256")
        md.update(plainTextPinCode.getBytes("UTF-8"))
        val pinCode3 = new String(md.digest(), "UTF-8")

        val newTelephone = n.copy(status = TelephoneStatus.WAITING_ENROLLMENT,
          pinCode3 = Some(pinCode3))
        EsClient.index(acc.copy(address = acc.address.map(_.copy(telephone = Option(newTelephone)))))

        def message = "Your 3 digits code is: " + plainTextPinCode
        smsHandler.sendSms(message, n.phone)
    }
  } getOrElse Failure(new AccountDoesNotExistError)

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

    import com.ebiznext.utils._
    import mogopay.handlers.EmailHandler._

    object EmailType extends Enumeration {
      type EmailType = Value
      val Signup = Value(0)
      val BypassLogin = Value(1)
    }

    import EmailType._

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

    def confirmSignup(token: String): Try[Boolean] = {
      if (!token.contains("-")) {
        Failure(new Exception("Invalid token."))
      } else Try {
        val splitToken = RSA.decrypt(token, Settings.RSA.privateKey).split("-")
        val timestamp = splitToken(1).toLong
        val accountId = splitToken(2)

        if (EmailType(Integer.parseInt(splitToken(0))) != EmailType.Signup) {
          false
        } else {
          val signupDate = new org.joda.time.DateTime(timestamp).getMillis
          val currentDate = new org.joda.time.DateTime().getMillis
          if (currentDate - signupDate > Settings.Emailing.MaxAge * 1000) {
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
      val splitToken = RSA.decrypt(token, Settings.RSA.privateKey).split("-")
      val timestamp = splitToken(1).toLong
      val accountId = splitToken(2).toLong

      if (EmailType(Integer.parseInt(splitToken(0))) != EmailType.BypassLogin) {
        None
      } else {
        val signupDate = new org.joda.time.DateTime(timestamp).getMillis
        val currentDate = new org.joda.time.DateTime().getMillis
        if (currentDate - signupDate > Settings.Emailing.MaxAge * 1000) {
          None
        } else {
          Some(session += "accountId" -> accountId)
        }
      }
    }
  }

  def generateNewSecret(accountId: String): Option[String] = load(accountId).map { acc =>
    val secret = UUID.randomUUID().toString
    update(acc.copy(secret = secret))
    secret
  }

  def addCreditCard(accountId: String, ccId: Option[String], holder: String,
                    number: String, expiryDate: String, ccType: String): Try[CreditCard] = ccId match {
    case None => createCard(accountId, holder, number, expiryDate, ccType)
    case Some(cardId) => updateCard(accountId, cardId, holder, number, expiryDate, ccType)
  }

  private def updateCard(accountId: String, ccId: String, holder: String,
                         cardNumber: String, expiryDate: String, ccType: String): Try[CreditCard] = {
    val account: Try[Account] = load(accountId) map (Success(_)) getOrElse Failure(new AccountDoesNotExistError)

    val filteredAccount = account match {
      case Failure(t) => Failure(t)
      case Success(a) =>
        val card = a.creditCards.find(_.uuid == ccId)
        if (card.nonEmpty) Success((a, card.get))
        else Failure(new CreditCardDoesNotExistException)
    }

    val numbers: Try[(Account, CreditCard, String, String)] = filteredAccount match {
      case Failure(t) => Failure(t)
      case Success((account, card)) => if (!UtilHandler.checkLuhn(cardNumber)) {
        Failure(new InvalidCardNumberException)
      } else {
        Success((account,
          card,
          UtilHandler.hideCardNumber(cardNumber, "X"),
          RSA.encrypt(cardNumber, Settings.RSA.publicKey)))
      }
    }

    numbers match {
      case Failure(t) => Failure(t)
      case Success((account, card, hiddenN, n)) =>
        val newCard = card.copy(number = n,
          holder = holder,
          expiryDate = new Timestamp(new SimpleDateFormat("yyyy-MM").parse(expiryDate).getTime),
          cardType = CreditCardType.withName(ccType),
          hiddenNumber = hiddenN)
        val newCards = account.creditCards.filter(_.uuid != ccId) :+ newCard
        EsClient.index(account.copy(creditCards = newCards))
        Success(newCard)
    }
  }

  private def createCard(accountId: String, holder: String, number: String,
                         expiryDate: String, ccType: String): Try[CreditCard] = {
    val account = load(accountId) map (Success(_)) getOrElse Failure(new AccountDoesNotExistError)

    val numbers = account match {
      case Failure(t) => Failure(t)
      case Success(account) => if (!UtilHandler.checkLuhn(number)) {
        Failure(new InvalidCardNumberException)
      } else {
        Success(account,
          UtilHandler.hideCardNumber(number, "X"),
          RSA.encrypt(number, Settings.RSA.publicKey))
      }
    }

    val withParsedExpiryDate = numbers match {
      case Failure(t) => Failure(t)
      case Success((account, hiddenN, n)) => Try((
        new Timestamp(new SimpleDateFormat("yyyy-MM").parse(expiryDate).getTime),
        account,
        hiddenN,
        n))
    }

    withParsedExpiryDate match {
      case Failure(t) => Failure(t)
      case Success((expiryDate, account, hiddenN, n)) =>
        val newCard = CreditCard(uuid = java.util.UUID.randomUUID().toString,
          number = n,
          holder = holder,
          expiryDate = expiryDate,
          cardType = CreditCardType.withName(ccType),
          account = accountId,
          hiddenNumber = hiddenN)
        val newCards = account.creditCards :+ newCard
        EsClient.index(account.copy(creditCards = newCards))
        Success(newCard)
    }
  }

  def getBillingAddress(accountId: String): Option[AccountAddress] = load(accountId).map(_.address).flatten

  def getShippingAddresses(accountId: String): Seq[ShippingAddress] =
    load(accountId) map (_.shippingAddresses) getOrElse Nil

  def getShippingAddress(accountId: String): Option[ShippingAddress] =
    getShippingAddresses(accountId: String) find (_.active)

  def assignBillingAddress(accountId: String, address: AddressToAssignFromGetParams) {
    load(accountId).map { account =>
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
    }
  }

  def deleteShippingAddress(accountId: String, addressId: String): Option[Unit] =
    for {
      account <- load(accountId)
      address <- account.shippingAddresses.find(_.uuid == addressId)
    } yield {
      val newAddresses = account.shippingAddresses diff List(address)
      val newAccount   = account.copy(shippingAddresses = newAddresses)
      update(newAccount)
    }

  def addShippingAddress(accountId: String, address: AddressToAddFromGetParams): Option[ShippingAddress] =
    load(accountId).map { account =>
      val shippAddr = ShippingAddress(java.util.UUID.randomUUID().toString, active = true, address.getAddress)
      val newAddrs = account.shippingAddresses.map(_.copy(active = false)) :+ shippAddr
      EsClient.index(account.copy(shippingAddresses = newAddrs))

      shippAddr
    }

  def updateShippingAddress(accountId: String, address: AddressToUpdateFromGetParams): Option[Unit] =
    load(accountId).map { account =>
      account.shippingAddresses.find(_.uuid == address.id) map { addr =>
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
        ()
      }
    }.flatten

  type ActiveCountryState = Map[Symbol, Option[String]]

  def getActiveCountryState(accountId: String): Option[ActiveCountryState] = {
    load(accountId).map { account =>
      val addr = account.shippingAddresses.find(_.active).map(_.address).orElse(account.address)
      addr.map { addr =>
        Map('countryCode -> addr.country, 'stateCode -> addr.admin1)
      }
    }.flatten
  }

  def selectShippingAddress(accountId: String, addrId: String) =
    load(accountId).map { account =>
      val newAddrs = account.shippingAddresses.map { addr => addr.copy(active = addr.uuid == addrId)}
      EsClient.index(account.copy(shippingAddresses = newAddrs))
    }

  def profileInfo(accountId: String): Option[Future[Map[Symbol, Any]]] = load(accountId).map { account =>
    val cards = Future(creditCardHandler.findByAccount(accountId))
    val countries = Future(countryHandler.findCountriesForBilling())

    Future.sequence(List(cards, countries)).map { case seq =>
      val cards = seq(0)
      val countries = seq(1)

      val paymentConfig = account.paymentConfig
      val paypalParam = paymentConfig.map(_.paypalParam).flatten
      val buysterParam = paymentConfig.map(_.buysterParam).flatten
      val kwixoParam = paymentConfig.map(_.kwixoParam).flatten

      val basePaymentProviderParam: Option[Map[String, String]] = paymentConfig.map(_.cbParam).flatten
        .map(JSON.parseFull).flatten
        .map(_.asInstanceOf[Map[String, String]])
      val paymentProviderParam = basePaymentProviderParam.map {
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
        'paymentProviderParam -> paymentProviderParam,
        'paypalParam -> paypalParam.map(JSON.parseFull).flatten,
        'buysterParam -> buysterParam.map(JSON.parseFull).flatten,
        'kwixoParam -> kwixoParam.map(JSON.parseFull).flatten,
        'emailField -> paymentConfig.map(_.emailField),
        'passwordField -> paymentConfig.map(_.passwordField),
        'callbackPrefix -> paymentConfig.map(_.callbackPrefix),
        'passwordPattern -> paymentConfig.map(_.passwordPattern))
    }
  }

  /*
  def saveProfileInfo(accountId: String,
                      fields: JObject,
                      httpSession: Session): Either[Try[Unit], Session] = {
    val profile: Profile = (fields \ "user").extract[Profile]

    var updatedHTTPSession: Session = null

    val isMerchant = RoleName.withName(profile.userType) == RoleName.MERCHANT
    val exists: Boolean = profile.id != None
    if (!exists && !profile.email.isEmpty) {
      return Left(Failure(new RuntimeException("error.email.required")))
    } else if (isMerchant && !profile.email.isEmpty) {
      val merchantConstraints: Seq[String] = Settings.AccountValidateMerchantEmails.split(",").map(_.trim)
      val found: Boolean = merchantConstraints.exists {
        c =>
          c.equals(profile.email) || (c.startsWith("@") && profile.email.contains(c))
      }
      if (!found && merchantConstraints.size > 0) {
        return Left(Failure(new UserEmailNotAllowedAsMerchantException))
      }
    }

    if (!isMerchant) {
      val merchant: Option[Account] =
        accountHandler.findByEmail(Settings.AccountValidateMerchantDefault).filter(_.owner.isEmpty)
      if (merchant != None) updatedHTTPSession = httpSession += "vendorId" -> merchant.get.uuid
    } else if (profile.company == None || profile.website == None) {
      return Left(Failure(new LackingInfoForMerchantException(
        "Company name & website are required for a merchant profile")))
    }

    if (!exists) {
      Failure(new AccountDoesNotExistException)
    } else {
      try {
        val paymentConfig: PaymentConfig = PaymentConfig(
          kwixoParam = Some(compact(render(profile.kwixoParam))),
          buysterParam = Some(compact(render(profile.buysterParam))),
          paypalParam = Some(compact(render(profile.paypalParam))),
          cbParam = null,
          cbProvider = null,
          paymentMethod = profile.paymentConfig.fold(CBPaymentMethod.THREEDS_NO)
            (pc => CBPaymentMethod.withName(pc.paymentMethod)),
          emailField = profile.paymentConfig.map(_.emailField) getOrElse "user_email",
          passwordField = profile.paymentConfig.map(_.passwordField) getOrElse "user_password",
          pwdEmailContent = profile.paymentConfig.map(_.pwdEmailContent).flatten,
          pwdEmailSubject = profile.paymentConfig.map(_.pwdEmailSubject).flatten,
          callbackPrefix = profile.paymentConfig.map(_.callbackPrefix).flatten,
          passwordPattern = profile.paymentConfig.map(_.passwordPattern).flatten)
        val roles = Seq(if (isMerchant) RoleName.MERCHANT else RoleName.CUSTOMER)

        val sessionMerchant: Option[Account] =
          if (httpSession.sessionData.isMerchant) {
            httpSession.sessionData.vendorId.map(uuid => {
              EsClient.load[Account](uuid)
            }).flatten
          }
          else
            None
        //accountHandler.saveProfile(profile, paymentConfig, roles, sessionMerchant)

        profile.paymentConfig.map(_.cbProvider).map {
          cbProvider: String =>
            if (CBPaymentProvider.withName(cbProvider) == CBPaymentProvider.SIPS) {
              val paymentProviderParam = fields \ "paymentProviderParam"

              val certificateData: Option[String] =
                (fields \ "paymentProviderParamEx" \ "sipsMerchantCertificateData").extractOpt[String]
              val certificateFile: Option[String] =
                (paymentProviderParam \ "sipsMerchantCertificateFile").extractOpt[String]

              val dir = new File(Settings.SipsCertifDir, profile.id.map(_.toString).getOrElse(""))
              dir.mkdirs
              val certificateTargetFile: File = new File(dir,
                "certif.%s.%s".format(
                  (paymentProviderParam \ "sipsMerchantCountry").extractOpt[String].get,
                  (paymentProviderParam \ "sipsMerchantId").extractOpt[String].get))

              if (certificateData != None && certificateFile != None) {
                certificateTargetFile.delete
                writeToFile(certificateTargetFile) {
                  p => p.print(certificateData.get.trim)
                }
              }

              val parcomData = (fields \ "paymentProviderParamEx" \ "sipsMerchantParcomData").extractOpt[String]
              val parcomFile = (paymentProviderParam \ "sipsMerchantParcomFile").extractOpt[String]
              val parcomTargetFile = new File(dir,
                "parcom." + (paymentProviderParam \ "sipsMerchantId").extractOpt[String].getOrElse(""))
              if (parcomData != None && parcomFile != None) {
                parcomTargetFile.delete
                writeToFile(parcomTargetFile) {
                  p => p.print(parcomData.get.trim)
                }
              }
              val targetFile = new File(dir, "pathfile")
              var isJSP = false
              if (certificateData != None) {
                isJSP = certificateData.get.indexOf("!jsp!") > 0
              } else if (targetFile.exists()) {
                val paramReader = new FileParamReader(targetFile.getAbsolutePath)
                isJSP = paramReader.getParam("F_CTYPE") == "jsp"
              }
              targetFile.delete

              writeToFile(targetFile) {
                p =>
                  p.print("D_LOGO!" + Settings.MogopayEndPoint + "images/sips/logo/!\n")
                  p.print("F_DEFAULT!" + Settings.SipsCertifDir + "parmcom.defaut!\n")
                  p.print("F_PARAM!" + new File(dir, "parcom").getAbsolutePath + "!\n")
                  p.print("F_CERTIFICATE!" + new File(dir, "certif").getAbsolutePath + "!\n")
                  if (isJSP) p.print("F_CTYPE!jsp!\n")
              }
              if (isJSP)
                certificateTargetFile.renameTo(new File(certificateTargetFile.getAbsolutePath + ".jsp"))
            }
        }
      } catch {
        case t: Throwable => Left(Failure(t))
      }
    }
    if (updatedHTTPSession == null) Left(Success(()))
    else Right(updatedHTTPSession)
  }
  */

  /*
  def saveProfile(profile: Profile, paymentConfig: PaymentConfig,
                  roles: Seq[RoleName], vendor: Option[Account]) = {
    def update(): Try[Unit] = {
      val uuid: Try[String] = profile.id match {
        case None => Failure(new NoAccountIdProvidedException)
        case Some(x) => Success(x)
      }

      val account: Try[Account] = uuid match {
        case Failure(t) => Failure(t)
        case Success(uuid) => accountHandler.load(uuid) match {
          case None => Failure(new AccountDoesNotExistError)
          case Some(x) => Success(x)
        }
      }

      val save: Try[(Account, Account)] = account match {
        case Failure(t) => Failure(t)
        case Success(acc) =>
          val isCustomer = acc.roles.contains(RoleName.CUSTOMER)

          if (isCustomer && vendor == None) {
            Failure(new VendorNotProvidedError)
          } else {
            val newPassword: String = profile.password.fold(acc.password)(pwd =>
              if (true) new Sha256Hash(pwd).toHex else acc.password
            )

            val newAddress: Option[AccountAddress] = profile.billingAddress.map { addr =>
              val tel = profile.lphone.map { lphone =>
                val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
                val phoneNumber = phoneUtil.parse(lphone, profile.billingAddress.map(_.country).flatten.getOrElse(""))
                Telephone(phone = phoneUtil.format(phoneNumber, PhoneNumberFormat.INTERNATIONAL),
                  lphone = phoneUtil.format(phoneNumber, PhoneNumberFormat.NATIONAL),
                  isoCode = profile.billingAddress.map(_.country).flatten.getOrElse(""),
                  pinCode3 = None,
                  status = TelephoneStatus.WAITING_ENROLLMENT)
              }
              addr.copy(telephone = tel)
            } orElse acc.address

            val updatedAccount = acc.copy(
              email = profile.email,
              company = profile.company,
              website = profile.website,
              password = newPassword,
              civility = profile.civility.map(c => Civility.withName(c)),
              firstName = profile.firstName,
              lastName = profile.lastName,
              birthDate = profile.birthDate.map(bd => buildDate(bd, "yyyy-MM-dd")),
              address = newAddress,
              paymentConfig = Some(paymentConfig),
              roles = roles.to[List])

            accountHandler.save(updatedAccount)

            Success(acc, updatedAccount)
          }
      }

      save match {
        case Failure(t) => Failure(t)
        case Success((oldAcc, acc)) =>
          val validateMerchantPhone: Boolean = false
          val validateCustomerPhone: Boolean = false

          if ((profile.isMerchant && validateMerchantPhone) || (!profile.isMerchant && validateCustomerPhone)) {
            val lphone: Option[String] = acc.address.map(_.telephone.map(_.lphone)).flatten
            val oldLPhone: Option[String] = oldAcc.address.map(_.telephone.map(_.lphone)).flatten
            if (acc.address.map(_.telephone).flatten != None && (oldLPhone == None || oldLPhone != lphone)) {
              generateAndSendPincode3(acc.uuid)
            }
          }

          Success(())
      }
    }
  }
  */

  def find(uuid: String) = EsClient.load[Account](uuid)

  def updateProfile(profile: UpdateProfile): Try[Unit] = {
    if (!profile.isMerchant && profile.vendor.isEmpty) Failure(new VendorNotProvidedError)
    else find(profile.id) match {
      case None => Failure(new AccountAddressDoesNotExistException)
      case Some(account) =>
        lazy val address = account.address.map { address =>
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

        lazy val civility = Try(Civility.withName(profile.civility))
          .orElse(Failure(new Exception("Incorrect civility.")))
        lazy val birthDate = Some(new SimpleDateFormat("yyyy-MM-dd").parse(profile.birthDate))

        lazy val password = profile.password.map { case (p1, p2) =>
          if (p1 != p2) Failure(new PasswordsDontMatchError)
          else          Success(new Sha256Hash(p1).toHex)
        } getOrElse Success(account.password)

        (for {
          c <- civility
          p <- password
        } yield (c, p)) map { case (civility, password) =>
          val newAccount = account.copy(
            password  = password,
            company   = Some(profile.company),
            website   = Some(profile.website),
            civility  = Some(civility),
            firstName = Some(profile.firstName),
            lastName  = Some(profile.lastName),
            birthDate = birthDate,
            address   = address
          )

          val validateMerchantPhone: Boolean = false // From the Groovy version
          val validateCustomerPhone: Boolean = false // …

          if ((profile.isMerchant && validateMerchantPhone) || (!profile.isMerchant && validateCustomerPhone)) {
            val lphone: Option[String] = newAccount.address.map(_.telephone.map(_.lphone)).flatten
            val oldLPhone: Option[String] = account.address.map(_.telephone.map(_.lphone)).flatten
            if (newAccount.address.map(_.telephone).flatten != None && (oldLPhone == None || oldLPhone != lphone)) {
              generateAndSendPincode3(newAccount.uuid)
            }
          }

          update(newAccount)
        }
    }
  }

  def recycle() {
    val req = select in Settings.DB.INDEX -> "Account" query {
      term("status", AccountStatus.WAITING_ENROLLMENT)
      range("waitingEmailSince") from 0 to (System.currentTimeMillis() - Settings.RecycleAccountDuration)
    } filter {
      termFilter("status", AccountStatus.WAITING_ENROLLMENT)
    }
    EsClient.searchAllRaw(req) map (_.getId) foreach (EsClient.delete[Account](_, false))
  }

  def enroll(accountId: String, lPhone: String, pinCode: String): Try[Unit] = {
    load(accountId).map { user =>
      if (user.address.map(_.telephone.map(_.lphone)).flatten.nonEmpty &&
        user.address.map(_.telephone.map(_.lphone)).flatten != Some(lPhone)) {
        val newTel = user.address.get.telephone.get.copy(lphone = lPhone)
        val newAddr = user.address.get.copy(telephone = Some(newTel))
        val newUser = user.copy(address = Some(newAddr))
        EsClient.index(newUser)
        Success(())
      } else {
        load(accountId).map { account =>
          val encryptedCode = new Sha256Hash(pinCode).toHex
          if (account.address.map(_.telephone.map(_.pinCode3)).flatten == Some(encryptedCode) &&
            account.address.map(_.telephone.map(_.status)).flatten == Some(TelephoneStatus.WAITING_ENROLLMENT)) {
            val newTel = Telephone("", lPhone, "", None, TelephoneStatus.ACTIVE)
            EsClient.index(account.copy(address = account.address.map(_.copy(telephone = Some(newTel)))))
            Success(())
          } else {
            Failure(new MogopayError(MogopayConstant.InvalidPhonePincode3))
          }
        }.getOrElse(Failure(new AccountDoesNotExistError))
      }
    } getOrElse Failure(new AccountDoesNotExistError)
  }

  def signup(signup: Signup): Try[(Token, Account)] = {
    if (!signup.isMerchant && signup.vendor.isEmpty) {
      Failure(new VendorNotProvidedError)
    } else {
      lazy val birthdate = Try(new SimpleDateFormat("yyyy-MM-dd").parse(signup.birthDate))

      lazy val civility = Try(Civility.withName(signup.civility)).orElse(Failure(new Exception("Incorrect civility.")))

      lazy val password =
        if (signup.password.isEmpty)
          Failure(new NoPasswordProvidedError)
        else if (signup.password != signup.password2)
          Failure(new PasswordsDontMatchError)
        else
          Success(new Sha256Hash(signup.password).toHex)

      lazy val country: Try[Country] = Try {
        val countryCode = signup.address.country.getOrElse(throw new Exception("Country not found."))
        countryHandler.findByCode(countryCode) map (Success(_)) getOrElse Failure(new CountryDoesNotExistException)
      }.flatten

      def address(country: Country): Try[AccountAddress] = Try {
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

      lazy val accountId = newUUID

      lazy val accountStatus = if ((signup.isMerchant && Settings.AccountValidateMerchantEmail) ||
        (!signup.isMerchant && Settings.AccountValidateCustomerEmail)) {
        AccountStatus.WAITING_ENROLLMENT
      } else {
        AccountStatus.ACTIVE
      }

      def token(accountStatus: AccountStatus.AccountStatus) = if (accountStatus == AccountStatus.ACTIVE) {
        Success("")
      } else {
        Token.generateAndSaveToken(accountId, TokenType.Signup)
          .fold(Failure(new AccountAddressDoesNotExistException): Try[Token])(t => Success(t))
      }

      lazy val account = for {
        b    <- birthdate
        civ  <- civility
        p    <- password
        coun <- country
        addr <- address(coun)
      } yield Account(
          uuid = accountId,
          email = signup.email,
          password = p,
          civility = Some(civ),
          firstName = Some(signup.firstName),
          lastName = Some(signup.lastName),
          birthDate = Some(b),
          status = accountStatus,
          secret = if (signup.isMerchant) newUUID else "",
          owner = if (signup.isMerchant) None else signup.vendor,
          address = Some(addr),
          waitingPhoneSince = System.currentTimeMillis(),
          waitingEmailSince = System.currentTimeMillis(),
          country = Some(coun),
          roles = List(if (signup.isMerchant) RoleName.MERCHANT else RoleName.CUSTOMER)
        )

      for {
        a <- account
        _ <- save(a)
        _ <- generateAndSendPincode3(a.uuid)
        t <- token(accountStatus)
      } yield (t, a)
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
  import TokenType._

  def generateAndSaveToken(accountId: String, tokenType: TokenType): Option[String] = {
    val timestamp: Long = (new java.util.Date).getTime
    val clearToken: String = tokenType.id + "-" + timestamp + "-" + accountId
    val token = RSA.encrypt(clearToken, Settings.RSA.publicKey)
    accountHandler.find(accountId).map { account =>
      accountHandler.save(account.copy(emailingToken = Some(token)))
      token
    }
  }
}

case class PaymentConfigForProfile(cbParam: Option[String],
                                   cbProvider: String,
                                   paymentMethod: String,
                                   emailField: String = "user_email",
                                   passwordField: String = "user_password",
                                   pwdEmailContent: Option[String],
                                   pwdEmailSubject: Option[String],
                                   callbackPrefix: Option[String],
                                   passwordPattern: Option[String])

case class PaymentProviderParam(paylineAccount: String,
                                paylineKey: String,
                                paylineContract: String,
                                paylineCustomPaymentPageCode: String,
                                paylineCustomPaymentTemplateURL: String,
                                payboxContract: String,
                                payboxSite: String,
                                payboxKey: String,
                                payboxRank: String,
                                payboxMerchantId: String,
                                sipsMerchantId: String,
                                sipsMerchantCountry: String,
                                sipsMerchantCertificateFile: String,
                                sipsMerchantParcomFile: String,
                                sipsMerchantLogoPath: String,
                                systempayShopId: String,
                                systempayContractNumber: String,
                                systempayCertificate: String)

case class PaymentProviderParamEx(sipsMerchantCertificateFile: String,
                                  sipsMerchantCertificateData: String,
                                  sipsMerchantParcomFile: String,
                                  sipsMerchantParcomData: String)

case class PaypalProviderParam(paypaluser: String,
                               paypalPassword: String,
                               paypalSignature: String)

case class BuysterProviderParam(buysteruser: String,
                                buysterPassword: String,
                                buysterSignature: String)

case class KwixoProviderParam(kwixoParam: String)


object AccountHandler extends AccountHandler