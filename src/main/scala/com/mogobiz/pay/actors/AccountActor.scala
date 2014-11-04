package com.mogobiz.pay.actors

import akka.actor.Actor
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.model.Mogopay.{Civility, AccountAddress}
import com.mogobiz.session.Session

import scala.util.Try

object AccountActor {

  case class DoesAccountExistByEmail(email: String, merchantId: Option[String])

  case class IsPatternValid(pattern: String)

  case class IsValidAccountId(id: String)

  case class RequestPasswordChange(email: String, merchantId: String,
                                   passwordCB: String, isCustomer: Boolean)

  case class GenerateLostPasswordToken(email: String, merchantSecret: String)

  case class CheckTokenValidity(token: String)

  case class SelectShippingAddress(accountId: String, addressId: String)

  case class UpdatePassword(password: String, vendorId: String, accountId: String)

  case class UpdateLostPassword(password: String, token: String)

  case class Verify(email: String, merchantSecret: String, mogopayToken: String)

  case class Login(email: String, password: String, merchantId: Option[String], isCustomer: Boolean)

  case class GenerateAndSendPincode3(accountId: String)

  //  case class GenerateNewEmailCode(accountId: String)
  case class SendSignupConfirmationEmail(accountId: String)

  case class ConfirmSignup(token: String)

  case class BypassLogin(token: String, session: Session)

  case class GenerateNewSecret(accountId: String)

  case class AddCreditCard(accountId: String, ccId: Option[String], holder: String,
                           number: Option[String], expiry: String, ccType: String)

  case class DeleteCreditCard(accountId: String, cardId: String)

  case class GetBillingAddress(accountId: String)

  case class GetShippingAddresses(accountId: String)

  case class GetShippingAddress(accountId: String)

  case class AddressToUpdateFromGetParams(id: String, road: String,
                                          city: String, road2: Option[String],
                                          zipCode: Option[String], extra: Option[String],
                                          civility: Option[String], firstName: Option[String],
                                          lastName: Option[String], country: Option[String],
                                          admin1: Option[String], admin2: Option[String])

  case class AddressToAddFromGetParams(road: String, city: String, road2: Option[String],
                                       zipCode: Option[String], extra: Option[String],
                                       civility: Option[String], firstName: Option[String],
                                       lastName: Option[String], country: Option[String],
                                       admin1: Option[String], admin2: Option[String]) {
    def getAddress = {
      val c = civility.map(Civility.withName)
      AccountAddress(road, road2, city, zipCode, extra, c, firstName,
        lastName, None, country, admin1, admin2)
    }
  }

  case class AddressToAssignFromGetParams(road: String, city: String,
                                          road2: Option[String], zipCode: Option[String],
                                          extra: Option[String], civility: Option[String],
                                          firstName: Option[String], lastName: Option[String],
                                          country: Option[String], admin1: Option[String],
                                          admin2: Option[String]) {
    def getAddress = {
      val c = civility.map(Civility.withName)
      AccountAddress(road, road2, city, zipCode, extra, c, firstName,
        lastName, None, country, admin1, admin2)
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
                    isMerchant: Boolean, vendor: Option[String], company: Option[String], website: Option[String])

  case class UpdateProfile(id: String, password: Option[(String, String)],
                           company: String, website: String, lphone: String, civility: String,
                           firstName: String, lastName: String, birthDate: String,
                           billingAddress: AccountAddress, vendor: Option[String], isMerchant: Boolean,
                           emailField: String, passwordField: String,
                           passwordSubject: Option[String], passwordContent: Option[String],
                           passwordPattern: Option[String], callbackPrefix: Option[String],
                           paymentMethod: String, cbProvider: String, cbParam: CBParams,
                           payPalParam: PayPalParam, kwixoParam: KwixoParam)

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
}

class AccountActor extends Actor {

  import AccountActor._

