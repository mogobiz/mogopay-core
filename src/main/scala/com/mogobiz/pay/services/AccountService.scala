package com.mogobiz.pay.services

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.mogobiz.pay.actors.AccountActor._
import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.exceptions.Exceptions.PasswordsDoNotMatchException
import com.mogobiz.pay.handlers.UtilHandler
import com.mogobiz.pay.implicits.Implicits
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.pay.model.Mogopay.RoleName.RoleName
import com.mogobiz.pay.model.Mogopay.TokenValidity._
import com.mogobiz.session.Session
import com.mogobiz.session.SessionESDirectives._
import com.mogobiz.pay.settings.Settings
import spray.http.MediaTypes._
import spray.http._
import spray.routing.Directives

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Try

class AccountService(actor: ActorRef)(implicit executionContext: ExecutionContext) extends Directives with DefaultComplete {

  import Implicits._

  implicit val timeout = Timeout(10.seconds)

  val route = {
    pathPrefix("account") {
      isPatternValid ~
        customerToken ~
        merchantToken ~
        alreadyExistEmail ~
        id ~
        secret ~
        isValidAccountId ~
        checkTokenValidity ~
        confirmSignup ~
        bypassLogin ~
        updatePassword ~
        updateLostPassword ~
        generateNewPhoneCode ~
        enroll ~
        generateNewSecret ~
        addCreditCard ~
        deleteCreditCard ~
        logout ~
        getBillingAddress ~
        getShippingAddresses ~
        getShippingAddress ~
        profileInfo ~
        assignBillingAddress ~
        addShippingAddress ~
        updateShippingAddress ~
        getActiveCountryState ~
        selectShippingAddress ~
        deleteShippingAddress ~
        deleteMerchantTestAccount ~
        generateLostPasswordToken

      // generateNewEmailCode ~
      // sendConfirmationEmail ~
    }
  }

  lazy val isPatternValid = path("is-pattern-valid" / Segment) { pattern =>
    get {
      onComplete((actor ? IsPatternValid(pattern)).mapTo[Try[Boolean]]) { call =>
        handleComplete(call, (isValid: Boolean) => complete(HttpResponse(StatusCodes.OK, HttpEntity(ContentType(`text/plain`), isValid.toString)))
        )
      }
    }
  }

  private def addCSRFTokenToSession(session: Session, isMerchant: Boolean): String = {
    session.sessionData.merchantSession = isMerchant
    session.sessionData.csrfToken = Some(UtilHandler.generateNonce())
    session.sessionData.csrfToken.get
  }

