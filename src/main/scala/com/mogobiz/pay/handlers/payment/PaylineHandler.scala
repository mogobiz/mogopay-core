/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.payment

import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import javax.xml.namespace.QName
import javax.xml.ws.{Binding, BindingProvider}

import com.experian.payline.ws.impl._
import com.experian.payline.ws.obj._
import com.experian.payline.ws.wrapper.WebPayment
import com.mogobiz.pay.codes.MogopayConstant
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.es.EsClient
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.exceptions.Exceptions._
import com.mogobiz.pay.handlers.UtilHandler
import com.mogobiz.pay.model.CreditCardType.CreditCardType
import com.mogobiz.pay.model.TransactionStep.TransactionStep
import com.mogobiz.pay.model.{ResponseCode3DS, TransactionStatus, _}
import com.mogobiz.utils.{GlobalUtil, NaiveHostnameVerifier, TrustedSSLFactory}
import com.mogobiz.utils.GlobalUtil._
import com.typesafe.scalalogging.StrictLogging
import org.json4s.jackson.JsonMethods._
import spray.http.Uri

import scala.util._
import scala.util.control.NonFatal

/**
  * @see com.ebiznext.mogopay.payment.PaylinePaymentService
  */
object PaylineHandler {
  val QUERY_STRING_SEP          = "&"
  val QUERY_STRING_ELEMENTS_SEP = "="

  def fromCreditCardType(`type`: CreditCardType): String = {
    var retour: String = "CB"
    if (CreditCardType.CB == `type`) {
      retour = "CB"
    } else if (CreditCardType.VISA == `type`) {
      retour = "CB"
    } else if (CreditCardType.MASTER_CARD == `type`) {
      retour = "MASTERCARD"
    } else if (CreditCardType.DISCOVER == `type`) {
      retour = "CB"
    } else if (CreditCardType.AMEX == `type`) {
      retour = "AMEX"
    } else if (CreditCardType.SWITCH == `type`) {
      retour = "SWITCH"
    } else if (CreditCardType.SOLO == `type`) {
      retour = "CB"
    }
    return retour
  }

  def toCreditCardType(`type`: String): CreditCardType = {
    var retour: CreditCardType = CreditCardType.CB
    if ("CB" == `type`) {
      retour = CreditCardType.VISA
    } else if ("MASTERCARD" == `type`) {
      retour = CreditCardType.MASTER_CARD
    } else if ("AMEX" == `type`) {
      retour = CreditCardType.AMEX
    } else if ("SWITCH" == `type`) {
      retour = CreditCardType.SWITCH
    }
    return retour
  }

  val ACTION_AUTHORISATION: String            = "100"
  val ACTION_AUTHORISATION_VALIDATION: String = "101"
  val ACTION_VALIDATION: String = "201"
  val ACTION_REFUND: String                   = "421"
  val MODE_COMPTANT: String                   = "CPT"
  val ServiceName: QName                      = new QName("http://impl.ws.payline.experian.com", "WebPaymentAPI")
}

class PaylineHandler(handlerName: String) extends PaymentHandler {
  PaymentHandler.register(handlerName, this)
  implicit val formats = new org.json4s.DefaultFormats {}
  val paymentType      = PaymentType.CREDIT_CARD

  import com.mogobiz.pay.handlers.payment.PaylineHandler._

  def startPayment(sessionData: SessionData): Either[String, Uri] = {
    val (transactionUUID, vendor, paymentConfig, paymentRequest) = getContext(sessionData)

    transactionHandler.startPayment(vendor,
                                    sessionData,
                                    transactionUUID,
                                    paymentRequest,
                                    PaymentType.CREDIT_CARD,
                                    CBPaymentProvider.PAYLINE)

    var threeDSResult: ThreeDSResult = null

    if (sessionData.mogopay) {
      val paymentResult = submit(sessionData,
                                 vendor,
                                 transactionUUID,
                                 paymentConfig,
                                 paymentRequest,
                                 true,
                                 sessionData.locale,
                                 TransactionStep.START_PAYMENT)
      Right(finishPayment(sessionData, paymentResult))
    } else if (!sessionData.mogopay && paymentConfig.paymentMethod == CBPaymentMethod.EXTERNAL) {
      val paymentResult = doWebPayment(vendor, transactionUUID, paymentConfig, paymentRequest, sessionData.uuid)
      sessionData.token = scala.Option(paymentResult.token)
      if (paymentResult.data != null && paymentResult.data.nonEmpty) {
        Left(paymentResult.data)
      } else {
        Right(finishPayment(sessionData, paymentResult))
      }
    } else if (Array(CBPaymentMethod.THREEDS_IF_AVAILABLE, CBPaymentMethod.THREEDS_REQUIRED).contains(
                   paymentConfig.paymentMethod)) {
      threeDSResult = check3DSecure(sessionData, vendor, transactionUUID, paymentConfig, paymentRequest)
      if (threeDSResult != null && threeDSResult.code == ResponseCode3DS.APPROVED) {
        // 3DS approuve, redirection vers payline
        sessionData.waitFor3DS = true
        val form = s"""
            <html>
              <head>
              </head>
              <body>
                Redirection vers la banque en cours...
                <form id="formpay" action="${threeDSResult.url}" method="${threeDSResult.method}" >
                <input type="hidden" name="${threeDSResult.pareqName}" value="${threeDSResult.pareqValue}" />
                <input type="hidden" name="${threeDSResult.termUrlName}" value="${threeDSResult.termUrlValue}" />
                <input type="hidden" name="${threeDSResult.mdName}" value="${threeDSResult.mdValue}" />
                </form>
                <script>document.getElementById("formpay").submit();</script>
              </body>
            </html>"""
        Left(form)
      } else if (paymentConfig.paymentMethod == CBPaymentMethod.THREEDS_IF_AVAILABLE) {
        // on lance un paiement classique
        val paymentResult = submit(sessionData,
                                   vendor,
                                   transactionUUID,
                                   paymentConfig,
                                   paymentRequest,
                                   sessionData.mogopay,
                                   sessionData.locale,
                                   TransactionStep.START_PAYMENT)
        Right(finishPayment(sessionData, paymentResult))
      } else {
        // La carte n'est pas 3Ds alors que c'est obligatoire
        Right(finishPayment(sessionData, createThreeDSNotEnrolledResult(paymentRequest)))
      }
    } else {
      // on lance un paiement classique
      val paymentResult = submit(sessionData,
                                 vendor,
                                 transactionUUID,
                                 paymentConfig,
                                 paymentRequest,
                                 sessionData.mogopay,
                                 sessionData.locale,
                                 TransactionStep.START_PAYMENT)
      Right(finishPayment(sessionData, paymentResult))
    }
  }

