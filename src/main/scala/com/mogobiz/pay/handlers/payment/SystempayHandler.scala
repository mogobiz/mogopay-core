package com.mogobiz.pay.handlers.payment

import java.net.URL
import java.text.SimpleDateFormat
import java.util
import java.util.{Date, GregorianCalendar}
import javax.xml.datatype.{DatatypeFactory, XMLGregorianCalendar}
import javax.xml.namespace.QName
import javax.xml.ws.BindingProvider
import javax.xml.ws.handler.MessageContext

import com.lyra.vads.ws.stub.{CreatePaiementInfo, Standard, StandardWS, TransactionInfo}
import com.lyra.vads.ws3ds.stub.{PaResInfo, ThreeDSecure, VeResPAReqInfo}
import com.mogobiz.pay.codes.MogopayConstant
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.es.EsClient
import com.mogobiz.pay.exceptions.Exceptions._
import com.mogobiz.pay.model.Mogopay.TransactionStatus._
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.pay.settings.{Settings, Environment}
import com.mogobiz.utils.GlobalUtil
import com.mogobiz.utils.GlobalUtil._
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, StringInput}
import spray.http.Uri

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util._
import scala.util.control.NonFatal


class SystempayHandler(handlerName: String) extends PaymentHandler {
  PaymentHandler.register(handlerName, this)
  implicit val formats = new org.json4s.DefaultFormats {}
  val systempayClient = new SystempayClient

  /**
   * Right for a redirect, Left for a complete
   * Returns either raw html of url to be redirected to
   */
  def startPayment(sessionData: SessionData): Either[String, Uri] = {
    val transactionUUID = sessionData.transactionUuid.get

    val paymentConfig: PaymentConfig = sessionData.paymentConfig.orNull
    val vendorId = sessionData.merchantId.get
    val paymentRequest = sessionData.paymentRequest.get

    if (paymentConfig == null || paymentConfig.cbProvider != CBPaymentProvider.SYSTEMPAY) {
      throw MogopayError(MogopayConstant.InvalidSystemPayConfig)
    } else {
      transactionHandler.startPayment(
        vendorId, transactionUUID, paymentRequest, PaymentType.CREDIT_CARD, CBPaymentProvider.SYSTEMPAY)

      var threeDSResult: ThreeDSResult = null

      if (sessionData.mogopay) {
        val paymentResult = systempayClient.submit(vendorId, transactionUUID, paymentConfig, paymentRequest)
        Right(finishPayment(sessionData, paymentResult))
      } else if (paymentConfig.paymentMethod == CBPaymentMethod.EXTERNAL) {
        val paymentResult = systempayClient.submit(vendorId, transactionUUID, paymentConfig, paymentRequest)
        if (paymentResult.data.nonEmpty) {
          Left(paymentResult.data)
        } else {
          Right(finishPayment(sessionData, paymentResult))
        }
      } else if (Array(CBPaymentMethod.THREEDS_IF_AVAILABLE, CBPaymentMethod.THREEDS_REQUIRED).contains(paymentConfig.paymentMethod)) {
        threeDSResult = systempayClient.check3DSecure(sessionData.uuid, vendorId, transactionUUID, paymentConfig, paymentRequest)

        if (Option(threeDSResult).map(_.code) == Some(ResponseCode3DS.APPROVED)) {
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
          val paymentResult = systempayClient.submit(vendorId, transactionUUID, paymentConfig, paymentRequest)
          Right(finishPayment(sessionData, paymentResult))
        } else {
          Right(finishPayment(sessionData, createThreeDSNotEnrolledResult()))
        }
      } else {
        val paymentResult = systempayClient.submit(vendorId, transactionUUID, paymentConfig, paymentRequest)
        Right(finishPayment(sessionData, paymentResult))
      }
    }
  }

