package mogopay.model

import com.fasterxml.jackson.module.scala.{JsonScalaEnumeration, DefaultScalaModule}
import mogopay.handlers.shipping.ShippingPrice
import mogopay.model.Mogopay._
import java.util.{Date, Calendar}
import mogopay.model.Mogopay.Account
import mogopay.model.Mogopay.AccountAddress
import mogopay.model.Mogopay.Telephone
import mogopay.model.Mogopay.AccountStatus
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import spray.httpx.unmarshalling.{FromStringDeserializer, MalformedContent}

object Mogopay {
  type Document = String

  object PaymentType extends Enumeration {
    type PaymentType = Value
    val NONE = Value("NONE")
    val CREDIT_CARD = Value("CREDIT_CARD")
    val PAYPAL = Value("PAYPAL")
  }

  class PaymentTypeRef extends TypeReference[PaymentType.type]

  import PaymentType._

  object AccountStatus extends Enumeration {
    type AccountStatus = Value
    val ACTIVE = Value("ACTIVE")
    val INACTIVE = Value("INACTIVE")
    val WAITING_ENROLLMENT = Value("WAITING_ENROLLMENT")
  }

  class AccountStatusRef extends TypeReference[AccountStatus.type]

  import AccountStatus._

  object CBPaymentMethod extends Enumeration {
    type CBPaymentMethod = Value
    val EXTERNAL = Value("EXTERNAL")
    val THREEDS_NO = Value("THREEDS_NO")
    val THREEDS_IF_AVAILABLE = Value("THREEDS_IF_AVAILABLE")
    val THREEDS_REQUIRED = Value("THREEDS_REQUIRED")
  }

  class CBPaymentMethodRef extends TypeReference[CBPaymentMethod.type]

  import CBPaymentMethod._

  object RoleName extends Enumeration {
    type RoleName = Value
    val ANONYMOUS = Value("ANONYMOUS")
    val ADMINISTRATOR = Value("ADMINISTRATOR")
    val CUSTOMER = Value("CUSTOMER")
    val MERCHANT = Value("MERCHANT")
  }

  class RoleNameRef extends TypeReference[RoleName.type]

  import RoleName._

  implicit def RoleNameUnmarshaller = new FromStringDeserializer[RoleName] {
    def apply(value: String) =
      try
        Right(RoleName.withName(value))
      catch {
        case ex: Throwable => Left(MalformedContent(s"Cannot parse: $value", ex))
      }
  }

  object TelephoneStatus extends Enumeration {
    type TelephoneStatus = Value
    val ACTIVE = Value("ACTIVE")
    val DELETED = Value("DELETED")
    val WAITING_ENROLLMENT = Value("WAITING_ENROLLMENT")
  }

  class TelephoneStatusRef extends TypeReference[TelephoneStatus.type]

  import TelephoneStatus._

  object Civility extends Enumeration {
    type Civility = Value
    val MR = Value("MR")
    val MRS = Value("MRS")
  }

  class CivilityRef extends TypeReference[Civility.type]

  import Civility._

  object ResponseCode3DS extends Enumeration {
    type ResponseCode3DS = Value
    val APPROVED = Value("APPROVED")
    val REFUSED = Value("REFUSED")
    val INVALID = Value("INVALID")
    val ERROR = Value("ERROR")
  }

  class ResponseCode3DSRef extends TypeReference[ResponseCode3DS.type]

  import ResponseCode3DS._

  object TransactionStatus extends Enumeration {
    type TransactionStatus = Value
    val INITIATED = Value("INITIATED")
    val VERIFICATION_THREEDS = Value("VERIFICATION_THREEDS")
    val THREEDS_TESTED = Value("THREEDS_TESTED")
    val PAYMENT_REQUESTED = Value("PAYMENT_REQUESTED")
    val PAYMENT_CONFIRMED = Value("PAYMENT_CONFIRMED")
    val PAYMENT_REFUSED = Value("PAYMENT_REFUSED")
    val CANCEL_REQUESTED = Value("CANCEL_REQUESTED")
    val CANCEL_FAILED = Value("CANCEL_FAILED")
    val CANCEL_CONFIRMED = Value("CANCEL_CONFIRMED")
    val CUSTOMER_REFUNDED = Value("CUSTOMER_REFUNDED")
  }