  lazy val customerToken = path("customer-token") {
    session { session =>
      val token = addCSRFTokenToSession(session, isMerchant = false)
      setSession(session) {
        complete {
          Map('token -> token)
        }
      }
    }
  }

  lazy val merchantToken = path("merchant-token") {
    session { session =>
      val token = addCSRFTokenToSession(session, isMerchant = true)
      setSession(session) {
        complete {
          Map('token -> token)
        }
      }
    }
  }

  lazy val alreadyExistEmail = path("already-exist-email") {
    get {
      session { session =>
        parameters('email, 'merchant_id.?, 'account_type.as[RoleName]) { (email, merchantId, accountType) =>
          assert(accountType == RoleName.CUSTOMER || accountType == RoleName.MERCHANT)
          val isCustomer = accountType == RoleName.CUSTOMER && !session.sessionData.merchantSession

          if (isCustomer && merchantId.isEmpty) {
            complete(StatusCodes.BadRequest -> Map('type -> "BadRequest", 'error -> "Merchant ID not specified."))
          } else {
            val message = DoesAccountExistByEmail(email, merchantId)
            onComplete((actor ? DoesAccountExistByEmail(email, merchantId)).mapTo[Try[Boolean]]) { call =>
              handleComplete(call, (exist: Boolean) => complete(HttpResponse(StatusCodes.OK, HttpEntity(ContentType(`text/plain`), exist.toString)))
              )
            }
          }
        }
      }
    }
  }


  lazy val id = path("id") {
    get {
      parameters('seller) { seller =>
        onComplete((actor ? MerchantComId(seller)).mapTo[Try[Option[String]]]) { call =>
          handleComplete(call,
            (res: Option[String]) =>
              if (res.isEmpty)
                complete(StatusCodes.NotFound)
              else
                complete(StatusCodes.OK -> Map("result" -> res.get))
          )
        }
      }
    }
  }

  lazy val secret = path("secret") {
    get {
      parameters('seller) { seller =>
        onComplete((actor ? MerchantComSecret(seller)).mapTo[Try[Option[String]]]) { call =>
          handleComplete(call,
            (res: Option[String]) =>
              if (res.isEmpty)
                complete(StatusCodes.NotFound)
              else
                complete(StatusCodes.OK -> Map("result" -> res.get))
          )
        }
      }
    }
  }


  lazy val checkTokenValidity = get {
    path("check-token-validity") {
      type UserInfo = Option[Map[Symbol, Option[String]]]
      parameters('token).as(CheckTokenValidity) { token =>
        onComplete((actor ? token).mapTo[Try[(TokenValidity, UserInfo)]]) { call =>
          handleComplete(call,
            (res: (TokenValidity, UserInfo)) =>
              complete(res._1 match {
                case TokenValidity.VALID => StatusCodes.OK -> res._2
                case TokenValidity.INVALID => StatusCodes.NotFound -> Map('type -> "NotFound", 'error -> "Invalid token.")
                case TokenValidity.EXPIRED => StatusCodes.Unauthorized -> Map('type -> "Unauthorized", 'error -> "Expired token.")
              })
          )
        }
      }
    }
  }

  lazy val isValidAccountId = path("is-valid-account-id") {
    get {
      parameters('id).as(IsValidAccountId) { isValidAccountId =>
        onComplete((actor ? isValidAccountId).mapTo[Try[Boolean]]) { call =>
          handleComplete(call,
            (res: Boolean) =>
              complete(
                res match {
                  case true => StatusCodes.OK -> Map('result -> true)
                  case false => StatusCodes.NotFound -> Map('result -> false)
                }
              )
          )
        }
      }
    }
  }

  lazy val updatePassword = path("update-password") {
    get {
      parameters('password) { password =>
        session { session =>
          session.sessionData.accountId match {
            case Some(accountId: String) =>
              session.sessionData.merchantId match {
                case Some(vendorId: String) =>
                  onComplete((actor ? UpdatePassword(password, vendorId, accountId)).mapTo[Try[Unit]]) { call =>
                    handleComplete(call,
                      (_: Unit) => complete(StatusCodes.OK)
                    )
                  }
                case _ => complete {
                  complete(StatusCodes.BadRequest)
                }
              }
            case _ => complete {
              complete(StatusCodes.Unauthorized -> Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in."))
            }
          }
        }
      }
    }
  }

  lazy val updateLostPassword = path("update-lost-password") {
    get {
      parameters('password, 'token) { (password, token) =>
        onComplete((actor ? UpdateLostPassword(password, token)).mapTo[Try[Unit]]) { call =>
          handleComplete(call,
            (_: Unit) => complete(StatusCodes.OK)
          )
        }
      }
    }
  }

  /*
  lazy val verify = path("verify") {
    get {
      parameters('email, 'merchantSecret, 'mogopayToken).as(Verify) { verify =>
        complete {
          (account ? verify).mapTo[String]
        }
      }
    }
  }
  */

  lazy val generateNewPhoneCode = path("generate-new-phone-code") {
    get {
      session {
        session =>
          session.sessionData.accountId match {
            case Some(accountId: String) =>
              onComplete((actor ? GenerateAndSendPincode3(accountId)).mapTo[Try[Unit]]) { call =>
                handleComplete(call,
                  (_: Unit) => complete(StatusCodes.OK)
                )
              }

            case _ => complete {
              StatusCodes.Unauthorized ->
                Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in.")
            }
          }
      }
    }
  }

  lazy val enroll = get {
    path("enroll") {
      parameters('lphone, 'pin_code) {
        (lPhone, pinCode) =>
          session {
            session =>
              session.sessionData.accountId match {
                case Some(id: String) => onComplete((actor ? Enroll(id, lPhone, pinCode)).mapTo[Try[Unit]]) { call =>
                  handleComplete(call,
                    (_: Unit) => complete(StatusCodes.OK)
                  )
                }
                case _ => complete {
                  StatusCodes.Unauthorized ->
                    Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in.")
                }
              }
          }
      }
    }
  }

  /*
  lazy val generateNewEmailCode = path("generateNewEmailCode") {
    get {
      withSession(accountId => {
        complete {
          (account ? GenerateNewEmailCode(accountId)).mapTo[String]
        }
      })
    }
  }
  */

  /*
        complete {
          (account ? SendSignupConfirmationEmail(accountId)).mapTo[String]
        }
      })
    }
  }
  */

  lazy val confirmSignup = path("confirm-signup") {
    get {
      parameters('token, 'return_url) { (token, returnURL) =>
        val confirmSignup = ConfirmSignup(token)
          onComplete((actor ? confirmSignup).mapTo[Try[Boolean]]) { call =>
            handleComplete(call, (ok: Boolean) =>
              if (ok)
                redirect(returnURL, StatusCodes.PermanentRedirect)
              else
                complete(StatusCodes.Unauthorized ->
                  Map('type -> "Unauthorized", 'error -> "The token is either not for signup, or expired")))
          }
      }
    }
  }

  lazy val bypassLogin = path("bypass-login") {
    get {
      session {
        session =>
          parameters('token) {
            token =>
              onComplete((actor ? BypassLogin(token, session)).mapTo[Try[Option[Session]]]) { call =>
                handleComplete(call, (s: Option[Session]) =>
                  if (s.isEmpty)
                    complete(StatusCodes.Unauthorized ->
                      Map('type -> "Unauthorized", 'error -> "The token is either not for signup, or expired"))
                  else
                    setSession(s.get) {
                      complete {
                        StatusCodes.OK -> Map()
                      }
                    }
                )
              }
          }
      }
    }
  }

  lazy val generateNewSecret = path("generate-new-secret") {
    get {
      session {
        session =>
          session.sessionData.accountId match {
            case Some(accountId: String) =>
              onComplete((actor ? GenerateNewSecret(accountId)).mapTo[Try[Option[String]]]) { call =>
                handleComplete(call,
                  (uuid: Option[String]) =>
                    complete(uuid match {
                      case None => StatusCodes.NotFound -> Map()
                      case Some(uuid) => StatusCodes.OK -> Map('uuid -> uuid)
                    })
                )
              }
            case _ => complete {
              StatusCodes.Unauthorized ->
                Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in.")
            }
          }
      }
    }
  }

  lazy val addCreditCard = path("add-credit-card") {
    get {
      parameters('card_id.?, 'holder, 'number.?, 'expiry_date, 'type) {
        (ccId, holder, number, expiryDate, ccType) =>
          session {
            session =>
              session.sessionData.accountId match {
                case Some(accountId: String) =>
                  onComplete((actor ? AddCreditCard(accountId, ccId, holder, number, expiryDate, ccType)).mapTo[Try[CreditCard]]) {
                    call => handleComplete(call, (creditCard: CreditCard) => complete(StatusCodes.OK -> creditCard))
                  }
                case _ => complete {
                  StatusCodes.Unauthorized ->
                    Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in.")
                }
              }
          }
      }
    }
  }

  lazy val deleteCreditCard = path("delete-credit-card") {
    get {
      parameters('card_id) {
        ccId =>
          session {
            session =>
              session.sessionData.accountId match {
                case Some(accountId: String) =>
                  onComplete((actor ? DeleteCreditCard(accountId, ccId)).mapTo[Try[Unit]]) {
                    call =>
                      handleComplete(call, (_: Unit) => complete(StatusCodes.OK -> Map()))
                  }
                case _ => complete {
                  StatusCodes.Unauthorized ->
                    Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in.")
                }
              }
          }

      }
    }
  }

  lazy val logout = get {
    path("logout") {
      session {
        s =>
          setSession(Session()) {
            complete(200, Map())
          }
      }
    }
  }

  lazy val getBillingAddress = get {
    path("billing-address") {
      session {
        session =>
          session.sessionData.accountId match {
            case Some(accountId: String) =>
              onComplete((actor ? GetBillingAddress(accountId)).mapTo[Try[Option[AccountAddress]]]) {
                call =>
                  handleComplete(call, (addr: Option[AccountAddress]) =>
                    addr match {
                      case Some(addr) => complete(StatusCodes.OK -> addr)
                      case None => complete(StatusCodes.NotFound)
                    })
              }
            case _ => complete {
              StatusCodes.Unauthorized ->
                Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in.")
            }
          }
      }
    }
  }

  lazy val getShippingAddresses = get {
    path("shipping-addresses") {
      session {
        session =>
          session.sessionData.accountId match {
            case Some(accountId: String) =>
              onComplete((actor ? GetShippingAddresses(accountId)).mapTo[Try[Seq[AccountAddress]]]) {
                call =>
                  handleComplete(call, (addr: Seq[AccountAddress]) => complete(StatusCodes.OK -> addr))
              }
            case _ => complete {
              StatusCodes.Unauthorized ->
                Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in.")
            }
          }
      }
    }
  }

  lazy val getShippingAddress = get {
    path("shipping-address") {
      session {
        session =>
          session.sessionData.accountId match {
            case Some(accountId: String) =>
              onComplete((actor ? GetShippingAddresses(accountId)).mapTo[Try[Option[AccountAddress]]]) {
                call =>
                  handleComplete(call, (addr: Option[AccountAddress]) =>
                    addr match {
                      case Some(addr) => complete(StatusCodes.OK -> addr)
                      case None => complete(StatusCodes.NotFound)
                    })
              }
            case _ => complete {
              StatusCodes.Unauthorized ->
                Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in.")
            }
          }
      }
    }
  }

  lazy val profileInfo = path("profile-info") {
    get {
      session {
        session =>
          session.sessionData.accountId match {
            case Some(accountId: String) =>
              onComplete((actor ? ProfileInfo(accountId)).mapTo[Try[Map[Symbol, Any]]]) {
                call =>
                  handleComplete(call, (res: Map[Symbol, Any]) => complete(StatusCodes.OK -> res))
              }
            case _ => complete {
              StatusCodes.Unauthorized ->
                Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in.")
            }
          }
      }
    }
  }

  lazy val assignBillingAddress = get {
    path("assign-billing-address") {
      val params = parameters('road, 'city, 'road2.?, 'zip_code, 'extra.?, 'civility,
        'firstname, 'lastname, 'country, 'admin1, 'admin2, 'lphone)

      params.as(AddressToAssignFromGetParams) {
        address =>
          session {
            session =>
              session.sessionData.accountId match {
                case Some(accountId: String) =>
                  onComplete((actor ? AssignBillingAddress(accountId, address)).mapTo[Try[Unit]]) {
                    call =>
                      handleComplete(call, (_: Unit) => complete(StatusCodes.OK))

                  }
                case _ => complete {
                  StatusCodes.Unauthorized ->
                    Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in.")
                }
              }
          }
      }
    }
  }

  lazy val deleteShippingAddress = get {
    path("delete-shipping-address") {
      parameters('address_id) {
        addressId =>
          session {
            session =>
              session.sessionData.accountId match {
                case Some(accountId: String) =>
                  onComplete((actor ? DeleteShippingAddress(accountId, addressId)).mapTo[Try[Unit]]) {
                    call =>
                      handleComplete(call, (_: Unit) => complete(StatusCodes.OK))
                  }
                case _ => complete {
                  StatusCodes.Unauthorized ->
                    Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in.")
                }
              }
          }
      }
    }
  }

  lazy val addShippingAddress = get {
    path("add-shipping-address") {
      val params = parameters('road, 'city, 'road2.?, 'zip_code, 'extra.?, 'civility,
        'firstname, 'lastname, 'country, 'admin1.?, 'admin2.?)

      params.as(AddressToAddFromGetParams) {
        address =>
          session {
            session =>
              session.sessionData.accountId match {
                case Some(accountId: String) =>
                  onComplete((actor ? AddShippingAddress(accountId, address)).mapTo[Try[Unit]]) {
                    call =>
                      handleComplete(call, (_: Unit) => complete(StatusCodes.OK))
                  }
                case _ => complete {
                  StatusCodes.Unauthorized ->
                    Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in.")
                }
              }
          }
      }
    }
  }

  lazy val updateShippingAddress = get {
    path("update-shipping-address") {
      val params = parameters('address_id.as[String], 'road.?, 'city.?, 'road2.?,
        'zip_code.?, 'extra.?, 'civility.?, 'firstname.?, 'lastname.?, 'country.?,
        'admin1.?, 'admin2.?)

      params.as(AddressToUpdateFromGetParams) {
        address =>
          session {
            session =>
              session.sessionData.accountId match {
                case Some(accountId: String) =>
                  onComplete((actor ? UpdateShippingAddress(accountId, address)).mapTo[Try[Unit]]) {
                    call =>
                      handleComplete(call, (_: Unit) => complete(StatusCodes.OK))
                  }
                case _ => complete {
                  StatusCodes.Unauthorized ->
                    Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in.")
                }
              }
          }
      }
    }
  }

  lazy val getActiveCountryState = get {
    path("active-country-state") {
      session {
        session =>
          session.sessionData.accountId match {
            case Some(accountId: String) =>
              onComplete((actor ? GetActiveCountryState(accountId)).mapTo[Try[Option[Map[Symbol, Option[String]]]]]) {
                call =>
                  handleComplete(call, (res: Option[Map[Symbol, Option[String]]]) =>
                    res match {
                      case Some(res) => complete(StatusCodes.OK -> res)
                      case None => complete(StatusCodes.NotFound)
                    })
              }
            case _ => complete {
              StatusCodes.Unauthorized ->
                Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in.")
            }
          }
      }
    }
  }

  lazy val generateLostPasswordToken = get {
    path("request-password-token") {
      parameters('email, 'secret) { (email, secret) =>
        onComplete((actor ? GenerateLostPasswordToken(email, secret)).mapTo[Try[String]]) { call =>
          handleComplete(call, (res: String) => complete(StatusCodes.OK, Map('token -> res)))
        }
      }
    }
  }

  lazy val selectShippingAddress = get {
    path("select-shipping-address") {
      parameters('address_id.as[String]) {
        addressId =>
          session {
            session =>
              session.sessionData.accountId match {
                case Some(accountId: String) =>
                  onComplete((actor ? SelectShippingAddress(accountId, addressId)).mapTo[Try[Unit]]) {
                    call =>
                      handleComplete(call, (_: Unit) => complete(StatusCodes.OK))
                  }

                case _ => complete {
                  StatusCodes.Unauthorized ->
                    Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in.")
                }
              }
          }
      }
    }
  }

  lazy val deleteMerchantTestAccount = path("delete-test-account") {
    get {
      complete {
        import com.sksamuel.elastic4s.ElasticDsl._
        val req = com.sksamuel.elastic4s.ElasticDsl.delete
          .from(Settings.Mogopay.EsIndex -> "Account")
          .where(regexQuery("email", "newuser"))
        com.mogobiz.es.EsClient().execute(req)

        StatusCodes.OK -> Map()
      }
    }
  }
}

class AccountServiceJsonless(actor: ActorRef)(implicit executionContext: ExecutionContext) extends Directives with DefaultComplete {

  import Implicits.MogopaySession

  implicit val timeout = Timeout(10.seconds)

  val route = {
    pathPrefix("account") {
      login ~
      updateProfile ~
      updateProfileLight ~
      signup
    }
  }

  lazy val login = path("login") {
    post {
      var fields = formFields('email, 'password, 'merchant_id.?, 'is_customer.as[Boolean])
      fields { (email, password, merchantId, isCustomer) =>
        session { session =>
          val login = Login(email, password, merchantId, isCustomer)
          onComplete((actor ? login).mapTo[Try[Account]]) { call =>
            handleComplete(call, (account: Account) => {
              session.sessionData.email = Some(email)
              session.sessionData.accountId = Some(account.uuid)
              session.sessionData.merchantId = account.owner
              session.sessionData.isMerchant = account.owner.isEmpty
              session.sessionData.authenticated = true
              setSession(session) {
                import Implicits._
                complete(StatusCodes.OK, account)
              }
            })
          }
        }
      }
    }
  }

  lazy val signup = path("signup") {
    post {
      type Token = String

      val fields = formFields('email, 'password, 'password2,
        'lphone, 'civility, 'firstname, 'lastname, 'birthday,
        'road, 'road2.?, 'extra.?, 'city, 'zip_code, 'admin1, 'admin2, 'country,
        'is_merchant.as[Boolean], 'merchant_id ?, 'company ?, 'website ?,
        'return_url)

      fields { (email, password, password2, lphone, civility, firstname,
                lastname, birthday, road, road2, extra, city, zipCode, admin1, admin2, country,
                isMerchant, merchantId: Option[String], company, website, returnURL) =>
        val address = AccountAddress(
          civility = Some(Civility.withName(civility)),
          firstName = Some(firstname),
          lastName = Some(lastname),
          road = road,
          road2 = road2,
          extra = extra,
          city = city,
          zipCode = Some(zipCode),
          country = Some(country),
          admin1 = Some(admin1),
          admin2 = Some(admin2)
        )

        val signup = Signup(
          email = email,
          password = password,
          password2 = password2,
          lphone = lphone,
          civility = civility,
          firstName = firstname,
          lastName = lastname,
          birthDate = birthday,
          address = address,
          isMerchant = isMerchant,
          vendor = Some(merchantId.getOrElse(Settings.AccountValidateMerchantDefault)),
          company = company,
          website = website,
          returnURL = returnURL
        )

        import Implicits._
        onComplete((actor ? signup).mapTo[Try[(Token, Account)]]) { call =>
          handleComplete(call, (p: (Token, Account)) => complete(StatusCodes.OK -> Map('token -> p._1, 'account -> p._2)))
        }
      }
    }
  }

  import shapeless._

  lazy val updateProfile = path("update-profile") {
    post {
      session {
        session =>
          session.sessionData.accountId match {
            case Some(accountId: String) =>
              val fields = formFields(('password ?) :: ('password2 ?) :: 'company ::
                'website :: 'lphone :: 'civility :: 'firstname :: 'lastname :: 'birthday ::
                'road :: ('road2 ?) :: ('city) :: 'zip_code :: 'country :: 'admin1 :: 'admin2 :: ('vendor ?) ::
                'payment_method :: 'cb_provider ::
                ('payline_account ?) :: ('payline_key ?) :: ('payline_contract ?) :: ('payline_custom_payment_page_code ?) ::
                ('payline_custom_payment_template_url ?) :: ('paybox_site ?) :: ('paybox_key ?) :: ('paybox_rank ?) ::
                ('paybox_merchant_id ?) :: ('sips_merchant_id ?) :: ('sips_merchant_country ?) ::
                ('sips_merchant_certificate_file_name.?.as[Option[String]]) ::
                ('sips_merchant_certificate_file_content.?.as[Option[String]]) ::
                ('sips_merchant_parcom_file_name.?.as[Option[String]]) ::
                ('sips_merchant_parcom_file_content.?.as[Option[String]]) :: ('sips_merchant_logo_path ?) ::
                ('systempay_shop_id ?) :: ('systempay_contract_number ?) :: ('systempay_certificate ?) ::
                ('password_subject ?) :: ('password_content ?) :: ('password_pattern ?) :: ('callback_prefix ?) ::
                ('paypal_user ?) :: ('paypal_password ?) :: ('paypal_signature ?) :: ('kwixo_params ?) ::
                'email_field :: 'password_field :: HNil)
              fields.happly {
                case password :: password2 :: company :: website :: lphone ::
                  civility :: firstname :: lastname :: birthday :: road :: road2 ::
                  city :: zipCode :: country :: admin1 :: admin2 :: vendor ::
                  paymentMethod :: cbProvider ::
                  paylineAccount :: paylineKey :: paylineContract :: paylineCustomPaymentPageCode :: paylineCustomPaymentTemplateURL ::
                  payboxSite :: payboxKey :: payboxRank :: payboxMerchantId ::
                  sipsMerchantId :: sipsMerchantCountry :: sipsMerchantCertificateFileName :: sipsMerchantCertificateFileContent ::
                  sipsMerchantParcomFileName :: sipsMerchantParcomFileContent :: sipsMerchantLogoPath ::
                  systempayShopId :: systempayContractNumber :: systempayCertificate :: passwordSubject :: passwordContent ::
                  passwordPattern :: callbackPrefix :: paypalUser :: paypalPassword :: paypalSignature ::
                  kwixoParams :: emailField :: passwordField :: HNil =>
                  val validPassword: Option[(String, String)] = (password, password2) match {
                    case (Some(p), Some(p2)) => Some((p, p2))
                    case _ => None
                  }

                  val billingAddress = AccountAddress(
                    road = road,
                    road2 = road2,
                    city = city,
                    zipCode = Some(zipCode),
                    country = Some(country),
                    admin1 = Some(admin1),
                    admin2 = Some(admin2)
                  )

                  // error handling for invalid cbProvider
                  // error handling for invalid paymentMethod

                  // error handling if a param isn't passed
                  val cbParam: CBParams = CBPaymentProvider.withName(cbProvider) match {
                    case CBPaymentProvider.NONE => NoCBParams()
                    case CBPaymentProvider.PAYLINE => PaylineParams(paylineAccount.get, paylineKey.get, paylineContract.get,
                      paylineCustomPaymentPageCode.get, paylineCustomPaymentTemplateURL.get)
                    case CBPaymentProvider.PAYBOX => PayboxParams(payboxSite.get, payboxKey.get, payboxRank.get, payboxMerchantId.get)
                    case CBPaymentProvider.SIPS => SIPSParams(sipsMerchantId.get, sipsMerchantCountry.get,
                      sipsMerchantCertificateFileName, sipsMerchantCertificateFileContent,
                      sipsMerchantParcomFileName, sipsMerchantParcomFileContent, sipsMerchantLogoPath.get)
                    case CBPaymentProvider.SYSTEMPAY => SystempayParams(systempayShopId.get, systempayContractNumber.get, systempayCertificate.get)
                  }

                  val profile = UpdateProfile(
                    id = accountId,
                    password = validPassword,
                    company = company,
                    website = website,
                    lphone = lphone,
                    civility = civility,
                    firstName = firstname,
                    lastName = lastname,
                    birthDate = birthday,
                    billingAddress = billingAddress,
                    isMerchant = session.sessionData.isMerchant,
                    vendor = vendor,
                    emailField = emailField,
                    passwordField = passwordField,
                    passwordSubject = passwordSubject,
                    passwordContent = passwordContent,
                    callbackPrefix = callbackPrefix,
                    passwordPattern = passwordPattern,
                    paymentMethod = paymentMethod,
                    cbProvider = cbProvider,
                    payPalParam = PayPalParam(
                      paypalUser = paypalUser,
                      paypalPassword = paypalPassword,
                      paypalSignature = paypalSignature
                    ),
                    kwixoParam = KwixoParam(kwixoParams),
                    cbParam = cbParam
                  )

                  import Implicits._

                  onComplete((actor ? profile).mapTo[Try[Unit]]) { call =>
                    handleComplete(call, (_: Unit) => complete(StatusCodes.OK -> Map()))
                  }
              }
            case _ => complete {

              import Implicits._

              StatusCodes.Unauthorized ->
                Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in.")
            }
          }
      }
    }
  }

  lazy val updateProfileLight = path("update-profile-light") {
    post {
      session {
        session =>
          session.sessionData.accountId match {
            case Some(accountId: String) =>
              val fields = formFields('password :: 'password2 :: 'company ::
                'website :: 'lphone :: 'civility :: 'firstname :: 'lastname :: 'birthday :: HNil)
              fields.happly {
                case password :: password2 :: company :: website :: lphone ::
                  civility :: firstName :: lastName :: birthday :: HNil =>

                  val profile = UpdateProfileLight(
                    id = accountId,
                    password = password,
                    password2 = password2,
                    company = company,
                    website = website,
                    lphone = lphone,
                    civility = civility,
                    firstName = firstName,
                    lastName = lastName,
                    birthDate = birthday
                  )

                  import Implicits._

                  onComplete((actor ? profile).mapTo[Try[Unit]]) { call =>
                    handleComplete(call, (_: Unit) => complete(StatusCodes.OK -> Map()))
                  }
              }
            case _ => complete {
              import Implicits._

              StatusCodes.Unauthorized ->
                Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in.")
            }
          }
      }
    }
  }
}