  def done(sessionData: SessionData, params: Map[String, String]): Uri = {
    val transactionUUID = sessionData.transactionUuid.get
    val vendorId = sessionData.merchantId.get
    val paymentRequest: PaymentRequest = sessionData.paymentRequest.get

    val transaction = boTransactionHandler.find(transactionUUID).orNull

    val resultatPaiement: PaymentResult =
      if (!Array(PAYMENT_CONFIRMED, PAYMENT_REFUSED).contains(transaction.status)) {
        handleResponse(params)
      } else {
        PaymentResult(
          newUUID, null, -1L, "", null, null, "", "", null, "", "",
          if (transaction.status == TransactionStatus.PAYMENT_CONFIRMED) {
            PaymentStatus.COMPLETE
          } else {
            PaymentStatus.FAILED
          },
          transaction.errorCodeOrigin.getOrElse(""),
          transaction.errorMessageOrigin, "", "", Some(""), "")
      }
    finishPayment(sessionData, resultatPaiement)
  }

  def callbackPayment(params: Map[String, String]): PaymentResult = handleResponse(params)

  private def handleResponse(params: Map[String, String]): PaymentResult = {
    val names: Seq[String] = params.filter({ case (k, v) => k.indexOf("vads_") == 0}).keys.toList.sorted
    val values: Seq[String] = names.map(params)

    val vendorAndUuid = params("vads_order_info").split("--")
    val vendorId = vendorAndUuid(0)
    val transactionUUID = vendorAndUuid(1)
    val vendor = accountHandler.load(vendorId).orNull
    val parametresProvider: Map[String, String] =
      parse(org.json4s.StringInput(vendor.paymentConfig.map(_.cbParam).flatten.getOrElse("{}"))).extract[Map[String, String]]
    val ok: Boolean = SystempayUtilities.checkSignature(params("signature"),
      parametresProvider("systempayCertificate"), values.asJava)
    if (ok) {
      val transaction = boTransactionHandler.find(transactionUUID).orNull

      val botlog = BOTransactionLog(newUUID, "IN", params.mkString(", "), "SYSTEMPAY", transaction.uuid)
      boTransactionLogHandler.save(botlog, false)
      val vads_card_brand = try {
        CreditCardType.withName(params.getOrElse("vads_card_brand", CreditCardType.CB.toString))
      }
      catch {
        case NonFatal(e) =>
          CreditCardType.CB
      }

      var pr = PaymentResult(transaction.uuid,
        transaction.creationDate,
        transaction.amount,
        params("vads_card_number"),
        vads_card_brand,
        null, "", "", null, "", "", null,
        params("vads_result").toString,
        Option(BankErrorCodes.getErrorMessage(params("vads_result").toString)), "", "", Some(""), "")

      val month = params.get("vads_expiry_month") match {
        case None => None
        case Some(x) => Some(if (x.length == 1) "0" + x else x)
      }
      val year = params.get("vads_expiry_year")

      if (month.nonEmpty && year.nonEmpty) {
        val simpleDateFormat: SimpleDateFormat = new SimpleDateFormat("MMyyyy")
        pr = pr.copy(expirationDate = simpleDateFormat.parse(month.get + year.get))
      }

      pr = pr.copy(
        cvv = null,
        gatewayTransactionId = transaction.uuid,
        transactionDate = new Date,
        status = if (params("vads_result") == "00") PaymentStatus.COMPLETE else PaymentStatus.FAILED
      )

      val creditCard = BOCreditCard(
        number = pr.ccNumber,
        holder = None,
        expiryDate = pr.expirationDate,
        cardType = pr.cardType
      )
      boTransactionHandler.update(transaction.copy(creditCard = Some(creditCard)), false)
      transactionHandler.finishPayment(vendorId,
        transactionUUID,
        if (params("vads_result") == "00")
          TransactionStatus.PAYMENT_CONFIRMED
        else TransactionStatus.PAYMENT_REFUSED, pr, params("vads_result"))
      pr
    } else {
      throw InvalidSignatureException("Invalid signature")
    }
  }

