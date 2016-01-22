/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.payment

import java.io.File
import java.text.{ DateFormat, NumberFormat, SimpleDateFormat }
import java.util.{ List => _, _ }

import com.mogobiz.es.EsClient
import com.mogobiz.json.JacksonConverter
import com.mogobiz.pay.codes.MogopayConstant
import com.mogobiz.pay.common._
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.exceptions.Exceptions._
import com.mogobiz.pay.handlers.EmailHandler.Mail
import com.mogobiz.pay.handlers.shipping.{ ShippingHandler, ShippingPrice }
import com.mogobiz.pay.handlers.{ EmailHandler, UtilHandler }
import com.mogobiz.pay.implicits.Implicits._
import com.mogobiz.pay.model.Mogopay.CBPaymentProvider.CBPaymentProvider
import com.mogobiz.pay.model.Mogopay.CreditCardType.CreditCardType
import com.mogobiz.pay.model.Mogopay.PaymentType.PaymentType
import com.mogobiz.pay.model.Mogopay.ResponseCode3DS.ResponseCode3DS
import com.mogobiz.pay.model.Mogopay.TransactionStatus.TransactionStatus
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.pay.model.ParamRequest
import com.mogobiz.utils.GlobalUtil._
import com.mogobiz.utils.{ GlobalUtil, SymmetricCrypt }
import com.sksamuel.elastic4s.ElasticDsl._
import org.apache.commons.lang.LocaleUtils
import org.elasticsearch.common.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTime
import org.json4s.JsonAST.{ JField, JObject }
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.collection.{ Map, _ }
import scala.util._

case class Submit(sessionData: SessionData, params: SubmitParams, actionName: Option[String], csrfToken: Option[String])

case class SubmitParams(successURL: String, errorURL: String, cardinfoURL: Option[String], authURL: Option[String],
    cvvURL: Option[String], transactionUUID: String, amount: Long, merchantId: String,
    transactionType: String, customerCVV: Option[String], ccNum: Option[String],
    customerEmail: Option[String], customerPassword: Option[String],
    transactionDescription: Option[String], gatewayData: Option[String],
    ccMonth: Option[String], ccYear: Option[String], ccType: Option[String],
    ccStore: Option[Boolean], private val _payers: Option[String], groupTxUUID: Option[String], locale: Option[String]) {
  def payers: Map[String, Long] = _payers.map { payers =>
    queryStringToMap(payers, sep = ",", elementsSep = ":").mapValues(_.toLong).map(identity)
  }.getOrElse(Map())
}

class TransactionHandler {

  def searchByCustomer(uuid: String): Seq[BOTransaction] = {
    val req = search in Settings.Mogopay.EsIndex -> "BOTransaction" postFilter {
      termFilter("customer.uuid", uuid)
    } from 0 size EsClient.MAX_SIZE
    EsClient.searchAll[BOTransaction](req)
  }

  def init(params: ParamRequest.TransactionInit, cart: Cart): String = {
    //    (rateHandler findByCurrencyCode params.currencyCode map { rate: Rate =>
    (accountHandler findBySecret params.merchantSecret map { vendor: Account =>
      if (!vendor.roles.contains(RoleName.MERCHANT)) {
        throw NotAVendorAccountException("")
      } else {
        //          val txSeqId = transactionSequenceHandler.nextTransactionId(vendor.uuid)
        //          val txReqUUID = newUUID
        //
        //          val txCurrency = TransactionCurrency(params.currencyCode, currency.getNumericCode, params.currencyRate, rate.currencyFractionDigits)
        //          val txRequest = TransactionRequest(txReqUUID, txSeqId, params.transactionAmount, params.extra, txCurrency, vendor.uuid)
        val txRequest = createTxReqForInit(vendor, params, cart.rate, None, params.groupPaymentExpirationDate, params.groupPaymentRefundPercentage)
        transactionRequestHandler.save(txRequest, false)
        txRequest.uuid
      }
    }).getOrElse(throw AccountDoesNotExistException("Invalid merchant secret"))
    //    }).getOrElse(throw CurrencyCodeNotFoundException(s"${params.currencyCode} not found"))
  }

  def createTxReqForInit(merchant: Account, params: ParamRequest.TransactionInit,
    rate: CartRate, groupTxUUID: Option[String],
    groupPaymentExpirationDate: Option[Long], groupPaymentRefundPercentage: Option[Int]): TransactionRequest = {
    val txSeqId = transactionSequenceHandler.nextTransactionId(merchant.uuid)
    val txReqUUID = newUUID

    TransactionRequest(txReqUUID, txSeqId, groupTxUUID, groupPaymentExpirationDate, groupPaymentRefundPercentage.getOrElse(100),
      params.transactionAmount, rate, merchant.uuid)
  }