  def receive: Receive = {
    case IsPatternValid(pattern: String) => {
      sender ! Try(accountHandler.isPatternValid(pattern))
    }

    case DoesAccountExistByEmail(email, merchantId) => {
      sender ! Try(accountHandler.alreadyExistEmail(email, merchantId))
    }

    case IsValidAccountId(id) => sender ! Try(accountHandler.load(id).nonEmpty)

    case GenerateLostPasswordToken(email, merchantSecret) =>
      sender ! Try(accountHandler.generateLostPasswordToken(email, merchantSecret))

    case CheckTokenValidity(token) => sender ! Try(accountHandler.checkTokenValidity(token))

    case UpdatePassword(password, vendorId, accountId) =>
      sender ! Try(accountHandler.updatePassword(password, vendorId, accountId))

    case UpdateLostPassword(password, token) =>
      sender ! Try(accountHandler.updateLostPassword(password, token))

    /*
    case Verify(email, merchantSecret, mogopayToken) =>
      sender ! accountHandler.verify(email, merchantSecret, mogopayToken)
    */

    case Login(email, password, merchantId, isCustomer) => {
      sender ! Try(accountHandler.login(email, password, merchantId, isCustomer))
    }

    case GenerateAndSendPincode3(accountId) =>
      sender ! Try(accountHandler.generateAndSendPincode3(accountId))

    /*
    case GenerateNewEmailCode(accountId) =>
      sender ! accountHandler.generateNewEmailCode(accountId)
    */

    /*
    case SendSignupConfirmationEmail(accountId) => {
      sender ! accountHandler.Emailing.sendSignupConfirmationEmail(accountId)
    }
    */

    case ConfirmSignup(token) =>
      sender ! Try(accountHandler.Emailing.confirmSignup(token))

    case BypassLogin(token, session) =>
      sender ! Try(accountHandler.Emailing.bypassLogin(token, session))

    case GenerateNewSecret(accountId) => sender ! Try(accountHandler.generateNewSecret(accountId))

    case AddCreditCard(accountId, ccId, holder, number, expiry, ccType) =>
      sender ! Try(accountHandler.addCreditCard(accountId, ccId, holder, number, expiry, ccType))

    case DeleteCreditCard(accountId, cardId) => sender ! Try(creditCardHandler.delete(accountId, cardId))

    case GetBillingAddress(accountId) => sender ! Try(accountHandler.getBillingAddress(accountId))

    case GetShippingAddresses(accountId) => sender ! Try(accountHandler.getShippingAddresses(accountId))

    case GetShippingAddress(accountId) => sender ! Try(accountHandler.getShippingAddress(accountId))

    case AssignBillingAddress(accountId, address) =>
      sender ! Try(accountHandler.assignBillingAddress(accountId, address))

    case AddShippingAddress(accountId, address) =>
      sender ! Try(accountHandler.addShippingAddress(accountId, address))

    case DeleteShippingAddress(accountId, addressId) =>
      sender ! Try(accountHandler.deleteShippingAddress(accountId, addressId))

    case UpdateShippingAddress(accountId, address) =>
      sender ! Try(accountHandler.updateShippingAddress(accountId, address))

    case GetActiveCountryState(accountId) =>
      sender ! Try(accountHandler.getActiveCountryState(accountId))

    case SelectShippingAddress(accountId, addressId) =>
      sender ! Try(accountHandler.selectShippingAddress(accountId, addressId))

    case ProfileInfo(accountId) => sender ! Try(accountHandler.profileInfo(accountId))

    case up: UpdateProfile => sender ! Try(accountHandler.updateProfile(up))

    case MerchantComId(seller) => sender ! Try(accountHandler.findByEmail(seller + "@merchant.com").map(_.uuid))

    case MerchantComSecret(seller) => sender ! Try(accountHandler.findByEmail(seller + "@merchant.com").map(_.secret))

    case Enroll(accountId, lPhone, pinCode) =>
      sender ! Try(accountHandler.enroll(accountId, lPhone, pinCode))

    case s: Signup => sender ! Try(accountHandler.signup(s))
  }
}