  def threeDSCallback(sessionData: SessionData, params: Map[String, String]): Uri = {
    if (!sessionData.waitFor3DS) {
      throw InvalidContextException( """Not verified: waitFor3DS""")
    }
    if (sessionData.transactionUuid.isEmpty) {
      throw InvalidContextException( """!sessionData.transactionUUID""")
    }

    sessionData.waitFor3DS = false
    val transactionUUID: String = sessionData.transactionUuid.get
    val paymentConfig: PaymentConfig = sessionData.paymentConfig.get
    val paymentRequest: PaymentRequest = sessionData.paymentRequest.get
    val errorURL: String = sessionData.errorURL.get
    val successURL: String = sessionData.successURL.get
    val vendorId: String = sessionData.merchantId.get
    val parametresProvider = parse(org.json4s.StringInput(paymentConfig.cbParam.getOrElse("{}"))).extract[Map[String, String]]
    val shopId: String = parametresProvider("systempayShopId")
    val contractNumber: String = parametresProvider("systempayContractNumber")
    val certificat: String = parametresProvider("systempayCertificate")

    val ctxMode: String = if (Settings.Env == Environment.DEV) "TEST" else "PRODUCTION"

    try {
      val sessionAndRequestId: String = params("MD")
      val pares: String = params("PaRes")
      val newPReq = paymentRequest.copy(
        paylineMd = sessionAndRequestId,
        paylinePares = pares
      )

      val requestId: String = sessionAndRequestId.substring(sessionAndRequestId.indexOf("|") + 1)
      val signature: String = SystempayUtilities.makeSignature(certificat, Seq(
        shopId,
        contractNumber,
        ctxMode,
        requestId,
        pares
      ).asJava)

      val transaction = boTransactionHandler.find(transactionUUID).orNull
      val secured: ThreeDSecure = SystempayUtilities.create3DSProxy(
        "/wsdl/SystemPay_3DSecure.wsdl",
        new TraceHandler(transaction, "SYSTEMPAY"))

      val paresInfo: PaResInfo = secured.analyzePAResTx(shopId, contractNumber, ctxMode, requestId, pares, signature)
      val status = paresInfo.getStatus
      status match {
        case "N" | "U" =>
          val map = Map("result" -> MogopayConstant.Error, "error.code" -> MogopayConstant.InvalidPassword)
          buildURL(errorURL, map)
        case "Y" | "A" =>
          val resultatPaiement = systempayClient.submit(vendorId, transactionUUID, paymentConfig, newPReq)
          finishPayment(sessionData, resultatPaiement)
        case _ =>
          throw new InvalidContextException(s"Invalid status $status")
      }
    } catch {
      case NonFatal(e) =>
        val queryString = Map("result" -> MogopayConstant.Error, "error.code" -> MogopayConstant.UnknownError)
        buildURL(errorURL, queryString)
    }
  }

  private def buildURL(url: String, params: Map[String, String]) = url + "?" + mapToQueryString(params)

}

class SystempayClient {
  implicit val formats = new DefaultFormats {}

  def submit(vendorId: String, transactionUUID: String, paymentConfig: PaymentConfig,
             paymentRequest: PaymentRequest): PaymentResult = {
    val parametres = paymentConfig.cbParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())

    val context = if (Settings.Env == Environment.DEV) "TEST" else "PRODUCTION"
    val ctxMode = context
    transactionHandler.updateStatus(vendorId, transactionUUID, null, TransactionStatus.PAYMENT_REQUESTED, null)
    var paymentResult: PaymentResult = PaymentResult(
      transactionSequence = paymentRequest.transactionSequence,
      orderDate = paymentRequest.orderDate,
      amount = paymentRequest.amount,
      ccNumber = "",
      cardType = null,
      expirationDate = null,
      cvv = "",
      gatewayTransactionId = "",
      transactionDate = null,
      transactionCertificate = null,
      authorizationId = null,
      status = null,
      errorCodeOrigin = "",
      errorMessageOrigin = Some(""),
      data = "",
      bankErrorCode = "",
      bankErrorMessage = Some(""),
      token = ""
    )

    val currency: Int = paymentRequest.currency.numericCode
    val shopId: String = parametres("systempayShopId")
    val contractNumber: String = parametres("systempayContractNumber")
    val certificat: String = parametres("systempayCertificate")

