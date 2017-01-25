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
import com.mogobiz.pay.codes.MogopayConstant
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.json.JacksonConverter
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.exceptions.Exceptions._
import com.mogobiz.pay.handlers.UtilHandler
import com.mogobiz.pay.model.CreditCardType.{CreditCardType, _}
import com.mogobiz.pay.model.{ResponseCode3DS, TransactionStatus, _}
import com.mogobiz.utils.{NaiveHostnameVerifier, TrustedSSLFactory}
import org.apache.commons.lang.StringUtils
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
    retour
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
    retour
  }

}


case class PaylineConfig(threeDSUse : CBPaymentMethod.CBPaymentMethod,
                         account: String,
                         key: String,
                         contractNumber: String,
                         customPaymentPageCode: scala.Option[String],
                         customPaymentTemplateURL: scala.Option[String])

case class PaylineData(config: PaylineConfig, // Config du vendeur
                       paylineMd : scala.Option[String] = None,  // data payline recu après processus 3DS
                       paylinePares: scala.Option[String] = None,  // data payline recu après processus 3DS
                       transactionId: scala.Option[String] = None,  // data payline recu après processus d'autorisation
                       transactionDate: scala.Option[Date] = None,  // data payline recu après processus d'autorisation
                       token: scala.Option[String] = None)  // data payline recu pour le paiement externe

class PaylineHandler(handlerName: String) extends CBProvider {
  PaymentHandler.register(handlerName, this)
  implicit val formats = new org.json4s.DefaultFormats {}

  import com.mogobiz.pay.handlers.payment.PaylineHandler._

  val SUCCESS_CODE = "00000"

  val CONFIG_CUSTOM_PAGE_CODE = ""
  val CONFIG_CUSTOM_TEMPLATE_URL = "paylineCustomPaymentTemplateURL"

  val PAYLINE_EXPIRATION_DATE_FORMAT = "MMyy"
  val PAYLINE_TRANSACTION_DATE_FORMAT = "dd/MM/yyyy HH:mm"

  val ACTION_AUTHORISATION: String            = "100"
  val ACTION_VALIDATION: String               = "201"
  val ACTION_REFUND: String                   = "421"
  val MODE_COMPTANT: String                   = "CPT"
  val WEB_PAYMENT_API_SERVICE_NAME: QName                      = new QName("http://impl.ws.payline.experian.com", "WebPaymentAPI")

  def startPayment(sessionData: SessionData): Either[FormRedirection, Uri] = {
    val paymentRequest = sessionData.paymentRequest.getOrElse(throw InvalidContextException("paymentRequest not found in session data"))

    val boTransaction = createBOTransaction(sessionData, paymentRequest, PaymentType.CREDIT_CARD)

    val paymentMethod = boTransaction.paymentConfig.paymentMethod
    val cart = paymentRequest.cart

    val params = getCreditCardConfig(boTransaction.paymentConfig)
    val paymentConfig = PaylineConfig(paymentMethod,
      params("paylineAccount"),
      params("paylineKey"),
      params("paylineContract"),
      params.get("paylineCustomPaymentPageCode"),
      params.get("paylineCustomPaymentTemplateURL"))
    val paymentData = PaylineData(paymentConfig)


    if (cart.shopCarts.isEmpty) {
      // Il n'y a rien à payer, la transaction est considérée un succès
      Right(redirectionToCallback(finishTransaction(boTransaction, TransactionStatus.COMPLETED)))
    }
    else {
      if (sessionData.mogopay) {
        Right(doMultiAuthorizations(boTransaction, paymentRequest, paymentData, true))
      }
      else {
        val withMultiShop = cart.shopCarts.length > 1

        if (paymentMethod == CBPaymentMethod.EXTERNAL) {
          //Payment Externe
          if (withMultiShop) {
            val finalBOShopTransaction = finishTransaction(boTransaction, TransactionStatus.FAILED, Some(MogopayConstant.ERROR_EXTERNAL_PAYMENT_NOT_ALLOWED_WITH_MANY_SHOPS))
            Right(redirectionToCallback(finalBOShopTransaction))
          }
          else {
            // On est en paiment externe sans multishop => on a donc forcément un seul shop
            val shopMogobiz = cart.shopCarts.head
            val boShopTransaction = createBOShopTransaction(boTransaction, shopMogobiz, serializePaymentData(paymentData))
            val (finalBOShopTransaction, redirectUri) = doWebPayment(sessionData.uuid, boTransaction, boShopTransaction)
            Right(redirectUri.getOrElse {
              // Echec du paiement externe, on termine la transaction
              redirectionToCallback(finishTransaction(boTransaction, TransactionStatus.FAILED, finalBOShopTransaction.errorCode))
            })
          }
        }
        else {
          val count = cart.shopCarts.count{shopCart => true}
          val boShopTransactions = cart.shopCarts.map { shopCart =>
            createBOShopTransaction(boTransaction, shopCart, serializePaymentData(paymentData))
          }

          // vérification de l'enrollement de la carte au systeme 3DS (si nécessaire)
          val threeDSResult = if (paymentMethod == CBPaymentMethod.THREEDS_REQUIRED || paymentMethod == CBPaymentMethod.THREEDS_IF_AVAILABLE)
            Some(verifyEnrollment(sessionData.uuid, boTransaction, boShopTransactions, paymentRequest, paymentData))
          else None

          val threeDSResultCode = threeDSResult.map {_.code}.getOrElse(ResponseCode3DS.REFUSED)

          if (threeDSResultCode == ResponseCode3DS.APPROVED) {
            if (count > 1) Right(redirectionToCallback(finishTransaction(boTransaction, TransactionStatus.FAILED, Some(MogopayConstant.ERROR_THREEDS_NOT_ALLOWED_WITH_MANY_SHOPS))))
            else {
              val form = s"""
            <html>
              <head>
              </head>
              <body>
                Redirection vers la banque en cours...
                <form id="formpay" action="${threeDSResult.get.url}" method="${threeDSResult.get.method}" >
                <input type="hidden" name="${threeDSResult.get.pareqName}" value="${threeDSResult.get.pareqValue}" />
                <input type="hidden" name="${threeDSResult.get.termUrlName}" value="${threeDSResult.get.termUrlValue}" />
                <input type="hidden" name="${threeDSResult.get.mdName}" value="${threeDSResult.get.mdValue}" />
                </form>
                <script>document.getElementById("formpay").submit();</script>
              </body>
            </html>"""
              Left(FormRedirection(form))
            }
          }
          else if (paymentMethod == CBPaymentMethod.THREEDS_REQUIRED) {
            // La carte n'est pas 3Ds alors que c'est obligatoire
            val finalBOShopTransaction = finishTransaction(boTransaction, TransactionStatus.FAILED, Some(MogopayConstant.ERROR_THREEDS_REQUIRED))
            Right(redirectionToCallback(finalBOShopTransaction))
          }
          else {
            Right(doMultiAuthorizations(boTransaction, boShopTransactions, paymentRequest, false))
          }
        }
      }
    }
  }