  def startPayment(vendorId: String, sessionData: SessionData, transactionRequestUUID: String,
    paymentRequest: PaymentRequest, paymentType: PaymentType, cbProvider: CBPaymentProvider) = {
    accountHandler.load(vendorId).map { account =>
      val customer = sessionData.accountId.map { uuid => accountHandler.load(uuid) }.getOrElse(None)
      val extra = serializeCart(paymentRequest.transactionExtra)
      var transaction = BOTransaction(
        transactionRequestUUID,
        transactionRequestUUID,
        sessionData.groupTxUUID,
        paymentRequest.groupPaymentExpirationDate,
        paymentRequest.groupPaymentRefundPercentage,
        "",
        Option(new Date),
        paymentRequest.amount,
        paymentRequest.transactionExtra.rate,
        TransactionStatus.INITIATED,
        None,
        BOPaymentData(paymentType, cbProvider, None, None, None, None, None),
        merchantConfirmation = false,
        Option(paymentRequest.transactionEmail),
        None,
        None,
        Option(extra),
        Option(paymentRequest.transactionDesc),
        Option(paymentRequest.gatewayData),
        None,
        None,
        None,
        Option(account),
        customer,
        Nil)

      if (paymentType == PaymentType.CREDIT_CARD &&
        account.paymentConfig.exists(_.paymentMethod != CBPaymentMethod.EXTERNAL)) {
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
      boTransactionHandler.save(transaction, refresh = false)
      Success(transaction)
    }.getOrElse(Failure(new InvalidContextException("Vendor not found")))
  }

  def updateStatus(transactionUUID: String, ipAddress: Option[String],
    newStatus: TransactionStatus, comment: Option[String] = None) {
    val maybeTx = boTransactionHandler.find(transactionUUID)
    maybeTx.map { transaction =>
      val modStatus = ModificationStatus(
        uuid = newUUID,
        xdate = new Date,
        ipAddr = ipAddress.orElse(transaction.modifications.collectFirst({ case e => e.ipAddr }).flatten),
        oldStatus = Option(transaction.status),
        newStatus = Option(newStatus),
        comment = comment
      )

      val newTx = transaction.copy(
        status = newStatus,
        endDate = computeEndDate(newStatus),
        modifications = transaction.modifications :+ modStatus
      )

      boTransactionHandler.update(newTx, refresh = false)
    }.getOrElse(throw TransactionNotFoundException(transactionUUID))
  }

  def updateStatus3DS(transactionUUID: String, ipAddress: Option[String], status3DS: ResponseCode3DS, codeRetour: String) {
    val maybeTx = boTransactionHandler.find(transactionUUID)
    maybeTx.map { transaction =>
      val modification = ModificationStatus(
        uuid = newUUID,
        xdate = new Date,
        oldStatus = Option(transaction.status),
        newStatus = Option(TransactionStatus.THREEDS_TESTED),
        comment = Option(codeRetour),
        ipAddr = ipAddress.orElse(transaction.modifications.collectFirst({ case e => e.ipAddr }).flatten)
      )

      val newTx = transaction.copy(
        status = TransactionStatus.THREEDS_TESTED,
        endDate = computeEndDate(TransactionStatus.THREEDS_TESTED),
        paymentData = transaction.paymentData.copy(status3DS = Option(status3DS)),
        modifications = transaction.modifications :+ modification
      )

      boTransactionHandler.update(newTx, false)
    }.getOrElse(Failure(BOTransactionNotFoundException(s"$transactionUUID")))
  }

  // called by other handlers
  def finishPayment(transactionUUID: String, newStatus: TransactionStatus,
    paymentResult: PaymentResult, returnCode: String, locale: Option[String], gatewayData: Option[String] = None): Unit = {
    //    val modification = ModificationStatus(newUUID, new Date, None, Option(transaction.status), Option(newStatus), Option(returnCode))
    updateStatus(transactionUUID, None, newStatus, Option(returnCode))

    val transaction = boTransactionHandler.find(transactionUUID)
      .getOrElse(throw BOTransactionNotFoundException(s"$transactionUUID"))

    val newTx = transaction.copy(
      status = newStatus,
      endDate = computeEndDate(newStatus),
      authorizationId = paymentResult.authorizationId,
      errorCodeOrigin = Option(paymentResult.errorCodeOrigin),
      errorMessageOrigin = paymentResult.errorMessageOrigin,
      gatewayData = gatewayData
    )

    val tx = if (paymentResult.transactionDate != null) {
      val finalTrans = newTx.copy(transactionDate = Option(paymentResult.transactionDate))
      boTransactionHandler.update(finalTrans, refresh = false)
      notifyPaymentFinished(finalTrans.copy(extra = None), finalTrans.extra.getOrElse("{}"), locale)
      finalTrans
    } else {
      boTransactionHandler.update(newTx, false)
      notifyPaymentFinished(newTx.copy(extra = None), newTx.extra.getOrElse("{}"), locale)
      newTx
    }

    Success()
  }

  def notifyPaymentFinished(transaction: BOTransaction, jsonCart: String, locale: Option[String]): Unit = {
    try {
      val jcart = parse(jsonCart)
      val jtransaction = Extraction.decompose(transaction)
      val json = jtransaction merge jcart
      val jsonString = compact(render(json))
      transaction.vendor.map { vendor =>
        val template = templateHandler.loadTemplateByVendor(Some(vendor), "mail-order", locale)
        val (subject, body) = templateHandler.mustache(template, jsonString)
        EmailHandler.Send(
          Mail(
            (transaction.vendor.get.email -> s"""${transaction.vendor.get.firstName.getOrElse("")} ${transaction.vendor.get.lastName.getOrElse("")}"""),
            List(transaction.email.get), List(), List(), subject, body, None, None
          ))
      } getOrElse (throw VendorNotProvidedError("Transaction cannot exist without a vendor"))
    } catch {
      case e: Throwable => if (!Settings.Mogopay.Anonymous) throw e
    }
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

  def verify(secret: String, amount: Option[Long], transactionUUID: String): (BOTransaction, Seq[TransactionRequest]) = {
    val transaction = boTransactionHandler.find(transactionUUID).getOrElse(throw TransactionNotFoundException(s"$transactionUUID"))

    if (transaction.vendor.get.secret != secret)
      throw InvalidMerchantAccountException("")

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
      boTransactionHandler.update(newTx, false)

      val transactions = transactionRequestHandler.findByGroupTxUUID(transactionUUID)

      (newTx, transactions)
    }
  }

  def shippingPrices(cart: Cart, accountId: String): Seq[ShippingPrice] = {
    val maybeCustomer = accountHandler.load(accountId)

    val customer = maybeCustomer.getOrElse(throw AccountDoesNotExistException(s"$accountId"))

    val address = shippingAddressHandler.findByAccount(customer.uuid).find(_.active)

    address.map(addr => ShippingHandler.calculatePrice(addr, cart)).getOrElse(Seq[ShippingPrice]())
  }

  def selectShippingPrice(sessionData: SessionData, accountId: String, shipmentId: String, rateId: String): ShippingPrice = {
    shippingAddressHandler.findByAccount(accountId).find(_.active).map { clientAddress =>
      sessionData.shippingPrices.map { shippingPrices: List[ShippingPrice] =>
        val shippingPriceOpt = transactionHandler.shippingPrice(shippingPrices, shipmentId, rateId)
        sessionData.selectShippingPrice = shippingPriceOpt
        shippingPriceOpt.map { shippingPrice =>
          sessionData.cart.map { cart =>
            cart.compagnyAddress.map { companyAddress =>
              if (!companyAddress.shippingInternational && companyAddress.country != clientAddress.address.country.getOrElse("")) {
                throw ShippingInternationalUnauthorized()
              }
            }
          }
          shippingPrice
        }.getOrElse { throw SelectedShippingPriceNotFound() }
      }.getOrElse { throw NoShippingPriceFound() }
    }.getOrElse(throw NoActiveShippingAddressFound())
  }

  def shippingPrice(prices: Seq[ShippingPrice], shipmentId: String, rateId: String): Option[ShippingPrice] = {
    prices.find {
      price =>
        price.shipmentId.equals(shipmentId) && price.rateId.equals(rateId)
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
   *         3. Mogopay Payment
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
    var transactionUUID = Option(submit.params.transactionUUID)
    var errorURL = Option(submit.params.errorURL)
    var successURL = Option(submit.params.successURL)
    var transactionType = Option(submit.params.transactionType)
    var amount = Option(submit.params.amount)
    val cardinfoURL = submit.params.cardinfoURL
    val authURL = submit.params.authURL
    val cvvURL = submit.params.cvvURL
    val sessionData = submit.sessionData
    var cardStore = submit.params.ccStore.getOrElse(false)

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
        accountHandler.load(sessionData.merchantId.get).getOrElse(throw VendorNotFoundException())
      } else {
        accountHandler.load(submit.params.merchantId).getOrElse(throw VendorNotFoundException())
      }

    val transactionRequest = transactionRequestHandler.find(transactionUUID.get).getOrElse(
      throw TransactionRequestNotFoundException(s"${transactionUUID.get}"))

    if (transactionRequest.vendor != vendor.uuid)
      throw TransactionRequestWasInitiatedByAnotherMerchantException()

    if (sessionData.payers.size > 1 && transactionRequest.groupPaymentExpirationDate.isEmpty)
      throw NotAGroupPaymentException()

    //    if (sessionData.payers.size < 2 && transactionRequest.groupPaymentExpirationDate.isDefined)
    //      throw MissingPayersForGroupPaymentException()
    //
    if (transactionRequest.amount != amount.get) {
      throw UnexpectedAmountException(s"${amount.get}")
    }

    val cart = sessionData.cart.getOrElse {
      if (Settings.Mogopay.Anonymous)
        Cart(count = 0,
          rate = CartRate(code = "EUR", numericCode = 978, rate = 0.01, fractionDigits = 2),
          price = 0,
          endPrice = 0,
          taxAmount = 0,
          reduction = 0,
          finalPrice = 0,
          cartItems = Array(),
          coupons = Array(),
          customs = immutable.Map[String, Any]())
      else
        throw InvalidContextException("Cart isn't set.")

    }
    var selectedShippingPrice: Option[ShippingPrice] = sessionData.selectShippingPrice

    if (!Settings.Mogopay.Anonymous) {
      if (sessionData.shippingPrices.getOrElse(throw InvalidContextException("The shippings list wasn't computed.")).nonEmpty
        && sessionData.selectShippingPrice.isEmpty) {
        throw InvalidContextException("Shipping price cannot be empty")
      }
    }

    val shippingPrice = sessionData.selectShippingPrice.map { _.price }.getOrElse(0L)

    val cartWithShipping = CartWithShipping(cart.count,
      shippingPrice,
      cart.rate,
      cart.price,
      cart.endPrice,
      cart.taxAmount,
      cart.reduction,
      cart.finalPrice + shippingPrice,
      cart.cartItems,
      cart.coupons,
      cart.customs)

    transactionRequestHandler.delete(transactionRequest.uuid, refresh = false)

    val transaction: Option[BOTransaction] = boTransactionHandler.find(transactionUUID.get)
    if (transaction.isDefined)
      throw TheBOTransactionAlreadyExistsException(s"${transactionUUID.get}")

    def checkParameters(vendor: Account): Boolean = {
      def checkBCParameters(paymentConfig: PaymentConfig): Boolean = {
        (CBPaymentProvider.NONE != paymentConfig.cbProvider
          && paymentConfig.cbParam.isDefined
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
    sessionData.locale = submit.params.locale
    if (submit.params.customerEmail.isDefined) sessionData.email = submit.params.customerEmail
    sessionData.password = submit.params.customerPassword

    if (!sessionData.mogopay)
      sessionData.mogopay = cardStore && (sessionData.authenticated || submit.params.customerPassword.nonEmpty || (submit.params.ccNum.isEmpty && submit.params.customerCVV.nonEmpty))

    sessionData.accountId.map {
      customerId =>
        // User is a mogopay user, he has authenticated and is coming back from the cardinfo screen
        if (submit.params.ccNum.nonEmpty && cardStore) {
          val customer = accountHandler.load(customerId).orNull
          // Mogopay avec une nouvele carte
          val ccNum = submit.params.ccNum.orNull
          val ccMonth = submit.params.ccMonth.orNull
          val ccYear = submit.params.ccYear.orNull
          val ccType = toCardType(submit.params.ccType.orNull)
          val simpleDateFormat = new SimpleDateFormat("ddMMyy")
          val expiryDate = simpleDateFormat.parse(s"01$ccMonth$ccYear")
          val cc = CreditCard(GlobalUtil.newUUID, SymmetricCrypt.encrypt(ccNum, Settings.Mogopay.Secret, "AES"), submit.params.customerEmail.getOrElse(""), expiryDate, ccType, UtilHandler.hideCardNumber(ccNum, "X"), customer.uuid)
          val cust2 = customer.copy(creditCards = List(cc))
          accountHandler.update(cust2, refresh = false)
        }
    }
    //    if (cardinfoURL.nonEmpty && authURL.nonEmpty && successURL.nonEmpty && errorURL.nonEmpty &&
    //      cvvURL.nonEmpty && submit.params.customerEmail.isEmpty && submit.params.customerCVV.isEmpty) {
    //      // Mogopay has to determine what to do.
    //      // if user is authenticated, then route him to cvv
    //      // if user is not authenticated then route him to auth screen
    //      // from auth screen, he will be routed to cardinfo_url if the card is not present in his account
    //      if (sessionData.authenticated) {
    //        (null, cvvURL.get)
    //      }
    //      else {
    //        (null, authURL.get)
    //      }
    //
    //    }
    if (sessionData.mogopay) {
      // this is a mogopay payment
      if (transactionType.getOrElse("CREDIT_CARD") == "CREDIT_CARD") {
        // only credit card payments are supported through mogopay
        if (submit.params.customerCVV.nonEmpty) {
          val customer = accountHandler.load(sessionData.accountId.get).orNull
          val card = customer.creditCards.head
          val cardNum = SymmetricCrypt.decrypt(card.number, Settings.Mogopay.CardSecret, "AES")
          val cardMonth = new SimpleDateFormat("MM").format(card.expiryDate)
          val cardYear = new SimpleDateFormat("yyyy").format(card.expiryDate)
          val paymentRequest = initPaymentRequest(vendor, transactionType, mogopay = true, transactionRequest.tid,
            cartWithShipping, sessionData, submit.params.transactionDescription.orNull,
            submit.params.gatewayData.orNull, submit.params.customerCVV.orNull, cardNum, cardMonth, cardYear,
            card.cardType, transactionRequest.groupPaymentExpirationDate)
          sessionData.paymentRequest = Some(paymentRequest)

          // user is already authenticated. We check start the mogopayment
          // the user has been authenticated at this step.
          //forward(controller: "mogopay", action: "startPayment", params: params + [xtoken: sessionData.csrfToken])
          //Success((buildNewSubmit(submit, newSession), "startPayment"))
          ("mogopay", "start")
        } else {
          // User submitted a password we authenticate him
          // we redirect the user to authentication screen
          // but we need first to recreate the transaction request
          transactionRequestHandler.save(transactionRequest, refresh = false)
          ("mogopay", "authenticate")
        }
      } else {
        throw NotACreditCardTransactionException(s"$transactionType")
      }
    } else {
      val paymentRequest = initPaymentRequest(vendor, transactionType, mogopay = false, transactionRequest.tid,
        cartWithShipping, sessionData, submit.params.transactionDescription.orNull,
        submit.params.gatewayData.orNull, submit.params.customerCVV.orNull, submit.params.ccNum.orNull,
        submit.params.ccMonth.orNull, submit.params.ccYear.orNull, toCardType(submit.params.ccType.orNull),
        transactionRequest.groupPaymentExpirationDate)
      sessionData.paymentRequest = Some(paymentRequest)
      val handler = if (submit.sessionData.transactionType.contains("CREDIT_CARD")) {
        sessionData.paymentConfig.get.cbProvider.toString.toLowerCase
      } else {
        sessionData.transactionType.get.toLowerCase
      }
      (handler, "start")
    }
  }

  def download(accountId: String, transactionUuid: String, pageFormat: String, langCountry: String): File = {
    val optTransaction = EsClient.load[BOTransaction](Settings.Mogopay.EsIndex, transactionUuid)
    optTransaction match {
      case Some(transaction) => {
        if (transaction.status == TransactionStatus.PAYMENT_CONFIRMED) {
          val jsonString = BOTransactionJsonTransform.transform(transaction, LocaleUtils.toLocale(langCountry))
          val template = templateHandler.loadTemplateByVendor(transaction.vendor, "download-bill", Some(langCountry))
          val (subject, body) = templateHandler.mustache(template, jsonString)
          pdfHandler.convertToPdf(pageFormat, body)
        } else throw new PaymentNotConfirmedException(transactionUuid)
      }
      case None => throw new BOTransactionNotFoundException(transactionUuid)
    }
  }

  private def initPaymentRequest(vendor: Account, transactionType: Option[String], mogopay: Boolean,
    transactionSequence: Long, cart: CartWithShipping, sessionData: SessionData,
    transactionDesc: String, gatewayData: String, ccCrypto: String, card_number: String,
    card_month: String, card_year: String, card_type: CreditCardType,
    groupPaymentExpirationDate: Option[Long]): PaymentRequest = {
    var errors: mutable.Seq[Exception] = mutable.Seq()

    var cc_type: CreditCardType = null
    val transactionEmail: String = sessionData.email.orNull
    var cc_num: String = null
    var cc_month: String = null
    var cc_year: String = null

    cc_type = card_type
    cc_month = card_month
    cc_year = card_year
    //    cc_num = if (card_number != null) card_number.replaceAll(" ", "") else ""
    //    val maskedCCNumber = hideStringExceptLastN(cc_num)
    cc_num = if (card_number != null) card_number.replaceAll(" ", "") else card_number
    val maskedCCNumber = if (cc_num == null) null else hideStringExceptLastN(cc_num)

    val amount: Long = sessionData.amount.getOrElse(0L)
    val externalPages: Boolean = vendor.paymentConfig.orNull.paymentMethod == CBPaymentMethod.EXTERNAL
    var paymentProvider = CBPaymentProvider.NONE
    var paymentRequest: PaymentRequest = PaymentRequest(UUID.randomUUID.toString, "-1", null, -1L, maskedCCNumber, "",
      null, null, "", "", "", "", cart, "", "",
      sessionData.csrfToken.orNull, cart.rate, groupPaymentExpirationDate)

    if (transactionType.getOrElse("CREDIT_CARD") == "CREDIT_CARD" && (!externalPages || mogopay)) {
      paymentProvider = vendor.paymentConfig.orNull.cbProvider
      if (cc_type == null) {
        throw SomeParameterIsMissingException(MogopayConstant.CreditCardTypeRequired)
      } else if (cc_num == null) {
        throw SomeParameterIsMissingException(MogopayConstant.CreditCardNumRequired)
      } else if (cc_month == null) {
        throw SomeParameterIsMissingException(MogopayConstant.CreditCardMonthRequired)
      } else if (cc_year == null) {
        throw SomeParameterIsMissingException(MogopayConstant.CreditCardYearRequired)
      } else if (ccCrypto == null) {
        throw SomeParameterIsMissingException(MogopayConstant.CreditCardCryptoRequired)
      } else {
        // Construction et controle de la date de validite
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, cc_month.toInt - 1)
        cal.set(Calendar.YEAR, cc_year.toInt)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.MONTH))

        // ce n'est pas à nous de contrôler la date de validité. C'est au prestataire de paiement
        val cc_date = cal.getTime
        if (cc_date.compareTo(new Date()) < 0) {
          throw SomeParameterIsMissingException(MogopayConstant.CreditCardExpiryDateInvalid)
        } else {
          paymentRequest = paymentRequest.copy(
            cardType = cc_type,
            ccNumber = cc_num, // we don't store the credit card number (for security purpose)
            expirationDate = cc_date,
            cvv = ccCrypto
          )
        }
      }
    }

    paymentRequest.copy(
      transactionEmail = transactionEmail,
      transactionSequence = transactionSequence.toString,
      orderDate = new Date,
      amount = amount,
      transactionDesc = transactionDesc,
      gatewayData = gatewayData
    )
  }

  def initGroupPayment(token: String): (Account, TransactionRequest, String, String, String) = {
    val decryptedToken = SymmetricCrypt.decrypt(token, Settings.Mogopay.Secret, "AES")
    val (expirationDate, txUUID, customerUUID, groupTxUUID, successURL, failureURL) = decryptedToken.split('|').toList match {
      case a :: b :: c :: d :: e :: f :: Nil => (a, b, c, d, e, f)
      case _ => throw new InvalidTokenException("")
    }

    if ((new Date).after(new Date(java.lang.Long.parseLong(expirationDate)))) {
      throw new TokenExpiredException
    }

    val txReq = transactionRequestHandler.find(txUUID).getOrElse(throw new BOTransactionNotFoundException(txUUID))
    val account = accountHandler.find(customerUUID).getOrElse(throw new AccountDoesNotExistException(""))

    (account, txReq, groupTxUUID, successURL, failureURL)
  }

  def refund(merchantSecret: String, boTransactionUUID: String, maybeAmount: Option[Long] = None) {
    implicit def longToBigDecimal(n: Long): java.math.BigDecimal = new java.math.BigDecimal(n * 1.0)

    type Params = (PaymentConfig, BOTransaction, Long)
    val handlers = Map(
      CBPaymentProvider.AUTHORIZENET -> ((p: Params) => authorizeNetHandler.refund(p._1, p._2, p._3)),
      CBPaymentProvider.SYSTEMPAY -> ((p: Params) => systempayHandler.refund(p._1, p._2, p._3)),
      CBPaymentProvider.PAYLINE -> ((p: Params) => paylineHandler.refund(p._1, p._2, p._3)),
      CBPaymentProvider.PAYBOX -> ((p: Params) => payboxHandler.refund(p._1, p._2, p._3)),
      CBPaymentProvider.SIPS -> ((p: Params) => sipsHandler.refund(p._1, p._2, p._3))
    )

    val merchant = accountHandler.findBySecret(merchantSecret).getOrElse(throw new VendorNotFoundException)
    val paymentConfig = merchant.paymentConfig.getOrElse(throw new PaymentConfigNotFoundException)
    val boTransaction = boTransactionHandler.find(boTransactionUUID).getOrElse(
      throw new BOTransactionNotFoundException(boTransactionUUID))

    if (maybeAmount.exists(_ > boTransaction.amount))
      throw new TheRefundAmountIsHigherThanTheInitialAmountException()

    if (boTransaction.status == TransactionStatus.CUSTOMER_REFUNDED) {
      throw new PaymentAlreadyRefundedException()
    }

    if (boTransaction.paymentData.cbProvider == CBPaymentProvider.NONE) {
      throw new RefundNotSupportedException()
    }

    val amount: Long = maybeAmount.getOrElse(
      (boTransaction.amount.toFloat * boTransaction.groupPaymentRefundPercentage / 100).toLong)

    val call = handlers.getOrElse(paymentConfig.cbProvider, throw new RefundNotSupportedException)
    val refundResult = call(paymentConfig, boTransaction, amount)

    if (refundResult.status == PaymentStatus.REFUNDED) {
      updateStatus(boTransaction.uuid, None, TransactionStatus.CUSTOMER_REFUNDED)
    } else {
      val message = refundResult.errorMessage.map(s => s" — $s").getOrElse("")
      throw new RefundException(s"[${
        paymentConfig.cbProvider
      }] ${
        refundResult.errorCode
      }$message")
    }
  }