    if (paymentConfig.paymentMethod == CBPaymentMethod.EXTERNAL) {
      val transDate: String = new SimpleDateFormat("yyyyMMddHHmmss").format(paymentRequest.orderDate)
      //      val xx: String = URLEncoder.encode(Settings.MogopayEndPoint + "systempay/done/" + paymentRequest.csrfToken, "UTF-8")

      val url: String = Settings.Systempay.Url
      val vads_action_mode: String = "INTERACTIVE"
      val vads_amount: String = "" + paymentRequest.amount
      val vads_ctx_mode: String = context
      val vads_currency: String = currency.toString
      val vads_order_info: String = vendorId + "--" + transactionUUID
      val vads_page_action: String = "PAYMENT"
      val vads_payment_config: String = "SINGLE"
      val vads_redirect_error_message: String = ""
      val vads_redirect_error_timeout: String = "0"
      val vads_redirect_success_message: String = ""
      val vads_redirect_success_timeout: String = "0"
      val vads_return_mode: String = "GET"
      val vads_site_id: String = shopId
      val vads_trans_date: String = transDate
      val vads_trans_id: String = "%06d".format(paymentRequest.transactionSequence.toLong)
      val vads_url_return: String = Settings.Mogopay.EndPoint + "systempay/done"
      val vads_version: String = Settings.Systempay.Version
      val signature: String =
        SystempayUtilities.encode(Seq(
          vads_action_mode,
          paymentRequest.amount.toString,
          context,
          currency,
          vads_order_info,
          vads_page_action,
          vads_payment_config,
          vads_redirect_error_message,
          vads_redirect_error_timeout,
          vads_redirect_success_message,
          vads_redirect_success_timeout,
          vads_return_mode,
          vads_site_id,
          vads_trans_date,
          vads_trans_id,
          vads_url_return,
          vads_version,
          certificat).mkString("+"))

      paymentResult = paymentResult.copy(
        data = s"""
      <html>
        <head>
        </head>
        <body>
          <form id="formpay" action="${url}" method="POST" >
            <input type="hidden" name="vads_action_mode" value="${vads_action_mode}" />
            <input type="hidden" name="vads_amount" value="${vads_amount}" />
            <input type="hidden" name="vads_currency" value="${vads_currency}" />
            <input type="hidden" name="vads_ctx_mode" value="${vads_ctx_mode}" />
            <input type="hidden" name="vads_order_info" value="${vads_order_info}" />
            <input type="hidden" name="vads_page_action" value="${vads_page_action}" />
            <input type="hidden" name="vads_payment_config" value="${vads_payment_config}" />
            <input type="hidden" name="vads_redirect_error_timeout" value="${vads_redirect_error_timeout}" />
            <input type="hidden" name="vads_redirect_error_message" value="${vads_redirect_error_message}" />
            <input type="hidden" name="vads_redirect_success_timeout" value="${vads_redirect_success_timeout}" />
            <input type="hidden" name="vads_redirect_success_message" value="${vads_redirect_success_message}" />
            <input type="hidden" name="vads_return_mode" value="${vads_return_mode}" />
            <input type="hidden" name="vads_site_id" value="${vads_site_id}" />
            <input type="hidden" name="vads_trans_date" value="${vads_trans_date}" />
            <input type="hidden" name="vads_trans_id" value="${vads_trans_id}" />
            <input type="hidden" name="vads_url_return" value="${vads_url_return}" />
            <input type="hidden" name="vads_version" value="${vads_version}" />
            <input type="hidden" name="signature" value="${signature}" />
          </form>
          <script>document.getElementById("formpay").submit();</script>
        </body>
      </html>
               """
      )
    } else {
      paymentResult = paymentResult.copy(
        cardType = paymentRequest.cardType,
        ccNumber = paymentRequest.ccNumber,
        expirationDate = paymentRequest.expirationDate,
        cvv = paymentRequest.cvv
      )
      val gcalendar: GregorianCalendar = new GregorianCalendar
      gcalendar.setTime(new Date)
      val xmlCalendar: XMLGregorianCalendar = DatatypeFactory.newInstance.newXMLGregorianCalendar(gcalendar)
      val expCalendar: GregorianCalendar = new GregorianCalendar
      expCalendar.setTime(paymentRequest.expirationDate)
      val xmlDateExpiration: XMLGregorianCalendar = DatatypeFactory.newInstance.newXMLGregorianCalendar(expCalendar)
      val payment: CreatePaiementInfo = new CreatePaiementInfo
      payment.setShopId(shopId)
      payment.setTransmissionDate(xmlCalendar)
      payment.setContractNumber(contractNumber)
      payment.setTransactionId("%06d".format(paymentRequest.transactionSequence.toLong))
      payment.setOrderId(transactionUUID)
      payment.setOrderInfo(vendorId + "-" + transactionUUID)
      payment.setAmount(paymentRequest.amount)
      payment.setDevise(currency)
      payment.setPresentationDate(xmlCalendar)
      payment.setCardNumber(paymentRequest.ccNumber)
      payment.setCardNetwork(paymentRequest.cardType.toString)
      payment.setCardExpirationDate(xmlDateExpiration)
      payment.setCvv(paymentRequest.cvv)
      payment.setCtxMode(ctxMode)

      val signature: String = SystempayUtilities.makeSignature(certificat, Seq(
        payment.getShopId,
        payment.getTransmissionDate,
        payment.getTransactionId,
        payment.getPaymentMethod,
        payment.getOrderId,
        payment.getOrderInfo,
        payment.getOrderInfo2,
        payment.getOrderInfo3,
        payment.getAmount,
        payment.getDevise,
        payment.getPresentationDate,
        payment.getValidationMode,
        payment.getCardNumber,
        payment.getCardNetwork,
        payment.getCardExpirationDate,
        payment.getCvv,
        payment.getContractNumber,
        null,
        payment.getSubPaymentType,
        payment.getSubReference,
        payment.getSubPaymentNumber,
        payment.getCustomerId,
        payment.getCustomerTitle,
        payment.getCustomerName,
        payment.getCustomerPhone,
        payment.getCustomerMail,
        payment.getCustomerAddress,
        payment.getCustomerZipCode,
        payment.getCustomerCity,
        payment.getCustomerCountry,
        payment.getCustomerLanguage,
        payment.getCustomerIP,
        payment.isCustomerSendEmail,
        payment.getCtxMode,
        payment.getComment).asInstanceOf[Seq[String]].asJava)

      val wsdlURL: URL = getClass.getClassLoader.getResource("wsdl/SystemPay_WSAPI.wsdl")
      val SERVICE_NAME: QName = new QName("http://v3.ws.vads.lyra.com/", "StandardWS")
      val ss: StandardWS = new StandardWS(wsdlURL, SERVICE_NAME)
      val port: Standard = ss.getStandardBeanPort
      val info: TransactionInfo = port.create(payment, signature)
      val code: Int = info.getErrorCode

      paymentResult = paymentResult.copy(
        gatewayTransactionId = info.getTransactionId,
        transactionDate = payment.getTransmissionDate.toGregorianCalendar.getTime,
        transactionCertificate = null,
        transactionSequence = paymentRequest.transactionSequence,
        amount = paymentRequest.amount,
        cardType = paymentRequest.cardType,
        ccNumber = paymentRequest.ccNumber,
        expirationDate = paymentRequest.expirationDate,
        cvv = paymentRequest.cvv,
        orderDate = new Date,
        errorCodeOrigin = code.toString,
        errorMessageOrigin = if (code == 5) Option(info.getExtendedErrorCode) else Some(SystempayClient.getExtendedMessage(code)),
        bankErrorCode = info.getAuthResult.toString,
        bankErrorMessage = Some(BankErrorCodes.getErrorMessage("%02d".format(info.getAuthResult)))
      )

      if (code == 0) {
        paymentResult = paymentResult.copy(status = PaymentStatus.COMPLETE)
        transactionHandler.finishPayment(vendorId, transactionUUID, TransactionStatus.PAYMENT_CONFIRMED, paymentResult, "" + code)
      } else {
        paymentResult = paymentResult.copy(status = PaymentStatus.FAILED)
        transactionHandler.finishPayment(vendorId, transactionUUID, TransactionStatus.PAYMENT_REFUSED, paymentResult, "" + code)
      }
    }