  def validatePayment(transaction: BOTransaction, amount: Long): scala.Option[ValidatePaymentResult] = {
    transaction.paymentData.transactionId.map { transactionId =>
      transaction.paymentConfig.map { paymentConfig =>
        val parametres  = getCreditCardConfig(paymentConfig)

        val payment: Payment = new Payment
        payment.setAmount(amount.toString)
        payment.setCurrency(transaction.currency.numericCode.toString)
        payment.setAction(ACTION_VALIDATION)
        payment.setMode(MODE_COMPTANT)
        payment.setContractNumber(parametres("paylineContract"))

        val doCaptureRequest = new DoCaptureRequest()
        doCaptureRequest.setTransactionID(transactionId)
        doCaptureRequest.setPayment(payment)

        var logdata = "transactionId=" + transactionId
        logdata += "&paiement.amount=" + payment.getAmount
        logdata += "&paiement.currency=" + payment.getCurrency
        logdata += "&paiement.action=" + payment.getAction
        logdata += "&paiement.mode=" + payment.getMode
        logdata += "&paiement.contractNumber=" + payment.getContractNumber
        boTransactionLogHandler.save(BOTransactionLog(newUUID, "OUT", logdata, "PAYLINE", transaction.uuid, TransactionStep.VALIDATE_PAYMENT), false)

        val doCaptureResponse = createProxy(parametres).doCapture(doCaptureRequest)
        val result = doCaptureResponse.getResult
        val paylineTransactionResult = doCaptureResponse.getTransaction
        logdata = ""
        logdata += "result.code=" + result.getCode
        logdata += "&result.shortMessage=" + result.getShortMessage
        logdata += "&result.longMessage=" + result.getLongMessage
        logdata += "&transaction.id=" + paylineTransactionResult.getId
        logdata += "&transaction.isPossibleFraud=" + paylineTransactionResult.getIsPossibleFraud
        logdata += "&transaction.isDuplicated=" + paylineTransactionResult.getIsDuplicated
        logdata += "&transaction.date=" + paylineTransactionResult.getDate
        boTransactionLogHandler.save(BOTransactionLog(newUUID, "IN", logdata, "PAYLINE", transaction.uuid, TransactionStep.VALIDATE_PAYMENT), false)

        val status = if (result.getCode == "00000") PaymentStatus.COMPLETE else PaymentStatus.INVALID
        new ValidatePaymentResult(status, paylineTransactionResult.getId, getTransactionDate(paylineTransactionResult))
      }
    }.getOrElse(None)
  }

  def refundPayment(transaction: BOTransaction, amount: Long): scala.Option[ValidatePaymentResult] = {
    transaction.paymentData.transactionId.map { transactionId =>
      transaction.paymentConfig.map { paymentConfig =>
        val parametres  = getCreditCardConfig(paymentConfig)

        val payment: Payment = new Payment
        payment.setAmount(amount.toString)
        payment.setCurrency(transaction.currency.numericCode.toString)
        payment.setAction(ACTION_REFUND)
        payment.setMode(MODE_COMPTANT)
        payment.setContractNumber(parametres("paylineContract"))

        val doRefundRequest = new DoRefundRequest
        doRefundRequest.setVersion("3")
        doRefundRequest.setTransactionID(transactionId)
        doRefundRequest.setPayment(payment)

        var logdata = "transactionId=" + transactionId
        logdata += "&paiement.amount=" + payment.getAmount
        logdata += "&paiement.currency=" + payment.getCurrency
        logdata += "&paiement.action=" + payment.getAction
        logdata += "&paiement.mode=" + payment.getMode
        logdata += "&paiement.contractNumber=" + payment.getContractNumber
        boTransactionLogHandler.save(BOTransactionLog(newUUID, "OUT", logdata, "PAYLINE", transaction.uuid, TransactionStep.REFUND), false)

        val doRefundResponse = createProxy(parametres).doRefund(doRefundRequest)
        val result = doRefundResponse.getResult
        val paylineTransactionResult = doRefundResponse.getTransaction
        logdata = ""
        logdata += "result.code=" + result.getCode
        logdata += "&result.shortMessage=" + result.getShortMessage
        logdata += "&result.longMessage=" + result.getLongMessage
        logdata += "&transaction.id=" + paylineTransactionResult.getId
        logdata += "&transaction.isPossibleFraud=" + paylineTransactionResult.getIsPossibleFraud
        logdata += "&transaction.isDuplicated=" + paylineTransactionResult.getIsDuplicated
        logdata += "&transaction.date=" + paylineTransactionResult.getDate
        boTransactionLogHandler.save(BOTransactionLog(newUUID, "IN", logdata, "PAYLINE", transaction.uuid, TransactionStep.REFUND), false)

        val status = if (result.getCode == "000000") PaymentStatus.REFUNDED else PaymentStatus.REFUND_FAILED
        new ValidatePaymentResult(status, paylineTransactionResult.getId, getTransactionDate(paylineTransactionResult))
      }
    }.getOrElse(None)
  }