  def validatePayment(boShopTransaction: BOShopTransaction): ValidatePaymentResult = {
    updateBOShopTransactionStatus(boShopTransaction, ShopTransactionStatus.VALIDATION_REQUESTED, None)

    val paymentData = extractPaymentData(boShopTransaction)
    val paymentConfig = paymentData.config

    val payment: Payment = new Payment
    payment.setAmount(boShopTransaction.amount.toString)
    payment.setCurrency(boShopTransaction.currency.numericCode.toString)
    payment.setAction(ACTION_VALIDATION)
    payment.setMode(MODE_COMPTANT)
    payment.setContractNumber(paymentConfig.contractNumber)

    val request = new DoCaptureRequest()
    request.setTransactionID(paymentData.transactionId.orNull)
    request.setPayment(payment)

    createTransactionLog(boShopTransaction, DIR_OUT, TransactionShopStep.VALIDATE_PAYMENT, transformDoCaptureRequestAsLog(request))

    val proxy = createDirectPaymentAPIProxy(boShopTransaction, paymentConfig, TransactionShopStep.VALIDATE_PAYMENT)
    val response: DoCaptureResponse = proxy.doCapture(request)

    createTransactionLog(boShopTransaction, DIR_IN, TransactionShopStep.VALIDATE_PAYMENT, transformDoCaptureResponseAsLog(response))

    val code = if (response != null && response.getResult != null) Some(response.getResult.getCode) else None
    val newStatus = if (code.getOrElse("") == SUCCESS_CODE) ShopTransactionStatus.VALIDATED
    else if (code.isDefined) ShopTransactionStatus.VALIDATION_REFUSED
    else ShopTransactionStatus.VALIDATION_FAILED

    val status = if (newStatus == ShopTransactionStatus.VALIDATED) PaymentStatus.COMPLETE else PaymentStatus.INVALID
    val transactionId = if (response != null) Some(response.getTransaction.getId) else None
    val transactionDate = if (response != null) getTransactionDate(response.getTransaction) else None

    val newPaymentData = paymentData.copy(transactionId = transactionId, transactionDate = transactionDate)
    val finalShopTransaction = updateBOShopTransactionStatusAndData(boShopTransaction, newStatus, code, serializePaymentData(newPaymentData))

    ValidatePaymentResult(status, transactionId, transactionDate, finalShopTransaction)
  }