  class TransactionStatusRef extends TypeReference[TransactionStatus.type]

  import TransactionStatus._

  object CBPaymentProvider extends Enumeration {
    type CBPaymentProvider = Value
    val NONE = Value("NONE")
    val PAYLINE = Value("PAYLINE")
    val PAYBOX = Value("PAYBOX")
    val SIPS = Value("SIPS")
    val SYSTEMPAY = Value("SYSTEMPAY")
  }

  class CBPaymentProviderRef extends TypeReference[CBPaymentProvider.type]

  import CBPaymentProvider._

  object CreditCardType extends Enumeration {
    type CreditCardType = Value
    val CB = Value("CB")
    val VISA = Value("VISA")
    val MASTER_CARD = Value("MASTER_CARD")
    val DISCOVER = Value("DISCOVER")
    val AMEX = Value("AMEX")
    val SWITCH = Value("SWITCH")
    val SOLO = Value("SOLO")
    val OTHER = Value("OTHER")
  }

  class CreditCardTypeRef extends TypeReference[CreditCardType.type]

  import CreditCardType._

  object TokenValidity extends Enumeration {
    type TokenValidity = Value
    val VALID = Value("VALID")
    val INVALID = Value("INVALID")
    val EXPIRED = Value("EXPIRED")
  }


  object PaymentStatus extends Enumeration {
    type PaymentStatus = Value

    val PENDING = Value("PENDING")
    val INVALID = Value("INVALID")
    val FAILED = Value("FAILED")
    val COMPLETE = Value("COMPLETE")
    val CANCELED = Value("CANCELED")
    val CANCEL_FAILED = Value("CANCEL_FAILED")
  }

  import PaymentStatus._

  class TokenValidityRef extends TypeReference[TokenValidity.type]

  /*
  public class SessionData implements Serializable {
	public String email
	public boolean isMerchant
	public boolean merchantSession
	public long accountId
	public DemandePaiementVO paymentRequest
	public String transactionUuid
	public String transactionType
	public PaymentConfig paymentConfig
	public long amount
	public long vendorId
	public String errorURL
	public String successURL
	public String cardinfoURL
	public String token
	public String password
	public long customerId
	public String csrfToken
	public String o3dSessionId
	public boolean cardSave
	public int pageNum = 0
    public List<ShippingPrice> listShipping
    public ShippingPrice selectShippingPrice
}
   */

  case class CreditCard(uuid: String,
                        number: String,
                        holder: String,
                        expiryDate: java.util.Date,
                        @JsonScalaEnumeration(classOf[CreditCardTypeRef]) cardType: CreditCardType,
                        hiddenNumber: String,
                        account: Document,
                        var dateCreated: Date = Calendar.getInstance().getTime,
                        var lastUpdated: Date = Calendar.getInstance().getTime)

  case class IdGenerator(val uuid: String,
                         idVendor: Long,
                         date: java.util.Date,
                         idTransaction: Long,
                         var dateCreated: Date = Calendar.getInstance().getTime,
                         var lastUpdated: Date = Calendar.getInstance().getTime)

  case class PaymentConfig(kwixoParam: Option[String],
                           buysterParam: Option[String],
                           paypalParam: Option[String],
                           cbParam: Option[String],
                           sipsData: Option[String] = None,
                           @JsonScalaEnumeration(classOf[CBPaymentProviderRef]) cbProvider: CBPaymentProvider,
                           @JsonScalaEnumeration(classOf[CBPaymentMethodRef]) paymentMethod: CBPaymentMethod,
                           emailField: String = "user_email",
                           passwordField: String = "user_password",
                           pwdEmailContent: Option[String],
                           pwdEmailSubject: Option[String],
                           callbackPrefix: Option[String],
                           passwordPattern: Option[String],
                           var dateCreated: Date = Calendar.getInstance().getTime,
                           var lastUpdated: Date = Calendar.getInstance().getTime)

  case class Country(uuid: String,
                     code: String,
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
                       @JsonScalaEnumeration(classOf[TelephoneStatusRef]) status: TelephoneStatus)

  case class AccountAddress(road: String,
                            road2: Option[String] = None,
                            city: String,
                            zipCode: Option[String] = None,
                            extra: Option[String] = None,
                            @JsonScalaEnumeration(classOf[CivilityRef]) civility: Option[Civility] = None,
                            firstName: Option[String] = None,
                            lastName: Option[String] = None,
                            telephone: Option[Telephone] = None,
                            country: Option[String] = None,
                            admin1: Option[String] = None,
                            admin2: Option[String] = None)