  def done(sessionData: SessionData, params: Map[String, String]): Uri = {
    val paymentResult = handleResponse(sessionData, params)
    finishPayment(sessionData, paymentResult)
  }

  def callbackPayment(sessionData: SessionData, params: Map[String, String]): PaymentResult = {
    handleResponse(sessionData, params)
  }

  private def handleResponse(sessionData: SessionData, params: Map[String, String]): PaymentResult = {
    val (transactionUUID, vendor, paymentConfig, paymentRequest) = getContext(sessionData)
    getWebPaymentDetails(sessionData,
                         vendor,
                         transactionUUID,
                         paymentConfig,
                         paymentRequest,
                         sessionData.token.orNull,
                         sessionData.locale)
  }

  def threeDSCallback(sessionData: SessionData, params: Map[String, String]): Uri = {
    if (!sessionData.waitFor3DS) {
      // invalid call
      throw InvalidContextException("Not expecting 3DSecure callback")
    } else {
      val errorURL                                                 = sessionData.errorURL.getOrElse("")
      val (transactionUUID, vendor, paymentConfig, paymentRequest) = getContext(sessionData)
      sessionData.waitFor3DS = false
      try {
        val paymentRequest2 = paymentRequest.copy(paylineMd = params("MD"), paylinePares = params("PaRes"))
        val result = submit(sessionData,
                            vendor,
                            transactionUUID,
                            paymentConfig,
                            paymentRequest2,
                            sessionData.mogopay,
                            sessionData.locale,
                            TransactionStep.THREEDS_CALLBACK)
        finishPayment(sessionData, result)
      } catch {
        case ex: Exception =>
          ex.printStackTrace()
          Uri(errorURL)
      }
    }
  }

  def check3DSecure(sessionData: SessionData,
                    vendor: Account,
                    transactionUuid: Mogopay.Document,
                    paymentConfig: PaymentConfig,
                    paymentRequest: PaymentRequest): ThreeDSResult = {
    val transaction           = boTransactionHandler.find(transactionUuid).get
    val parametres            = getCreditCardConfig(paymentConfig)
    val numeroContrat: String = parametres("paylineContract")
    var logdata: String = null
    transactionHandler.updateStatus(transactionUuid, None, TransactionStatus.VERIFICATION_THREEDS)
    val paiement: Payment = new Payment
    paiement.setAmount(paymentRequest.amount.toString)
    paiement.setCurrency(paymentRequest.currency.numericCode.toString)
    paiement.setAction(ACTION_AUTHORISATION)
    paiement.setMode(MODE_COMPTANT)
    paiement.setContractNumber(numeroContrat)
    val formatDatePayline = new SimpleDateFormat("MMyy")

    logdata = "paiement.amount=" + paiement.getAmount
    logdata += " paiement.currency=" + paiement.getCurrency
    logdata += "&paiement.action=" + paiement.getAction
    logdata += "&paiement.mode=" + paiement.getMode
    logdata += "&paiement.contractNumber=" + paiement.getContractNumber
    val card: Card = new Card
    card.setNumber(paymentRequest.ccNumber)
    card.setType(fromCreditCardType(paymentRequest.cardType))
    card.setExpirationDate(formatDatePayline.format(paymentRequest.expirationDate))
    card.setCvx(paymentRequest.cvv)
    logdata += "&card.number=" + UtilHandler.hideCardNumber(card.getNumber, "X")
    logdata += "&card.type=" + card.getType
    logdata += "&card.expirationDate=" + card.getExpirationDate
    logdata += "&card.cvx=XXX"
    val requete: VerifyEnrollmentRequest = new VerifyEnrollmentRequest
    requete.setPayment(paiement)
    requete.setCard(card)
    requete.setOrderRef(paymentRequest.transactionSequence)
    logdata += "&orderRef=" + requete.getOrderRef
    val botlog =
      BOTransactionLog(newUUID, "OUT", logdata, "PAYLINE", transaction.uuid, step = TransactionStep.CHECK_THREEDS)
    boTransactionLogHandler.save(botlog, false)

    val response: VerifyEnrollmentResponse = createProxy(parametres).verifyEnrollment(requete)
    val result: Result                     = response.getResult
    val code: String                       = result.getCode
    logdata = ""
    logdata += "result.code=" + result.getCode
    logdata += "&result.shortMessage=" + result.getShortMessage
    logdata += "&result.longMessage=" + result.getLongMessage
    val default3DS = ThreeDSResult(
        code = ResponseCode3DS.ERROR,
        url = null,
        method = null,
        mdName = null,
        mdValue = null,
        pareqName = null,
        pareqValue = null,
        termUrlName = null,
        termUrlValue = null
    )

    val retour = if ("00000".equals(code) || "03000".equals(code)) {
      logdata += s"&response.actionUrl=${response.getActionUrl}"
      logdata += s"&response.actionMethod=${response.getActionMethod}"
      logdata += s"&response.mdFieldName=${response.getMdFieldName}"
      logdata += s"&response.mdFieldValue=${response.getMdFieldValue}"
      logdata += s"&response.pareqFieldName=${response.getPareqFieldName}"
      logdata += s"&response.pareqFieldValue=${response.getPareqFieldValue}"
      logdata += s"&response.termUrlName=${response.getTermUrlName}"
      logdata += s"&response.termUrlValue=${response.getTermUrlValue}"
      default3DS.copy(
          code = ResponseCode3DS.APPROVED,
          url = response.getActionUrl,
          method = response.getActionMethod,
          mdName = response.getMdFieldName,
          mdValue = response.getMdFieldValue,
          pareqName = response.getPareqFieldName,
          pareqValue = response.getPareqFieldValue,
          termUrlName = response.getTermUrlName,
          termUrlValue = s"${Settings.Mogopay.EndPoint}payline/3ds-callback/${sessionData.uuid}" //response.getTermUrlValue
      )

    } else if (code != null && code.startsWith("023")) {
      default3DS.copy(code = ResponseCode3DS.INVALID)
    } else if (code != null && (code.startsWith("01") || code.startsWith("03"))) {
      default3DS.copy(code = ResponseCode3DS.REFUSED)
    } else {
      default3DS.copy(code = ResponseCode3DS.ERROR)
    }
    val botlogIn =
      BOTransactionLog(newUUID, "OUT", logdata, "PAYLINE", transaction.uuid, step = TransactionStep.CHECK_THREEDS)
    boTransactionLogHandler.save(botlogIn, false)
    transactionHandler.updateStatus3DS(transactionUuid, sessionData.ipAddress, retour.code, code)
    retour
  }