  def refundPayment(boShopTransaction: BOShopTransaction): RefundPaymentResult = {
    updateBOShopTransactionStatus(boShopTransaction, ShopTransactionStatus.REFUND_REQUESTED, None)

    val paymentData = extractPaymentData(boShopTransaction)
    val paymentConfig = paymentData.config

    val payment: Payment = new Payment
    payment.setAmount(boShopTransaction.amount.toString)
    payment.setCurrency(boShopTransaction.currency.numericCode.toString)
    payment.setAction(ACTION_REFUND)
    payment.setMode(MODE_COMPTANT)
    payment.setContractNumber(paymentConfig.contractNumber)

    val request = new DoRefundRequest
    request.setVersion("3")
    request.setTransactionID(paymentData.transactionId.orNull)
    request.setPayment(payment)

    createTransactionLog(boShopTransaction, DIR_OUT, TransactionShopStep.REFUND, transformDoRefundRequestAsLog(request))

    val proxy = createDirectPaymentAPIProxy(boShopTransaction, paymentConfig, TransactionShopStep.REFUND)
    val response: DoRefundResponse = proxy.doRefund(request)

    createTransactionLog(boShopTransaction, DIR_IN, TransactionShopStep.REFUND, transformDoRefundResponseAsLog(response))

    val code = if (response != null && response.getResult != null) Some(response.getResult.getCode) else None
    val newStatus = if (code.getOrElse("") == SUCCESS_CODE) ShopTransactionStatus.REFUNDED
    else if (code.isDefined) ShopTransactionStatus.REFUND_REFUSED
    else ShopTransactionStatus.REFUND_FAILED

    val status = if (newStatus == ShopTransactionStatus.REFUNDED) PaymentStatus.COMPLETE else PaymentStatus.INVALID
    val transactionId = if (response != null) Some(response.getTransaction.getId) else None
    val transactionDate = if (response != null) getTransactionDate(response.getTransaction) else None

    val newPaymentData = paymentData.copy(transactionId = transactionId, transactionDate = transactionDate)
    val finalShopTransaction = updateBOShopTransactionStatusAndData(boShopTransaction, newStatus, code, serializePaymentData(newPaymentData))

    RefundPaymentResult(status, transactionId, transactionDate, finalShopTransaction)
  }

  def done(boShopTransactionUuid: String, params: Map[String, String]): Uri = {
    val boTransaction = processPaylineResponse(boShopTransactionUuid, params)
    redirectionToCallback(boTransaction)
  }

  def callbackPayment(boShopTransactionUuid: String, params: Map[String, String]): Unit = {
    processPaylineResponse(boShopTransactionUuid, params)
  }

  def threeDSCallback(sessionData: scala.Option[SessionData], boTransactionUuid: String, params: Map[String, String]): Uri = {
    boTransactionHandler.find(boTransactionUuid).map { boTransaction =>
      sessionData.flatMap { _.paymentRequest}.map { paymentRequest =>
        val boShopTransactions = boShopTransactionHandler.findByTransactionUuid(boTransactionUuid).map { boShopTransaction =>
          val paymentData = extractPaymentData(boShopTransaction).copy(paylineMd = Some(params("MD")), paylinePares = Some(params("PaRes")))
          boShopTransaction.copy(paymentData = serializePaymentData(paymentData))
        }
        doMultiAuthorizations(boTransaction, boShopTransactions, paymentRequest, false)
      }.getOrElse{
        redirectionToCallback(finishTransaction(boTransaction, TransactionStatus.FAILED, Some(MogopayConstant.ERROR_PAYMENT_REQUEST_NOT_FOUND)))
      }
    }.getOrElse(throw TransactionNotFoundException(s"Transaction ${boTransactionUuid} is not found"))
  }

  protected def doMultiAuthorizations(boTransaction: BOTransaction,
                                      paymentRequest: PaymentRequest,
                                      paylineData: PaylineData,
                                      mogopay: Boolean): Uri = {
    val cart = paymentRequest.cart
    val boShopTransactions = cart.shopCarts.map { shopCart =>
      createBOShopTransaction(boTransaction, shopCart, serializePaymentData(paylineData))
    }
    doMultiAuthorizations(boTransaction, boShopTransactions, paymentRequest, mogopay)
  }

  protected def doMultiAuthorizations(boTransaction: BOTransaction,
                                      boShopTransactions : List[BOShopTransaction],
                                      paymentRequest: PaymentRequest,
                                      mogopay: Boolean): Uri = {
    val newBoShopTransactions = boShopTransactions.map { boShopTransaction =>
      doAuthorization(boTransaction, boShopTransaction, paymentRequest, TransactionShopStep.START_PAYMENT, mogopay)
    }
    val transactionsOK_KO = newBoShopTransactions.span { _.status == ShopTransactionStatus.AUTHORIZED }
    val transactionsRefused_Other = transactionsOK_KO._2.span { _.status == ShopTransactionStatus.AUTHORIZATION_REFUSED }
    val newStatus = if (transactionsOK_KO._2.isEmpty) TransactionStatus.PAYMENT_AUTHORIZED
    else if (transactionsRefused_Other._2.isEmpty) TransactionStatus.PAYMENT_REFUSED
    else TransactionStatus.PAYMENT_FAILED
    redirectionToCallback(finishTransaction(boTransaction, newStatus))
  }

