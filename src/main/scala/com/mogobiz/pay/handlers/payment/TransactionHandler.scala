package com.mogobiz.pay.handlers.payment

import java.text.SimpleDateFormat
import java.util.{Calendar, Currency, Date}

import com.sksamuel.elastic4s.ElasticDsl._
import com.mogobiz.pay.actors.TransactionActor.Submit
import com.mogobiz.pay.codes.MogopayConstant
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.settings.Settings
import com.mogobiz.es.EsClient
import com.mogobiz.pay.exceptions.Exceptions._
import com.mogobiz.pay.handlers.UtilHandler
import com.mogobiz.pay.handlers.shipping.{ShippingService, ShippingPrice}
import com.mogobiz.pay.implicits.Implicits
import com.mogobiz.pay.model.Mogopay.CBPaymentProvider.CBPaymentProvider
import com.mogobiz.pay.model.Mogopay.CreditCardType.CreditCardType
import com.mogobiz.pay.model.Mogopay.PaymentType.PaymentType
import com.mogobiz.pay.model.Mogopay.ResponseCode3DS.ResponseCode3DS
import com.mogobiz.pay.model.Mogopay.TransactionStatus.TransactionStatus
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.utils.{SymmetricCrypt, RSA, GlobalUtil}
import com.mogobiz.utils.GlobalUtil._
import org.json4s.JValue
import org.json4s.jackson.JsonMethods._
import scala.collection._
import scala.util._
import Implicits._
import com.mogobiz.es.{Settings => esSettings}

class TransactionHandler {
  def searchByCustomer(uuid: String): Seq[BOTransaction] = {
    val req = search in esSettings.ElasticSearch.Index -> "BOTransaction" filter {
      termFilter("customer.uuid", uuid)
    }
    EsClient.searchAll[BOTransaction](req)
  }

  def init(secret: String, amount: Long, currencyCode: String,
           currencyRate: Double, extra: Option[String]): String = {
    (rateHandler findByCurrencyCode currencyCode map { rate: Rate =>
      val currency = Currency.getInstance(currencyCode)
      (accountHandler findBySecret secret map { vendor: Account =>
        if (!vendor.roles.contains(RoleName.MERCHANT)) {
          throw NotAVendorAccountException("")
        } else {
          val txSeqId = transactionSequenceHandler.nextTransactionId(vendor.uuid)
          val txReqUUID = newUUID

          val txCurrency = TransactionCurrency(currencyCode, currency.getNumericCode, currencyRate, rate.currencyFractionDigits)
          val txRequest = TransactionRequest(txReqUUID, txSeqId, amount, extra, txCurrency, vendor.uuid)

          EsClient.index(txRequest)
          txReqUUID
        }
      }).getOrElse(throw AccountDoesNotExistException(""))
    }).getOrElse(throw CurrencyCodeNotFoundException(""))
  }

  /*
			transaction.cbProvider = CBPaymentProvider.NONE;

   */
  def startPayment(vendorId: String, transactionUUID: String, paymentRequest: PaymentRequest,
                   paymentType: PaymentType, cbProvider: CBPaymentProvider) = {
    accountHandler.load(vendorId).map { account =>
      var transaction = BOTransaction(transactionUUID, transactionUUID, "", Option(new Date), paymentRequest.amount,
        paymentRequest.currency, TransactionStatus.INITIATED, new Date, None,
        BOPaymentData(paymentType, cbProvider, None, None, None, None, None),
        false,
        Option(paymentRequest.transactionEmail), None, None, Option(paymentRequest.transactionExtra),
        Option(paymentRequest.transactionDesc), None, Option(account), None, Nil)

      if (paymentType == PaymentType.CREDIT_CARD &&
        account.paymentConfig.map(_.paymentMethod) != Some(CBPaymentMethod.EXTERNAL)) {
        val creditCard = BOCreditCard(
          number = UtilHandler.hideCardNumber(paymentRequest.ccNumber, "X"),
          holder = None,
          expiryDate = paymentRequest.expirationDate,
          cardType = paymentRequest.cardType
        )
        transaction = transaction.copy(creditCard = Option(creditCard))
      }
      transaction = transaction.copy(
        paymentData = transaction.paymentData.copy(
          transactionSequence = Option(paymentRequest.transactionSequence),
          orderDate = Option(paymentRequest.orderDate)
        )
      )
      EsClient.index(transaction, false)
      Success(transaction)
    }.getOrElse(Failure(new InvalidContextException("Vendor not foundu")))
  }