  private def submit(sessionData: SessionData,
                     vendor: Account,
                     transactionUuid: Mogopay.Document,
                     paymentConfig: PaymentConfig,
                     infosPaiement: PaymentRequest,
                     mogopay: Boolean,
                     locale: scala.Option[String],
                     step: TransactionStep): PaymentResult = {
    val transaction = boTransactionHandler.find(transactionUuid).get
    val parametres  = getCreditCardConfig(paymentConfig)
    var logdata: String = ""
    val formatDatePayline = new SimpleDateFormat("MMyy")

    transactionHandler.updateStatus(transactionUuid, None, TransactionStatus.PAYMENT_REQUESTED)
    val numeroContrat: String = parametres("paylineContract")

    if (mogopay) {

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////
      // NEVER DELETE THE LINES BELOW BEFORE WALLET IS IMPLEMENTED
      //    val walletRequest: GetWalletRequest = new GetWalletRequest
      //    walletRequest.setContractNumber(numeroContrat)
      //    walletRequest.setVersion("4")
      //    walletRequest.setWalletId("TEST")
      //    val walletResponse: GetWalletResponse = createProxy(transaction, parametres).getWallet(walletRequest)
      //    if (walletResponse.getResult.getCode == "02503") {
      //      val cwr: CreateWalletRequest = new CreateWalletRequest
      //      cwr.setVersion("4")
      //      cwr.setContractNumber(numeroContrat)
      //      val card: Card = new Card
      //      card.setNumber(infosPaiement.ccNumber)
      //      card.setType(fromCreditCardType(infosPaiement.cardType))
      //      card.setExpirationDate(formatDatePayline.format(infosPaiement.expirationDate))
      //      card.setCvx(infosPaiement.cvv)
      //      logdata += "&card.number=" + UtilHandler.hideCardNumber(card.getNumber, "X")
      //      logdata += "&card.type=" + card.getType
      //      logdata += "&card.expirationDate=" + card.getExpirationDate
      //      logdata += "&card.cvx=" + "XXX"
      //      val w: Wallet = new Wallet
      //      w.setCard(card)
      //      w.setWalletId("TEST")
      //      w.setFirstName("FirstName")
      //      w.setLastName("LastName")
      //      w.setEmail("test@test.fr")
      //      cwr.setWallet(w)
      //      val cwresp: CreateWalletResponse = createProxy(transaction, parametres).createWallet(cwr)
      //      logger.info(cwresp.getCard)
      //    }
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    val orderDateFormat   = new SimpleDateFormat("dd/MM/yyyy HH:mm")
    val paiement: Payment = new Payment
    paiement.setAmount(infosPaiement.amount.toString)
    paiement.setCurrency(infosPaiement.currency.numericCode.toString)
    paiement.setAction(ACTION_AUTHORISATION)
    paiement.setMode(MODE_COMPTANT)
    paiement.setContractNumber(numeroContrat)
    logdata = "paiement.amount=" + paiement.getAmount
    logdata += "&paiement.currency=" + paiement.getCurrency
    logdata += "&paiement.action=" + paiement.getAction
    logdata += "&paiement.mode=" + paiement.getMode
    logdata += "&paiement.contractNumber=" + paiement.getContractNumber
    val card: Card = new Card
    card.setNumber(infosPaiement.ccNumber)
    card.setType(fromCreditCardType(infosPaiement.cardType))
    card.setExpirationDate(
        if (infosPaiement.expirationDate != null) formatDatePayline.format(infosPaiement.expirationDate) else null)
    card.setCvx(infosPaiement.cvv)

    logdata += "&card.number=" + UtilHandler.hideCardNumber(card.getNumber, "X")
    logdata += "&card.type=" + card.getType
    logdata += "&card.expirationDate=" + card.getExpirationDate
    logdata += "&card.cvx=" + "XXX"
    val order: Order = new Order
    order.setRef(infosPaiement.transactionSequence)
    order.setAmount(infosPaiement.amount.toString)
    order.setCurrency(infosPaiement.currency.numericCode.toString)
    order.setDate(orderDateFormat.format(infosPaiement.orderDate))

    logdata += "&order.ref=" + order.getRef
    logdata += "&order.amount=" + order.getAmount
    logdata += "&order.currency=" + order.getCurrency
    logdata += "&order.date=" + order.getDate
    var authen: Authentication3DSecure = null
    if (infosPaiement.paylineMd != null && infosPaiement.paylinePares != null) {
      authen = new Authentication3DSecure
      authen.setMd(infosPaiement.paylineMd)
      authen.setPares(infosPaiement.paylinePares)
      logdata += "&authen.md=" + authen.getMd
      logdata += "&authen.pares=" + authen.getPares
    }
    val botlog = BOTransactionLog(newUUID, "OUT", logdata, "PAYLINE", transaction.uuid, step = step)
    boTransactionLogHandler.save(botlog, false)
    val requete: DoAuthorizationRequest = new DoAuthorizationRequest
    requete.setPayment(paiement)
    requete.setCard(card)
    requete.setOrder(order)
    requete.setAuthentication3DSecure(authen)
    val response: DoAuthorizationResponse = createProxy(parametres).doAuthorization(requete)
    val result: Result                    = response.getResult
    logdata = ""
    logdata += "result.code=" + result.getCode
    logdata += "&result.shortMessage=" + result.getShortMessage
    logdata += "&result.longMessage=" + result.getLongMessage
    if (response.getCard != null) {
      logdata += "&card.cardHolder=" + response.getCard.getCardholder
      logdata += "&card.expirationDate=" + response.getCard.getExpirationDate
      logdata += "&card.number=" + response.getCard.getNumber
    }
    val code: String = result.getCode

    var paymentResult = PaymentResult(
        transactionSequence = infosPaiement.transactionSequence,
        orderDate = infosPaiement.orderDate,
        ccNumber = UtilHandler.hideCardNumber(infosPaiement.ccNumber, "X"),
        expirationDate = infosPaiement.expirationDate,
        cvv = infosPaiement.cvv,
        amount = infosPaiement.amount,
        cardType = infosPaiement.cardType,
        gatewayTransactionId = null,
        transactionDate = null,
        transactionCertificate = null,
        authorizationId = null,
        status = null,
        errorCodeOrigin = "",
        errorMessageOrigin = None,
        data = null,
        bankErrorCode = "",
        bankErrorMessage = None,
        token = null,
        errorShipment = None
    )

    paymentResult = if ("00000" == code) {
      val authorisation: Authorization = response.getAuthorization
      logdata += "&authorisation.number=" + authorisation.getNumber
      logdata += "&authorisation.date=" + authorisation.getDate
      val tr: Transaction = response.getTransaction
      val transactionDate = getTransactionDate(tr)
      paymentResult.copy(errorCodeOrigin = code,
                         gatewayTransactionId = tr.getId,
                         transactionDate = transactionDate,
                         authorizationId = authorisation.getNumber,
                         status = PaymentStatus.COMPLETE)

    } else {
      paymentResult.copy(errorCodeOrigin = code,
                         errorMessageOrigin = scala.Option(result.getLongMessage),
                         bankErrorCode = "",
                         bankErrorMessage = scala.Option(BankErrorCodes.getErrorMessage("")),
                         status = PaymentStatus.FAILED)
    }
    paymentResult = transactionHandler.finishPayment(
        this,
        sessionData,
        transactionUuid,
        if ("00000" == code) TransactionStatus.PAYMENT_CONFIRMED else TransactionStatus.PAYMENT_REFUSED,
        paymentResult,
        locale,
        Some(response.getTransaction.getId))

    val botlogIn = BOTransactionLog(newUUID, "IN", logdata, "PAYLINE", transaction.uuid, step = step)
    boTransactionLogHandler.save(botlogIn, false)

    paymentResult
  }

  private def getTransactionDate(transaction: Transaction) =  new SimpleDateFormat("dd/MM/yy HH:mm").parse(transaction.getDate)

  private def cancel(vendorUuid: Mogopay.Document,
                     transactionUuid: String,
                     paymentConfig: PaymentConfig,
                     infosPaiement: CancelRequest): CancelResult = {
    val vendor      = accountHandler.load(vendorUuid).get
    val transaction = boTransactionHandler.find(transactionUuid).get
    val parametres  = getCreditCardConfig(paymentConfig)
    transactionHandler.updateStatus(transactionUuid, None, TransactionStatus.CANCEL_REQUESTED)

    val requete: DoResetRequest = new DoResetRequest
    requete.setTransactionID(infosPaiement.id)
    var logdata: String = ""
    logdata = "requete.transactionID=" + requete.getTransactionID
    val botlog = BOTransactionLog(newUUID, "OUT", logdata, "PAYLINE", transaction.uuid, step = TransactionStep.CANCEL)
    boTransactionLogHandler.save(botlog, false)

    val response: DoResetResponse = createProxy(parametres).doReset(requete)
    val result: Result            = response.getResult
    logdata = ""
    logdata += "&result.code=" + result.getCode
    logdata += "&result.shortMessage=" + result.getShortMessage
    logdata += "&result.longMessage=" + result.getLongMessage
    val botlogIn = BOTransactionLog(newUUID, "IN", logdata, "PAYLINE", transaction.uuid, step = TransactionStep.CANCEL)
    boTransactionLogHandler.save(botlogIn, false)

    transactionHandler.updateStatus(
        transactionUuid,
        None,
        if ("00000" == result.getCode) TransactionStatus.CANCEL_CONFIRMED else TransactionStatus.CANCEL_FAILED)

    CancelResult(id = infosPaiement.id,
                 status = if ("00000" == result.getCode) PaymentStatus.CANCELED else PaymentStatus.CANCEL_FAILED,
                 errorCodeOrigin = result.getCode,
                 errorMessageOrigin = scala.Option(result.getLongMessage))
  }

  private def createProxy(parametres: Map[String, String]): DirectPaymentAPI = {
    val accountId: String                 = parametres("paylineAccount")
    val cleAccess: String                 = parametres("paylineKey")
    val endpoint: String                  = Settings.Payline.DirectEndPoint
    val url: URL                          = classOf[PaylineHandler].getResource("/wsdl/DirectPaymentAPI_v4.38.wsdl")
    val service: DirectPaymentAPI_Service = new DirectPaymentAPI_Service(url)
    val proxy: DirectPaymentAPI           = service.getDirectPaymentAPI
    proxy.asInstanceOf[BindingProvider].getRequestContext.put("javax.xml.ws.security.auth.username", accountId)
    proxy.asInstanceOf[BindingProvider].getRequestContext.put("javax.xml.ws.security.auth.password", cleAccess)
    proxy.asInstanceOf[BindingProvider].getRequestContext.put("javax.xml.ws.service.endpoint.address", endpoint)
    proxy
      .asInstanceOf[BindingProvider]
      .getRequestContext
      .put(TrustedSSLFactory.JaxwsSslSockeetFactory, TrustedSSLFactory.getTrustingSSLSocketFactory)
    proxy
      .asInstanceOf[BindingProvider]
      .getRequestContext
      .put(NaiveHostnameVerifier.JaxwsHostNameVerifier, new NaiveHostnameVerifier)
    val binding: javax.xml.ws.Binding = (proxy.asInstanceOf[BindingProvider]).getBinding
    proxy
  }

  def doWebPayment(vendor: Account,
                   transactionUuid: Mogopay.Document,
                   paymentConfig: PaymentConfig,
                   paymentRequest: PaymentRequest,
                   sessionId: String): PaymentResult = {
    val transaction      = boTransactionHandler.find(transactionUuid).get
    val parametres       = getCreditCardConfig(paymentConfig)
    val payment: Payment = new Payment
    payment.setAmount("" + paymentRequest.amount)
    payment.setCurrency("" + paymentRequest.currency.numericCode)
    payment.setAction(ACTION_AUTHORISATION)
    payment.setMode(MODE_COMPTANT)
    if (payment.getContractNumber == null || payment.getContractNumber.length == 0)
      payment.setContractNumber(parametres("paylineContract"))

    var logdata: String = ""
    logdata += "payment.amount=" + payment.getAmount
    logdata += "&payment.currency=" + payment.getCurrency
    logdata += "&payment.contractNumbner=" + payment.getContractNumber
    logdata += "&payment.action=" + payment.getAction
    logdata += "&payment.mode=" + payment.getMode
    val order: Order = new Order
    order.setRef(transactionUuid)
    order.setAmount("" + paymentRequest.amount)
    order.setCurrency("" + paymentRequest.currency.numericCode)
    val s: SimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm")
    order.setDate(s.format(new Date))
    logdata += "&order.ref=" + order.getRef
    logdata += "&order.amount=" + order.getAmount
    logdata += "&order.currency=" + order.getCurrency
    logdata += "&order.date=" + order.getDate
    val parameters: DoWebPaymentRequest = new DoWebPaymentRequest
    parameters.setVersion(Settings.Payline.Version)
    parameters.setPayment(payment)
    val contractList = new SelectedContractList()
    contractList.getSelectedContract.add(payment.getContractNumber)
    parameters.setSelectedContractList(contractList)
    parameters.setReturnURL(Settings.Mogopay.EndPoint + "payline/done/" + sessionId)
    parameters.setCancelURL(Settings.Mogopay.EndPoint + "payline/done/" + sessionId)
    parameters.setNotificationURL(Settings.Mogopay.EndPoint + "payline/callback/" + sessionId)
    parameters.setOrder(order)
    parameters.setSecurityMode(Settings.Payline.SecurityMode)
    parameters.setLanguageCode(Settings.Payline.LanguageCode)
    parameters.setBuyer(null)
    parameters.setPrivateDataList(null)
    parameters.setRecurring(null)
    parameters.setCustomPaymentPageCode(parametres.getOrElse("paylineCustomPaymentPageCode", null))
    parameters.setCustomPaymentTemplateURL(parametres.getOrElse("paylineCustomPaymentTemplateURL", null))
    logdata += "&returnURL=" + parameters.getReturnURL
    logdata += "&languageCode=" + parameters.getLanguageCode
    logdata += "&securityMode=" + parameters.getSecurityMode
    var result: DoWebPaymentResponse = new DoWebPaymentResponse
    val port: WebPaymentAPI       = null
    val webPayment: WebPayment    = new WebPayment
    val url: URL                  = classOf[WebPaymentAPI].getResource("/wsdl/WebPaymentAPI_v4.38.wsdl")
    val ss: WebPaymentAPI_Service = new WebPaymentAPI_Service(url, ServiceName)

    val proxy: WebPaymentAPI = ss.getWebPaymentAPI
    proxy
      .asInstanceOf[BindingProvider]
      .getRequestContext
      .put("javax.xml.ws.security.auth.username", parametres("paylineAccount"))
    proxy
      .asInstanceOf[BindingProvider]
      .getRequestContext
      .put("javax.xml.ws.security.auth.password", parametres("paylineKey"))
    proxy
      .asInstanceOf[BindingProvider]
      .getRequestContext
      .put("javax.xml.ws.service.endpoint.address", Settings.Payline.WebEndPoint)
    proxy
      .asInstanceOf[BindingProvider]
      .getRequestContext
      .put(TrustedSSLFactory.JaxwsSslSockeetFactory, TrustedSSLFactory.getTrustingSSLSocketFactory)
    proxy
      .asInstanceOf[BindingProvider]
      .getRequestContext
      .put(NaiveHostnameVerifier.JaxwsHostNameVerifier, new NaiveHostnameVerifier)
    proxy
      .asInstanceOf[BindingProvider]
      .getRequestContext
      .put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true.asInstanceOf[Object])

    val binding: Binding = (proxy.asInstanceOf[BindingProvider]).getBinding
    val handlerList      = binding.getHandlerChain
    handlerList.add(new TraceHandler(transaction, "PAYLINE"))
    binding.setHandlerChain(handlerList)
    val botlog = BOTransactionLog(newUUID, "OUT", logdata, "PAYLINE", transaction.uuid, TransactionStep.DO_WEB_PAYMENT)
    boTransactionLogHandler.save(botlog, false)

    result = proxy.doWebPayment(parameters)

    logdata = ""
    if (result != null) {
      if (result.getResult != null) {
        logdata += "result.code=" + result.getResult.getCode
        logdata += "&result.shortMessage=" + result.getResult.getShortMessage
        logdata += "&result.longMessage=" + result.getResult.getLongMessage
      }
      logdata += "&result.token=" + result.getToken
      logdata += "&result.redirectURL=" + result.getRedirectURL
    }
    val botlogIn =
      BOTransactionLog(newUUID, "IN", logdata, "PAYLINE", transaction.uuid, TransactionStep.DO_WEB_PAYMENT)
    boTransactionLogHandler.save(botlogIn, false)
    /*
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
                               errorMessageOrigin: scala.Option[String],
                               data: String,
                               bankErrorCode: String,
                               bankErrorMessage: scala.Option[String],
                               token: String)

     */
    if (result != null && result.getResult != null) {
      if (result.getResult.getCode == "00000") {
        PaymentResult(status = null,
                      transactionSequence = paymentRequest.transactionSequence,
                      orderDate = paymentRequest.orderDate,
                      amount = paymentRequest.amount,
                      ccNumber = paymentRequest.ccNumber,
                      cardType = paymentRequest.cardType,
                      expirationDate = paymentRequest.expirationDate,
                      cvv = paymentRequest.cvv,
                      transactionCertificate = null,
                      transactionDate = new Date,
                      authorizationId = null,
                      gatewayTransactionId = transactionUuid,
                      errorCodeOrigin = result.getResult.getCode,
                      errorMessageOrigin = scala.Option(result.getResult.getLongMessage),
                      bankErrorCode = "",
                      bankErrorMessage = scala.Option(BankErrorCodes.getErrorMessage("")),
                      token = result.getToken,
                      data = s"""
               |<html>
               |<head>
               |<meta http-equiv="refresh" content="0; URL=${result.getRedirectURL}">
               |</head>
               |<body>
               |</body>
               |</html>
              """.stripMargin,
                      errorShipment = None)
      } else {
        throw NotAvailablePaymentGatewayException(result.getResult.getCode)
      }
    } else {
      throw NotAvailablePaymentGatewayException("Unkown")
    }
  }