  protected def doAuthorization(boTransaction: BOTransaction,
                                boShopTransaction: BOShopTransaction,
                                paymentRequest: PaymentRequest,
                                transactionStep: TransactionShopStep.TransactionShopStep,
                                mogopay: Boolean): BOShopTransaction = {

    updateBOShopTransactionStatus(boShopTransaction, ShopTransactionStatus.AUTHORIZATION_REQUESTED, None)

    val paymentData = extractPaymentData(boShopTransaction)
    val paymentConfig = paymentData.config

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

    val payment: Payment = new Payment
    payment.setAmount(boShopTransaction.amount.toString)
    payment.setCurrency(boShopTransaction.currency.numericCode.toString)
    payment.setAction(ACTION_AUTHORISATION)
    payment.setMode(MODE_COMPTANT)
    payment.setContractNumber(paymentConfig.contractNumber)

    val card: Card = new Card
    card.setNumber(paymentRequest.ccNumber.orNull)
    card.setType(fromCreditCardType(paymentRequest.cardType.getOrElse(CreditCardType.CB)))
    card.setExpirationDate(paymentRequest.expirationDate.map {new SimpleDateFormat(PAYLINE_EXPIRATION_DATE_FORMAT).format(_)}.orNull)
    card.setCvx(paymentRequest.cvv.orNull)

    val order : Order = new Order
    order.setRef(boShopTransaction.uuid)
    order.setAmount(payment.getAmount)
    order.setCurrency(payment.getCurrency)
    order.setDate(new SimpleDateFormat(PAYLINE_TRANSACTION_DATE_FORMAT).format(new Date))

    val authen: Authentication3DSecure = if (paymentData.paylineMd.isDefined && paymentData.paylinePares.isDefined) {
      val authen = new Authentication3DSecure
      authen.setMd(paymentData.paylineMd.get)
      authen.setPares(paymentData.paylinePares.get)
      authen
    } else null

    val request: DoAuthorizationRequest = new DoAuthorizationRequest
    request.setPayment(payment)
    request.setCard(card)
    request.setOrder(order)
    request.setAuthentication3DSecure(authen)

    createTransactionLog(boShopTransaction, DIR_OUT, transactionStep, transformDoAuthorizationRequestAsLog(request))

    val proxy = createDirectPaymentAPIProxy(boShopTransaction, paymentConfig, transactionStep)
    val response: DoAuthorizationResponse = proxy.doAuthorization(request)

    createTransactionLog(boShopTransaction, DIR_IN, transactionStep, transformDoAuthorizationResponseAsLog(response))

    val transactionId = if (response != null && response.getTransaction != null) scala.Option(response.getTransaction.getId) else None
    val transactionDate = if (response != null) getTransactionDate(response.getTransaction) else None

    val code = if (response != null && response.getResult != null) Some(response.getResult.getCode) else None

    val newStatus = if (code.getOrElse("") == SUCCESS_CODE) ShopTransactionStatus.AUTHORIZED
    else if (code.isDefined) ShopTransactionStatus.AUTHORIZATION_REFUSED
    else ShopTransactionStatus.AUTHORIZATION_FAILED

    val newPaymentData = paymentData.copy(transactionId = transactionId, transactionDate = transactionDate)
    updateBOShopTransactionStatusAndData(boShopTransaction, newStatus, code, serializePaymentData(newPaymentData))
  }