  def refundGroupPayments() = {
    val transactions = boTransactionHandler.findAllGroupTransactions()
    transactions
      .filter(tx => tx.groupPaymentExpirationDate.exists(_ * 1000 <= (new Date).getTime))
      .filter(tx => tx.status == TransactionStatus.PAYMENT_CONFIRMED)
      .foreach(transaction => transactionHandler.refund(transaction.vendor.get.secret, transaction.uuid))
  }

  private def serializeCart(cart: CartWithShipping): String = {
    JacksonConverter.serialize(cart)
  }
}

object BOTransactionJsonTransform {

  def transform(transaction: BOTransaction, locale: Locale) = {
    val json = Extraction.decompose(transaction)
    transformJValue(json, locale)
  }

  def transformJValue(jsonTransaction: JValue, locale: Locale) = {
    compact(render(jsonTransaction.transform(transformBOTransaction(locale))))
  }

  private def transformBOTransaction(locale: Locale): PartialFunction[JValue, JValue] = {
    case obj: JObject => {
      (obj \ "transactionUUID",
        obj \ "transactionDate",
        obj \ "amount",
        obj \ "currency" \ "code",
        obj \ "currency" \ "fractionDigits",
        obj \ "status" \ "name",
        obj \ "paymentData" \ "paymentType" \ "name",
        obj \ "email",
        obj \ "extra") match {
          case (JString(transactionUuid),
            JString(transactionDate),
            JInt(amount),
            JString(currencyCode),
            JInt(fractionDigits),
            JString(status),
            JString(paymentType),
            JString(email),
            JString(extra)) => {
            JObject(
              JField("transactionUuid", JString(transactionUuid)),
              JField("transactionDate", JString(formatDateTime(locale, transactionDate))),
              JField("amount", JString(formatPrice(locale, amount, currencyCode, fractionDigits))),
              JField("status", JString(status)),
              JField("paymentType", JString(paymentType)),
              JField("email", JString(email)),
              JField("cart", transformCart(parse(extra).extract[CartWithShipping], locale))
            )
          }
          case _ => obj
        }
    }
  }