  def getWebPaymentDetails(sessionData: SessionData,
                           vendor: Account,
                           transactionUuid: Mogopay.Document,
                           paymentConfig: PaymentConfig,
                           paymentRequest: PaymentRequest,
                           token: String,
                           locale: scala.Option[String]): PaymentResult = {
    val transaction               = boTransactionHandler.find(transactionUuid).get
    val parametres                = getCreditCardConfig(paymentConfig)
    val url: URL                  = classOf[WebPaymentAPI].getResource("/wsdl/WebPaymentAPI_v4.38.wsdl")
    val ss: WebPaymentAPI_Service = new WebPaymentAPI_Service(url, ServiceName)
    val proxy: WebPaymentAPI      = ss.getWebPaymentAPI
    (proxy
      .asInstanceOf[BindingProvider])
      .getRequestContext
      .put("javax.xml.ws.security.auth.username", parametres("paylineAccount"))
    (proxy
      .asInstanceOf[BindingProvider])
      .getRequestContext
      .put("javax.xml.ws.security.auth.password", parametres("paylineKey"))
    (proxy
      .asInstanceOf[BindingProvider])
      .getRequestContext
      .put("javax.xml.ws.service.endpoint.address", Settings.Payline.WebEndPoint)
    val binding: Binding = (proxy.asInstanceOf[BindingProvider]).getBinding
    (proxy
      .asInstanceOf[BindingProvider])
      .getRequestContext
      .put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true.asInstanceOf[Object])
    val handlerList = binding.getHandlerChain
    handlerList.add(new TraceHandler(transaction, "PAYLINE"))
    binding.setHandlerChain(handlerList)
    var result: GetWebPaymentDetailsResponse = new GetWebPaymentDetailsResponse
    val parameters: GetWebPaymentDetailsRequest = new GetWebPaymentDetailsRequest
    parameters.setVersion(Settings.Payline.Version)
    parameters.setToken(token)
    var logdata: String = null
    logdata = "version=" + parameters.getVersion
    logdata += "&token=" + parameters.getToken
    val botlog =
      BOTransactionLog(newUUID, "OUT", logdata, "PAYLINE", transaction.uuid, TransactionStep.GET_WEB_PAYMENT_DETAILS)
    boTransactionLogHandler.save(botlog, false)
    result = proxy.getWebPaymentDetails(parameters)

