package mogopay.services

import akka.actor.ActorRef
import mogopay.actors.AccountActor._
import mogopay.config.Settings
import mogopay.exceptions.Exceptions._
import mogopay.handlers.UtilHandler
import mogopay.model.Mogopay._
import mogopay.model.Mogopay.RoleName.RoleName
import mogopay.model.Mogopay.TokenValidity._
import mogopay.services.Util._
import mogopay.session.Session
import mogopay.session.SessionESDirectives._
import spray.http.MediaTypes._
import spray.http.{ContentType, HttpResponse, HttpEntity, StatusCodes}
import spray.routing.Directives

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class AccountService(account: ActorRef)(implicit executionContext: ExecutionContext) extends Directives {

  import mogopay.config.Implicits._

  import akka.pattern.ask
  import akka.util.Timeout
  import scala.concurrent.duration._

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
        login ~
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
        deleteMerchantTestAccount

      // generateNewEmailCode ~
      // sendConfirmationEmail ~
      // generateLostPasswordToken ~
    }
  }

  lazy val isPatternValid = path("is-pattern-valid") {
    get {
      parameters('pattern).as(IsPatternValid) { v =>
        complete {
          (account ? v).mapTo[Boolean].map { isValid =>
            HttpResponse(200, HttpEntity(ContentType(`text/plain`), isValid.toString))
          }
        }
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

  // TODO: Instead of "true" or "false", I get "{}"
  lazy val alreadyExistEmail = path("already-exist-email") {
    get {
      session { session =>
        parameters('email, 'merchant_id.?, 'account_type.as[RoleName]) { (email, merchantId, accountType) =>
          assert(accountType == RoleName.CUSTOMER || accountType == RoleName.MERCHANT)
          val isCustomer = accountType == RoleName.CUSTOMER && !session.sessionData.merchantSession
          complete {
            if (isCustomer && merchantId.isEmpty) {
              StatusCodes.BadRequest -> Map('error -> "Merchant ID not specified.")
            } else {
              val message = DoesAccountExistByEmail(email, merchantId)
              (account ? message).mapTo[Boolean].map { exists =>
                (if (exists) 200 else 404) -> Map()
              }
            }
          }
        }
      }
    }
  }


  lazy val id = path("id") {
    get {
      parameters('seller) { seller =>
        complete {
          (account ? MerchantComId(seller)).mapTo[Option[String]] map {
            case None => 404 -> Map()
            case Some(x) => 200 -> Map("result" -> x)
          }
        }
      }
    }
  }

  lazy val secret = path("secret") {
    get {
      parameters('seller) { seller =>
        complete {
          (account ? MerchantComSecret(seller)).mapTo[Option[String]] map {
            case None => 404 -> Map()
            case Some(x) => 200 -> Map("result" -> x)
          }
        }
      }
    }
  }


  lazy val checkTokenValidity = getPath("check-token-validity") {
    type UserInfo = Option[Map[Symbol, Option[String]]]
    parameters('token).as(CheckTokenValidity) { token =>
      complete {
        val response = (account ? token).mapTo[(TokenValidity, UserInfo)]
        response.map { case (validity, userInfo) => {
          validity match {
            case TokenValidity.VALID => StatusCodes.OK -> userInfo
            case TokenValidity.INVALID => StatusCodes.NotFound -> Map('error -> "Invalid token.")
            case TokenValidity.EXPIRED => StatusCodes.Unauthorized -> Map('error -> "Expired token.")
          }
        }
        }
      }
    }
  }


  /*
  lazy val generateLostPasswordToken = path("generateLostPasswordToken") {
    get {
      parameters('email, 'merchantId.as[Option[String]]) { (email, merchantId) =>
        complete {
          val message: GenerateLostPasswordToken = GenerateLostPasswordToken(email, merchantId)
          (account ? message).mapTo[Try[String]].map { try_ => try_ match {
            case Failure(t) => StatusCodes.NotFound -> Map('error -> t.toString)
            case Success(t) => StatusCodes.OK       -> Map('token -> t)
          }}
        }
      }
    }
  }
  */

  lazy val isValidAccountId = path("is-valid-account-id") {
    get {
      parameters('id).as(IsValidAccountId) { isValidAccountId =>
        complete {
          (account ? isValidAccountId).mapTo[Boolean] map {
            case true => StatusCodes.OK -> Map('result -> true)
            case false => StatusCodes.NotFound -> Map('result -> false)
          }
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
              session.sessionData.vendorId match {
                case Some(vendorId: String) =>
                  val updatePassword = UpdatePassword(password, vendorId, accountId)
                  complete {
                    (account ? updatePassword).mapTo[Try[Unit]] map {
                      case Failure(e) => toHTTPResponse(e) -> e.toString
                      case Success(unit) => StatusCodes.OK
                    }
                  }
                case _ => complete {
                  StatusCodes.BadRequest
                }
              }
            case _ => complete {
              StatusCodes.Unauthorized -> Map('error -> "ID missing or incorrect. The user is probably not logged in.")
            }
          }
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

  // TODO: Use POST (and formFields() instead of parameters())
  lazy val login = path("login") {
    //    post {
    get {
      parameters('email, 'password, 'merchant_id.?, 'is_customer.as[Boolean]) { (email, password, merchantId, isCustomer) =>
        session { session =>
          val login = Login(email, password, merchantId, isCustomer)
          onComplete((account ? login).mapTo[Try[Account]]) {
            case Failure(e) => complete(StatusCodes.InternalServerError)
            case Success(ta) => {
              ta match {
                case Failure(e) => complete(toHTTPResponse(e), e.toString)
                case Success(account) => {
                  session.sessionData.email = Some(email)
                  session.sessionData.accountId = Some(account.uuid)
                  session.sessionData.vendorId = account.owner
                  session.sessionData.isMerchant = account.owner.isEmpty
                  setSession(session) {
                    complete(StatusCodes.OK, account)
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  lazy val generateNewPhoneCode = path("generate-new-phone-code") {
    get {
      withAccount(
        accountId => {
          complete {
            val message = GenerateAndSendPincode3(accountId)
            (account ? message).mapTo[Try[Unit]] map {
              case Success(_) => StatusCodes.OK -> Map()
              case Failure(t: NoPhoneNumberFoundException) =>
                StatusCodes.NotFound -> Map('error -> t.toString)
              case Failure(t: AccountDoesNotExistError) =>
                StatusCodes.NotFound -> Map('error -> t.toString)
              case Failure(t) =>
                StatusCodes.InternalServerError -> Map('error -> t.toString)
            }
          }
        })
    }
  }

  lazy val enroll = getPath("enroll") {
    parameters('lphone, 'pinCode) {
      (lPhone, pinCode) =>
        withAccount(accountId => complete {
          val message = Enroll(accountId, lPhone, pinCode)
          (account ? message) map {
            case Failure(e) => toHTTPResponse(e) -> e.toString
            case r => StatusCodes.OK -> Map()
          }
        })
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
      parameters('token).as(ConfirmSignup) {
        confirmSignup =>
          complete {
            (account ? confirmSignup).mapTo[Try[Boolean]] map {
              case Failure(e) => toHTTPResponse(e) -> Map('error -> e.toString)
              case Success(true) => StatusCodes.OK -> Map()
              case Success(false) => StatusCodes.Gone ->
                """{"error": "The token is either not for signup, or expired."}"""
            }
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
              complete {
                (account ? BypassLogin(token, session)).mapTo[Option[Session]].map {
                  case Some(s) => setSession(s) {
                    complete {
                      StatusCodes.OK -> Map()
                    }
                  }
                  case None => StatusCodes.Gone ->
                    """{"error": "The token is either not for login bypass, or expired."}"""
                }
              }
          }
      }
    }
  }

  lazy val generateNewSecret = path("generate-new-secret") {
    get {
      withAccount(accountId => {
        complete {
          val message = GenerateNewSecret(accountId)
          (account ? message).mapTo[Option[String]].map {
            case None => StatusCodes.NotFound -> Map()
            case Some(uuid) => StatusCodes.OK -> Map('uuid -> uuid)
          }
        }
      })
    }
  }

  lazy val addCreditCard = path("add-credit-card") {
    get {
      parameters('card_id.?, 'holder, 'number, 'expiry_date, 'type) {
        (ccId, holder, number, expiryDate, ccType) =>
          withAccount(accountId => complete {
            val message: AddCreditCard = AddCreditCard(accountId, ccId, holder, number, expiryDate, ccType)
            (account ? message).mapTo[Try[CreditCard]].map {
              case Failure(e: java.text.ParseException) =>
                StatusCodes.BadRequest -> Map('error -> e.toString)
              case Failure(e) =>
                toHTTPResponse(e) -> Map('error -> e.toString)
              case Success(creditCard) => StatusCodes.OK -> creditCard
            }
          })
      }
    }
  }

  lazy val deleteCreditCard = path("delete-credit-card") {
    get {
      parameters('card_id) {
        ccId =>
          withAccount(accountId => complete {
            (account ? DeleteCreditCard(accountId, ccId)).mapTo[Try[Unit]].map {
              case Success(_) => StatusCodes.OK -> Map()
              case Failure(t) => StatusCodes.NotFound -> Map('error -> t.toString)
            }
          })
      }
    }
  }

  lazy val logout = getPath("logout") {
    session {
      s =>
        setSession(Session()) {
          complete(200, Map())
        }
    }
  }

  lazy val getBillingAddress = getPath("billing-address") {
    withAccount(accountId => complete {
      (account ? GetBillingAddress(accountId)).mapTo[Option[AccountAddress]]
    })
  }

  lazy val getShippingAddresses = getPath("shipping-addresses") {
    withAccount(accountId => complete {
      val message = GetShippingAddresses(accountId)
      (account ? message).mapTo[Seq[AccountAddress]]
    })
  }

  lazy val getShippingAddress = getPath("shipping-address") {
    withAccount(accountId => complete {
      val message = GetShippingAddress(accountId)
      (account ? message).mapTo[Option[AccountAddress]]
    })
  }

  lazy val profileInfo = path("profile-info") {
    get {
      withAccount(accountId => complete {
        (account ? ProfileInfo(accountId)).mapTo[Option[Future[Map[Symbol, Any]]]]
      })
    }
  }

  lazy val assignBillingAddress = getPath("assign-billing-address") {
    val params = parameters('road, 'city, 'road2.?, 'zipCode.?, 'extra.?, 'civility.?,
      'firstname.?, 'lastname.?, 'country.?, 'admin1.?, 'admin2.?)

    params.as(AddressToAssignFromGetParams) {
      address =>
        withAccount(accountId => complete {
          (account ? AssignBillingAddress(accountId, address)).mapTo[Unit]
        })
    }
  }

  lazy val deleteShippingAddress = getPath("delete-shipping-address") {
    parameters('addressId) { addressId =>
      withAccount(accountId =>
        complete {
          account ? DeleteShippingAddress(accountId, addressId)
        })
    }
  }

  lazy val addShippingAddress = getPath("add-shipping-address") {
    val params = parameters('road, 'city, 'road2.?, 'zipCode, 'extra.?, 'civility,
      'firstname, 'lastname, 'country, 'admin1.?, 'admin2.?)

    params.as(AddressToAddFromGetParams) {
      address =>
        withAccount(accountId => complete {
          (account ? AddShippingAddress(accountId, address)).mapTo[Option[ShippingAddress]]
        })
    }
  }

  lazy val updateShippingAddress = getPath("update-shipping-address") {
    val params = parameters('addressId.as[String], 'road.?, 'city.?, 'road2.?,
      'zipCode.?, 'extra.?, 'civility.?, 'firstname.?, 'lastname.?, 'country.?,
      'admin1.?, 'admin2.?)

    params.as(AddressToUpdateFromGetParams) {
      address =>
        withAccount(accountId => complete {
          account ? UpdateShippingAddress(accountId, address)
        })
    }
  }

  lazy val getActiveCountryState = getPath("active-country-state") {
    withAccount(accountId => complete {
      (account ? GetActiveCountryState(accountId)).mapTo[Option[Map[Symbol, String]]]
    })
  }

  lazy val selectShippingAddress = getPath("select-shipping-address") {
    parameters('addressId.as[String]) {
      addressId =>
        withAccount(accountId => complete {
          account ? SelectShippingAddress(accountId, addressId)
        })
    }
  }

  lazy val deleteMerchantTestAccount = path("delete-test-account") {
    get {
      complete {
        import com.sksamuel.elastic4s.ElasticDsl._
        val req = com.sksamuel.elastic4s.ElasticDsl.delete
          .from(Settings.ElasticSearch.Index -> "Account")
          .where(regexQuery("email", "newuser"))
        scala.concurrent.Await.result(mogopay.es.EsClient.client.execute(req), Duration.Inf)

        "{}"
      }
    }
  }

  private def withAccount(response: String => spray.routing.StandardRoute) = {
    session {
      session =>
        session.sessionData.accountId match {
          case Some(id: String) => response(id)
          case _ => complete {
            StatusCodes.Unauthorized ->
              Map('error -> "ID missing or incorrect. The user is probably not logged in.")
          }
        }
    }
  }
}

class AccountServiceJsonless(actor: ActorRef)(implicit executionContext: ExecutionContext) extends Directives {

  import mogopay.config.Implicits.MogopaySession

  import akka.pattern.ask
  import akka.util.Timeout
  import scala.concurrent.duration._

  implicit val timeout = Timeout(10.seconds)

  val route = {
    pathPrefix("account") {
      updateProfile ~
        signup
    }
  }

  import shapeless._
  lazy val updateProfile = path("update-profile") {
    post {
      session { session =>
        session.sessionData.accountId match {
          case Some(accountId: String) =>
            val fields = formFields(('password?) :: ('password2?) :: 'company ::
              'website :: 'lphone :: 'civility :: 'firstname :: 'lastname :: 'birthday ::
              'road :: ('road2?) :: ('city) :: 'zipCode :: 'country :: 'admin1 :: 'admin2 :: ('vendor?) ::
              'paymentMethod :: 'cbProvider ::
              ('paylineAccount?) :: ('paylineKey?) :: ('paylineContract?) :: ('paylineCustomPaymentPageCode?) :: ('paylineCustomPaymentTemplateURL?) ::
              ('payboxSite?) :: ('payboxKey?) :: ('payboxRank?) :: ('payboxMerchantId?) ::
              ('sipsMerchantId?) :: ('sipsMerchantCountry?) :: ('sipsMerchantCertificateFileName.?.as[Option[String]]) ::
              ('sipsMerchantCertificateFileContent.?.as[Option[String]]) :: ('sipsMerchantParcomFileName.?.as[Option[String]]) ::
              ('sipsMerchantParcomFileContent.?.as[Option[String]]) :: ('sipsMerchantLogoPath?) ::
              ('systempayShopId?) :: ('systempayContractNumber?) :: ('systempayCertificate?) ::
              ('passwordSubject?) :: ('passwordContent?) :: ('passwordPattern?) :: ('callbackPrefix?) ::
              ('paypalUser?) :: ('paypalPassword?) :: ('paypalSignature?) :: ('kwixoParams?) :: HNil)
            fields.happly { case password :: password2 :: company :: website :: lphone ::
                      civility :: firstname :: lastname :: birthday :: road :: road2 ::
                      city :: zipCode :: country :: admin1 :: admin2 :: vendor ::
                      paymentMethod :: cbProvider ::
                      paylineAccount :: paylineKey :: paylineContract :: paylineCustomPaymentPageCode :: paylineCustomPaymentTemplateURL ::
                      payboxSite :: payboxKey :: payboxRank :: payboxMerchantId ::
                      sipsMerchantId :: sipsMerchantCountry :: sipsMerchantCertificateFileName :: sipsMerchantCertificateFileContent ::
                      sipsMerchantParcomFileName :: sipsMerchantParcomFileContent :: sipsMerchantLogoPath ::
                      systempayShopId :: systempayContractNumber :: systempayCertificate :: passwordSubject :: passwordContent ::
                      passwordPattern :: callbackPrefix :: paypalUser :: paypalPassword :: paypalSignature ::
                      kwixoParams :: HNil =>
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
                passwordSubject = passwordSubject,
                passwordContent = passwordContent,
                callbackPrefix  = callbackPrefix,
                passwordPattern = passwordPattern,
                paymentMethod = paymentMethod,
                cbProvider = cbProvider,
                payPalParam = PayPalParam(
                  paypalUser      = paypalUser,
                  paypalPassword  = paypalPassword,
                  paypalSignature = paypalSignature
                ),
                kwixoParam = KwixoParam(kwixoParams),
                cbParam = cbParam
              )

              import mogopay.config.Implicits._
              onComplete(actor ? profile) {
                case Failure(e) => complete(500, e.toString)
                case Success(x) => x match {
                  case Failure(e) => complete {
                    toHTTPResponse(e) -> Map('error -> e.toString)
                  }
                  case Success(_) => complete {
                    200 -> Map
                  }
                }
              }
            }
          case _ => complete {
            import mogopay.config.Implicits._
            StatusCodes.Unauthorized ->
              Map('error -> "ID missing or incorrect. The user is probably not logged in.")
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
        'road, 'city, 'zipCode, 'admin1, 'admin2, 'country,
        'isMerchant.as[Boolean], 'merchantId ?)

      fields { (email, password, password2, lphone, civility, firstname,
                lastname, birthday, road, city, zipCode, admin1, admin2, country,
                isMerchant, merchantId: Option[String]) =>
        val address = AccountAddress(
          road = road,
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
          vendor = Some(merchantId.getOrElse(Settings.AccountValidateMerchantDefault))
        )

        import mogopay.config.Implicits._
        val r = (actor ? signup).mapTo[Try[(Token, Account)]]
        onComplete(r) {
          case Failure(e) => complete {
            500 -> Map('error -> e.toString)
          }
          case Success(x) => x match {
            case Failure(e: AccountWithSameEmailAddressAlreadyExistsError) =>
              complete {
                409 -> Map('error -> e.toString)
              }
            case Failure(e) => complete {
              toHTTPResponse(e) -> Map('error -> e.toString)
            }
            case Success(p) => complete {
              200 -> Map('token -> p._1, 'account -> p._2)
            }
          }
        }
      }
    }
  }
}
