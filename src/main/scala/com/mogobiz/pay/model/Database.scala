/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.model

import java.util.{Calendar, Date, UUID}

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import com.mogobiz.pay.common._
import spray.httpx.unmarshalling.{FromStringDeserializer, MalformedContent}

import scala.util.control.NonFatal

object Mogopay {
  type Document = String
}

  object PaymentType extends Enumeration {
    type PaymentType = Value
    val NONE        = Value("NONE")
    val CREDIT_CARD = Value("CREDIT_CARD")
    val PAYPAL      = Value("PAYPAL")
  }

  class PaymentTypeRef extends TypeReference[PaymentType.type]

  object AccountStatus extends Enumeration {
    type AccountStatus = Value
    val ACTIVE             = Value("ACTIVE")
    val INACTIVE           = Value("INACTIVE")
    val WAITING_ENROLLMENT = Value("WAITING_ENROLLMENT")
  }

  class AccountStatusRef extends TypeReference[AccountStatus.type]

  object CBPaymentMethod extends Enumeration {
    type CBPaymentMethod = Value
    val EXTERNAL             = Value("EXTERNAL")
    val THREEDS_NO           = Value("THREEDS_NO")
    val THREEDS_IF_AVAILABLE = Value("THREEDS_IF_AVAILABLE")
    val THREEDS_REQUIRED     = Value("THREEDS_REQUIRED")
  }

  class CBPaymentMethodRef extends TypeReference[CBPaymentMethod.type]

  object RoleName extends Enumeration {
    type RoleName = Value
    val ANONYMOUS     = Value("ANONYMOUS")
    val ADMINISTRATOR = Value("ADMINISTRATOR")
    val CUSTOMER      = Value("CUSTOMER")
    val MERCHANT      = Value("MERCHANT")
  }

  class RoleNameRef extends TypeReference[RoleName.type]

  object TelephoneStatus extends Enumeration {
    type TelephoneStatus = Value
    val ACTIVE             = Value("ACTIVE")
    val DELETED            = Value("DELETED")
    val WAITING_ENROLLMENT = Value("WAITING_ENROLLMENT")
  }

  class TelephoneStatusRef extends TypeReference[TelephoneStatus.type]

  object Civility extends Enumeration {
    type Civility = Value
    val MR   = Value("MR")
    val MRS  = Value("MRS")
    val MSS  = Value("MRS")
    val DR   = Value("DR")
    val ME   = Value("ME")
    val PR   = Value("PR")
    val SIR  = Value("SIR")
    val LADY = Value("LADY")
    val LORD = Value("LORD")
    val VVE  = Value("VVE")
    val MGR  = Value("MGR")
  }

  class CivilityRef extends TypeReference[Civility.type]

  object ResponseCode3DS extends Enumeration {
    type ResponseCode3DS = Value
    val APPROVED = Value("APPROVED")
    val REFUSED  = Value("REFUSED")
    val INVALID  = Value("INVALID")
    val ERROR    = Value("ERROR")
  }

  class ResponseCode3DSRef extends TypeReference[ResponseCode3DS.type]

  object TransactionStatus extends Enumeration {
    type TransactionStatus = Value
    val INITIATED            = Value("INITIATED")
    val VERIFICATION_THREEDS = Value("VERIFICATION_THREEDS")
    val THREEDS_TESTED       = Value("THREEDS_TESTED")
    val PAYMENT_REQUESTED    = Value("PAYMENT_REQUESTED")
    val PAYMENT_AUTHORIZED   = Value("PAYMENT_AUTHORIZED")
    val PAYMENT_CONFIRMED    = Value("PAYMENT_CONFIRMED")
    val PAYMENT_REFUSED      = Value("PAYMENT_REFUSED")
    val CANCEL_REQUESTED     = Value("CANCEL_REQUESTED")
    val CANCEL_FAILED        = Value("CANCEL_FAILED")
    val CANCEL_CONFIRMED     = Value("CANCEL_CONFIRMED")
    val CUSTOMER_REFUNDED    = Value("CUSTOMER_REFUNDED")
    val SHIPMENT_ERROR       = Value("SHIPMENT_ERROR")
    val LITIGATION           = Value("LITIGATION")
  }

  class TransactionStatusRef extends TypeReference[TransactionStatus.type]

  object CBPaymentProvider extends Enumeration {
    type CBPaymentProvider = Value
    val NONE         = Value("NONE")
    val AUTHORIZENET = Value("AUTHORIZENET")
    val PAYLINE      = Value("PAYLINE")
    val PAYBOX       = Value("PAYBOX")
    val SIPS         = Value("SIPS")
    val SYSTEMPAY    = Value("SYSTEMPAY")
    val CUSTOM       = Value("CUSTOM")
  }

  class CBPaymentProviderRef extends TypeReference[CBPaymentProvider.type]

  object CreditCardType extends Enumeration {
    type CreditCardType = Value
    val CB          = Value("CB")
    val VISA        = Value("VISA")
    val MASTER_CARD = Value("MASTER_CARD")
    val DISCOVER    = Value("DISCOVER")
    val AMEX        = Value("AMEX")
    val SWITCH      = Value("SWITCH")
    val SOLO        = Value("SOLO")
    val OTHER       = Value("OTHER")
  }

  class CreditCardTypeRef extends TypeReference[CreditCardType.type]

  object TokenValidity extends Enumeration {
    type TokenValidity = Value
    val VALID   = Value("VALID")
    val INVALID = Value("INVALID")
    val EXPIRED = Value("EXPIRED")
  }

  class TokenValidityRef extends TypeReference[TokenValidity.type]

  object PaymentStatus extends Enumeration {
    type PaymentStatus = Value

    val PENDING       = Value("PENDING")
    val INVALID       = Value("INVALID")
    val FAILED        = Value("FAILED")
    val COMPLETE      = Value("COMPLETE")
    val CANCELED      = Value("CANCELED")
    val CANCEL_FAILED = Value("CANCEL_FAILED")
    val REFUNDED      = Value("REFUNDED")
    val REFUND_FAILED = Value("REFUND_FAILED")
  }

  object TransactionStep extends Enumeration {
    type TransactionStep = Value
    val START_PAYMENT = Value("PAYMENT")
    val VALIDATE_PAYMENT = Value("VALIDATE_PAYMENT")
    val FINISH = Value("FINISH")
    val CANCEL = Value("CANCEL")
    val REFUND = Value("REFUND")
    val SUBMIT = Value("SUBMIT")
    val DONE = Value("DONE")
    val CHECK_THREEDS = Value("CHECK_THREEDS")
    val THREEDS_CALLBACK = Value("THREEDS_CALLBACK")
    val SUCCESS = Value("SUCCESS")
    val DO_WEB_PAYMENT = Value("DO_WEB_PAYMENT")
    val GET_WEB_PAYMENT_DETAILS = Value("GET_WEB_PAYMENT_DETAILS")
    val CALLBACK_PAYMENT = Value("CALLBACK_PAYMENT")
    val ORDER_THREEDS = Value("ORDER_THREEDS")
  }

  class TransactionStepRef extends TypeReference[TransactionStep.type]

  case class CreditCard(uuid: String,
    number: String,
    holder: String,
    expiryDate: java.util.Date,
    @JsonScalaEnumeration(classOf[CreditCardTypeRef]) cardType: CreditCardType.CreditCardType,
    hiddenNumber: String,
    account: Mogopay.Document,
    var dateCreated: Date = Calendar.getInstance().getTime,
    var lastUpdated: Date = Calendar.getInstance().getTime)

  case class IdGenerator(val uuid: String,
    idVendor: Long,
    date: java.util.Date,
    idTransaction: Long,
    var dateCreated: Date = Calendar.getInstance().getTime,
    var lastUpdated: Date = Calendar.getInstance().getTime)

  case class PaymentConfig(kwixoParam: Option[String],
    paypalParam: Option[String],
    applePayParam: Option[String],
    cbParam: Option[String],
    @JsonScalaEnumeration(classOf[CBPaymentProviderRef]) cbProvider: CBPaymentProvider.CBPaymentProvider,
    @JsonScalaEnumeration(classOf[CBPaymentMethodRef]) paymentMethod: CBPaymentMethod.CBPaymentMethod,
    emailField: String = "user_email",
    passwordField: String = "user_password",
    senderName: Option[String],
    senderEmail: Option[String],
    callbackPrefix: Option[String],
    passwordPattern: Option[String],
    var dateCreated: Date = Calendar.getInstance().getTime,
    var lastUpdated: Date = Calendar.getInstance().getTime,
    pwdEmailContent: Option[String] = None,
    pwdEmailSubject: Option[String] = None,
    groupPaymentInfo: Option[GroupPaymentInfo] = None)

  case class GroupPaymentInfo(returnURLforNextPayers: String,
    successURL: String,
    failureURL: String)

  case class Country(uuid: String,
    code: String,
    isoCode3: String,
    isoNumericCode: String,
    name: String,
    shipping: Boolean,
    billing: Boolean,
    zipCodeRegex: Option[String],
    currencyCode: Option[String],
    currencyNumericCode: Option[String],
    currencyName: Option[String],
    phoneCode: Option[String],
    var dateCreated: Date = Calendar.getInstance().getTime,
    var lastUpdated: Date = Calendar.getInstance().getTime)

  case class CountryRef(uuid: String, code: String)

  case class CountryAdminRef(uuid: String, code: String)

  case class CountryAdmin(uuid: String,
    code: Option[String],
    name: Option[String],
    level: Int,
    country: CountryRef,
    parentCountryAdmin1: Option[CountryAdminRef],
    parentCountryAdmin2: Option[CountryAdminRef],
    var dateCreated: Date = Calendar.getInstance().getTime,
    var lastUpdated: Date = Calendar.getInstance().getTime)

  case class Telephone(phone: String,
    lphone: String,
    isoCode: String,
    pinCode3: Option[String],
    @JsonScalaEnumeration(classOf[TelephoneStatusRef]) status: TelephoneStatus.TelephoneStatus)

  case class AccountAddress(road: String,
    road2: Option[String] = None,
    city: String,
    zipCode: Option[String] = None,
    extra: Option[String] = None,
    @JsonScalaEnumeration(classOf[CivilityRef]) civility: Option[Civility.Civility] = None,
    firstName: Option[String] = None,
    lastName: Option[String] = None,
    company: Option[String] = None,
    telephone: Option[Telephone] = None,
    country: Option[String] = None,
    admin1: Option[String] = None,
    admin2: Option[String] = None,
    geoCoordinates: Option[String] = None)

  case class Account(uuid: String,
      email: String,
      company: Option[String] = None,
      website: Option[String] = None,
      password: String,
      @JsonScalaEnumeration(classOf[CivilityRef]) civility: Option[Civility.Civility] = None,
      firstName: Option[String] = None,
      lastName: Option[String] = None,
      birthDate: Option[java.util.Date] = None,
      address: Option[AccountAddress] = None,
      @JsonScalaEnumeration(classOf[AccountStatusRef]) status: AccountStatus.AccountStatus,
      loginFailedCount: Int = 0,
      waitingPhoneSince: Long = -1L,
      waitingEmailSince: Long = -1L,
      extra: Option[String] = None,
      lastLogin: Option[java.util.Date] = None,
      paymentConfig: Option[PaymentConfig] = None,
      country: Option[Country] = None,
      @JsonScalaEnumeration(classOf[RoleNameRef]) roles: List[RoleName.RoleName] = Nil,
      owner: Option[Mogopay.Document] = None,
      emailingToken: Option[String] = None,
      shippingAddresses: List[ShippingAddress] = Nil,
      secret: String,
      creditCards: List[CreditCard] = Nil,
      walletId: Option[String] = None,
      var dateCreated: Date = Calendar.getInstance().getTime,
      var lastUpdated: Date = Calendar.getInstance().getTime) {

    lazy val isCustomer: Boolean = {
      hasRoleName(RoleName.CUSTOMER)
    }

    lazy val isMerchant: Boolean = {
      hasRoleName(RoleName.MERCHANT)
    }

    private def hasRoleName(roleName: RoleName.RoleName): Boolean = roles.contains(roleName)
  }

  case class ShippingAddress(uuid: String,
    active: Boolean = false,
    address: AccountAddress)

  object ShippingPriceError extends Enumeration {
    type ShippingPriceError = Value
    val UNKNOWN         = Value("UNKNOWN")
    val SHIPPING_ZONE_NOT_ALLOWED = Value("SHIPPING_ZONE_NOT_ALLOWED")
    val SHIPPING_TYPE_NOT_ALLOWED = Value("SHIPPING_TYPE_NOT_ALLOWED")
    val INTERNATIONAL_SHIPPING_NOT_ALLOWED = Value("INTERNATIONAL_SHIPPING_NOT_ALLOWED")
    val NO_COMPAGNY_ADDRESS = Value("NO_COMPAGNY_ADDRESS")
  }
  class ShippingPriceErrorRef extends TypeReference[ShippingPriceError.type]

  case class ShippingDataList(@JsonScalaEnumeration(classOf[ShippingPriceErrorRef]) error: Option[ShippingPriceError.ShippingPriceError],
                              shippingPrices: List[ShippingData]) {
    val hasError = !error.isEmpty
    val empty = !hasError && shippingPrices.isEmpty

    def findById(id: String) = shippingPrices.find(_.id == id)
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class ShippingData(shippingAddress: AccountAddress,
    shipmentId: String,
    rateId: String,
    provider: String,
    service: String,
    rateType: String,
    price: Long,
    currencyCode: String,
    currencyFractionDigits: Int,
    confirm: Boolean = false,
    trackingCode: Option[String] = None,
    extra: Option[String] = None,
    trackingHistory: List[String] = Nil,
    id: String = UUID.randomUUID().toString)

  case class ExternalShippingDataList(@JsonScalaEnumeration(classOf[ShippingPriceErrorRef]) error: Option[ShippingPriceError.ShippingPriceError],
                              shippingPrices: List[ExternalShippingData]) {
    val hasError = !error.isEmpty
    val empty = !hasError && shippingPrices.isEmpty
  }

  case class ExternalShippingData(externalCode: ExternalCode, shippingData: ShippingData)

  case class ShippingCart(internalShippingPrices: ShippingDataList,
                          externalShippingPricesByCartItemId: Map[String, ExternalShippingDataList]) {
    val hasError = internalShippingPrices.hasError || externalShippingPricesByCartItemId.exists{_._2.hasError}
  }

  case class SelectShippingCart(shippingAddress: AccountAddress,
                                internalShippingPrice: Option[ShippingData],
                                externalShippingPriceByCode: Map[ExternalCode, ShippingData]) {
    val price = internalShippingPrice.map{_.price}.getOrElse(0L) + externalShippingPriceByCode.map { _._2.price }.sum
  }

  case class ModificationStatus(uuid: String,
    xdate: java.util.Date,
    ipAddr: Option[String],
    @JsonScalaEnumeration(classOf[TransactionStatusRef]) oldStatus: Option[TransactionStatus.TransactionStatus],
    @JsonScalaEnumeration(classOf[TransactionStatusRef]) newStatus: Option[TransactionStatus.TransactionStatus],
    comment: Option[String],
    var dateCreated: Date = Calendar.getInstance().getTime,
    var lastUpdated: Date = Calendar.getInstance().getTime)

  case class BOTransactionLog(uuid: String,
    direction: String,
    log: String,
    provider: String,
    transaction: Mogopay.Document,
    @JsonScalaEnumeration(classOf[TransactionStepRef]) step: TransactionStep.TransactionStep,
    var dateCreated: Date = Calendar.getInstance().getTime,
    var lastUpdated: Date = Calendar.getInstance().getTime)

  case class BOPaymentData(@JsonScalaEnumeration(classOf[PaymentTypeRef]) paymentType: PaymentType.PaymentType,
    @JsonScalaEnumeration(classOf[CBPaymentProviderRef]) cbProvider: CBPaymentProvider.CBPaymentProvider,
    transactionSequence: Option[String],
    orderDate: Option[java.util.Date],
    @JsonScalaEnumeration(classOf[ResponseCode3DSRef]) status3DS: Option[ResponseCode3DS.ResponseCode3DS],
    transactionId: Option[String],
    authorizationId: Option[String])

  case class BOCreditCard(number: String,
    holder: Option[String],
    expiryDate: java.util.Date,
    @JsonScalaEnumeration(classOf[CreditCardTypeRef]) cardType: CreditCardType.CreditCardType)

  case class TransactionUser(email: String, amount: Long, status: PaymentStatus.PaymentStatus, master: Boolean)

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class BOTransaction(uuid: String,
    transactionUUID: String,
    groupTransactionUUID: Option[String] = None,
    @JsonDeserialize(contentAs = classOf[java.lang.Long]) groupPaymentExpirationDate: Option[Long] = None,
    groupPaymentRefundPercentage: Int = 100,
    authorizationId: String,
    transactionDate: Option[java.util.Date],
    amount: Long,
    mogobizAmount: Long,
    currency: CartRate,
    @JsonScalaEnumeration(classOf[TransactionStatusRef]) status: TransactionStatus.TransactionStatus,
    endDate: Option[java.util.Date],
    paymentData: BOPaymentData,
    merchantConfirmation: Boolean = false,
    email: Option[String],
    errorCodeOrigin: Option[String],
    errorMessageOrigin: Option[String],
    extra: Option[String],
    description: Option[String],
    gatewayData: Option[String],
    creditCard: Option[BOCreditCard],
    shippingInfo: Option[String],
    shippingData: Option[ShippingData],
    vendor: Option[Account],
    customer: Option[Account],
    modifications: List[ModificationStatus],
    paymentConfig: Option[PaymentConfig],
    var dateCreated: Date = Calendar.getInstance().getTime,
    var lastUpdated: Date = Calendar.getInstance().getTime)

  case class TransactionRequest(uuid: String,
    tid: Long,
    groupTransactionUUID: Option[String] = None,
    groupPaymentExpirationDate: Option[Long] = None,
    groupPaymentRefundPercentage: Int = 100,
    amount: Long,
    currency: CartRate,
    vendorUuid: Mogopay.Document,
    var dateCreated: Date = Calendar.getInstance().getTime,
    var lastUpdated: Date = Calendar.getInstance().getTime)

  case class Rate(uuid: String,
    currencyCode: String,
    activationDate: Date,
    currencyRate: Double = 0.01,
    currencyFractionDigits: Integer = 2,
    var dateCreated: Date = Calendar.getInstance().getTime,
    var lastUpdated: Date = Calendar.getInstance().getTime)

  case class TransactionSequence(uuid: String,
    transactionId: Long,
    var dateCreated: Date = Calendar.getInstance().getTime,
    var lastUpdated: Date = Calendar.getInstance().getTime)

  case class CancelRequest(id: String, currency: CartRate)

  case class CancelResult(id: String, status: PaymentStatus.PaymentStatus, errorCodeOrigin: String, errorMessageOrigin: Option[String])

  case class RefundResult(status: PaymentStatus.PaymentStatus, errorCode: String, errorMessage: Option[String])

  case class PaymentResult(transactionSequence: String,
    orderDate: Date,
    amount: Long,
    ccNumber: String,
    cardType: CreditCardType.CreditCardType,
    expirationDate: Date,
    cvv: String,
    gatewayTransactionId: String,
    transactionDate: Date,
    transactionCertificate: String,
    authorizationId: String,
    status: PaymentStatus.PaymentStatus,
    errorCodeOrigin: String,
    errorMessageOrigin: Option[String],
    data: String,
    bankErrorCode: String,
    bankErrorMessage: Option[String],
    token: String,
    errorShipment: Option[String])

  case class ValidatePaymentResult(status: PaymentStatus.PaymentStatus,
                                 gatewayTransactionId: String,
                                 transactionDate: Date)

  case class PaymentRequest(uuid: String,
    transactionSequence: String,
    orderDate: Date,
    amount: Long,
    ccNumber: String,
    holder: String,
    cardType: CreditCardType.CreditCardType,
    expirationDate: Date,
    cvv: String,
    paylineMd: String,
    paylinePares: String,
    transactionEmail: String,
    transactionExtra: CartWithShipping,
    transactionDesc: String,
    gatewayData: String,
    csrfToken: String,
    currency: CartRate,
    groupPaymentExpirationDate: Option[Long] = None,
    groupPaymentRefundPercentage: Int = 100,
    var dateCreated: Date = Calendar.getInstance().getTime,
    var lastUpdated: Date = Calendar.getInstance().getTime)

  case class SessionData(uuid: String = java.util.UUID.randomUUID.toString,
    var authenticated: Boolean = false,
    var email: Option[String] = None,
    var mogopay: Boolean = false,
    var isMerchant: Boolean = false,
    var merchantSession: Boolean = false,
    var accountId: Option[Mogopay.Document] = None,
    var finished: Boolean = false,
    var transactionUuid: Option[String] = None,
    var transactionType: Option[String] = None,
    var paymentConfig: Option[PaymentConfig] = None,
    var amount: Option[Long] = None,
    var merchantId: Option[Mogopay.Document] = None,
    var errorURL: Option[String] = None,
    var successURL: Option[String] = None,
    var cardinfoURL: Option[String] = None,
    var cvvURL: Option[String] = None,
    var token: Option[String] = None,
    var password: Option[String] = None,
    var csrfToken: Option[String] = None,
    var o3dSessionId: Option[String] = None,
    var cardSave: Boolean = false,
    var waitFor3DS: Boolean = false,
    var pageNum: Integer = 0,
    var shippingCart: Option[ShippingCart] = None,
    var selectShippingCart: Option[SelectShippingCart] = None,
    var id3d: Option[String] = None,
    var payers: Map[String, Long] = Map(),
    var groupTxUUID: Option[String] = None,
    var paymentRequest: Option[PaymentRequest] = None,
    var locale: Option[String] = None,
    var ipAddress: Option[String] = None,
    var cart: Option[Cart] = None)

  case class ShippingParcel(height: Double, width: Double, length: Double, weight: Double)

  case class CartWithShipping(count: Int,
    shippingPrice: Long,
    rate: CartRate,
    price: Long = 0,
    endPrice: Long = 0,
    mogobizFinalPrice: Long = 0,
    taxAmount: Long = 0,
    reduction: Long = 0,
    finalPrice: Long = 0,
    cartItems: List[CartItem] = Nil,
    coupons: List[Coupon] = Nil,
    customs: Map[String, Any])

object EnumUnmarshaller {

  implicit def RoleNameUnmarshaller = new FromStringDeserializer[RoleName.RoleName] {
    def apply(value: String) =
      try Right(RoleName.withName(value)) catch {
        case NonFatal(ex) => Left(MalformedContent(s"Cannot parse: $value", ex))
      }
  }

}