  protected def verifyEnrollment(sessionId: String,
                                 bOTransaction: BOTransaction,
                                 boShopTransactions : List[BOShopTransaction],
                                 paymentRequest: PaymentRequest,
                                 paymentData: PaylineData): ThreeDSResult = {

    // On prend le premier shop. Il sera utiliser pour stocker les traces et obtenir le OrderRef
    val boShopTransaction = boShopTransactions.head

    boShopTransactions.map { boShopTransaction =>
      updateBOShopTransactionStatus(boShopTransaction, ShopTransactionStatus.VERIFICATION_THREEDS, None)
    }

    val paymentConfig = paymentData.config

    val payment: Payment = new Payment
    payment.setAmount(bOTransaction.amount.toString)
    payment.setCurrency(bOTransaction.currency.numericCode.toString)
    payment.setAction(ACTION_AUTHORISATION)
    payment.setMode(MODE_COMPTANT)
    payment.setContractNumber(paymentConfig.contractNumber)

    val card: Card = new Card
    card.setNumber(paymentRequest.ccNumber.orNull)
    card.setType(fromCreditCardType(paymentRequest.cardType.getOrElse(CreditCardType.CB)))
    card.setExpirationDate(paymentRequest.expirationDate.map {new SimpleDateFormat(PAYLINE_EXPIRATION_DATE_FORMAT).format(_)}.orNull)
    card.setCvx(paymentRequest.cvv.orNull)

    val request: VerifyEnrollmentRequest = new VerifyEnrollmentRequest
    request.setPayment(payment)
    request.setCard(card)
    request.setOrderRef(boShopTransaction.uuid)

    boShopTransactions.map { boShopTransaction =>
      createTransactionLog(boShopTransaction, DIR_OUT, TransactionShopStep.CHECK_THREEDS, transformVerifyEnrollmentRequestAsLog(request))
    }

    val proxy = createDirectPaymentAPIProxy(boShopTransaction, paymentConfig, TransactionShopStep.CHECK_THREEDS)
    val response: VerifyEnrollmentResponse = proxy.verifyEnrollment(request)

    boShopTransactions.map { boShopTransaction =>
      createTransactionLog(boShopTransaction, DIR_IN, TransactionShopStep.CHECK_THREEDS, transformVerifyEnrollmentResponseAsLog(response))
    }

    val code = if (response != null && response.getResult != null) Some(response.getResult.getCode) else None

    val code3DS = if (Array("00000", "03000").contains(code.getOrElse(""))) ResponseCode3DS.APPROVED else ResponseCode3DS.REFUSED

    boShopTransactions.map { boShopTransaction =>
      updateBOShopTransactionStatus(boShopTransaction, ShopTransactionStatus.THREEDS_TESTED, code)
    }

    ThreeDSResult(
        code = code3DS,
        url = response.getActionUrl,
        method = response.getActionMethod,
        mdName = response.getMdFieldName,
        mdValue = response.getMdFieldValue,
        pareqName = response.getPareqFieldName,
        pareqValue = response.getPareqFieldValue,
        termUrlName = response.getTermUrlName,
        termUrlValue = s"${Settings.Mogopay.EndPoint}payline/3ds-callback/$sessionId/${bOTransaction.uuid}"
    )
  }
/*
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
  }*/

  protected def doWebPayment(sessionId: String,
                             bOTransaction: BOTransaction,
                             boShopTransaction: BOShopTransaction): (BOShopTransaction, scala.Option[Uri]) = {

    updateBOShopTransactionStatus(boShopTransaction, ShopTransactionStatus.PAYMENT_EXTERNAL_REQUESTED, None)

    val paymentData = extractPaymentData(boShopTransaction)
    val paymentConfig = paymentData.config

    val payment: Payment = new Payment
    payment.setAmount(boShopTransaction.amount.toString)
    payment.setCurrency(boShopTransaction.currency.numericCode.toString)
    payment.setAction(ACTION_AUTHORISATION)
    payment.setMode(MODE_COMPTANT)
    payment.setContractNumber(paymentConfig.contractNumber)

    val order : Order = new Order
    order.setRef(boShopTransaction.transactionUUID)
    order.setAmount(payment.getAmount)
    order.setCurrency(payment.getCurrency)
    order.setDate(new SimpleDateFormat(PAYLINE_TRANSACTION_DATE_FORMAT).format(new Date))

    val contractList = new SelectedContractList()
    contractList.getSelectedContract.add(payment.getContractNumber)

    val request: DoWebPaymentRequest = new DoWebPaymentRequest
    request.setVersion(Settings.Payline.Version)
    request.setPayment(payment)
    request.setSelectedContractList(contractList)
    request.setReturnURL(Settings.Mogopay.EndPoint + "payline/done/" + sessionId + "/" + boShopTransaction.uuid)
    request.setCancelURL(request.getReturnURL)
    request.setNotificationURL(Settings.Mogopay.EndPoint + "payline/callback/" + sessionId + "/"  + boShopTransaction.uuid)
    request.setOrder(order)
    request.setSecurityMode(Settings.Payline.SecurityMode)
    request.setLanguageCode(Settings.Payline.LanguageCode)
    request.setBuyer(null)
    request.setPrivateDataList(null)
    request.setRecurring(null)
    request.setCustomPaymentPageCode(paymentConfig.customPaymentPageCode.orNull)
    request.setCustomPaymentTemplateURL(paymentConfig.customPaymentTemplateURL.orNull)

    createTransactionLog(boShopTransaction, DIR_OUT, TransactionShopStep.DO_WEB_PAYMENT, transformDoWebPaymentRequestAsLog(request))

    val proxy = createWebPaymentAPIProxy(boShopTransaction, paymentConfig, TransactionShopStep.DO_WEB_PAYMENT)
    val response: DoWebPaymentResponse = proxy.doWebPayment(request)

    createTransactionLog(boShopTransaction, DIR_IN, TransactionShopStep.DO_WEB_PAYMENT, transformDoWebPaymentResponseAsLog(response))

    val code = if (response != null && response.getResult != null) scala.Option(response.getResult.getCode) else None
    val newStatus = if (code.getOrElse("") == SUCCESS_CODE) ShopTransactionStatus.PAYMENT_EXTERNAL_PROCESSING
    else ShopTransactionStatus.PAYMENT_EXTERNAL_FAILED

    val redirectUri = if (code.getOrElse("") == SUCCESS_CODE) Some(Uri(response.getRedirectURL))
    else None

    val newPaymentData = paymentData.copy(token = Some(response.getToken))

    (updateBOShopTransactionStatus(boShopTransaction.copy(paymentData = serializePaymentData(newPaymentData)), newStatus, code), redirectUri)
  }