  def updateStatus(vendorId: String, transactionUUID: String, ipAddress: String, newStatus: TransactionStatus, comment: String): Unit = {
    val maybeTx = EsClient.load[BOTransaction](transactionUUID)
    maybeTx.map { transaction =>
      val modStatus: ModificationStatus = ModificationStatus(
        uuid = newUUID,
        xdate = new Date,
        ipAddr = Option(ipAddress),
        oldStatus = Option(transaction.status),
        newStatus = Option(newStatus),
        comment = Option(comment)
      )

      val newTx = transaction.copy(
        status = newStatus,
        endDate = computeEndDate(newStatus),
        modifications = transaction.modifications :+ modStatus
      )

      EsClient.update(newTx, true, false)
    }.getOrElse(throw TransactionNotFoundException(""))
  }

  def updateStatus3DS(vendorId: String, transactionUUID: String, status3DS: ResponseCode3DS, codeRetour: String) {
    val maybeTx = EsClient.load[BOTransaction](transactionUUID)
    maybeTx.map { transaction =>
      val modification = ModificationStatus(
        uuid = newUUID,
        xdate = new Date,
        oldStatus = Option(transaction.status),
        newStatus = Option(TransactionStatus.THREEDS_TESTED),
        comment = Option(codeRetour),
        ipAddr = None
      )

      val newTx = transaction.copy(
        status = TransactionStatus.THREEDS_TESTED,
        endDate = computeEndDate(TransactionStatus.THREEDS_TESTED),
        paymentData = transaction.paymentData.copy(status3DS = Option(status3DS)),
        modifications = transaction.modifications :+ modification
      )

      EsClient.update(newTx, true, false)
    }.getOrElse(Failure(BOTransactionNotFoundException(s"$transactionUUID")))
  }

  def finishPayment(vendorId: String, transactionUUID: String, newStatus: TransactionStatus,
                    paymentResult: PaymentResult, returnCode: String): Unit = {
    val transaction = EsClient.load[BOTransaction](transactionUUID)
    transaction.map { transaction: BOTransaction =>
      val modification = ModificationStatus(newUUID, new Date, None, Option(transaction.status), Option(newStatus), Option(returnCode))
      val newTx = transaction.copy(
        status = newStatus,
        endDate = computeEndDate(newStatus),
        authorizationId = paymentResult.authorizationId,
        errorCodeOrigin = Option(paymentResult.errorCodeOrigin),
        errorMessageOrigin = paymentResult.errorMessageOrigin,
        modifications = transaction.modifications :+ modification
      )

      if (paymentResult.transactionDate != null)
        EsClient.update(newTx.copy(transactionDate = Option(paymentResult.transactionDate)), true, false)
      else
        EsClient.index(newTx, false)
      Success()
    }.getOrElse(throw BOTransactionNotFoundException(s"$transactionUUID"))
  }

  private def toCardType(xtype: String): CreditCardType = {
    import com.mogobiz.pay.model.Mogopay.CreditCardType._
    val `type`: String = if (xtype == null) "CB" else xtype.toUpperCase
    `type` match {
      case "CB" => CB
      case "VISA" | "VISA_ELECTRON" => VISA
      case "AMEX" => AMEX
      case x if (x.startsWith("MASTERCARD")) => MASTER_CARD
      case _ => CB
    }
  }

  private def computeEndDate(status: TransactionStatus): Option[Date] = {
    import com.mogobiz.pay.model.Mogopay.TransactionStatus._
    status match {
      case PAYMENT_CONFIRMED | PAYMENT_REFUSED | CANCEL_CONFIRMED | CUSTOMER_REFUNDED => Some(new Date())
      case _ => None
    }
  }