    paymentResult
  }

  def check3DSecure(sessionDataUuid: String, vendorId: String, transactionUUID: String, paymentConfig: PaymentConfig,
                    paymentRequest: PaymentRequest): ThreeDSResult = {
    val transaction = boTransactionHandler.find(transactionUUID).orNull
    if (transaction == null) throw new BOTransactionNotFoundException("")

    val parametres: Map[String, String] = parse(StringInput(paymentConfig.cbParam.getOrElse("{}"))).extract[Map[String, String]]
    val shopId: String = parametres("systempayShopId")
    val contractNumber: String = parametres("systempayContractNumber")
    val certificat: String = parametres("systempayCertificate")

    transactionHandler.updateStatus(vendorId, transactionUUID, null, TransactionStatus.VERIFICATION_THREEDS, null)
    var result: ThreeDSResult = ThreeDSResult(code = ResponseCode3DS.ERROR, url = null, method = null, mdName = null,
      mdValue = null, pareqName = null, pareqValue = null, termUrlName = null, termUrlValue = null)
    val context = if (Settings.Env == Environment.DEV) "TEST" else "PRODUCTION"
    val browserUserAgent = ""
    val browserUserAccept = ""
    val cardNumber = paymentRequest.ccNumber
    val purchaseAmount = paymentRequest.amount.toString
    val currency = paymentRequest.currency.numericCode.toString
    val cardExpiration = new SimpleDateFormat("MMyy").format(paymentRequest.expirationDate)
    val wssignature = SystempayUtilities.makeSignature(certificat, Seq(
      shopId,
      contractNumber,
      context,
      cardNumber,
      browserUserAgent,
      browserUserAccept,
      purchaseAmount,
      currency,
      cardExpiration).asJava)

    val traceHandler = new TraceHandler(transaction, "SYSTEMPAY")
    val secured: ThreeDSecure = SystempayUtilities.create3DSProxy("/wsdl/SystemPay_3DSecure.wsdl", traceHandler)
    val vresInfo: VeResPAReqInfo = secured.sendVEReqAndbuildPAReqTx(shopId, contractNumber, context,
      cardNumber, browserUserAgent, browserUserAccept, purchaseAmount, currency, cardExpiration, wssignature)
    val ctx: util.Map[String, AnyRef] = secured.asInstanceOf[BindingProvider].getResponseContext
    val responseHeaders: mutable.Map[String, List[String]] = ctx.get(MessageContext.HTTP_RESPONSE_HEADERS)
      .asInstanceOf[java.util.Map[String, List[String]]].asScala
    for (headerName <- responseHeaders.keySet) {
      val headerValues: List[String] = responseHeaders(headerName).asInstanceOf[java.util.List[String]].asScala.toList
      for (headerValue <- headerValues) {
      }
    }
    var sessionId: String = ""
    val headerValues: List[String] = responseHeaders("Set-Cookie").asInstanceOf[java.util.List[String]].asScala.toList
    for (headerValue <- headerValues) {
      if (headerValue.startsWith("JSESSIONID")) {
        sessionId = headerValue.substring("JSESSIONID=".length, headerValue.indexOf(";"))
      }
    }
    val requestId = vresInfo.getRequestId
    val enrolled = vresInfo.getEnrolled
    val acsURL = vresInfo.getAcsUrl
    val codeRetour = vresInfo.getErrorCode
    if ("0" == codeRetour) {
      if ("Y" == enrolled) {
        val encodedPareq = vresInfo.getEncodedPareq
        result = result.copy(
          code = ResponseCode3DS.APPROVED,
          mdName = "MD",
          mdValue = sessionId + "|" + requestId,
          pareqName = "PaReq",
          pareqValue = encodedPareq,
          termUrlName = "TermUrl",
          termUrlValue = s"${Settings.Mogopay.EndPoint}systempay/3ds-callback/${sessionDataUuid}",
          url = acsURL,
          method = "POST"
        )

        val form = s"""
        <html>
          <head>
          </head>
          <body>
            Redirection vers la banque en cours...
            <form id="formpay" action="${result.url}" method="${result.method}" >
            <input type="hidden" name="${result.pareqName}" value="${result.pareqValue}" />
            <input type="hidden" name="${result.termUrlName}" value="${result.termUrlValue}" />
            <input type="hidden" name="${result.mdName}" value="${result.mdValue}" />
            </form>
            <script>document.getElementById("formpay").submit();</script>
          </body>
        </html>"""
        println(form)

        if (Settings.Env == Environment.DEV)
          result = result.copy(url = result.url + ";jsessionid=" + sessionId)
      }
    } else {
      result = result.copy(code = ResponseCode3DS.INVALID)
    }
    transactionHandler.updateStatus3DS(vendorId, transactionUUID, result.code, codeRetour)

    result
  }
}