  def getWebPaymentDetails(bOTransaction: BOTransaction,
                           boShopTransaction: BOShopTransaction): BOShopTransaction = {

    updateBOShopTransactionStatus(boShopTransaction, ShopTransactionStatus.PAYMENT_VERIFICATION, None)

    val paymentData = extractPaymentData(boShopTransaction)
    val paymentConfig = paymentData.config

    paymentData.token.map { token =>

      val request: GetWebPaymentDetailsRequest = new GetWebPaymentDetailsRequest
      request.setVersion(Settings.Payline.Version)
      request.setToken(token)

      createTransactionLog(boShopTransaction, DIR_OUT, TransactionShopStep.GET_WEB_PAYMENT_DETAILS, transformGetWebPaymentDetailsRequestAsLog(request))

      val proxy = createWebPaymentAPIProxy(boShopTransaction, paymentConfig, TransactionShopStep.GET_WEB_PAYMENT_DETAILS)
      val response : GetWebPaymentDetailsResponse = proxy.getWebPaymentDetails(request)

      createTransactionLog(boShopTransaction, DIR_IN, TransactionShopStep.GET_WEB_PAYMENT_DETAILS, transformGetWebPaymentDetailsResponseAsLog(response))

      val code = if (response != null && response.getResult != null) scala.Option(response.getResult.getCode) else None
      val newStatus = if (code.getOrElse("") == SUCCESS_CODE) ShopTransactionStatus.PAYMENT_VERIFIED
      else ShopTransactionStatus.PAYMENT_VERIFICATION_FAILED

      val creditCard = if (response.getCard != null) {
        val expirationDate = try {
          new SimpleDateFormat("MMyy").parse(response.getCard().getExpirationDate)
        } catch {
          case NonFatal(e) => null
        }
        val cardType = toCreditCardType(response.getCard.getType)
        Some(BOCreditCard(response.getCard.getNumber, holder = None, expiryDate = expirationDate, cardType = cardType))
      }
      else None

      updateBOShopTransactionStatus(boShopTransaction.copy(creditCard = creditCard), newStatus, code)
    }.getOrElse {
      updateBOShopTransactionStatus(boShopTransaction, ShopTransactionStatus.PAYMENT_VERIFICATION_FAILED, None)
    }
  }

  protected def processPaylineResponse(boShopTransactionUuid: String, params: Map[String, String]): BOTransaction = {
    boShopTransactionHandler.find(boShopTransactionUuid).map { boShopTransaction =>
      boTransactionHandler.find(boShopTransaction.transactionUUID).map { boTransaction =>
        val newShopTransaction = getWebPaymentDetails(boTransaction, boShopTransaction)
        val newStatus = if (newShopTransaction.status == ShopTransactionStatus.PAYMENT_VERIFIED) TransactionStatus.COMPLETED
        else TransactionStatus.PAYMENT_REFUSED

        finishTransaction(boTransaction.copy(creditCard = newShopTransaction.creditCard), newStatus, boShopTransaction.errorCode)
      }.getOrElse(throw TransactionNotFoundException(s"Transaction ${boShopTransaction.transactionUUID} is not found"))
    }.getOrElse(throw TransactionNotFoundException(s"Shop Transaction $boShopTransactionUuid is not found"))
  }

  protected def extractPaymentData(boShopTransaction: BOShopTransaction) : PaylineData = {
    JacksonConverter.deserialize[PaylineData](boShopTransaction.paymentData)
  }

  protected def serializePaymentData(paymentData: PaylineData) : String = {
    JacksonConverter.serialize(paymentData)
  }

  protected def getTransactionDate(transaction: Transaction) = {
    if (transaction != null && StringUtils.isNotEmpty(transaction.getDate)) Some(new SimpleDateFormat(PAYLINE_TRANSACTION_DATE_FORMAT).parse(transaction.getDate))
    else None
  }

/*
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
  }*/

  protected def transformDoResetRequestAsLog(data: DoResetRequest) : List[String] = {
    if (data == null) Nil
    else List(
      "transaction.id=" + data.getTransactionID
    )
  }

  protected def transformDoResetResponseAsLog(data: DoResetResponse) : List[String] = {
    if (data == null) Nil
    else transformResultAsLog(data.getResult) ++ transformTransactionAsLog(data.getTransaction)
  }

  protected def transformDoRefundRequestAsLog(data: DoRefundRequest) : List[String] = {
    if (data == null) Nil
    else List(
      "version=" + data.getVersion,
      "transaction.id=" + data.getTransactionID
    ) ++ transformPaymentAsLog(data.getPayment)
  }

