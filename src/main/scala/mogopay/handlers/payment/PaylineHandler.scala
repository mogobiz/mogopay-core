package mogopay.handlers.payment

import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import javax.xml.namespace.QName
import javax.xml.ws.{Binding, BindingProvider}

import com.experian.payline.ws.impl.{DirectPaymentAPI, DirectPaymentAPI_Service, DoAuthorizationRequest, DoAuthorizationResponse, DoResetRequest, DoResetResponse, DoWebPaymentRequest, DoWebPaymentResponse, GetWebPaymentDetailsRequest, GetWebPaymentDetailsResponse, VerifyEnrollmentRequest, VerifyEnrollmentResponse, WebPaymentAPI, WebPaymentAPI_Service}
import com.experian.payline.ws.obj.{Authentication3DSecure, Authorization, Card, Order, Payment, Result, Transaction}
import com.experian.payline.ws.wrapper.WebPayment
import mogopay.codes.MogopayConstant
import mogopay.config.HandlersConfig._
import mogopay.config.Settings
import mogopay.es.EsClient
import mogopay.exceptions.Exceptions.MogopayError
import mogopay.handlers.UtilHandler
import mogopay.model.Mogopay.CreditCardType.CreditCardType
import mogopay.model.Mogopay.{ResponseCode3DS, TransactionStatus, _}
import mogopay.util.{NaiveHostnameVerifier, TrustedSSLFactory, GlobalUtil}
import mogopay.util.GlobalUtil._
import org.json4s.jackson.JsonMethods._
import spray.http.Uri

import scala.util._


/**
 * @see com.ebiznext.mogopay.payment.PaylinePaymentService
 */
object PaylineHandler {

  def fromCreditCardType(`type`: CreditCardType): String = {
    var retour: String = "CB"
    if (CreditCardType.CB == `type`) {
      retour = "CB"
    }
    else if (CreditCardType.VISA == `type`) {
      retour = "CB"
    }
    else if (CreditCardType.MASTER_CARD == `type`) {
      retour = "MASTERCARD"
    }
    else if (CreditCardType.DISCOVER == `type`) {
      retour = "CB"
    }
    else if (CreditCardType.AMEX == `type`) {
      retour = "AMEX"
    }
    else if (CreditCardType.SWITCH == `type`) {
      retour = "SWITCH"
    }
    else if (CreditCardType.SOLO == `type`) {
      retour = "CB"
    }
    return retour
  }

  def toCreditCardType(`type`: String): CreditCardType = {
    var retour: CreditCardType = CreditCardType.CB
    if ("CB" == `type`) {
      retour = CreditCardType.VISA
    }
    else if ("MASTERCARD" == `type`) {
      retour = CreditCardType.MASTER_CARD
    }
    else if ("AMEX" == `type`) {
      retour = CreditCardType.AMEX
    }
    else if ("SWITCH" == `type`) {
      retour = CreditCardType.SWITCH
    }
    return retour
  }

  val ACTION_AUTHORISATION_VALIDATION: String = "101"
  val MODE_COMPTANT: String = "CPT"
  val ServiceName: QName = new QName("http://impl.ws.payline.experian.com", "WebPaymentAPI")
}

class PaylineHandler(handlerName:String) extends PaymentHandler {
  PaymentHandler.register(handlerName, this)
  implicit val formats = new org.json4s.DefaultFormats {
  }

  import mogopay.handlers.payment.PaylineHandler._

