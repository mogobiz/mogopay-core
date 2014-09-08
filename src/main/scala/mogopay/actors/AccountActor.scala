package mogopay.actors

import akka.actor.Actor
import mogopay.config.HandlersConfig._
import mogopay.model.Mogopay.{Civility, AccountAddress}
import org.json4s.JObject
import mogopay.session.Session

object AccountActor {
  case class DoesAccountExistByEmail(email: String, merchantId: Option[String])
  case class IsPatternValid(pattern: String)
  case class IsValidAccountId(id: String)
  case class RequestPasswordChange(email: String, merchantId: String,
                                   passwordCB: String, isCustomer: Boolean)
  case class GenerateLostPasswordToken(email: String, merchantId: Option[String])
  case class CheckTokenValidity(token: String)
  case class SelectShippingAddress(accountId: String, addressId: String)
  case class UpdatePassword(password: String, vendorId: String, accountId: String)
  case class Verify(email: String, merchantSecret: String, mogopayToken: String)
  case class Login(email: String, password: String, merchantId: Option[String], isCustomer: Boolean)
  case class GenerateAndSendPincode3(accountId: String)
//  case class GenerateNewEmailCode(accountId: String)
  case class SendSignupConfirmationEmail(accountId: String)
  case class ConfirmSignup(token: String)
  case class BypassLogin(token: String, session: Session)
  case class GenerateNewSecret(accountId: String)
  case class AddCreditCard(accountId: String, ccId: Option[String], holder: String,
                           number: String, expiry: String, ccType: String)
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
  case class UpdateProfile(id: String, password: Option[(String, String)],
                           company: String, website: String, lphone: String, civility: String,
                           firstName: String, lastName: String, birthDate: String,
                           billingAddress: AccountAddress, vendor: Option[String], isMerchant: Boolean)
  case class Signup(email: String, password: String, password2: String,
                    lphone: String, civility: String, firstName: String,
                    lastName: String, birthDate: String, address: AccountAddress,
                    isMerchant: Boolean, vendor: Option[String])
}

class AccountActor extends Actor {
  import AccountActor._

  def receive: Receive = {
    case IsPatternValid(pattern: String) => {
      sender ! accountHandler.isPatternValid(pattern)
    }

    case DoesAccountExistByEmail(email, merchantId) => {
      sender ! accountHandler.alreadyExistEmail(email, merchantId)
    }

    case IsValidAccountId(id) => sender ! accountHandler.load(id).nonEmpty

    /*
    case GenerateLostPasswordToken(email, merchantId) =>
      sender ! accountHandler.generateLostPasswordToken(email, merchantId)
    */

    case CheckTokenValidity(token) => sender ! accountHandler.checkTokenValidity(token)

    case UpdatePassword(password, vendorId, accountId) =>
      sender ! accountHandler.updatePassword(password, vendorId, accountId)

    /*
    case Verify(email, merchantSecret, mogopayToken) =>
      sender ! accountHandler.verify(email, merchantSecret, mogopayToken)
    */

    case Login(email, password, merchantId, isCustomer) => {
      sender ! accountHandler.login(email, password, merchantId, isCustomer)
    }

    case GenerateAndSendPincode3(accountId) =>
      sender ! accountHandler.generateAndSendPincode3(accountId)

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
      sender ! accountHandler.Emailing.confirmSignup(token)

    case BypassLogin(token, session) =>
      sender ! accountHandler.Emailing.bypassLogin(token, session)

    case GenerateNewSecret(accountId) => sender ! accountHandler.generateNewSecret(accountId)

    case AddCreditCard(accountId, ccId, holder, number, expiry, ccType) =>
      sender ! accountHandler.addCreditCard(accountId, ccId, holder, number, expiry, ccType)

    case DeleteCreditCard(accountId, cardId) => sender ! creditCardHandler.delete(accountId, cardId)

    case GetBillingAddress(accountId) => sender ! accountHandler.getBillingAddress(accountId)

    case GetShippingAddresses(accountId) => sender ! accountHandler.getShippingAddresses(accountId)

    case GetShippingAddress(accountId) => sender ! accountHandler.getShippingAddress(accountId)

    case AssignBillingAddress(accountId, address) =>
      sender ! accountHandler.assignBillingAddress(accountId, address)

    case AddShippingAddress(accountId, address) =>
      sender ! accountHandler.addShippingAddress(accountId, address)

    case DeleteShippingAddress(accountId, addressId) =>
      sender ! accountHandler.deleteShippingAddress(accountId, addressId)

    case UpdateShippingAddress(accountId, address) =>
      sender ! accountHandler.updateShippingAddress(accountId, address)

    case GetActiveCountryState(accountId) =>
      sender ! accountHandler.getActiveCountryState(accountId)

    case SelectShippingAddress(accountId, addressId) =>
      sender ! accountHandler.selectShippingAddress(accountId, addressId)

    case ProfileInfo(accountId) => sender ! accountHandler.profileInfo(accountId)

    case up: UpdateProfile => sender ! accountHandler.updateProfile(up)

    case MerchantComId(seller) => sender ! accountHandler.findByEmail(seller + "@merchant.com").map(_.uuid)

    case MerchantComSecret(seller) => sender ! accountHandler.findByEmail(seller + "@merchant.com").map(_.secret)

    case Enroll(accountId, lPhone, pinCode) =>
      sender ! accountHandler.enroll(accountId, lPhone, pinCode)

    case s: Signup => sender ! accountHandler.signup(s)
  }
}