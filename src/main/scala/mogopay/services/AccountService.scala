package mogopay.services

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
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
import spray.http._
import spray.routing.Directives

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class AccountService(actor: ActorRef)(implicit executionContext: ExecutionContext) extends Directives with DefaultComplete {

  import mogopay.config.Implicits._

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
        onComplete((actor ? v).mapTo[Try[Boolean]]) { call =>
          handleComplete(call, (isValid: Boolean) => complete(HttpResponse(StatusCodes.OK, HttpEntity(ContentType(`text/plain`), isValid.toString)))
          )
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

  lazy val alreadyExistEmail = path("already-exist-email") {
    get {
      session { session =>
        parameters('email, 'merchant_id.?, 'account_type.as[RoleName]) { (email, merchantId, accountType) =>
          assert(accountType == RoleName.CUSTOMER || accountType == RoleName.MERCHANT)
          val isCustomer = accountType == RoleName.CUSTOMER && !session.sessionData.merchantSession

          if (isCustomer && merchantId.isEmpty) {
            complete(StatusCodes.BadRequest -> Map('error -> "Merchant ID not specified."))
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
                case TokenValidity.INVALID => StatusCodes.NotFound -> Map('error -> "Invalid token.")
                case TokenValidity.EXPIRED => StatusCodes.Unauthorized -> Map('error -> "Expired token.")
              })
          )
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
              complete(StatusCodes.Unauthorized -> Map('error -> "ID missing or incorrect. The user is probably not logged in."))
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

  lazy val generateNewPhoneCode = path("generate-new-phone-code") {
    get {
      withAccount(
        accountId => {
          complete {
            onComplete((actor ? GenerateAndSendPincode3(accountId)).mapTo[Try[Unit]]) { call =>
              handleComplete(call,
                (_: Unit) => complete(StatusCodes.OK)
              )
            }
          }
        })
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
                    Map('error -> "ID missing or incorrect. The user is probably not logged in.")
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
      parameters('token).as(ConfirmSignup) {
        confirmSignup =>
          complete {
            (actor ? confirmSignup).mapTo[Try[Boolean]] map {
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
                (actor ? BypassLogin(token, session)).mapTo[Option[Session]].map {
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
          (actor ? message).mapTo[Option[String]].map {
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
            (actor ? message).mapTo[Try[CreditCard]].map {
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
            (actor ? DeleteCreditCard(accountId, ccId)).mapTo[Try[Unit]].map {
              case Success(_) => StatusCodes.OK -> Map()
              case Failure(t) => StatusCodes.NotFound -> Map('error -> t.toString)
            }
          })
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
      withAccount(accountId => complete {
        (actor ? GetBillingAddress(accountId)).mapTo[Option[AccountAddress]]
      })
    }
  }

  lazy val getShippingAddresses = get {
    path("shipping-addresses") {
      withAccount(accountId => complete {
        val message = GetShippingAddresses(accountId)
        (actor ? message).mapTo[Seq[AccountAddress]]
      })
    }
  }

  lazy val getShippingAddress = get {
    path("shipping-address") {
      withAccount(accountId => complete {
        val message = GetShippingAddress(accountId)
        (actor ? message).mapTo[Option[AccountAddress]]
      })
    }
  }

  lazy val profileInfo = path("profile-info") {
    get {
      withAccount(accountId => complete {
        (actor ? ProfileInfo(accountId)).mapTo[Option[Future[Map[Symbol, Any]]]]
      })
    }
  }

  lazy val assignBillingAddress = get {
    path("assign-billing-address") {
      val params = parameters('road, 'city, 'road2.?, 'zip_code.?, 'extra.?, 'civility.?,
        'firstname.?, 'lastname.?, 'country.?, 'admin1.?, 'admin2.?)

      params.as(AddressToAssignFromGetParams) {
        address =>
          withAccount(accountId => complete {
            (actor ? AssignBillingAddress(accountId, address)).mapTo[Unit]
          })
      }
    }
  }

  lazy val deleteShippingAddress = get {
    path("delete-shipping-address") {
      parameters('address_id) { addressId =>
        withAccount(accountId =>
          complete {
            actor ? DeleteShippingAddress(accountId, addressId)
          })
      }
    }
  }

  lazy val addShippingAddress = get {
    path("add-shipping-address") {
      val params = parameters('road, 'city, 'road2.?, 'zip_code, 'extra.?, 'civility,
        'firstname, 'lastname, 'country, 'admin1.?, 'admin2.?)

      params.as(AddressToAddFromGetParams) {
        address =>
          withAccount(accountId => complete {
            (actor ? AddShippingAddress(accountId, address)).mapTo[Option[ShippingAddress]]
          })
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
          withAccount(accountId => complete {
            actor ? UpdateShippingAddress(accountId, address)
          })
      }
    }
  }

  lazy val getActiveCountryState = get {
    path("active-country-state") {
      withAccount(accountId => complete {
        (actor ? GetActiveCountryState(accountId)).mapTo[Option[Map[Symbol, String]]]
      })
    }
  }

  lazy val selectShippingAddress = get {
    path("select-shipping-address") {
      parameters('address_id.as[String]) {
        addressId =>
          withAccount(accountId => complete {
            actor ? SelectShippingAddress(accountId, addressId)
          })
      }
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

  implicit val timeout = Timeout(10.seconds)

  val route = {
    pathPrefix("account") {
      login ~
        updateProfile ~
        signup
    }
  }

  lazy val login = path("login") {
    post {
      var fields = formFields('email, 'password, 'merchant_id.?, 'is_customer.as[Boolean])
      fields { (email, password, merchantId, isCustomer) =>
        session { session =>
          val login = Login(email, password, merchantId, isCustomer)
          onComplete((actor ? login).mapTo[Try[Account]]) {
            case Failure(e) => complete(StatusCodes.InternalServerError)
            case Success(ta) => {
              ta match {
                case Failure(e) => complete(toHTTPResponse(e), e.toString)
                case Success(account) => {
                  session.sessionData.email = Some(email)
                  session.sessionData.accountId = Some(account.uuid)
                  session.sessionData.merchantId = account.owner
                  session.sessionData.isMerchant = account.owner.isEmpty
                  setSession(session) {
                    import mogopay.config.Implicits._
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

  lazy val signup = path("signup") {
    post {
      type Token = String

      val fields = formFields('email, 'password, 'password2,
        'lphone, 'civility, 'firstname, 'lastname, 'birthday,
        'road, 'city, 'zip_code, 'admin1, 'admin2, 'country,
        'is_merchant.as[Boolean], 'merchant_id ?)

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

  import shapeless._

  lazy val updateProfile = path("update-profile") {
    post {
      session { session =>
        session.sessionData.accountId match {
          case Some(accountId: String) =>
            val fields = formFields(('password ?) :: ('password2 ?) :: 'company ::
              'website :: 'lphone :: 'civility :: 'firstname :: 'lastname :: 'birthday ::
              'road :: ('road2 ?) :: ('city) :: 'zipCode :: 'country :: 'admin1 :: 'admin2 :: ('vendor ?) ::
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
              ('paypal_user ?) :: ('paypal_password ?) :: ('paypal_signature ?) :: ('kwixo_params ?) :: HNil)
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
}