  def startPayment(sessionData: SessionData): Try[Either[String, Uri]] = {
    val transactionUUID = sessionData.transactionUuid.get

    val paymentConfig: PaymentConfig = sessionData.paymentConfig.orNull
    val vendorId = sessionData.merchantId.get
    val paymentRequest = sessionData.paymentRequest.get

    if (paymentConfig == null || paymentConfig.cbProvider != CBPaymentProvider.PAYLINE) {
      Failure(new MogopayError(MogopayConstant.InvalidSystemPayConfig))
    } else {
      transactionHandler.startPayment(
        vendorId, transactionUUID, paymentRequest, PaymentType.CREDIT_CARD, CBPaymentProvider.PAYLINE)

      var threeDSResult: ThreeDSResult = null

      if (sessionData.mogopay) {
        val paymentResult = submit(vendorId, transactionUUID, paymentConfig, paymentRequest, true)
        Success(Right(finishPayment(sessionData, paymentResult)))
      } else if (!sessionData.mogopay && paymentConfig.paymentMethod == CBPaymentMethod.EXTERNAL) {
        val paymentResult = doWebPayment(vendorId, transactionUUID, paymentConfig, paymentRequest, sessionData.uuid)
        sessionData.token = Option(paymentResult.token)
        if (paymentResult.data != null && paymentResult.data.nonEmpty) {
          Success(Left(paymentResult.data))
        } else {
          Success(Right(finishPayment(sessionData, paymentResult)))
        }
      } else if (Array(CBPaymentMethod.THREEDS_IF_AVAILABLE, CBPaymentMethod.THREEDS_REQUIRED).contains(paymentConfig.paymentMethod)) {
        threeDSResult = check3DSecure(sessionData.uuid, vendorId, transactionUUID, paymentConfig, paymentRequest)
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
          Success(Left(form))
        }
        else if (paymentConfig.paymentMethod == CBPaymentMethod.THREEDS_IF_AVAILABLE) {
          // on lance un paiement classique
          val paymentResult = submit(vendorId, transactionUUID, paymentConfig, paymentRequest, sessionData.mogopay)
          Success(Right(finishPayment(sessionData, paymentResult)))
        }
        else {
          // La carte n'est pas 3Ds alors que c'est obligatoire
          Success(Right(finishPayment(sessionData, GlobalUtil.createThreeDSNotEnrolledResult())))
        }
      } else {
        // on lance un paiement classique
        val paymentResult = submit(vendorId, transactionUUID, paymentConfig, paymentRequest, sessionData.mogopay)
        Success(Right(finishPayment(sessionData, paymentResult)))
      }
    }
  }

  def done(sessionData: SessionData, params: Map[String, String]): Try[Uri] = {
    val paymentResult = handleResponse(sessionData, params)
    Success(finishPayment(sessionData, paymentResult))
  }

  def callbackPayment(sessionData: SessionData, params: Map[String, String]): Try[PaymentResult] = {
    val paymentResult = handleResponse(sessionData, params)
    Success(paymentResult)
  }

  private def handleResponse(sessionData: SessionData, params: Map[String, String]): PaymentResult = {
    val transactionUuid = sessionData.transactionUuid.get
    val paymentConfig = sessionData.paymentConfig.get
    val vendorId = sessionData.merchantId.get
    val paymentRequest = sessionData.paymentRequest.get
    val paymentResult = getWebPaymentDetails(vendorId, transactionUuid, paymentConfig, paymentRequest, sessionData.token.orNull)
    paymentResult
  }

  def threeDSCallback(sessionData: SessionData, params: Map[String, String]): Try[Uri] = {
    if (!sessionData.waitFor3DS) {
      // invalid call
      Failure(throw new Exception("Invalid payment hain"))
    }
    else {
      val errorURL = sessionData.errorURL.getOrElse("")
      val transactionUUID = sessionData.transactionUuid.get
      val vendorId = sessionData.merchantId.get
      val paymentRequest = sessionData.paymentRequest.orNull
      val paymentConfig = sessionData.paymentConfig
      sessionData.waitFor3DS = false
      try {
        val paymentRequest2 = paymentRequest.copy(paylineMd = params("MD"), paylinePares = params("PaRes"))
        val result = submit(vendorId, transactionUUID, paymentConfig.orNull, paymentRequest2, sessionData.mogopay)
        Success(finishPayment(sessionData, result))
      }
      catch {
        case ex: Exception =>
          ex.printStackTrace()
          Success(Uri(errorURL))
      }
    }
  }


  def check3DSecure(sessionUuid: String, vendorUuid: Document, transactionUuid: Document, paymentConfig: PaymentConfig, infosPaiement: PaymentRequest): ThreeDSResult = {
    val vendor = EsClient.load[Account](vendorUuid).get
    val transaction = EsClient.load[BOTransaction](transactionUuid).get
    val parametres = paymentConfig.cbParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())
    val numeroContrat: String = parametres("paylineContract")
    var logdata: String = null
    transactionHandler.updateStatus(vendorUuid, transactionUuid, null, TransactionStatus.VERIFICATION_THREEDS, null)
    val paiement: Payment = new Payment

    paiement.setAmount(infosPaiement.amount.toString)
    paiement.setCurrency(infosPaiement.currency.numericCode.toString)
    paiement.setAction(ACTION_AUTHORISATION_VALIDATION)
    paiement.setMode(MODE_COMPTANT)
    paiement.setContractNumber(numeroContrat)
    val formatDatePayline = new SimpleDateFormat("MMyy")

    logdata = "paiement.amount=" + paiement.getAmount
    logdata += " paiement.currency=" + paiement.getCurrency
    logdata += "&paiement.action=" + paiement.getAction
    logdata += "&paiement.mode=" + paiement.getMode
    logdata += "&paiement.contractNumber=" + paiement.getContractNumber
    val card: Card = new Card
    card.setNumber(infosPaiement.ccNumber)
    card.setType(fromCreditCardType(infosPaiement.cardType))
    card.setExpirationDate(formatDatePayline.format(infosPaiement.expirationDate))
    card.setCvx(infosPaiement.cvv)
    logdata += "&card.number=" + UtilHandler.hideCardNumber(card.getNumber, "X")
    logdata += "&card.type=" + card.getType
    logdata += "&card.expirationDate=" + card.getExpirationDate
    logdata += "&card.cvx=XXX"
    val requete: VerifyEnrollmentRequest = new VerifyEnrollmentRequest
    requete.setPayment(paiement)
    requete.setCard(card)
    requete.setOrderRef(infosPaiement.transactionSequence)
    logdata += "&orderRef=" + requete.getOrderRef
    val botlog = BOTransactionLog(newUUID, "OUT", logdata, "PAYLINE", transaction.uuid)
    EsClient.index(botlog, false)

    val response: VerifyEnrollmentResponse = createProxy(transaction, parametres).verifyEnrollment(requete)
    val result: Result = response.getResult
    val code: String = result.getCode
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
        termUrlValue = s"${Settings.MogopayEndPoint}payline/3ds-callback/${sessionUuid}" //response.getTermUrlValue
      )

    }
    else if (code != null && code.startsWith("023")) {
      default3DS.copy(code = ResponseCode3DS.INVALID)
    }
    else if (code != null && (code.startsWith("01") || code.startsWith("03"))) {
      default3DS.copy(code = ResponseCode3DS.REFUSED)
    }
    else {
      default3DS.copy(code = ResponseCode3DS.ERROR)
    }
    val botlogIn = BOTransactionLog(newUUID, "OUT", logdata, "PAYLINE", transaction.uuid)
    EsClient.index(botlogIn, false)
    transactionHandler.updateStatus3DS(vendorUuid, transactionUuid, retour.code, code)
    retour
  }

  private def submit(vendorUuid: Document, transactionUuid: Document, paymentConfig: PaymentConfig, infosPaiement: PaymentRequest, mogopay: Boolean): PaymentResult = {
    val vendor = EsClient.load[Account](vendorUuid).get
    val transaction = EsClient.load[BOTransaction](transactionUuid).get
    val parametres = paymentConfig.cbParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())
    var logdata: String = ""
    val formatDatePayline = new SimpleDateFormat("MMyy")

    transactionHandler.updateStatus(vendorUuid, transactionUuid, null, TransactionStatus.PAYMENT_REQUESTED, null)
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
      //      println(cwresp.getCard)
      //    }
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    val orderDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm")
    val paiement: Payment = new Payment
    paiement.setAmount(infosPaiement.amount.toString)
    paiement.setCurrency(infosPaiement.currency.numericCode.toString)
    paiement.setAction(ACTION_AUTHORISATION_VALIDATION)
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
    card.setExpirationDate(if (infosPaiement.expirationDate != null) formatDatePayline.format(infosPaiement.expirationDate) else null)
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
    val botlog = BOTransactionLog(newUUID, "OUT", logdata, "PAYLINE", transaction.uuid)
    EsClient.index(botlog, false)
    val requete: DoAuthorizationRequest = new DoAuthorizationRequest
    requete.setPayment(paiement)
    requete.setCard(card)
    requete.setOrder(order)
    requete.setAuthentication3DSecure(authen)
    val response: DoAuthorizationResponse = createProxy(transaction, parametres).doAuthorization(requete)
    val result: Result = response.getResult
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
      token = null
    )


    paymentResult = if ("00000" == code) {
      val authorisation: Authorization = response.getAuthorization
      logdata += "&authorisation.number=" + authorisation.getNumber
      logdata += "&authorisation.date=" + authorisation.getDate
      val tr: Transaction = response.getTransaction
      val transactionDate = new SimpleDateFormat("dd/MM/yy HH:mm").parse(tr.getDate)
      paymentResult.copy(
        gatewayTransactionId = tr.getId,
        transactionDate = transactionDate,
        authorizationId = authorisation.getNumber,
        status = PaymentStatus.COMPLETE)

    }
    else {
      paymentResult.copy(
        errorCodeOrigin = code,
        errorMessageOrigin = Option(result.getLongMessage),
        bankErrorCode = "",
        bankErrorMessage = Option(BankErrorCodes.getErrorMessage("")),
        status = PaymentStatus.FAILED)
    }
    transactionHandler.finishPayment(vendorUuid, transactionUuid, if ("00000" == code) TransactionStatus.PAYMENT_CONFIRMED else TransactionStatus.PAYMENT_REFUSED, paymentResult, code)

    val botlogIn = BOTransactionLog(newUUID, "IN", logdata, "PAYLINE", transaction.uuid)
    EsClient.index(botlogIn, false)

    paymentResult
  }

  private def cancel(vendorUuid: Document, transactionUuid: String, paymentConfig: PaymentConfig, infosPaiement: CancelRequest): CancelResult = {
    val vendor = EsClient.load[Account](vendorUuid).get
    val transaction = EsClient.load[BOTransaction](transactionUuid).get
    val parametres = paymentConfig.cbParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())
    transactionHandler.updateStatus(vendorUuid, transactionUuid, null, TransactionStatus.CANCEL_REQUESTED, null)

    val requete: DoResetRequest = new DoResetRequest
    requete.setTransactionID(infosPaiement.id)
    var logdata: String = ""
    logdata = "requete.transactionID=" + requete.getTransactionID
    val botlog = BOTransactionLog(newUUID, "OUT", logdata, "PAYLINE", transaction.uuid)
    EsClient.index(botlog, false)


    val response: DoResetResponse = createProxy(transaction, parametres).doReset(requete)
    val result: Result = response.getResult
    logdata = ""
    logdata += "&result.code=" + result.getCode
    logdata += "&result.shortMessage=" + result.getShortMessage
    logdata += "&result.longMessage=" + result.getLongMessage
    val botlogIn = BOTransactionLog(newUUID, "IN", logdata, "PAYLINE", transaction.uuid)
    EsClient.index(botlogIn, false)

    val code: String = result.getCode

    transactionHandler.updateStatus(vendorUuid, transactionUuid, null, if ("00000" == code) TransactionStatus.CANCEL_CONFIRMED else TransactionStatus.CANCEL_FAILED, code)

    CancelResult(id = infosPaiement.id, status = if ("00000" == code) PaymentStatus.CANCELED else PaymentStatus.CANCEL_FAILED, errorCodeOrigin = code, errorMessageOrigin = Option(result.getLongMessage))
  }

  private def createProxy(transaction: BOTransaction, parametres: Map[String, String]): DirectPaymentAPI = {
    val accountId: String = parametres("paylineAccount")
    val cleAccess: String = parametres("paylineKey")
    val endpoint: String = Settings.Payline.DirectEndPoint
    val url: URL = classOf[PaylineHandler].getResource("/wsdl/DirectPaymentAPI_v4.38.wsdl")
    val service: DirectPaymentAPI_Service = new DirectPaymentAPI_Service(url)
    val proxy: DirectPaymentAPI = service.getDirectPaymentAPI
    (proxy.asInstanceOf[BindingProvider]).getRequestContext.put("javax.xml.ws.security.auth.username", accountId)
    (proxy.asInstanceOf[BindingProvider]).getRequestContext.put("javax.xml.ws.security.auth.password", cleAccess)
    (proxy.asInstanceOf[BindingProvider]).getRequestContext.put("javax.xml.ws.service.endpoint.address", endpoint)
    (proxy.asInstanceOf[BindingProvider]).getRequestContext.put(TrustedSSLFactory.JaxwsSslSockeetFactory, TrustedSSLFactory.getTrustingSSLSocketFactory)
    (proxy.asInstanceOf[BindingProvider]).getRequestContext.put(NaiveHostnameVerifier.JaxwsHostNameVerifier, new NaiveHostnameVerifier)
    val binding: javax.xml.ws.Binding = (proxy.asInstanceOf[BindingProvider]).getBinding
    proxy
  }

  def doWebPayment(vendorUuid: Document, transactionUuid: Document, paymentConfig: PaymentConfig, paymentRequest: PaymentRequest, sessionId: String): PaymentResult = {
    val vendor = EsClient.load[Account](vendorUuid).get
    val transaction = EsClient.load[BOTransaction](transactionUuid).get
    val parametres = paymentConfig.cbParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())
    val payment: Payment = new Payment
    payment.setAmount("" + paymentRequest.amount)
    payment.setCurrency("" + paymentRequest.currency.numericCode)
    payment.setAction(Settings.Payline.PaymentAction)
    payment.setMode(Settings.Payline.PaymentMode)
    if (payment.getContractNumber == null || payment.getContractNumber.length == 0)
      payment.setContractNumber(parametres("paylineContract"))

    var logdata: String = ""
    logdata += "payment.amount=" + payment.getAmount
    logdata += "&payment.currency=" + payment.getCurrency
    logdata += "&payment.contractNumner=" + payment.getContractNumber
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
    parameters.setReturnURL(Settings.MogopayEndPoint + "payline/done/" + sessionId)
    parameters.setCancelURL(Settings.MogopayEndPoint + "payline/done/" + sessionId)
    parameters.setNotificationURL(Settings.MogopayEndPoint + "payline/callback/" + sessionId)
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
    val port: WebPaymentAPI = null
    val webPayment: WebPayment = new WebPayment
    val url: URL = classOf[WebPaymentAPI].getResource("/wsdl/WebPaymentAPI_v4.38.wsdl")
    val ss: WebPaymentAPI_Service = new WebPaymentAPI_Service(url, ServiceName)

    val proxy: WebPaymentAPI = ss.getWebPaymentAPI
    (proxy.asInstanceOf[BindingProvider]).getRequestContext.put("javax.xml.ws.security.auth.username", parametres("paylineAccount"))
    (proxy.asInstanceOf[BindingProvider]).getRequestContext.put("javax.xml.ws.security.auth.password", parametres("paylineKey"))
    (proxy.asInstanceOf[BindingProvider]).getRequestContext.put("javax.xml.ws.service.endpoint.address", Settings.Payline.WebEndPoint)
    (proxy.asInstanceOf[BindingProvider]).getRequestContext.put(TrustedSSLFactory.JaxwsSslSockeetFactory, TrustedSSLFactory.getTrustingSSLSocketFactory)
    (proxy.asInstanceOf[BindingProvider]).getRequestContext.put(NaiveHostnameVerifier.JaxwsHostNameVerifier, new NaiveHostnameVerifier)
    (proxy.asInstanceOf[BindingProvider]).getRequestContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true.asInstanceOf[Object])

    val binding: Binding = (proxy.asInstanceOf[BindingProvider]).getBinding
    val handlerList = binding.getHandlerChain
    handlerList.add(new TraceHandler(transaction, "PAYLINE"))
    binding.setHandlerChain(handlerList)
    val botlog = BOTransactionLog(newUUID, "OUT", logdata, "PAYLINE", transaction.uuid)
    EsClient.index(botlog, false)

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
    val botlogIn = BOTransactionLog(newUUID, "IN", logdata, "PAYLINE", transaction.uuid)
    EsClient.index(botlogIn, false)
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
                               errorMessageOrigin: Option[String],
                               data: String,
                               bankErrorCode: String,
                               bankErrorMessage: Option[String],
                               token: String)

     */
    if (result != null && result.getResult != null) {
      if (result.getResult.getCode == "00000") {
        PaymentResult(
          status = null,
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
          errorMessageOrigin = Option(result.getResult.getLongMessage),
          bankErrorCode = "",
          bankErrorMessage = Option(BankErrorCodes.getErrorMessage("")),
          token = result.getToken,
          data =
            s"""
                |<html>
                |<head>
                |<meta http-equiv="refresh" content="0; URL=${result.getRedirectURL}">
                |</head>
                |<body>
                |</body>
                |</html>
              """.stripMargin)
      }
      else {
        throw new Exception(result.getResult.getCode)
      }
    }
    else {
      throw new Exception("Unkown")
    }
  }

  def getWebPaymentDetails(vendorUuid: Document, transactionUuid: Document, paymentConfig: PaymentConfig, paymentRequest: PaymentRequest, token: String): PaymentResult = {
    val vendor = EsClient.load[Account](vendorUuid).get
    val transaction = EsClient.load[BOTransaction](transactionUuid).get
    val parametres = paymentConfig.cbParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())
    val url: URL = classOf[WebPaymentAPI].getResource("/wsdl/WebPaymentAPI_v4.38.wsdl")
    val ss: WebPaymentAPI_Service = new WebPaymentAPI_Service(url, ServiceName)
    val proxy: WebPaymentAPI = ss.getWebPaymentAPI
    (proxy.asInstanceOf[BindingProvider]).getRequestContext.put("javax.xml.ws.security.auth.username", parametres("paylineAccount"))
    (proxy.asInstanceOf[BindingProvider]).getRequestContext.put("javax.xml.ws.security.auth.password", parametres("paylineKey"))
    (proxy.asInstanceOf[BindingProvider]).getRequestContext.put("javax.xml.ws.service.endpoint.address", Settings.Payline.WebEndPoint)
    val binding: Binding = (proxy.asInstanceOf[BindingProvider]).getBinding
    (proxy.asInstanceOf[BindingProvider]).getRequestContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true.asInstanceOf[Object])
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
    val botlog = BOTransactionLog(newUUID, "OUT", logdata, "PAYLINE", transaction.uuid)
    EsClient.index(botlog, false)
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
    val botlogIn = BOTransactionLog(newUUID, "IN", logdata, "PAYLINE", transaction.uuid)
    EsClient.index(botlogIn, false)

    val paymentResult = PaymentResult(
      status = if (result.getResult.getCode == "00000") PaymentStatus.COMPLETE else PaymentStatus.FAILED,
      transactionSequence = paymentRequest.transactionSequence,
      orderDate = paymentRequest.orderDate,
      amount = if (result.getPayment() != null) result.getPayment.getAmount.toLong else paymentRequest.amount,
      ccNumber = if (result.getCard != null) result.getCard.getNumber else null,
      cardType = if (result.getCard != null) toCreditCardType(result.getCard.getType) else null,
      expirationDate = if (result.getCard() != null && result.getCard().getExpirationDate != null) new SimpleDateFormat("MMyy").parse(result.getCard().getExpirationDate) else null,
      cvv = paymentRequest.cvv,
      transactionCertificate = null,
      transactionDate = if (result.getTransaction() != null) new SimpleDateFormat("dd/MM/yy HH:mm").parse(result.getTransaction().getDate) else new Date,
      authorizationId = if (result.getAuthorization() != null) result.getAuthorization().getNumber else null,
      gatewayTransactionId = if (result.getTransaction() != null) result.getTransaction().getId else null,
      errorCodeOrigin = result.getResult.getCode,
      errorMessageOrigin = Option(result.getResult.getLongMessage),
      bankErrorCode = "",
      bankErrorMessage = Option(BankErrorCodes.getErrorMessage("")),
      token = null,
      data = null)

    transactionHandler.finishPayment(vendorUuid, transactionUuid, if (result.getResult.getCode == "00000") TransactionStatus.PAYMENT_CONFIRMED else TransactionStatus.PAYMENT_REFUSED, paymentResult, result.getResult().getCode())
    paymentResult
  }
}