  case class Account(uuid: String,
                     email: String,
                     company: Option[String] = None,
                     website: Option[String] = None,
                     password: String,
                     @JsonScalaEnumeration(classOf[CivilityRef]) civility: Option[Civility],
                     firstName: Option[String],
                     lastName: Option[String],
                     birthDate: Option[java.util.Date] = None,
                     address: Option[AccountAddress] = None,
                     @JsonScalaEnumeration(classOf[AccountStatusRef]) status: AccountStatus,
                     loginFailedCount: Int = 0,
                     waitingPhoneSince: Long = -1L,
                     waitingEmailSince: Long = -1L,
                     extra: Option[String] = None,
                     lastLogin: Option[java.util.Date] = None,
                     paymentConfig: Option[PaymentConfig] = None,
                     country: Option[Country] = None,
                     @JsonScalaEnumeration(classOf[RoleNameRef]) roles: List[RoleName] = Nil,
                     owner: Option[Document] = None,
                     emailingToken: Option[String] = None,
                     shippingAddresses: List[ShippingAddress] = Nil,
                     secret: String,
                     creditCards: List[CreditCard] = Nil,
                     walletId: Option[String] = None,
                     var dateCreated: Date = Calendar.getInstance().getTime,
                     var lastUpdated: Date = Calendar.getInstance().getTime)

  case class ShippingAddress(uuid: String,
                             active: Boolean = false,
                             address: AccountAddress)

  case class ModificationStatus(uuid: String,
                                xdate: java.util.Date,
                                ipAddr: Option[String],
                                @JsonScalaEnumeration(classOf[TransactionStatusRef]) oldStatus: Option[TransactionStatus],
                                @JsonScalaEnumeration(classOf[TransactionStatusRef]) newStatus: Option[TransactionStatus],
                                comment: Option[String],
                                //                                transaction: Document,
                                var dateCreated: Date = Calendar.getInstance().getTime,
                                var lastUpdated: Date = Calendar.getInstance().getTime)

  case class BOTransactionLog(uuid: String,
                              direction: String,
                              log: String,
                              provider: String,
                              transaction: Document,
                              var dateCreated: Date = Calendar.getInstance().getTime,
                              var lastUpdated: Date = Calendar.getInstance().getTime)

  case class BOPaymentData(@JsonScalaEnumeration(classOf[PaymentTypeRef]) paymentType: PaymentType,
                           @JsonScalaEnumeration(classOf[CBPaymentProviderRef]) cbProvider: CBPaymentProvider,
                           idCommandCB: Option[String],
                           dateCommandCB: Option[java.util.Date],
                           @JsonScalaEnumeration(classOf[ResponseCode3DSRef]) status3DS: Option[ResponseCode3DS],
                           transactionId: Option[String],
                           authorizationId: Option[String])

  case class BOCreditCard(number: String,
                          holder: Option[String],
                          expiryDate: java.util.Date,
                          @JsonScalaEnumeration(classOf[CreditCardTypeRef]) cardType: CreditCardType)

  case class BOTransaction(uuid: String,
                           transactionUUID: String,
                           authorizationId: String,
                           transactionDate: Option[java.util.Date],
                           amount: Long,
                           currency: TransactionCurrency,
                           @JsonScalaEnumeration(classOf[TransactionStatusRef]) status: TransactionStatus,
                           creationDate: java.util.Date,
                           endDate: Option[java.util.Date],
                           paymentData: BOPaymentData,
                           merchantConfirmation: Boolean = false,
                           email: Option[String],
                           errorCodeOrigin: Option[String],
                           errorMessageOrigin: Option[String],
                           extra: Option[String],
                           description: Option[String],
                           creditCard: Option[BOCreditCard],
                           vendor: Option[Account],
                           customer: Option[Account],
                           modifications: List[ModificationStatus],
                           var dateCreated: Date = Calendar.getInstance().getTime,
                           var lastUpdated: Date = Calendar.getInstance().getTime)

  case class TransactionCurrency(code: String,
                                 numericCode: Int,
                                 rate: Double = 0.01,
                                 fractionDigits: Int = 2)