  def verify(secret: String, amount: Option[Long], transactionUUID: String): BOTransaction = {
    val maybeVendor = accountHandler.findBySecret(secret)

    val account: Account = maybeVendor match {
      case None => throw AccountDoesNotExistException("secret=****")
      case Some(a) => a
    }

    val vendorUUID = if (account.roles.contains(RoleName.MERCHANT)) account.uuid else throw NotAVendorAccountException("secret=****")

    val transaction: BOTransaction = EsClient.load[BOTransaction](transactionUUID).getOrElse(throw TransactionNotFoundException(s"$transactionUUID"))


    val validatedTx = if (amount.map(transaction.amount == _).getOrElse(true)) transaction else throw UnexpectedAmountException(s"$amount")

    val txDate = validatedTx.endDate.map(_.getTime).getOrElse(-1L)
    val now = new Date().getTime
    val duration = (now - txDate) / 1000
    val durationOK = duration < Settings.TransactionDuration
    if (!durationOK) {
      throw TransactionTimeoutException(MogopayConstant.Timeout.toString)
    } else if (transaction.status != TransactionStatus.PAYMENT_CONFIRMED) {
      throw PaymentNotConfirmedException(MogopayConstant.PaymentNotConfirmed)
    } else if (transaction.merchantConfirmation) {
      throw TransactionAlreadyConfirmedException(MogopayConstant.TransactionAlreadyConfirmed)
    } else {
      val newTx = transaction.copy(merchantConfirmation = true)
      EsClient.update(newTx, false, false)
      newTx
    }
  }

  def shippingPrices(currencyCode: String, transactionExtra: String,
                     accountId: String): Seq[ShippingPrice] = {
    val maybeCustomer = accountHandler.load(accountId)

    val customer = maybeCustomer.getOrElse(throw AccountDoesNotExistException(s"$accountId"))

    val address = shippingAddressHandler.findByAccount(customer.uuid).find(_.active)

    address.map(addr => computePrice(addr, currencyCode, parse(transactionExtra))).getOrElse(Seq[ShippingPrice]())

  }

  def shippingPrice(prices: Seq[ShippingPrice], provider: String,
                    service: String, rateType: String): Option[ShippingPrice] = {
    prices.find {
      price =>
        price.provider.equals(provider) && price.service.equals(service) && price.rateType.equals(rateType)
    }
  }