  private def transformCart(cart: CartWithShipping, locale: Locale): JValue = {
    val currencyCode: String = cart.rate.code
    val fractionDigits: Int = cart.rate.fractionDigits
    JObject(
      JField("shipping", JString(formatPrice(locale, cart.shippingPrice, currencyCode, fractionDigits))),
      JField("price", JString(formatPrice(locale, cart.price, currencyCode, fractionDigits))),
      JField("taxAmount", JString(formatPrice(locale, cart.taxAmount, currencyCode, fractionDigits))),
      JField("endPrice", JString(formatPrice(locale, cart.endPrice, currencyCode, fractionDigits))),
      JField("reduction", JString(formatPrice(locale, cart.reduction, currencyCode, fractionDigits))),
      JField("finalPrice", JString(formatPrice(locale, cart.finalPrice, currencyCode, fractionDigits))),
      JField("cartItems", JArray(cart.cartItems.toList.map { cartItem => transformCartItem(cartItem, locale, currencyCode, fractionDigits) })),
      JField("coupons", JArray(cart.coupons.toList.map { coupon => transformCoupon(coupon, locale, currencyCode, fractionDigits) }))
    ).merge(Extraction.decompose(cart.customs))
  }

  private def transformCartItem(cartItem: CartItem, locale: Locale, currencyCode: String, fractionDigits: Int): JValue = {
    JObject(
      JField("quantity", JInt(cartItem.quantity)),
      JField("price", JString(formatPrice(locale, cartItem.price, currencyCode, fractionDigits))),
      JField("endPrice", JString(formatPrice(locale, cartItem.endPrice, currencyCode, fractionDigits))),
      JField("tax", JDouble(cartItem.tax)),
      JField("taxAmount", JString(formatPrice(locale, cartItem.taxAmount, currencyCode, fractionDigits))),
      JField("totalPrice", JString(formatPrice(locale, cartItem.totalPrice, currencyCode, fractionDigits))),
      JField("totalEndPrice", JString(formatPrice(locale, cartItem.totalEndPrice, currencyCode, fractionDigits))),
      JField("totalTaxAmount", JString(formatPrice(locale, cartItem.totalTaxAmount, currencyCode, fractionDigits))),
      JField("salePrice", JString(formatPrice(locale, cartItem.salePrice, currencyCode, fractionDigits))),
      JField("saleEndPrice", JString(formatPrice(locale, cartItem.saleEndPrice, currencyCode, fractionDigits))),
      JField("saleTaxAmount", JString(formatPrice(locale, cartItem.saleTaxAmount, currencyCode, fractionDigits))),
      JField("saleTotalPrice", JString(formatPrice(locale, cartItem.saleTotalPrice, currencyCode, fractionDigits))),
      JField("saleTotalEndPrice", JString(formatPrice(locale, cartItem.saleTotalEndPrice, currencyCode, fractionDigits))),
      JField("saleTotalTaxAmount", JString(formatPrice(locale, cartItem.saleTotalTaxAmount, currencyCode, fractionDigits))),
      JField("registeredCartItems", JArray(cartItem.registeredCartItems.toList.map { registeredCartItem => transformRegisteredCartItem(registeredCartItem, locale, currencyCode, fractionDigits) })),
      JField("shipping", cartItem.shipping.map { shipping => transformShipping(shipping, locale, currencyCode, fractionDigits) }.getOrElse(JNothing))
    ).merge(Extraction.decompose(cartItem.customs))
  }