    logdata = ""
    if (result != null) {
      logdata += "result.code=" + result.getResult.getCode
      logdata += "&result.shortMessage=" + result.getResult.getShortMessage
      logdata += "&result.longMessage=" + result.getResult.getLongMessage
      if (result.getAuthorization != null) {
        logdata += "&result.authorization.number" + result.getAuthorization.getNumber
        logdata += "&result.authorization.date" + result.getAuthorization.getDate
      }
      if (result.getCard != null) {
        logdata += "&result.card.expirationdate" + result.getCard.getExpirationDate
        logdata += "&result.card.number" + result.getCard.getNumber
        logdata += "&result.card.type" + result.getCard.getType
        logdata += "&result.card.cardholder" + result.getCard.getCardholder
      }
      if (result.getCard != null) {
        logdata += "&result.transaction.date" + result.getTransaction.getDate
        logdata += "&result.transaction.id" + result.getTransaction.getId
      }
      if (result.getAuthentication3DSecure != null) {
        logdata += "&result.getAuthentication3DSecure.typeSecurisation" + result.getAuthentication3DSecure.getTypeSecurisation
        logdata += "&result.getAuthentication3DSecure.vadsResult" + result.getAuthentication3DSecure.getVadsResult
      }
    }
    val botlogIn =
      BOTransactionLog(newUUID, "IN", logdata, "PAYLINE", transaction.uuid, TransactionStep.GET_WEB_PAYMENT_DETAILS)
    boTransactionLogHandler.save(botlogIn, false)