  /**
   * @param submit
   * @return (ServiceName, methodName)
   *
   *         1. External Payment
   *         callback_success, callback_error, merchant_id, transaction_id, transaction_type, transaction_amount
   *         2. Custom Payment
   *         callback_success, callback_error, merchant_id, transaction_id, transaction_type, transaction_amount,
   *         card_type, card_month,card_year,card_cvv
   *         3. Mogpay Payment
   *         3.1 First URL (amount only)
   *         callback_success, callback_error, merchant_id, transaction_id, transaction_type, transaction_amount
   *         callback_cardinfo, callback_cvv, callback_auth
   *         3.2 second URL (come back from auth screen) - sent here when user was not authenticated
   *         callback_success, callback_error, merchant_id, transaction_id, transaction_type, transaction_amount
   *         callback_cardinfo, callback_cvv, callback_auth
   *         user_email, user_password
   *         3.3 third URL (come back from cvv screen) - sent here once user is authenticated
   *         callback_success, callback_error, merchant_id, transaction_id, transaction_type, transaction_amount
   *         callback_cardinfo, callback_cvv, callback_auth
   *         card_cvv
   */
  def submit(submit: Submit): (String, String) = {

    val mogopayAuth = false
    /*
            String merchantId = MogopayUtil.extractStringParam(params["merchant_id"]);
            String transactionType = MogopayUtil.extractStringParam(params["transaction_type"]);
     */
    var transactionUUID = submit.params.transactionUUID
    var errorURL = submit.params.errorURL
    var successURL = submit.params.successURL
    var transactionType = submit.params.transactionType
    var amount = submit.params.amount
    val cardinfoURL = submit.params.cardinfoURL
    val authURL = submit.params.authURL
    val cvvURL = submit.params.cvvURL
    val sessionData = submit.sessionData

    // The first time a user come, mogopay is false & cardinfo is true.
    // This definitely set the mogopay status for the whole session.

    val vendor =
      if (sessionData.accountId.isDefined && successURL.isEmpty && errorURL.isEmpty && cardinfoURL.isEmpty && authURL.isEmpty) {
        // user is authenticated and is coming back from the CVV screen.
        transactionUUID = sessionData.transactionUuid
        errorURL = sessionData.errorURL
        successURL = sessionData.successURL
        transactionType = sessionData.transactionType
        amount = sessionData.amount
        EsClient.load[Account](sessionData.merchantId.get).orNull
      } else if (submit.params.merchantId.isDefined) {
        EsClient.load[Account](submit.params.merchantId.get).orNull
      } else {
        throw AccountDoesNotExistException(s"${submit.params.merchantId.get}")
      }
    val transactionRequest: TransactionRequest = EsClient.load[TransactionRequest](transactionUUID.get).getOrElse(throw TransactionNotFoundException(s"${transactionUUID.get}"))
    if (transactionRequest.amount != amount.get) {
      throw UnexpectedAmountException(s"${amount.get}")
    }

    var transactionExtra = transactionRequest.extra.orNull

    val listShipping = sessionData.accountId.map {
      accountId =>
        shippingPrices(transactionRequest.currency.code, transactionExtra, accountId)
    } getOrElse (Seq[ShippingPrice]())

    var selectedShippingPrice: Option[ShippingPrice] = None
    if (listShipping.length > 0) {
      if (sessionData.selectShippingPrice.isEmpty) {
        throw InvalidContextException("Shipping price cannot be empty")
      } else {
        val sp = sessionData.selectShippingPrice.get
        selectedShippingPrice = shippingPrice(listShipping, sp.provider, sp.service, sp.rateType)

        if (selectedShippingPrice.isEmpty)
          throw InvalidContextException("Shipping Price cannot be empty")
      }
    }

    selectedShippingPrice.map { selectedShippingPrice =>
      val cart0: JValue = parse(transactionExtra) merge parse( s""""
        {
        "shipping" : ${
        selectedShippingPrice.price
      }
        }
        """)

      val cart1 = cart0 merge parse( s"""
        {
        "finalPrice" : ${
        (cart0 \ "finalPrice").extract[Long] + selectedShippingPrice.price
      }
        }
      """)
      transactionExtra = render(cart1).toString
    }

    val transactionCurrency: TransactionCurrency = transactionRequest.currency
    EsClient.delete[TransactionRequest](transactionRequest.uuid, false)

    val transaction: Option[BOTransaction] = EsClient.load[BOTransaction](transactionUUID.get)
    if (transaction.isDefined)
      throw BOTransactionNotFoundException(s"${transactionUUID.get}")

    def checkParameters(vendor: Account): Boolean = {
      def checkBCParameters(paymentConfig: PaymentConfig): Boolean = {
        (CBPaymentProvider.NONE != paymentConfig.cbProvider
          && paymentConfig.cbParam != None
          && paymentConfig.cbParam.exists(_ != ""))
      }

      val isMerchant = vendor.roles.contains(RoleName.MERCHANT)
      if (isMerchant && vendor.paymentConfig != null && vendor.status == AccountStatus.ACTIVE) {
        val paymentConfig = vendor.paymentConfig
        paymentConfig.exists(checkBCParameters)
      } else {
        false
      }
    }

    if (amount.getOrElse(0L) <= 0 && !mogopayAuth)
      throw SomeParameterIsMissingException("Not a mogopay payment and amount is missing.")
    if (transactionUUID.isEmpty)
      throw SomeParameterIsMissingException("transactionUUID is missing.")
    if (successURL.isEmpty)
      throw SomeParameterIsMissingException("successURL is missing.")
    if (errorURL.isEmpty)
      throw SomeParameterIsMissingException("errorURL is missing.")
    if (!checkParameters(vendor))
      throw SomeParameterIsMissingException("Invalid vendor parameters.")

    sessionData.amount = amount
    sessionData.transactionUuid = transactionUUID
    sessionData.errorURL = errorURL
    sessionData.successURL = successURL
    sessionData.cardinfoURL = submit.params.cardinfoURL
    sessionData.cvvURL = submit.params.cvvURL
    sessionData.transactionType = transactionType
    sessionData.merchantId = Some(vendor.uuid)
    sessionData.paymentConfig = vendor.paymentConfig
    sessionData.email = submit.params.customerEmail
    sessionData.password = submit.params.customerPassword

    if (!sessionData.mogopay)
      sessionData.mogopay = submit.params.customerPassword.nonEmpty || (submit.params.ccNum.isEmpty && submit.params.customerCVV.nonEmpty)

    sessionData.accountId.map {
      customerId =>
        // User is a mogopay user, he has authenticated and is coming back from the cardinfo screen
        val cust = EsClient.load[Account](customerId).orNull
        if (submit.params.ccNum.nonEmpty) {
          // Mogopay avec unen nouvele carte
          val ccNum = submit.params.ccNum.orNull
          val ccMonth = submit.params.ccMonth.orNull
          val ccYear = submit.params.ccYear.orNull
          val ccType = toCardType(submit.params.ccType.orNull)
          val simpleDateFormat = new SimpleDateFormat("ddMMyy")
          val expiryDate = simpleDateFormat.parse(s"01$ccMonth$ccYear")
          val cc = CreditCard(GlobalUtil.newUUID, SymmetricCrypt.encrypt(ccNum, Settings.ApplicationSecret, "AES"), submit.params.customerEmail.getOrElse(""), expiryDate, ccType, UtilHandler.hideCardNumber(ccNum, "X"), cust.uuid)
          val cust2 = cust.copy(creditCards = List(cc))
          EsClient.update(cust2, false, false)
        }
    }
    if (cardinfoURL.nonEmpty && authURL.nonEmpty && successURL.nonEmpty && errorURL.nonEmpty &&
      cvvURL.nonEmpty && submit.params.customerEmail.isEmpty && submit.params.customerCVV.isEmpty) {
      // Mogopay has to determine what to do.
      // if user is authenticated, then route him to cvv
      // if user is not authenticated then route him to auth screen
      // from auth screen, he will be routed to cardinfo_url if the card is not present in his account
      if (sessionData.authenticated) {
        (null, cvvURL.get)
      }
      else {
        (null, authURL.get)
      }

    }

    if (sessionData.mogopay) {
      // this is a mogopay payment
      if (transactionType.getOrElse("CREDIT_CARD") == "CREDIT_CARD") {
        // only credit card payments are supported through mogopay
        if (submit.params.customerCVV.nonEmpty) {
          val customer = EsClient.load[Account](sessionData.accountId.get).orNull
          val card = customer.creditCards(0)
          val cardNum = SymmetricCrypt.decrypt(card.number, Settings.ApplicationSecret, "AES")
          val cardMonth = new SimpleDateFormat("MM").format(card.expiryDate)
          val cardYear = new SimpleDateFormat("yyyy").format(card.expiryDate)
          val paymentRequest = initPaymentRequest(vendor, transactionType, true, transactionRequest.tid,
            transactionExtra, transactionCurrency, sessionData, submit.params.transactionDescription.orNull,
            submit.params.customerCVV.orNull, cardNum, cardMonth, cardYear, card.cardType)
          sessionData.paymentRequest = Some(paymentRequest)

          // user is already authenticated. We check start the mogopayment
          // the user has been authenticated at this step.
          //forward(controller: "mogopay", action: "startPayment", params: params + [xtoken: sessionData.csrfToken])
          //Success((buildNewSubmit(submit, newSession), "startPayment"))
          ("mogopay", "start")
        }
        else if (submit.params.customerPassword.nonEmpty) {
          // User submitted a password we authenticate him
          // we redirect the user to authentication screen
          // but we need first to recreate the transaction request
          EsClient.index(transactionRequest, false)
          //forward(controller: "mogopay", action: "authenticate", params: params + [xtoken: sessionData.csrfToken])
          //Success((build{ewSubmit(submit, newSession), "authenticate"))
          ("mogopay", "authenticate")
        }
        else {
          throw SomeParameterIsMissingException("CVV is missing")
        }
      }
      else {
        throw NotACreditCardTransactionException(s"${transactionType}")
      }
    }
    else {
      val paymentRequest = initPaymentRequest(vendor, transactionType, false, transactionRequest.tid,
        transactionExtra, transactionCurrency, sessionData, submit.params.transactionDescription.orNull,
        submit.params.customerCVV.orNull, submit.params.ccNum.orNull, submit.params.ccMonth.orNull, submit.params.ccYear.orNull,
        toCardType(submit.params.ccType.orNull))
      sessionData.paymentRequest = Some(paymentRequest)
      val handler = if (submit.sessionData.transactionType == Some("CREDIT_CARD")) {
        sessionData.paymentConfig.get.cbProvider.toString.toLowerCase
      }
      else {
        sessionData.transactionType.get.toLowerCase
      }
      (handler, "start")
    }
  }