  private def transformShipping(shipping: Shipping, locale: Locale, currencyCode: String, fractionDigits: Int): JValue = {
    JObject(
      JField("weight", JInt(shipping.weight)),
      JField("weightUnit", JString(shipping.weightUnit)),
      JField("width", JInt(shipping.width)),
      JField("height", JInt(shipping.height)),
      JField("depth", JInt(shipping.depth)),
      JField("linearUnit", JString(shipping.linearUnit)),
      JField("amount", JInt(shipping.amount)),
      JField("free", JBool(shipping.free))
    ).merge(Extraction.decompose(shipping.customs))
  }

  private def transformRegisteredCartItem(registeredCartItem: RegisteredCartItem, locale: Locale, currencyCode: String, fractionDigits: Int): JValue = {
    JObject(
      JField("email", JString(registeredCartItem.email)),
      JField("firstname", registeredCartItem.firstname.map { firstname => JString(firstname) }.getOrElse(JNothing)),
      JField("lastname", registeredCartItem.lastname.map { lastname => JString(lastname) }.getOrElse(JNothing)),
      JField("phone", registeredCartItem.phone.map { phone => JString(phone) }.getOrElse(JNothing)),
      JField("birthdate", registeredCartItem.birthdate.map { birthdate => JString(formatDate(locale, birthdate)) }.getOrElse(JNothing))
    ).merge(Extraction.decompose(registeredCartItem.customs))
  }

