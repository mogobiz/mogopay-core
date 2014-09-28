package mogopay.handlers

import mogopay.config.HandlersConfig._
import mogopay.config.Settings
import mogopay.exceptions.Exceptions._
import mogopay.model.Mogopay._
import mogopay.util.GlobalUtil._
import mogopay.util.RSA
import org.apache.shiro.crypto.hash.Sha256Hash

import scala.collection.mutable
import scala.util._

class UserHandler {
  val USER_OR_PASSWORD_IS_NULL = "101"
  val USER_UNKNOWN = "102"
  val ACCOUNT_INACTIVE = "103"
  val ACCOUNT_ALREADY_EXIST = "103"
  val ACCOUNT_DOES_NOT_EXIST = "104"
  val ACCOUNT_PASSWORD_ERROR = "105"
  val ACCOUNT_TOO_MANY_ATTEMPTS = "106"
  val VERIFY_TIMEOUT = "151"
  val VERIFY_INVALID_EMAIL = "152"
  val VERIFY_FAIL = "153"
  val SUCCESS = "0"

  def register(successURL: String, errorURL: String, merchantId: String,
               email: String, password: String): Map[String, String] = {
    val vendor = accountHandler.load(merchantId).getOrElse(throw AccountDoesNotExistException(""))
    if (!vendor.roles.contains(RoleName.MERCHANT)) throw NotAVendorAccountException("")

    if (accountHandler.findByEmail(email).nonEmpty) {
      throw AccountAlreadyExistsException("")
    } else {
      val data1: Map[String, String] = Map(
        "callback_success" -> successURL,
        "callback_error" -> errorURL,
        "merchant_id" -> merchantId,
        "userEmail" -> email,
        "userPassword" -> password
      )

      val clearData = email + "" + System.currentTimeMillis() + "" + newUUID + "" + SUCCESS
      val encodedData = RSA.encrypt(clearData, Settings.RSA.publicKey)

      val data2 = data1 + ("mogopay_token" -> encodedData)

      val account = Account(uuid = newUUID,
        email = email,
        company = None,
        website = None,
        password = new Sha256Hash(password).toHex,
        civility = None,
        firstName = None,
        lastName = None,
        birthDate = None,
        address = None,
        status = AccountStatus.ACTIVE,
        loginFailedCount = 0,
        waitingPhoneSince = -1L,
        waitingEmailSince = -1L,
        extra = Some(data2.mkString(", ")),
        lastLogin = None,
        paymentConfig = None,
        country = None,
        roles = Nil,
        owner = None,
        emailingToken = None,
        shippingAddresses = Nil,
        secret = null,
        creditCards = Nil)

      accountHandler.save(account)
      data2
    }
  }
}