  def computePrice(address: ShippingAddress, currencyCode: String, cart: JValue): Seq[ShippingPrice] = {
    val servicesList: Seq[ShippingService] = Seq(kialaShippingHandler)

    servicesList.map {
      service =>
        service.calculatePrice(address, currencyCode, cart)
    }.flatten
  }

  private def initPaymentRequest(vendor: Account, transactionType: Option[String], mogopay: Boolean,
                                 transactionSequence: Long, transactionExtra: String,
                                 transactionCurrency: TransactionCurrency, sessionData: SessionData,
                                 transactionDesc: String, ccCrypto: String, card_number: String,
                                 card_month: String, card_year: String, card_type: CreditCardType): PaymentRequest = {
    var errors: mutable.Seq[Exception] = mutable.Seq()

    var cc_type: CreditCardType = null
    val transactionEmail: String = sessionData.email.orNull
    var cc_num: String = null
    var cc_month: String = null
    var cc_year: String = null

    cc_type = card_type
    cc_month = card_month
    cc_year = card_year
    cc_num = if (card_number != null) card_number.replaceAll(" ", "") else card_number

    val amount: Long = sessionData.amount.getOrElse(0L)
    val externalPages: Boolean = vendor.paymentConfig.orNull.paymentMethod == CBPaymentMethod.EXTERNAL
    var paymentProvider = CBPaymentProvider.NONE
    var paymentRequest: PaymentRequest = PaymentRequest("-1", null, -1L, "", "",
      null, null, "", "", "", "", "", "",
      sessionData.csrfToken.orNull, transactionCurrency)

    if (transactionType.getOrElse("CREDIT_CARD") == "CREDIT_CARD" && (!externalPages || mogopay)) {
      paymentProvider = vendor.paymentConfig.orNull.cbProvider
      if (cc_type == null) {
        throw SomeParameterIsMissingException(MogopayConstant.CreditCardTypeRequired)
      }
      else if (cc_num == null) {
        throw SomeParameterIsMissingException(MogopayConstant.CreditCardNumRequired)
      }
      else if (cc_month == null) {
        throw SomeParameterIsMissingException(MogopayConstant.CreditCardMonthRequired)
      }
      else if (cc_year == null) {
        throw SomeParameterIsMissingException(MogopayConstant.CreditCardYearRequired)
      }
      else if (ccCrypto == null) {
        throw SomeParameterIsMissingException(MogopayConstant.CreditCardCryptoRequired)
      }
      else {
        // Construction et controle de la date de validite
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, cc_month.toInt - 1)
        cal.set(Calendar.YEAR, cc_year.toInt)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.MONTH))

        // ce n'est pas à nous de contrôler la date de validité. C'est au prestataire de paiement
        val cc_date = cal.getTime
        if (cc_date.compareTo(new Date()) < 0) {
          throw SomeParameterIsMissingException(MogopayConstant.CreditCardExpiryDateInvalid)
        }
        else {
          paymentRequest = paymentRequest.copy(
            cardType = cc_type,
            ccNumber = cc_num,
            expirationDate = cc_date,
            cvv = ccCrypto
          )
        }
      }
    }

    paymentRequest.copy(
      transactionEmail = transactionEmail,
      transactionExtra = transactionExtra,
      transactionSequence = transactionSequence.toString,
      orderDate = new Date,
      amount = amount
    )
  }
}