  case class TransactionRequest(uuid: String,
                                tid: Long,
                                amount: Long,
                                extra: Option[String],
                                currency: TransactionCurrency,
                                vendor: Document,
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
                                 vendorId: Document,
                                 transactionId: Long,
                                 var dateCreated: Date = Calendar.getInstance().getTime,
                                 var lastUpdated: Date = Calendar.getInstance().getTime)

  case class CancelRequest(id: String, currency: TransactionCurrency)

  case class CancelResult(id: String, status: PaymentStatus, errorCodeOrigin: String, errorMessageOrigin: Option[String])

  case class PaymentResult(transactionSequence: String,
                           orderDate: Date,
                           amount: Long,
                           ccNumber: String,
                           cardType: CreditCardType,
                           expirationDate: Date,
                           cvv: String,
                           gatewayTransactionId: String,
                           transactionDate: Date,
                           transactionCertificate: String,
                           authorizationId: String,
                           status: PaymentStatus,
                           errorCodeOrigin: String,
                           errorMessageOrigin: Option[String],
                           data: String,
                           bankErrorCode: String,
                           bankErrorMessage: Option[String],
                           token: String)

  case class PaymentRequest(transactionSequence: String,
                            orderDate: Date,
                            amount: Long,
                            ccNumber: String,
                            holder: String,
                            cardType: CreditCardType,
                            expirationDate: Date,
                            cvv: String,
                            paylineMd: String,
                            paylinePares: String,
                            transactionEmail: String,
                            transactionExtra: String,
                            transactionDesc: String,
                            csrfToken: String,
                            currency: TransactionCurrency)

  case class SessionData(uuid: String,
                         var finished: Boolean = false,
                         var authenticated: Boolean = false,
                         var email: Option[String] = None,
                         var mogopay: Boolean = false,
                         var isMerchant: Boolean = false,
                         var merchantSession: Boolean = false,
                         var accountId: Option[Document] = None,
                         var paymentRequest: Option[PaymentRequest] = None,
                         var transactionUuid: Option[String] = None,
                         var transactionType: Option[String] = None,
                         var paymentConfig: Option[PaymentConfig] = None,
                         var amount: Option[Long] = None,
                         var vendorId: Option[Document] = None,
                         var errorURL: Option[String] = None,
                         var successURL: Option[String] = None,
                         var cardinfoURL: Option[String] = None,
                         var cvvURL: Option[String] = None,
                         var token: Option[String] = None,
                         var password: Option[String] = None,
                         var customerId: Option[Document] = None,
                         var csrfToken: Option[String] = None,
                         var o3dSessionId: Option[String] = None,
                         var cardSave: Boolean = false,
                         var waitFor3DS: Boolean = false,
                         var pageNum: Integer = 0,
                         var shippingPrices: Option[List[ShippingPrice]] = None,
                         var selectShippingPrice: Option[ShippingPrice] = None,
                         var id3d: Option[String] = None
                          )


}


object TestApp extends App {
  lazy val mapperSingleton = new ObjectMapper().registerModule(DefaultScalaModule)
  val account = Account(
    java.util.UUID.randomUUID().toString,
    "me@you.com",
    Some("ebiznext"),
    Some("http://www.ebiznext.com"),
    "changeit",
    Some(Civility.MR),
    Some("Me"),
    Some("You"),
    Some(Calendar.getInstance().getTime),
    Some(AccountAddress("Rue Meriau",
      Some("Tour Panorama"),
      "Paris",
      Some("75015"),
      None,
      Some(Civility.MR),
      Some("Me2"),
      Some("You2"),
      Some(Telephone(
        "0102030405",
        "3314567890987",
        "987",
        Some("123"),
        TelephoneStatus.ACTIVE)),
      Some("FRANCE"),
      Some("Ile De France"),
      Some("Paris"))),
    AccountStatus.ACTIVE,
    0,
    1000L,
    10000L,
    None,
    None,
    None,
    None,
    List(RoleName.ADMINISTRATOR, RoleName.CUSTOMER),
    None,
    Some("email token"),
    Nil,
    java.util.UUID.randomUUID().toString,
    Nil)

  val json = mapperSingleton.writerWithDefaultPrettyPrinter().writeValueAsString(account)
  println(json)
  val acc = mapperSingleton.readValue(json, classOf[Account])
  println(acc)
}
