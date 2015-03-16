package com.mogobiz.pay.services

import com.mogobiz.pay.config.{Settings, DefaultComplete}
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.handlers._
import com.mogobiz.pay.implicits.Implicits
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.pay.model.Mogopay.RoleName.RoleName
import com.mogobiz.pay.model.Mogopay.TokenValidity._
import com.mogobiz.pay.model.RequestParameters.{UpdateProfileLightRequest, UpdateProfileRequest, SignupRequest, LoginRequest}
import com.mogobiz.session.Session
import com.mogobiz.session.SessionESDirectives._
import spray.http.MediaTypes._
import spray.http._
import spray.routing.Directives

class AccountService extends Directives with DefaultComplete {

  import Implicits._

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
        getActiveCountryStateShipping ~
        selectShippingAddress ~
        deleteShippingAddress ~
        deleteMerchantTestAccount ~
        sendNewPassword ~
        listCompagnies
    }
  }

  lazy val isPatternValid = path("is-pattern-valid" / Segment) { pattern =>
    get {
      handleCall(accountHandler.isPatternValid(pattern),
        (isValid: Boolean) => complete(HttpResponse(StatusCodes.OK, HttpEntity(ContentType(`text/plain`), isValid.toString))))
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
            handleCall(accountHandler.alreadyExistEmail(email, merchantId),
              (exist: Boolean) => complete(HttpResponse(StatusCodes.OK, HttpEntity(ContentType(`text/plain`), exist.toString))))
          }
        }
      }
    }
  }


  lazy val id = path("id") {
    get {
      parameters('seller) { seller =>
        handleCall(accountHandler.findByEmail(seller + "@merchant.com", None).map(_.uuid),
          (res: Option[String]) =>
            if (res.isEmpty)
              complete(StatusCodes.NotFound)
            else
              complete(StatusCodes.OK -> Map("result" -> res.get))
        )
      }
    }
  }

  lazy val secret = path("secret") {
    get {
      parameters('seller) { seller =>
        handleCall(accountHandler.findByEmail(seller + "@merchant.com", None).map(_.secret),
          (res: Option[String]) =>
            if (res.isEmpty)
              complete(StatusCodes.NotFound)
            else
              complete(StatusCodes.OK -> Map("result" -> res.get))
        )
      }
    }
  }


  lazy val checkTokenValidity = get {
    path("check-token-validity") {
      type UserInfo = Option[Map[Symbol, Option[String]]]
      parameters('token) { token =>
        handleCall(accountHandler.checkTokenValidity(token),
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

  lazy val isValidAccountId = path("is-valid-account-id") {
    get {
      parameters('id) { id =>
        handleCall(accountHandler.load(id).nonEmpty,
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

  lazy val updatePassword = path("update-password") {
    get {
      parameters('password) { password =>
        session { session =>
          session.sessionData.accountId match {
            case Some(accountId: String) =>
              session.sessionData.merchantId match {
                case Some(vendorId: String) =>
                  handleCall(accountHandler.updatePassword(password, vendorId, accountId),
                    (_: Unit) => complete(StatusCodes.OK))
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

  lazy val generateNewPhoneCode = path("generate-new-phone-code") {
    get {
      session {
        session =>
          session.sessionData.accountId match {
            case Some(accountId: String) =>
              handleCall(accountHandler.generateAndSendPincode3(accountId),
                (_: Unit) => complete(StatusCodes.OK)
              )
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
                case Some(id: String) =>
                  handleCall(accountHandler.enroll(id, lPhone, pinCode),
                    (_: Unit) => complete(StatusCodes.OK))
                case _ => complete {
                  StatusCodes.Unauthorized ->
                    Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in.")
                }
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
              handleCall(accountHandler.generateNewSecret(accountId),
                (uuid: Option[String]) =>
                  complete(uuid match {
                    case None => StatusCodes.NotFound -> Map()
                    case Some(uuid) => StatusCodes.OK -> Map('uuid -> uuid)
                  })
              )
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
                  handleCall(accountHandler.addCreditCard(accountId, ccId, holder, number, expiryDate, ccType),
                    (creditCard: CreditCard) => complete(StatusCodes.OK -> creditCard))
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
                  handleCall(creditCardHandler.delete(accountId, ccId),
                    (_: Unit) => complete(StatusCodes.OK -> Map()))
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
              handleCall(accountHandler.getBillingAddress(accountId),
                (addr: Option[AccountAddress]) =>
                  addr match {
                    case Some(addr) => complete(StatusCodes.OK -> addr)
                    case None => complete(StatusCodes.NotFound)
                  })
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
              handleCall(accountHandler.getShippingAddresses(accountId),
                (addr: Seq[ShippingAddress]) => complete(StatusCodes.OK -> addr))
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
              handleCall(accountHandler.getShippingAddress(accountId),
                (addr: Option[ShippingAddress]) =>
                  addr match {
                    case Some(addr) => complete(StatusCodes.OK -> addr)
                    case None => complete(StatusCodes.NotFound)
                  })
            case _ => complete {
              StatusCodes.Unauthorized ->
                Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in.")
            }
          }
      }
    }
  }

  lazy val profileInfo = path("profile-info") {
    parameter('email.?) { email =>
      get {
        session {
          session =>
            session.sessionData.accountId match {
              case Some(accountId: String) =>
                handleCall(accountHandler.profileInfo(accountId),
                  (res: Map[Symbol, Any]) => complete(StatusCodes.OK -> res))
              case _ => complete {
                StatusCodes.Unauthorized ->
                  Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in.")
              }
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
                  handleCall(accountHandler.assignBillingAddress(accountId, address),
                    (_: Unit) => complete(StatusCodes.OK))
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
                  handleCall(accountHandler.deleteShippingAddress(accountId, addressId),
                    (_: Unit) => complete(StatusCodes.OK))

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
        'firstname, 'lastname, 'country, 'admin1, 'admin2, 'lphone)

      params.as(AddressToAddFromGetParams) {
        address =>
          session {
            session =>
              session.sessionData.accountId match {
                case Some(accountId: String) =>
                  handleCall(accountHandler.addShippingAddress(accountId, address),
                    (_: Unit) => complete(StatusCodes.OK))

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
      val params = parameters('address_id, 'road, 'city, 'road2.?,
        'zip_code, 'extra.?, 'civility, 'firstname, 'lastname, 'country,
        'admin1, 'admin2, 'lphone)

      params.as(AddressToUpdateFromGetParams) {
        address =>
          session {
            session =>
              session.sessionData.accountId match {
                case Some(accountId: String) =>
                  handleCall(accountHandler.updateShippingAddress(accountId, address),
                    (_: Unit) => complete(StatusCodes.OK))
                case _ => complete {
                  StatusCodes.Unauthorized ->
                    Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in.")
                }
              }
          }
      }
    }
  }

  lazy val getActiveCountryStateShipping = get {
    path("active-country-state-shipping") {
      session {
        session =>
          session.sessionData.accountId match {
            case Some(accountId: String) =>
              handleCall(accountHandler.getActiveCountryStateShipping(accountId),
                (res: Option[Map[Symbol, Option[String]]]) =>
                  res match {
                    case Some(res) => complete(StatusCodes.OK -> res)
                    case None => complete(StatusCodes.NotFound)
                  })
            case _ => complete {
              StatusCodes.Unauthorized ->
                Map('type -> "Unauthorized", 'error -> "ID missing or incorrect. The user is probably not logged in.")
            }
          }
      }
    }
  }

  lazy val listCompagnies = path("list-compagnies") {
    get {
      session { session =>
        handleCall(accountHandler.listCompagnies(session.sessionData.accountId),
          (res: List[String]) => complete(StatusCodes.OK -> res))
      }
    }
  }

  lazy val sendNewPassword = post {
    path("send-new-password") {
      entity(as[SendNewPasswordParams]) { params =>
        handleCall(accountHandler.sendNewPassword(params),
          (_: Unit) => complete(StatusCodes.OK))
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
                  handleCall(accountHandler.selectShippingAddress(accountId, addressId),
                    (_: Unit) => complete(StatusCodes.OK))

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
        com.mogobiz.es.EsClient().execute(req).await

        StatusCodes.OK -> Map()
      }
    }
  }
}

class AccountServiceJsonless extends Directives with DefaultComplete {

  import Implicits.MogopaySession

  val route = {
    pathPrefix("account") {
      login ~
        updateProfile ~
        updateProfileLight ~
        signup ~
        confirmSignup
    }
  }

  lazy val login = path("login") {
    import com.mogobiz.pay.implicits.Implicits._
    post {
      entity(as[LoginRequest]) { req =>
        session { session =>
          handleCall(accountHandler.login(req.email, req.password, req.merchant_id, req.is_customer),
            (account: Account) => {
              authenticateSession(session, account)
              setSession(session) {
                import Implicits._
                complete(StatusCodes.OK, account)
              }
            })
        }
      }
    }
  }

  lazy val confirmSignup = path("confirm-signup") {
    get {
      parameters('token) { (token) =>
        session { session =>
          handleCall(accountHandler.confirmSignup(token),
            (account: Account) => {
              authenticateSession(session, account)
              setSession(session) {
                import Implicits._
                complete(StatusCodes.OK, account)
              }
            })
        }
      }
    }
  }

  private def authenticateSession(session: Session, account: Account) = {
    session.sessionData.email = Some(account.email)
    session.sessionData.accountId = Some(account.uuid)
    session.sessionData.merchantId = account.owner
    session.sessionData.isMerchant = account.owner.isEmpty
    session.sessionData.authenticated = true
  }

  lazy val signup = path("signup") {
    import Implicits._
    post {
      type Token = String

      entity(as[SignupRequest]) { req =>
        val address = AccountAddress(
          civility = Some(Civility.withName(req.civility)),
          firstName = Some(req.firstname),
          lastName = Some(req.lastname),
          road = req.road,
          road2 = req.road2,
          extra = req.extra,
          city = req.city,
          zipCode = Some(req.zip_code),
          country = Some(req.country),
          admin1 = Some(req.admin1),
          admin2 = Some(req.admin2)
        )

        val signup = Signup(
          email = req.email,
          password = req.password,
          password2 = req.password2,
          lphone = req.lphone,
          civility = req.civility,
          firstName = req.firstname,
          lastName = req.lastname,
          birthDate = req.birthday,
          address = address,
          isMerchant = req.is_merchant,
          vendor = req.merchant_id,
          company = req.company,
          website = req.website,
          validationUrl = req.validation_url,
          fromName = req.from_name,
          fromEmail = req.from_email
        )

        import Implicits._
        handleCall(accountHandler.signup(signup),
          (p: (Token, Account)) => complete(StatusCodes.OK -> Map('token -> p._1, 'account -> p._2)))
      }
    }
  }

  lazy val updateProfile = path("update-profile") {
    import Implicits._
    post {
      session {
        session =>
          session.sessionData.accountId match {
            case Some(accountId: String) =>
              entity(as[UpdateProfileRequest]) { req =>
                val validPassword: Option[(String, String)] = (req.password, req.password2) match {
                  case (Some(p), Some(p2)) => Some((p, p2))
                  case _ => None
                }

                val billingAddress = AccountAddress(
                  road = req.road,
                  road2 = req.road2,
                  city = req.city,
                  zipCode = Some(req.zip_code),
                  country = Some(req.country),
                  admin1 = Some(req.admin1),
                  admin2 = Some(req.admin2)
                )

                // error handling for invalid cbProvider
                // error handling for invalid paymentMethod

                // error handling if a param isn't passed
                val cbParam: CBParams = CBPaymentProvider.withName(req.cb_provider) match {
                  case CBPaymentProvider.NONE => NoCBParams()
                  case CBPaymentProvider.PAYLINE => PaylineParams(req.payline_account.get, req.payline_key.get, req.payline_contract.get,
                    req.payline_custom_payment_page_code.get, req.payline_custom_payment_template_url.get)

                  case CBPaymentProvider.PAYBOX => PayboxParams(req.paybox_site.get, req.paybox_key.get, req.paybox_rank.get, req.paybox_merchant_id.get)

                  case CBPaymentProvider.SIPS => SIPSParams(req.sips_merchant_id.get, req.sips_merchant_country.get,
                    req.sips_merchant_certificate_file_name, req.sips_merchant_certificate_file_content,
                    req.sips_merchant_parcom_file_name, req.sips_merchant_parcom_file_content, req.sips_merchant_logo_path.get)

                  case CBPaymentProvider.SYSTEMPAY => SystempayParams(req.systempay_shop_id.get, req.systempay_contract_number.get, req.systempay_certificate.get)
                }

                val profile = UpdateProfile(
                  id = accountId,
                  password = validPassword,
                  company = req.company,
                  website = req.website,
                  lphone = req.lphone,
                  civility = req.civility,
                  firstName = req.firstname,
                  lastName = req.lastname,
                  birthDate = req.birthday,
                  billingAddress = billingAddress,
                  isMerchant = session.sessionData.isMerchant,
                  vendor = req.vendor,
                  emailField = req.email_field,
                  passwordField = req.password_field,
                  passwordSubject = req.password_subject,
                  passwordContent = req.password_content,
                  callbackPrefix = req.callback_prefix,
                  passwordPattern = req.password_pattern,
                  paymentMethod = req.payment_method,
                  cbProvider = req.cb_provider,
                  payPalParam = PayPalParam(
                    paypalUser = req.paypal_user,
                    paypalPassword = req.paypal_password,
                    paypalSignature = req.paypal_signature
                  ),
                  kwixoParam = KwixoParam(req.kwixo_params),
                  cbParam = cbParam
                )

                import Implicits._

                handleCall(accountHandler.updateProfile(profile),
                  (_: Unit) => complete(StatusCodes.OK -> Map()))
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
    import Implicits._
    post {
      session {
        session =>
          session.sessionData.accountId match {
            case Some(accountId: String) =>
              entity(as[UpdateProfileLightRequest]) { req =>
                val profile = UpdateProfileLight(
                  id = accountId,
                  password = req.password,
                  password2 = req.password2,
                  civility = req.civility,
                  firstName = req.firstname,
                  lastName = req.lastname,
                  birthDate = req.birthday
                )
                handleCall(accountHandler.updateProfileLight(profile),
                  (_: Unit) => complete(StatusCodes.OK -> Map()))
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