    val paymentResult = PaymentResult(
        status = if (result.getResult.getCode == "00000") PaymentStatus.COMPLETE else PaymentStatus.FAILED,
        transactionSequence = paymentRequest.transactionSequence,
        orderDate = paymentRequest.orderDate,
        amount = try {
          result.getPayment.getAmount.toLong
        } catch {
          case NonFatal(e) =>
            paymentRequest.amount
        },
        ccNumber = if (result.getCard != null) result.getCard.getNumber else null,
        cardType = if (result.getCard != null) toCreditCardType(result.getCard.getType) else null,
        expirationDate = try {
          new SimpleDateFormat("MMyy").parse(result.getCard().getExpirationDate)
        } catch {
          case NonFatal(e) =>
            null
        },
        cvv = paymentRequest.cvv,
        transactionCertificate = null,
        transactionDate = try {
          new SimpleDateFormat("dd/MM/yy HH:mm").parse(result.getTransaction().getDate)
        } catch {
          case NonFatal(e) =>
            new Date
        },
        authorizationId = if (result.getAuthorization() != null) result.getAuthorization().getNumber else null,
        gatewayTransactionId = if (result.getTransaction() != null) result.getTransaction().getId else null,
        errorCodeOrigin = result.getResult.getCode,
        errorMessageOrigin = scala.Option(result.getResult.getLongMessage),
        bankErrorCode = "",
        bankErrorMessage = scala.Option(BankErrorCodes.getErrorMessage("")),
        token = null,
        data = null,
        errorShipment = None
    )