  protected def transformDoRefundResponseAsLog(data: DoRefundResponse) : List[String] = {
    if (data == null) Nil
    else transformResultAsLog(data.getResult) ++ transformTransactionAsLog(data.getTransaction)
  }

  protected def transformDoCaptureRequestAsLog(data: DoCaptureRequest) : List[String] = {
    if (data == null) Nil
    else transformPaymentAsLog(data.getPayment) :+ ("transaction.id=" + data.getTransactionID)
  }

  protected def transformDoCaptureResponseAsLog(data: DoCaptureResponse) : List[String] = {
    if (data == null) Nil
    else transformResultAsLog(data.getResult) ++ transformTransactionAsLog(data.getTransaction)
  }

  protected def transformVerifyEnrollmentRequestAsLog(data: VerifyEnrollmentRequest) : List[String] = {
    if (data == null) Nil
    else transformPaymentAsLog(data.getPayment) ++ transformCardAsLog(data.getCard) :+ ("orderRef=" + data.getOrderRef)
  }

  protected def transformVerifyEnrollmentResponseAsLog(data: VerifyEnrollmentResponse) : List[String] = {
    if (data == null) Nil
    else List(
      "response.actionUrl=" + data.getActionUrl,
      "response.actionMethod=" + data.getActionMethod,
      "response.mdFieldName=" + data.getMdFieldName,
      "response.mdFieldValue=" + data.getMdFieldValue,
      "response.pareqFieldName=" + data.getPareqFieldName,
      "response.pareqFieldValue=" + data.getPareqFieldValue,
      "response.termUrlName=" + data.getTermUrlName,
      "response.termUrlValue=" + data.getTermUrlValue
    ) ++ transformResultAsLog(data.getResult)
  }

  protected def transformDoAuthorizationRequestAsLog(data: DoAuthorizationRequest) : List[String] = {
    if (data == null) Nil
    else transformPaymentAsLog(data.getPayment) ++
      transformCardAsLog(data.getCard) ++
      transformOrderAsLog(data.getOrder) ++
      transformAuthentication3DSecureAsLog(data.getAuthentication3DSecure)
  }

  protected def transformDoAuthorizationResponseAsLog(data: DoAuthorizationResponse) : List[String] = {
    if (data == null) Nil
    else transformResultAsLog(data.getResult) ++
      transformCardOutAsLog(data.getCard) ++
      transformTransactionAsLog(data.getTransaction)
  }

  protected def transformGetWebPaymentDetailsRequestAsLog(data: GetWebPaymentDetailsRequest) : List[String] = {
    if (data == null) Nil
    else List(
      "version=" + data.getVersion,
      "token=" + data.getToken
    )
  }

  protected def transformGetWebPaymentDetailsResponseAsLog(data: GetWebPaymentDetailsResponse) : List[String] = {
    if (data == null) Nil
    else transformResultAsLog(data.getResult) ++
      transformAuthorizationAsLog(data.getAuthorization) ++
      transformCardOutAsLog(data.getCard) ++
      transformTransactionAsLog(data.getTransaction) ++
      transformAuthentication3DSecureAsLog(data.getAuthentication3DSecure)
  }

  protected def transformAuthorizationAsLog(data: Authorization) : List[String] = {
    if (data == null) Nil
    else List(
      "result.authorization.number=" + data.getNumber,
      "result.authorization.date=" + data.getDate
    )
  }

  protected def transformCardOutAsLog(data: CardOut) : List[String] = {
    if (data == null) Nil
    else List(
      "result.card.expirationdate=" + data.getExpirationDate,
      "result.card.number=" + data.getNumber,
      "result.card.type=" + data.getType,
      "result.card.cardholder=" + data.getCardholder
    )
  }

  protected def transformTransactionAsLog(data: Transaction) : List[String] = {
    if (data == null) Nil
    else List(
      "result.transaction.id=" + data.getId,
      "result.transaction.date=" + data.getDate,
      "result.transaction.isPossibleFraud=" + data.getIsPossibleFraud,
      "result.transaction.isDuplicated=" + data.getIsDuplicated
    )
  }

  protected def transformDoWebPaymentResponseAsLog(data: DoWebPaymentResponse) : List[String] = {
    if (data == null) Nil
    else List(
      "result.token=" + data.getToken,
      "result.redirectURL=" + data.getRedirectURL
    ) ++ transformResultAsLog(data.getResult)
  }

  protected def transformResultAsLog(data: Result) : List[String] = {
    if (data == null) Nil
    else List(
      "result.code=" + data.getCode,
      "result.shortMessage=" + data.getShortMessage,
      "result.longMessage=" + data.getLongMessage
    )
  }