  private def transformCoupon(coupon: Coupon, locale: Locale, currencyCode: String, fractionDigits: Int): JValue = {
    JObject(
      JField("code", JString(coupon.code)),
      JField("startDate", coupon.startDate.map { date => JString(formatDate(locale, date)) }.getOrElse(JNothing)),
      JField("endDate", coupon.endDate.map { date => JString(formatDate(locale, date)) }.getOrElse(JNothing)),
      JField("price", JString(formatPrice(locale, coupon.price, currencyCode, fractionDigits))),
      JField("customs", Extraction.decompose(coupon.customs))
    )
  }

  private def transformExtra(locale: Locale, currencyCode: String, fractionDigits: BigInt): PartialFunction[JField, JField] = {
    case JField("salePrice", JInt(salePrice)) => JField("salePrice", JString(formatPrice(locale, salePrice, currencyCode, fractionDigits)))
    case JField("saleEndPrice", JInt(saleEndPrice)) => JField("saleEndPrice", JString(formatPrice(locale, saleEndPrice, currencyCode, fractionDigits)))
    case JField("totalPrice", JInt(totalPrice)) => JField("totalPrice", JString(formatPrice(locale, totalPrice, currencyCode, fractionDigits)))
    case JField("totalEndPrice", JInt(totalEndPrice)) => JField("totalEndPrice", JString(formatPrice(locale, totalEndPrice, currencyCode, fractionDigits)))
    case JField("saleTotalPrice", JInt(saleTotalPrice)) => JField("saleTotalPrice", JString(formatPrice(locale, saleTotalPrice, currencyCode, fractionDigits)))
    case JField("saleTotalEndPrice", JInt(saleTotalEndPrice)) => JField("saleTotalEndPrice", JString(formatPrice(locale, saleTotalEndPrice, currencyCode, fractionDigits)))
    case JField("startDate", JString(startDate)) => JField("startDate", JString(formatDate(locale, startDate)))
    case JField("endDate", JString(endDate)) => JField("endDate", JString(formatDate(locale, endDate)))
    case JField("birthdate", JString(birthdate)) => JField("birthdate", JString(formatDate(locale, birthdate)))
  }