object SystempayClient {
  def getExtendedMessage(code :Int): String =extendedErrorCodes.getOrElse(code, "")

  val extendedErrorCodes = Map[Int, String](
    0 -> "Action réalisée avec succès",
    1 -> "Action non autorisée",
    2 -> "Transaction non trouvée",
    3 -> "Transaction pas dans le bon état ￼",
    4 -> "Transaction existe déjà",
    5 -> "Mauvaise signature",
    6 -> "Mauvaise date",
    10 -> "Mauvais montant",
    11 -> "Mauvaise devise",
    12 -> "Type de carte inconnu",
    13 -> "Paramètre 'date d'expiration' invalide",
    14 -> "Paramètre ‘cvv’ invalide ",
    15 -> "Contrat inconnu",
    16 -> "Paramètre ‘Numéro de carte’ invalide",
    17 -> "Identifiant non trouvé",
    18 -> "Identifiant non valide (Résilié, ...)",
    19 -> "Souscription non trouvée",
    20 -> "Souscription non valide",
    21 -> "Identifiant déjà existant",
    22 -> "Création d'identifiatn refusé",
    23 -> "Identifiant purgé",
    26 -> "Pas de changement",
    40 -> "Plage non trouvée",
    50 -> "Paramètre 'shopId' invalide",
    51 -> "Paramètre 'transmissionDate' invalide",
    52 -> "Paramètre 'transactionId' invalide",
    53 -> "Paramètre 'ctxMode' invalide",
    54 -> "Paramètre 'comment' invalide",
    55 -> "Paramètre 'AutoNb' invalide",
    56 -> "Paramètre 'AutoDate' invalide",
    57 -> "Paramètre 'presentationDate' invalide",
    58 -> "Paramètre 'newTransactionId' invalide",
    59 -> "Paramètre 'validationMode' invalide",
    60 -> "Paramètre 'orderId' invalide",
    61 -> "Paramètre 'orderInfo1' invalide",
    62 -> "Paramètre 'orderInfo2' invalide",
    63 -> "Paramètre 'orderInfo3' invalide",
    64 -> "Paramètre 'PaymentMethod' invalide",
    65 -> "Paramètre 'CardNumber' invalide",
    66 -> "Paramètre 'ContractNumber' invalide",
    67 -> "Paramètre 'CustomerId' invalide",
    68 -> "Paramètre 'customerTitle' invalide",
    69 -> "Paramètre 'customerName' invalide",
    70 -> "Paramètre 'customerPhone' invalide",
    71 -> "Paramètre 'customerMail' invalide",
    72 -> "Paramètre 'customerAddress' invalide",
    73 -> "Paramètre 'customerZipCode' invalide",
    74 -> "Paramètre 'customerCity' invalide",
    75 -> "Paramètre 'customerCountry' invalide",
    76 -> "Paramètre 'customerLanguage' invalide",
    77 -> "Paramètre 'customerIp' invalide",
    78 -> "Paramètre 'customerSendMail' invalide",
    79 -> "Paramètre 'customerMobilePhone' invalide",
    80 -> "Paramètre 'subPaiementType' invalide",
    81 -> "Paramètre 'subReference' invalide",
    82 -> "Paramètre 'initialAmount' invalide",
    83 -> "Paramètre 'occInitialAmount' invalide",
    84 -> "Paramètre 'effectDate' invalide",
    85 -> "Paramètre 'state' invalide",
    90 -> "Paramètre 'enrolled' invalide",
    91 -> "Paramètre 'authStatus' invalide",
    92 -> "Paramètre 'eci' invalide",
    93 -> "Paramètre 'xid' invalide",
    94 -> "Paramètre 'cavv' invalide",
    95 -> "Paramètre 'cavvAlgo' invalide",
    96 -> "Paramètre 'brand' invalide",
    98 -> "Paramètre 'requestId' invalide",
    99 -> "Autre erreur"
  )
}