  protected def transformDoWebPaymentRequestAsLog(data: DoWebPaymentRequest) : List[String] = {
    if (data == null) Nil
    else List(
      "returnURL=" + data.getReturnURL,
      "languageCode=" + data.getLanguageCode,
      "securityMode=" + data.getSecurityMode,
      "customPaymentPageCode=" + data.getCustomPaymentPageCode,
      "customPaymentTemplateURL=" + data.getCustomPaymentTemplateURL
    ) ++ transformPaymentAsLog(data.getPayment) ++ transformOrderAsLog(data.getOrder)
  }

  protected def transformPaymentAsLog(data: Payment) : List[String] = {
    if (data == null) Nil
    else List(
      "payment.amount=" + data.getAmount,
      "payment.currency=" + data.getCurrency,
      "payment.contractNumbner=" + data.getContractNumber,
      "payment.action=" + data.getAction,
      "payment.mode=" + data.getMode
    )
  }

  protected def transformOrderAsLog(data: Order) : List[String] = {
    if (data == null) Nil
    else List(
      "order.ref=" + data.getRef,
      "order.amount=" + data.getAmount,
      "order.currency=" + data.getCurrency,
      "order.date=" + data.getDate
    )
  }

  protected def transformCardAsLog(data: Card) : List[String] = {
    if (data == null) Nil
    else List(
      "card.number=" + UtilHandler.hideCardNumber(data.getNumber, "X"),
      "card.type=" + data.getType,
      "card.expirationDate=" + data.getExpirationDate,
      "card.cvx=XXX"
    )
  }

  protected def transformAuthentication3DSecureAsLog(data: Authentication3DSecure) : List[String] = {
    if (data == null) Nil
    else List(
      "authentication3DSecure.typeSecurisation=" + data.getTypeSecurisation,
      "authentication3DSecure.vadsResult=" + data.getVadsResult,
      "authentication3DSecure.md=" + data.getMd,
      "authentication3DSecure.pares=" + data.getPares
    )
  }

  protected def createWebPaymentAPIProxy(boShopTransaction: BOShopTransaction,
                                         config: PaylineConfig,
                                         step: TransactionShopStep.TransactionShopStep) : WebPaymentAPI = {
    val url: URL                  = classOf[WebPaymentAPI].getResource("/wsdl/WebPaymentAPI_v4.38.wsdl")
    val ss: WebPaymentAPI_Service = new WebPaymentAPI_Service(url, WEB_PAYMENT_API_SERVICE_NAME)
    val proxy: WebPaymentAPI = ss.getWebPaymentAPI
    val proxyAsBindingProvider : BindingProvider = proxy.asInstanceOf[BindingProvider]
    val requestContext = proxyAsBindingProvider.getRequestContext
    requestContext.put("javax.xml.ws.security.auth.username", config.account)
    requestContext.put("javax.xml.ws.security.auth.password", config.key)
    requestContext.put("javax.xml.ws.service.endpoint.address", Settings.Payline.WebEndPoint)
    requestContext.put(TrustedSSLFactory.JaxwsSslSockeetFactory, TrustedSSLFactory.getTrustingSSLSocketFactory)
    requestContext.put(NaiveHostnameVerifier.JaxwsHostNameVerifier, new NaiveHostnameVerifier)
    requestContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true.asInstanceOf[Object])

    val binding: Binding = proxyAsBindingProvider.getBinding
    val handlerList      = binding.getHandlerChain
    handlerList.add(new TraceHandler(boShopTransaction.transactionUUID, boShopTransaction.uuid, "PAYLINE", step))
    binding.setHandlerChain(handlerList)
    proxy
  }

  private def createDirectPaymentAPIProxy(boShopTransaction: BOShopTransaction,
                                          config: PaylineConfig,
                                          step: TransactionShopStep.TransactionShopStep): DirectPaymentAPI = {
    val url: URL                          = classOf[WebPaymentAPI].getResource("/wsdl/DirectPaymentAPI_v4.38.wsdl")
    val service: DirectPaymentAPI_Service = new DirectPaymentAPI_Service(url)
    val proxy: DirectPaymentAPI           = service.getDirectPaymentAPI
    val proxyAsBindingProvider : BindingProvider = proxy.asInstanceOf[BindingProvider]
    val requestContext = proxyAsBindingProvider.getRequestContext
    requestContext.put("javax.xml.ws.security.auth.username", config.account)
    requestContext.put("javax.xml.ws.security.auth.password", config.key)
    requestContext.put("javax.xml.ws.service.endpoint.address", Settings.Payline.DirectEndPoint)
    requestContext.put(TrustedSSLFactory.JaxwsSslSockeetFactory, TrustedSSLFactory.getTrustingSSLSocketFactory)
    requestContext.put(NaiveHostnameVerifier.JaxwsHostNameVerifier, new NaiveHostnameVerifier)

    val binding: Binding = proxyAsBindingProvider.getBinding
    val handlerList      = binding.getHandlerChain
    handlerList.add(new TraceHandler(boShopTransaction.transactionUUID, boShopTransaction.uuid, "PAYLINE", step))
    binding.setHandlerChain(handlerList)
    proxy
  }
}