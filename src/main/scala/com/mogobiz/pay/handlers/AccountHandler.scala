package com.mogobiz.pay.handlers

import java.io.File
import java.net.URLEncoder
import java.security.MessageDigest
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.{Calendar, UUID}

import com.atosorigin.services.cad.common.util.FileParamReader
import com.mogobiz.pay.handlers.EmailHandler.Mail
import com.mogobiz.pay.handlers.EmailType.EmailType
import com.mogobiz.pay.sql.BOAccountDAO
import com.sksamuel.elastic4s.ElasticDsl._
import com.mogobiz.pay.codes.MogopayConstant
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.es.EsClient
import com.mogobiz.pay.exceptions.Exceptions._
import com.mogobiz.pay.handlers.Token.{Token, TokenType}
import com.mogobiz.pay.handlers.Token.TokenType.TokenType
import com.mogobiz.pay.settings.Settings
import com.mogobiz.utils.GlobalUtil._
import com.mogobiz.pay.model.Mogopay.TokenValidity.TokenValidity
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.utils.SymmetricCrypt
import org.apache.shiro.crypto.hash.Sha256Hash
import org.json4s.jackson.Serialization.write
import org.json4s.jackson.Serialization.read

import scala.util._
import scala.util.control.NonFatal
import scala.util.parsing.json.JSON

class LoginException(msg: String) extends Exception(msg)

case class DoesAccountExistByEmail(email: String, merchantId: Option[String])

case class IsPatternValid(pattern: String)

case class RequestPasswordChange(email: String, merchantId: String,
                                 passwordCB: String, isCustomer: Boolean)


case class SelectShippingAddress(accountId: String, addressId: String)

case class UpdatePassword(password: String, vendorId: String, accountId: String)

case class Verify(email: String, merchantSecret: String, mogopayToken: String)

case class Login(email: String, password: String, merchantId: Option[String], isCustomer: Boolean)

case class GenerateAndSendPincode3(accountId: String)

case class SendSignupConfirmationEmail(accountId: String)

case class ConfirmSignup(token: String)

case class GenerateNewSecret(accountId: String)

case class AddCreditCard(accountId: String, ccId: Option[String], holder: String,
                         number: Option[String], expiry: String, ccType: String)

case class DeleteCreditCard(accountId: String, cardId: String)

case class GetBillingAddress(accountId: String)

case class GetShippingAddresses(accountId: String)

case class GetShippingAddress(accountId: String)

case class AddressToUpdateFromGetParams(id: String, road: String,
                                        city: String, road2: Option[String],
                                        zipCode: String, extra: String,
                                        civility: String, firstName: String,
                                        lastName: String, country: String,
                                        admin1: String, admin2: String, lphone: String)

case class AddressToAddFromGetParams(road: String, city: String, road2: Option[String],
                                     zipCode: String, extra: Option[String],
                                     civility: String, firstName: String,
                                     lastName: String, country: String,
                                     admin1: String, admin2: String, lphone: String) {
  def getAddress = {
    val telephone = telephoneHandler.buildTelephone(lphone, country, TelephoneStatus.WAITING_ENROLLMENT)
    AccountAddress(road, road2, city, Option(zipCode), extra, Option(Civility.withName(civility)), Option(firstName),
      Option(lastName), Option(telephone), Option(country), Option(admin1), Option(admin2))
  }
}

case class AddressToAssignFromGetParams(road: String, city: String,
                                        road2: Option[String], zipCode: String,
                                        extra: Option[String], civility: String,
                                        firstName: String, lastName: String,
                                        country: String, admin1: String,
                                        admin2: String, lphone: String) {
  def getAddress = {
    val c = Civility.withName(civility)
    AccountAddress(road, road2, city, Some(zipCode), extra, Some(c), Some(firstName),
      Some(lastName), None, Some(country), Some(admin1), Some(admin2))
  }
}

case class AssignBillingAddress(accountId: String, address: AddressToAssignFromGetParams)

case class AddShippingAddress(accountId: String, address: AddressToAddFromGetParams)