    val creditCard = BOCreditCard(
        number = paymentResult.ccNumber,
        holder = None,
        expiryDate = paymentResult.expirationDate,
        cardType = paymentResult.cardType
    )
    boTransactionHandler.update(transaction.copy(creditCard = Some(creditCard)), false)

    transactionHandler.finishPayment(this,
                                     sessionData,
                                     transactionUuid,
                                     if (result.getResult.getCode == "00000") TransactionStatus.PAYMENT_AUTHORIZED
                                     else TransactionStatus.PAYMENT_REFUSED,
                                     paymentResult,
                                     locale)
  }

  override def refund(paymentConfig: PaymentConfig,
                      boTx: BOTransaction,
                      amount: Long,
                      paymentResult: PaymentResult): RefundResult = {
    val parameters = getCreditCardConfig(paymentConfig)

    val payment = new Payment
    payment.setAmount(amount.toString)
    payment.setCurrency(boTx.currency.numericCode.toString)
    payment.setAction(ACTION_REFUND)
    payment.setMode(MODE_COMPTANT)
    payment.setContractNumber(parameters("paylineContract"))

    val request = new DoRefundRequest
    request.setVersion("3")
    request.setTransactionID(boTx.gatewayData.getOrElse(throw new TransactionIdNotFoundException))
    request.setPayment(payment)

    val queryOUT = Map(
        "amount"        -> payment.getAmount,
        "currency"      -> payment.getCurrency,
        "action"        -> payment.getAction,
        "mode"          -> payment.getMode,
        "contactNumber" -> payment.getContractNumber,
        "version"       -> request.getVersion,
        "transactionID" -> request.getTransactionID
    )
    val logOUT = new BOTransactionLog(uuid = newUUID,
                                      provider = "PAYLINE",
                                      direction = "OUT",
                                      transaction = boTx.uuid,
                                      log = GlobalUtil.mapToQueryString(queryOUT),
                                      step = TransactionStep.REFUND)
    EsClient.index(Settings.Mogopay.EsIndex, logOUT, false)

    val response = createProxy(parameters).doRefund(request)

    val responseMap = Map(
        "code"            -> response.getResult.getCode,
        "shortMessage"    -> response.getResult.getShortMessage,
        "longMessage"     -> response.getResult.getLongMessage,
        "transactionId"   -> response.getTransaction.getId,
        "date"            -> response.getTransaction.getDate,
        "isDuplicated"    -> response.getTransaction.getIsDuplicated,
        "isPossibleFraud" -> response.getTransaction.getIsPossibleFraud
    )
    val logIN = new BOTransactionLog(uuid = newUUID,
                                     provider = "PAYLINE",
                                     direction = "IN",
                                     transaction = boTx.uuid,
                                     log = mapToQueryString(responseMap),
                                     step = TransactionStep.REFUND)
    EsClient.index(Settings.Mogopay.EsIndex, logIN, false)

    val status      = if (response.getResult.getCode == "00000") PaymentStatus.REFUNDED else PaymentStatus.REFUND_FAILED
    val code        = response.getResult.getCode
    val longMessage = response.getResult.getLongMessage
    RefundResult(status, code, scala.Option(longMessage))
  }
}