  private def formatDate(locale: Locale, value: String) = {
    val date = ISODateTimeFormat.dateTime().parseDateTime(value)
    val formatter = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
    formatter.format(date.toDate)
  }

  private def formatDateTime(locale: Locale, value: String) = {
    val date = Try(ISODateTimeFormat.dateTime().parseDateTime(value)) match {
      case Success(d) => d
      case _ => ISODateTimeFormat.dateTimeNoMillis().parseDateTime(value)
    }
    val formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale);
    formatter.format(date.toDate)
  }

  private def formatDate(locale: Locale, value: DateTime) = {
    val formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale);
    formatter.format(value.toDate)
  }

  private def formatPrice(locale: Locale, amount: BigInt, currencyCode: String, fractionDigits: BigInt) = {
    val numberFormat = NumberFormat.getCurrencyInstance(locale)
    numberFormat.setCurrency(Currency.getInstance(currencyCode))
    numberFormat.format(amount.toLong / Math.pow(10, fractionDigits.toLong))
  }

  private def formatPrice(locale: Locale, amount: Long, currencyCode: String, fractionDigits: Int) = {
    val numberFormat = NumberFormat.getCurrencyInstance(locale)
    numberFormat.setCurrency(Currency.getInstance(currencyCode))
    numberFormat.format(amount / Math.pow(10, fractionDigits.toLong))
  }
}