case class DeleteShippingAddress(accountId: String, addressId: String)

case class UpdateShippingAddress(accountId: String, address: AddressToUpdateFromGetParams)

case class GetActiveCountryState(accountId: String)

case class ProfileInfo(accountId: String)

case class MerchantComId(seller: String)

case class MerchantComSecret(seller: String)

case class Enroll(accountId: String, lPhone: String, pinCode: String)

case class Signup(email: String, password: String, password2: String,
                  lphone: String, civility: String, firstName: String,
                  lastName: String, birthDate: String, address: AccountAddress,
                  isMerchant: Boolean, vendor: Option[String], company: Option[String],
                  website: Option[String], validationUrl: String)

case class UpdateProfile(id: String, password: Option[(String, String)],
                         company: String, website: String, lphone: String, civility: String,
                         firstName: String, lastName: String, birthDate: String,
                         billingAddress: AccountAddress, vendor: Option[String], isMerchant: Boolean,
                         emailField: String, passwordField: String,
                         passwordSubject: Option[String], passwordContent: Option[String],
                         passwordPattern: Option[String], callbackPrefix: Option[String],
                         paymentMethod: String, cbProvider: String, cbParam: CBParams,
                         payPalParam: PayPalParam, kwixoParam: KwixoParam)

case class UpdateProfileLight(id: String, password: String, password2: String, civility: String,
                              firstName: String, lastName: String, birthDate: String)

sealed trait CBParams

case class NoCBParams() extends CBParams

case class PayPalParam(paypalUser: Option[String], paypalPassword: Option[String], paypalSignature: Option[String]) extends CBParams

case class KwixoParam(kwixoParams: Option[String]) extends CBParams

case class PaylineParams(paylineAccount: String, paylineKey: String, paylineContract: String,
                         paylineCustomPaymentPageCode: String, paylineCustomPaymentTemplateURL: String) extends CBParams

case class PayboxParams(payboxSite: String, payboxKey: String, payboxRank: String, payboxMerchantId: String) extends CBParams

case class SIPSParams(sipsMerchantId: String, sipsMerchantCountry: String,
                      sipsMerchantCertificateFileName: Option[String], sipsMerchantCertificateFileContent: Option[String],
                      sipsMerchantParcomFileName: Option[String], sipsMerchantParcomFileContent: Option[String],
                      sipsMerchantLogoPath: String) extends CBParams

case class SystempayParams(systempayShopId: String, systempayContractNumber: String, systempayCertificate: String) extends CBParams

case class SendNewPassword(accountId: String, returnURL: String)

class AccountHandler {
  implicit val formats = new org.json4s.DefaultFormats {}

  lazy val birthDayDateFormat = new SimpleDateFormat("yyyy-MM-dd")

  def findByEmail(email: String): Option[Account] = {
    val req = search in Settings.Mogopay.EsIndex types "Account" limit 1 from 0 filter {
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
      search in Settings.Mogopay.EsIndex -> "Account" limit 1 from 0 filter {
        and(
          termFilter("email", email),
          termFilter("owner", merchantId.get)
        )
      }
    } else {
      search in Settings.Mogopay.EsIndex types "Account" filter {
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

  def login(email: String, password: String, merchantId: Option[String], isCustomer: Boolean): Account = {
    val lowerCaseEmail = email.toLowerCase
    val userAccountRequest =
      if (isCustomer) {
        val merchantReq = if (merchantId.isDefined) {
          search in Settings.Mogopay.EsIndex -> "Account" limit 1 from 0 filter {
            and(
              termFilter("uuid", merchantId.get),
              missingFilter("owner") existence true includeNull true
            )
          }
        } else {
          search in Settings.Mogopay.EsIndex -> "Account" limit 1 from 0 filter {
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

        search in Settings.Mogopay.EsIndex -> "Account" limit 1 from 0 filter {
          and(
            termFilter("email", lowerCaseEmail),
            termFilter("owner", merchant.uuid)
          )
        }
      } else {
        search in Settings.Mogopay.EsIndex -> "Account" limit 1 from 0 filter {
          and(
            termFilter("email", lowerCaseEmail),
            missingFilter("owner") existence true includeNull true
          )
        }
      }

    EsClient.search[Account](userAccountRequest).map { userAccount =>
      if (userAccount.loginFailedCount > MogopayConstant.MaxAttempts)
        throw TooManyLoginAttemptsException(s"${userAccount.email}")
      else if (userAccount.status == AccountStatus.WAITING_ENROLLMENT)
        throw AccountNotConfirmedException(s"${userAccount.email}")
      else if (userAccount.status == AccountStatus.INACTIVE)
        throw InactiveAccountException(s"${userAccount.email}")
      else if (userAccount.password != new Sha256Hash(password).toString) {
        accountHandler.update(userAccount.copy(loginFailedCount = userAccount.loginFailedCount + 1), true)
        throw InvalidPasswordErrorException(s"${userAccount.email}")
      } else {
        val userAccountToIndex = userAccount.copy(loginFailedCount = 0, lastLogin = Some(Calendar.getInstance().getTime))
        accountHandler.update(userAccountToIndex, true)
        userAccountToIndex.copy(password = "")
      }
    }.getOrElse(throw AccountDoesNotExistException(""))
  }

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

  def findBySecret(secret: String): Option[Account] = {
    val req = search in Settings.Mogopay.EsIndex -> "Account" filter termFilter("secret", secret)
    EsClient.search[Account](req)
  }

  def save(account: Account, refresh: Boolean = true): Try[Unit] = findByEmail(account.email) match {
    case Some(_) => Failure(AccountWithSameEmailAddressAlreadyExistsError(s"${account.email}"))
    case None =>
      BOAccountDAO.upsert(account)
      EsClient.index(Settings.Mogopay.EsIndex, account, refresh)
      Success()
  }

  def update(account: Account, refresh: Boolean): Boolean = {
    BOAccountDAO.update(account)
    EsClient.update[Account](Settings.Mogopay.EsIndex, account, upsert = false, refresh = refresh)
  }

  def getMerchant(merchantId: String): Option[Account] = {
    val merchant = accountHandler.find(merchantId)
    merchant flatMap { merchant =>
      if (merchant.owner.isEmpty) Some(merchant) else None
    }
  }

  type UserInfo = Option[Map[Symbol, Option[String]]]

  def checkTokenValidity(token: String): (TokenValidity, UserInfo) = {
    import org.joda.time._

    val uncryptedToken = SymmetricCrypt.decrypt(token, Settings.Mogopay.Secret, "AES").split(";")
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

    val account = accountHandler.find(accountId).getOrElse(throw AccountDoesNotExistException(""))
    val merchant = getMerchant(vendorId).getOrElse(throw VendorNotFoundException(s"$vendorId"))
    val paymentConfig = merchant.paymentConfig.getOrElse(throw PaymentConfigNotFoundException(""))
    val pattern = paymentConfig.passwordPattern.getOrElse(throw PasswordPatternNotFoundException(""))
    val matching: Boolean = `match`(pattern, password)

    if (!matching) throw PasswordDoesNotMatchPatternException("")
    accountHandler.update(account.copy(password = new Sha256Hash(password).toHex), false)
  }

  def sendNewPassword(accountId: String, returnURL: String): Unit = find(accountId).map { account =>
    val newPassword: String = newUUID.split("-")(4)
    update(account.copy(password = new Sha256Hash(newPassword).toHex), refresh = true)
    sendNewPasswordEmail(account, returnURL, newPassword)

    def sendNewPasswordEmail(account: Account, returnURL: String, newPassword: String) = {
      val template = scala.io.Source.fromInputStream(this.getClass.getResourceAsStream("/template/new-password.mustache")).mkString

      val data = s"""{"url": "$returnURL", "newPassword": "$newPassword"}"""
      EmailHandler.Send.to(
        Mail(
          from = ("contact@mogopay.com", "Mogopay"),
          to = Seq(account.email),
          subject = "Your new password",
          message = templateHandler.mustache(template, data)))
    }
  }

  def isMerchant(account: Account) = account.roles.contains(RoleName.MERCHANT)

  /**
   * Generates, saves and sends a pin code
   */
  def generateAndSendPincode3(uuid: String): Unit = {
    val acc = accountHandler.find(uuid) getOrElse (throw AccountDoesNotExistException(""))
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
    accountHandler.update(acc.copy(address = acc.address.map(_.copy(telephone = Option(newTelephone)))), false)

    def message = "Your 3 digits code is: " + plainTextPinCode
    smsHandler.sendSms(message, phoneNumber.phone)
  }

  def confirmSignup(token: String): Account = {
    val (emailType, timestamp, accountId) = Token.parseToken(token)

    if (emailType != EmailType.Signup) {
      throw new InvalidTokenException("")
    } else {
      val signupDate = new org.joda.time.DateTime(timestamp).getMillis
      val currentDate = new org.joda.time.DateTime().getMillis
      if (currentDate - signupDate > Settings.Mail.MaxAge) {
        throw new TokenExpiredException("")
      } else {
        val account = find(accountId).map { a => a.copy(status = AccountStatus.ACTIVE)};
        if (account.isDefined) {
          accountHandler.update(account.get, refresh = true)
          account.get
        }
        else throw new InvalidTokenException("")
      }
    }
  }

  def generateNewSecret(accountId: String): Option[String] = find(accountId).map {
    acc =>
      val secret = UUID.randomUUID().toString
      accountHandler.update(acc.copy(secret = secret), false)
      secret
  }

  def addCreditCard(accountId: String, ccId: Option[String], holder: String,
                    number: Option[String], expiryDate: String, ccType: String): CreditCard = ccId match {
    case None => createCard(accountId, holder, number.get, expiryDate, ccType)
    case Some(cardId) => updateCard(accountId, cardId, holder, expiryDate, ccType)
  }

  private def updateCard(accountId: String, ccId: String, holder: String,
                         expiryDate: String, ccType: String): CreditCard = {
    val account = accountHandler.find(accountId) getOrElse (throw AccountDoesNotExistException(""))

    val card = account.creditCards.find(_.uuid == ccId).getOrElse(throw CreditCardDoesNotExistException(""))

    val newCard = card.copy(
      holder = holder,
      expiryDate = new Timestamp(new SimpleDateFormat("yyyy-MM").parse(expiryDate).getTime),
      cardType = CreditCardType.withName(ccType)
    )

    val newCards = account.creditCards.filter(_.uuid != ccId) :+ newCard
    accountHandler.update(account.copy(creditCards = newCards), true)

    newCard
  }

  private def createCard(accountId: String, holder: String, number: String,
                         expiryDate: String, ccType: String): CreditCard = {
    val account = find(accountId) getOrElse (throw AccountDoesNotExistException(""))

    val (hiddenN, cryptedN) =
      if (!UtilHandler.checkLuhn(number)) {
        throw InvalidCardNumberException("")
      } else {
        (UtilHandler.hideCardNumber(number, "X"), SymmetricCrypt.encrypt(number, Settings.Mogopay.Secret, "AES"))
      }

    val expiryTime = new Timestamp(new SimpleDateFormat("yyyy-MM").parse(expiryDate).getTime)

    val newCard = CreditCard(uuid = java.util.UUID.randomUUID().toString,
      number = cryptedN,
      holder = holder,
      expiryDate = expiryTime,
      cardType = CreditCardType.withName(ccType),
      account = accountId,
      hiddenNumber = hiddenN)
    val newCards = account.creditCards :+ newCard
    accountHandler.update(account.copy(creditCards = newCards), true)
    newCard.copy(number = UtilHandler.hideCardNumber(newCard.number, "X"))
  }

  def getBillingAddress(accountId: String): Option[AccountAddress] = find(accountId).flatMap(_.address)

  def getShippingAddresses(accountId: String): Seq[ShippingAddress] =
    find(accountId) map (_.shippingAddresses) getOrElse Nil

  def getShippingAddress(accountId: String): Option[ShippingAddress] =
    getShippingAddresses(accountId: String) find (_.active)

  def assignBillingAddress(accountId: String, address: AddressToAssignFromGetParams): Unit = {
    find(accountId).map {
      account =>
        val newAddress = account.address match {
          case None => address.getAddress
          case Some(addr) => {
            val telephone = telephoneHandler.buildTelephone(address.lphone, address.country, TelephoneStatus.WAITING_ENROLLMENT)

            addr.copy(road = address.road,
              road2 = address.road2,
              city = address.city,
              zipCode = Some(address.zipCode),
              extra = address.extra,
              civility = Some(Civility.withName(address.civility)),
              firstName = Some(address.firstName),
              lastName = Some(address.lastName),
              country = Some(address.country),
              admin1 = Some(address.admin1),
              admin2 = Some(address.admin2),
              telephone = Some(telephone))
          }
        }
        accountHandler.update(account.copy(address = Some(newAddress)), refresh = true)
    } getOrElse (throw AccountDoesNotExistException(s"$accountId"))
  }

  def deleteShippingAddress(accountId: String, addressId: String): Unit =
    for {
      account <- find(accountId)
      address <- account.shippingAddresses.find(_.uuid == addressId)
    } yield {
      val newAddresses = account.shippingAddresses diff List(address)
      val newAccount = account.copy(shippingAddresses = newAddresses)
      accountHandler.update(newAccount, refresh = false)
    }

  def addShippingAddress(accountId: String, address: AddressToAddFromGetParams): Unit =
    find(accountId).map {
      account =>
        val shippAddr = ShippingAddress(java.util.UUID.randomUUID().toString, active = true, address.getAddress)
        val newAddrs = account.shippingAddresses.map(_.copy(active = false)) :+ shippAddr
        accountHandler.update(account.copy(shippingAddresses = newAddrs), true)
        shippAddr
    } getOrElse (throw AccountDoesNotExistException(s"$accountId"))

  def updateShippingAddress(accountId: String, address: AddressToUpdateFromGetParams): Unit =
    find(accountId).map {
      account =>
        account.shippingAddresses.find(_.uuid == address.id) map {
          addr =>
            val telephone = telephoneHandler.buildTelephone(address.lphone,
              addr.address.country.get,
              TelephoneStatus.WAITING_ENROLLMENT)
            val newAddrs = account.shippingAddresses.filterNot(_ == addr) :+ addr.copy(
              address = addr.address.copy(
                road = address.road,
                road2 = address.road2,
                city = address.city,
                zipCode = Option(address.zipCode),
                extra = Option(address.extra),
                civility = Option(Civility.withName(address.civility)),
                firstName = Option(address.firstName),
                lastName = Option(address.lastName),
                country = Option(address.country),
                admin1 = Option(address.admin1),
                admin2 = Option(address.admin2),
                telephone = Option(telephone)))
            accountHandler.update(account.copy(shippingAddresses = newAddrs), refresh = true)
        }
    } getOrElse (throw AccountDoesNotExistException(s"$accountId"))

  type ActiveCountryState = Map[Symbol, Option[String]]

  def getActiveCountryState(accountId: String): Option[ActiveCountryState] = {
    find(accountId).map {
      account =>
        val addr = account.shippingAddresses.find(_.active).map(_.address).orElse(account.address)
        addr.map {
          addr =>
            Map('countryCode -> addr.country, 'stateCode -> addr.admin1)
        }
    } getOrElse (throw AccountDoesNotExistException(s"$accountId"))
  }

  def selectShippingAddress(accountId: String, addrId: String): Unit =
    find(accountId).map {
      account =>
        val newAddrs = account.shippingAddresses.map {
          addr => addr.copy(active = addr.uuid == addrId)
        }
        accountHandler.update(account.copy(shippingAddresses = newAddrs), true)
    } getOrElse (throw AccountDoesNotExistException(s"$accountId"))

  def profileInfo(accountId: String): Map[Symbol, Any] = find(accountId).map {
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

  def find(uuid: String) = EsClient.load[Account](Settings.Mogopay.EsIndex, uuid)

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

        lazy val birthDate = Some(birthDayDateFormat.parse(profile.birthDate))

        lazy val password = profile.password.map {
          case (p1, p2) =>
            if (p1 != p2) throw PasswordsDoNotMatchException("*****")
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
                 |D_LOGO!${Settings.Mogopay.EndPoint}${Settings.ImagesPath}sips/logo/!
                                                                             |F_DEFAULT!${Settings.Sips.CertifDir}${File.separator}parmcom.defaut!
                                                                                                                                     |F_PARAM!${new File(dir, "parcom").getAbsolutePath}!
                                                                                                                                                                                          |F_CERTIFICATE!${new File(dir, "certif").getAbsolutePath}!
                                                                                                                                                                                                                                                     |${if (isJSP) "F_CTYPE!jsp!" else ""}"
           """.stripMargin.trim
            )

            if (isJSP)
              certificateTargetFile.renameTo(new File(certificateTargetFile.getAbsolutePath + ".jsp"))
          } else {
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
        accountHandler.update(newAccount, false)
    }
  }

  def updateProfileLight(profile: UpdateProfileLight): Unit = {
    val newPassword = if (profile.password != "") {
      if (profile.password == profile.password2)
        Some(new Sha256Hash(profile.password).toHex)
      else
        throw new PasswordsDoNotMatchException("")
    } else {
      None
    }

    find(profile.id) match {
      case None => Failure(new AccountAddressDoesNotExistException(""))
      case Some(account) =>
        lazy val birthDate = try {
          Some(birthDayDateFormat.parse(profile.birthDate))
        } catch {
          case e: java.text.ParseException => throw new InvalidDateFormatException("Correct format: " + birthDayDateFormat.toPattern)
          case e: Throwable => throw e
        }

        val newAccount = account.copy(
          password = newPassword.getOrElse(account.password),
          civility = Some(Civility.withName(profile.civility)),
          firstName = Some(profile.firstName),
          lastName = Some(profile.lastName),
          birthDate = birthDate
        )

        accountHandler.update(newAccount, refresh = false)
    }
  }

  def recycle() {
    val req = select in Settings.Mogopay.EsIndex -> "Account" filter {
      and(
        termFilter("status", AccountStatus.WAITING_ENROLLMENT),
        rangeFilter("waitingEmailSince") from 0 to (System.currentTimeMillis() - Settings.AccountRecycleDuration)
      )
    }
    val ids = (EsClient searchAllRaw req).getHits map (_ getId) foreach delete
  }

  def delete(id: String) = {
    BOAccountDAO.delete(id)
    EsClient.delete[Account](Settings.Mogopay.EsIndex, id, refresh = false)
  }

  def enroll(accountId: String, lPhone: String, pinCode: String): Unit = {
    find(accountId).map {
      user =>
        if (user.address.map(_.telephone.map(_.lphone)).flatten.nonEmpty &&
          user.address.map(_.telephone.map(_.lphone)).flatten != Some(lPhone)) {
          val newTel = user.address.get.telephone.get.copy(lphone = lPhone)
          val newAddr = user.address.get.copy(telephone = Some(newTel))
          val newUser = user.copy(address = Some(newAddr))
          accountHandler.update(newUser, refresh = true)
        } else {
          find(accountId).map {
            account =>
              val encryptedCode = new Sha256Hash(pinCode).toHex
              if (account.address.map(_.telephone.map(_.pinCode3)).flatten == Some(encryptedCode) &&
                account.address.map(_.telephone.map(_.status)).flatten == Some(TelephoneStatus.WAITING_ENROLLMENT)) {
                val newTel = Telephone("", lPhone, "", None, TelephoneStatus.ACTIVE)
                accountHandler.update(account.copy(address = account.address.map(_.copy(telephone = Some(newTel)))), refresh = true)
              } else {
                throw new MogopayError(MogopayConstant.InvalidPhonePincode3)
              }
          }.getOrElse(throw new AccountDoesNotExistException(""))
        }
    } getOrElse(throw new AccountDoesNotExistException(""))
  }

  def signup(signup: Signup): (Token, Account) = {
    val req = search in Settings.Mogopay.EsIndex -> "Account" filter termFilter("email", signup.email)
    if (EsClient.search[Account](req).isDefined)
      throw new AccountWithSameEmailAddressAlreadyExistsError("")

    val birthdate = birthDayDateFormat.parse(signup.birthDate)
    val civility = Civility.withName(signup.civility)

    if (signup.password.isEmpty)
      throw NoPasswordProvidedError("****")

    if (signup.password != signup.password2)
      throw PasswordsDoNotMatchException("****")

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

      val tel = telephoneHandler.buildTelephone(signup.lphone, country.code, phoneStatus)

      signup.address.copy(telephone = Some(tel))
    }

    val addr = address(country)
    val accountId = newUUID

    val needEmailValidation = (signup.isMerchant && Settings.AccountValidateMerchantEmail) ||
      (!signup.isMerchant && Settings.AccountValidateCustomerEmail)
    val accountStatus = if (needEmailValidation) {
      AccountStatus.WAITING_ENROLLMENT
    } else {
      AccountStatus.ACTIVE
    }

    val token = if (accountStatus == AccountStatus.ACTIVE) ""
    else Token.generateToken(accountId, TokenType.Signup)

    val owner = if (signup.isMerchant) {
      None
    } else {
      Some(signup.vendor.getOrElse({
        val account = accountHandler.findByEmail(Settings.AccountValidateMerchantDefault).getOrElse {
          throw new VendorNotFoundException(Settings.AccountValidateMerchantDefault)
        }
        account.uuid
      }))
    }

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
      owner = owner,
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

    val tryToSave = accountHandler.save(account, refresh = true)
    tryToSave.map { _ => if (needEmailValidation) sendConfirmationEmail(account, signup.validationUrl, token)}

    (token, account)
  }

  private def sendConfirmationEmail(account: Account, validationUrl: String, token: String): Unit = {
    val template = scala.io.Source.fromInputStream(this.getClass.getResourceAsStream("/template/signup-confirmation.mustache")).mkString

    val url = validationUrl + (if (validationUrl.indexOf("?") == -1) "?" else "&") + "token=" + URLEncoder.encode(token, "UTF-8")

    EmailHandler.Send.to(
      Mail(
        from = ("contact@mogopay.com", "Mogopay"),
        to = Seq(account.email),
        subject = "Please confirm your account",
        message = templateHandler.mustache(template, s"""{"url": "$url"}""")))
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
    val token = SymmetricCrypt.encrypt(clearToken, Settings.Mogopay.Secret, "AES")
    accountHandler.find(accountId).map { account =>
      accountHandler.update(account.copy(emailingToken = Some(token)), refresh = true)
      token
    }
  }

  def generateToken(accountId: String, tokenType: TokenType): String = {
    val timestamp: Long = (new java.util.Date).getTime
    val clearToken: String = tokenType.id + "-" + timestamp + "-" + accountId.replace("-", "!")
    SymmetricCrypt.encrypt(clearToken, Settings.Mogopay.Secret, "AES")
  }

  def parseToken(token: String): (EmailType, Long, String) = {
    val unencryptedToken = SymmetricCrypt.decrypt(token, Settings.Mogopay.Secret, "AES")

    if (!unencryptedToken.contains("-"))
      throw InvalidTokenException("Invalid token.")

    val splitToken = unencryptedToken.split("-")
    val emailType = EmailType(splitToken(0).toInt)
    val timestamp = splitToken(1).toLong
    val accountId = splitToken(2).replace("!", "-")

    (emailType, timestamp, accountId)
  }
}

object EmailType extends Enumeration {
  type EmailType = Value
  val Signup = Value(0)
  val BypassLogin = Value(1)
}
