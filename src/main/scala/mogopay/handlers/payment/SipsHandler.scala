package com.ebiznext.mogopay.payment


import java.io.{StringWriter, File, IOException}
import java.net.URLEncoder
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Enumeration
import mogopay.codes.MogopayConstant
import mogopay.config.HandlersConfig._
import com.atosorigin.services.cad.apipayment.SIPSCallParm
import com.atosorigin.services.cad.apipayment.web.SIPSApiWeb
import com.atosorigin.services.cad.apiserver.components.service.office.SIPSOfficeApi
import com.atosorigin.services.cad.apiserver.components.service.office.SIPSOfficeRequestParm
import com.atosorigin.services.cad.apiserver.components.service.office.SIPSOfficeResponseParm
import com.atosorigin.services.cad.apiserver.components.service.checkout.{SIPSCheckoutResponseParm, SIPSCheckoutRequestParm, SIPSCheckoutApi}
import com.atosorigin.services.cad.apiserver.components.service.office.SIPSParm
import com.atosorigin.services.cad.common.SIPSDataObject
import mogopay.config.Settings
import mogopay.es.EsClient
import mogopay.exceptions.Exceptions.MogopayError
import mogopay.handlers.payment.{BankErrorCodes, ThreeDSResult, PaymentHandler}
import mogopay.model.Mogopay.ResponseCode3DS
import mogopay.model.Mogopay.ResponseCode3DS._
import mogopay.model.Mogopay._
import mogopay.util.{GlobalUtil, CustomSslConfiguration}
import mogopay.util.GlobalUtil._
import org.json4s.jackson.JsonMethods._
import spray.http.Uri

import scala.collection.immutable
import scala.util._

/*
 <service component="checkout" name="card3D_Order"> <card3D_Order origin="Batch" merchant_id="011223344553334" merchant_country="fr"
 transaction_id="130685" amount="12300" return_context="context" order_id="OI_131100_8744" capture_mode="VALIDATION" capture_day="2"
 data=""
 order_validity=""
 o3d_session_id=” 71B78471DAC1B849421690AF2C418931.sips_3doffice-1” /></service>
 Pour connaître la signification de ces différents champs référez-vous au chapitre 8. Ci-dessous est présentée la réponse XML de la demande d’autorisation 3D précédente.
 Ce document est propriété de Atos Worldline. Il ne peut être reproduit ou divulgué sans autorisation écrite préalable. 24 Réf : GuideComposantCheckout103.doc Date : 12/05/2009
 ￼
 ￼
 Guide du composant Checkout
 ￼
 Page : 25
 ￼<response component="checkout" name="card3D_Order"> <card3D_Order response_code="00" o3d_response_code=”00”
 transaction_time="114147"
 transaction_date="20030801" transaction_certificate="1059730907" authorisation_id="1059" status="TO_VALIDATE" currency_code="978"
 data="" avs_response_code=”” cvv_response_code="4D" bank_response_code="00" complementary_code="" complementary_info="" /> </response>
 */


/**
 * @see com.ebiznext.mogopay.payment.ISipsPaymentService
 */
class SipsHandler extends PaymentHandler {
  implicit val formats = new org.json4s.DefaultFormats {
  }

  import SipsHandler._


  def startPayment(sessionData: SessionData): Try[Either[String, Uri]] = {
    val transactionUUID = sessionData.transactionUuid.get
    val paymentConfig: PaymentConfig = sessionData.paymentConfig.orNull
    val vendorUuid = sessionData.vendorId.get
    val paymentRequest = sessionData.paymentRequest.get

    if (paymentConfig == null || paymentConfig.cbProvider != CBPaymentProvider.SIPS) {
      Failure(new MogopayError(MogopayConstant.InvalidSystemPayConfig))
    }
    else {
      transactionHandler.startPayment(vendorUuid, transactionUUID, paymentRequest, PaymentType.CREDIT_CARD, CBPaymentProvider.SIPS)
      if (paymentConfig.paymentMethod == CBPaymentMethod.EXTERNAL) {
        val resultat = submit(vendorUuid, transactionUUID, paymentConfig, paymentRequest)
        if (resultat.data != null)
          Success(Left(resultat.data))
        else
          Success(Right(finishPayment(sessionData, resultat)))
      }
      else if (paymentConfig.paymentMethod == CBPaymentMethod.THREEDS_IF_AVAILABLE || paymentConfig.paymentMethod == CBPaymentMethod.THREEDS_REQUIRED) {
        val resultat3DS = check3DSecure(sessionData, vendorUuid, transactionUUID, paymentConfig, paymentRequest)
        if (resultat3DS != null && resultat3DS.code == ResponseCode3DS.APPROVED) {
          // 3DS approuve, redirection vers sips
          // resultat3DS.termUrlValue = createLink(action: "threedsCallback", params:[xtoken:sessionData.csrfToken], absolute: true).toString();
          sessionData.waitFor3DS = true
          sessionData.o3dSessionId = Some(resultat3DS.mdValue)
          val data =
            s"""
              |<html>
              |    <head>
              |    </head>
              |    <body>
              |    	<form id="formsips" action="${resultat3DS.url}" method="${resultat3DS.method}" >
              |    	</form>
              |    	<script>document.getElementById("formsips").submit();</script>
              |    </body>
              |</html>
              |            """.stripMargin
          Success(Left(data))
        }
        else if (paymentConfig.paymentMethod == CBPaymentMethod.THREEDS_IF_AVAILABLE) {
          // on lance un paiement classique
          val resultat = submit(vendorUuid, transactionUUID, paymentConfig, paymentRequest)
          Success(Right(finishPayment(sessionData, resultat)))
        }
        else {
          // La carte n'est pas 3Ds alors que c'est obligatoire
          Success(Right(finishPayment(sessionData, GlobalUtil.createThreeDSNotEnrolledResult())))
        }
      }
      else {
        val resultat = submit(vendorUuid, transactionUUID, paymentConfig, paymentRequest)
        Success(Right(finishPayment(sessionData, resultat)))
      }
    }
  }

  def callbackPayment(params: Map[String, String], vendorUuid: Document): Try[Uri] = {
    handleResponse(vendorUuid, params("DATA"), params("response_code"))
    Success(Uri(Settings.MogopayEndPoint))
  }

  def threeDSCallback(sessionData: SessionData, params: Map[String, String]): Try[Uri] = {
    if (!sessionData.waitFor3DS) {
      // invalid call
      Failure(throw new Exception("Invalid payment hain"))
    }
    else {
      val errorURL = sessionData.errorURL.getOrElse("")
      val transactionUUID = sessionData.transactionUuid.get
      val successURL = sessionData.successURL.getOrElse("")
      val vendorId = sessionData.vendorId.get
      val paymentRequest = sessionData.paymentRequest.orNull
      val paymentConfig = sessionData.paymentConfig
      val parametresProvider: Map[String, String] =
        parse(org.json4s.StringInput(paymentConfig.map(_.cbParam).flatten.getOrElse("{}"))).extract[Map[String, String]]
      sessionData.waitFor3DS = false
      try {
        val result = order3D(sessionData, vendorId, transactionUUID, paymentConfig.orNull, paymentRequest);
        Success(finishPayment(sessionData, result))
      }
      catch {
        case ex: Exception =>
          ex.printStackTrace()
          Success(Uri(errorURL))
      }
    }
  }

  def done(sessionData: SessionData, params: Map[String, String]): Try[Uri] = {
    val transactionUUID = sessionData.transactionUuid.get
    val vendorId = sessionData.vendorId.get
    val paymentRequest: PaymentRequest = sessionData.paymentRequest.get

    val transaction: BOTransaction = EsClient.load[BOTransaction](transactionUUID).orNull
    val amount = sessionData.amount
    val errorURL = sessionData.errorURL
    val successURL = sessionData.successURL
    var resultatPaiement: PaymentResult =
      if (transaction.status != TransactionStatus.PAYMENT_CONFIRMED && transaction.status != TransactionStatus.PAYMENT_REFUSED) {
        try {
          handleResponse(vendorId, params("DATA"), params("response_code"))
        }
        catch {
          case e: Exception =>
            e.printStackTrace()
            throw new Exception("Invalid call")
        }
      }
      else {
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
    Success(finishPayment(sessionData, resultatPaiement))

  }

  private def handleResponse(vendorUuid: Document, cypheredtxt: String, responseCode: String): PaymentResult = {
    val dir: File = new File(Settings.Sips.CertifDir, vendorUuid)
    val targetFile: File = new File(dir, "pathfile")
    val api = new SIPSApiWeb(targetFile.getAbsolutePath)
    val resp = api.sipsPaymentResponseFunc(cypheredtxt)
    val out = new StringWriter()
    out.append("merchant_id = " + resp.getValue("merchant_id"))
    out.append("&merchant_country = " + resp.getValue("merchant_country"))
    out.append("&amount = " + resp.getValue("amount"))
    out.append("&transaction_id = " + resp.getValue("transaction_id"))
    out.append("&transmission_date = " + resp.getValue("transmission_date"))
    out.append("&payment_means = " + resp.getValue("payment_means"))
    out.append("&payment_time = " + resp.getValue("payment_time"))
    out.append("&payment_date = " + resp.getValue("payment_date"))
    out.append("&response_code = " + resp.getValue("response_code"))
    out.append("&payment_certificate = " + resp.getValue("payment_certificate"))
    out.append("&authorisation_id = " + resp.getValue("authorisation_id"))
    out.append("&currency_code = " + resp.getValue("currency_code"))
    out.append("&card_number = " + resp.getValue("card_number"))
    out.append("&cvv_flag = " + resp.getValue("cvv_flag"))
    out.append("&cvv_response_code = " + resp.getValue("cvv_response_code"))
    out.append("&bank_response_code = " + resp.getValue("bank_response_code"))
    out.append("&complementary_code = " + resp.getValue("complementary_code"))
    out.append("&complementary_info = " + resp.getValue("complementary_info"))
    out.append("&return_context = " + resp.getValue("return_context"))
    out.append("&caddie = " + resp.getValue("caddie"))
    out.append("&receipt_complement = " + resp.getValue("receipt_complement"))
    out.append("&merchant_language = " + resp.getValue("merchant_language"))
    out.append("&language = " + resp.getValue("language"))
    out.append("&customer_id = " + resp.getValue("customer_id"))
    out.append("&order_id = " + resp.getValue("order_id"))
    out.append("&customer_email = " + resp.getValue("customer_email"))
    out.append("&customer_ip_address = " + resp.getValue("customer_ip_address"))
    out.append("&capture_day = " + resp.getValue("capture_day"))
    out.append("&capture_mode = " + resp.getValue("capture_mode"))
    out.append("&data = " + resp.getValue("data"))
    out.append("&order_validity = " + resp.getValue("order_validity"))
    out.append("&transaction_condition = " + resp.getValue("transaction_condition"))
    out.append("&statement_reference = " + resp.getValue("statement_reference"))
    out.append("&card_validity = " + resp.getValue("card_validity"))
    out.append("&score_color = " + resp.getValue("score_color"))
    out.append("&score_info = " + resp.getValue("score_info"))
    out.append("&score_value = " + resp.getValue("score_value"))
    out.append("&score_threshold = " + resp.getValue("score_threshold"))
    out.append("&score_profile = " + resp.getValue("score_profile"))
    out.append("&threed_ls_code = " + resp.getValue("threed_ls_code"))
    out.append("&threed_relegation_code = " + resp.getValue("threed_relegation_code"))
    val orderId = resp.getValue("order_id");
    //
    val transactionUuid = orderId.substring(0, 8) + "-" + orderId.substring(8, 12) + "-" + orderId.substring(12, 16) + "-" + orderId.substring(16, 20) + "-" + orderId.substring(20, 32)
    val vendor = EsClient.load[Account](vendorUuid).orNull
    val parametresProvider: Map[String, String] =
      parse(org.json4s.StringInput(vendor.paymentConfig.map(_.cbParam).flatten.getOrElse("{}"))).extract[Map[String, String]]
    val transaction = EsClient.load[BOTransaction](transactionUuid).orNull
    val botlog = BOTransactionLog(newUUID, "IN", out.toString, "SIPS", transaction.uuid)
    EsClient.index(botlog, false)

    val num = resp.getValue("card_number")
    val ccNum =
      if (num.indexOf(".") > 0) {
        num.substring(0, num.indexOf(".")) + "XXXXXXXX" + num.substring(num.indexOf(".") + 1)
      }
      else {
        "UNKNOWN"
      }

    val simpleDateFormat = new SimpleDateFormat("yyyyMM");
    val expirationDate =
      try {
        simpleDateFormat.parse(resp.getValue("card_validity"));
      }
      catch {
        // The customer did not give his card number
        case e: Exception =>
          null //new Date(1970,0,1)
      }

    val paymentResult = new PaymentResult(transactionSequence = "",
      orderDate = transaction.transactionDate.getOrElse(new Date),
      amount = transaction.amount,
      ccNumber = ccNum,
      cardType = CreditCardType.CB,
      expirationDate = expirationDate,
      cvv = null,
      gatewayTransactionId = transaction.transactionUUID,
      transactionDate = new Date(),
      transactionCertificate = null,
      authorizationId = null,
      status = if (resp.getValue("response_code") == "00") PaymentStatus.COMPLETE else PaymentStatus.FAILED,
      errorCodeOrigin = resp.getValue("response_code"),
      errorMessageOrigin = Option(resp.getValue("response_code")),
      data = null,
      bankErrorCode = resp.getValue("bank_response_code"),
      bankErrorMessage = Option(BankErrorCodes.getErrorMessage(resp.getValue("bank_response_code"))),
      token = null)

    transactionHandler.finishPayment(vendorUuid, transactionUuid, if (paymentResult.errorCodeOrigin == "00") TransactionStatus.PAYMENT_CONFIRMED else TransactionStatus.PAYMENT_REFUSED, paymentResult, responseCode)
    paymentResult
  }


  private[payment] def check3DSecure(sessionData: SessionData, vendorUuid: Document, transactionUuid: Document, paymentConfig: PaymentConfig, paymentRequest: PaymentRequest): ThreeDSResult = {
    transactionHandler.updateStatus(vendorUuid, transactionUuid, null, TransactionStatus.VERIFICATION_THREEDS, null)
    val vendor = EsClient.load[Account](vendorUuid).get

    val parametres = paymentConfig.cbParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())
    val formatDateAtos: SimpleDateFormat = new SimpleDateFormat("yyyyMM")
    val dir: File = new File(Settings.Sips.CertifDir, vendorUuid)
    val targetFile: File = new File(dir, "pathfile")
    val merchantCountry: String = parametres("sipsMerchantCountry")
    val merchantId: String = parametres("sipsMerchantId")
    val api: SIPSCheckoutApi = new SIPSCheckoutApi(targetFile.getAbsolutePath)
    val sipsRequest: SIPSDataObject = new SIPSCheckoutRequestParm(0)
    val sipsResponse: SIPSDataObject = new SIPSCheckoutResponseParm(0)
    sipsRequest.setValue("merchant_id", merchantId)
    sipsRequest.setValue("merchant_country", merchantCountry)
    sipsRequest.setValue("transaction_id", "" + paymentRequest.transactionSequence)
    sipsRequest.setValue("card_number", paymentRequest.ccNumber + "00")
    sipsRequest.setValue("cvv_flag", "1")
    sipsRequest.setValue("cvv_key", "" + paymentRequest.cvv)
    sipsRequest.setValue("card_validity", formatDateAtos.format(paymentRequest.expirationDate))
    sipsRequest.setValue("card_type", paymentRequest.cardType.toString)
    sipsRequest.setValue("amount", "" + paymentRequest.amount)
    sipsRequest.setValue("currency_code", "" + paymentRequest.currency.numericCode)
    sipsRequest.setValue("merchant_name", vendor.company.getOrElse(""))
    sipsRequest.setValue("merchant_url", vendor.website.getOrElse(""))
    var resultat: ThreeDSResult = new ThreeDSResult(
      code = null,
      url = null,
      method = "GET",
      mdName = null,
      mdValue = null,
      pareqName = null,
      pareqValue = null,
      termUrlName = null,
      termUrlValue = s"${
        Settings.MogopayEndPoint
      }sips/3ds-callback/${
        sessionData.uuid
      }"
    )
    sipsRequest.setValue("merchant_url_return", resultat.termUrlValue)
    val botlog = new BOTransactionLog(uuid = newUUID, provider = "SIPS", direction = "OUT", transaction = transactionUuid, log = serialize(sipsRequest))
    EsClient.index(botlog, false)
    api.checkEnroll3d(sipsRequest, sipsResponse)
    val botlogIn = new BOTransactionLog(uuid = newUUID, provider = "SIPS", direction = "IN", transaction = transactionUuid, log = serialize(sipsResponse))
    EsClient.index(botlog, false)

    val responseCode: String = sipsResponse.getValue(SIPSCheckoutResponseParm.O3D_RESPONSE_CODE)
    val reponseUrlAcs: String = sipsResponse.getValue(SIPSCheckoutResponseParm.O3D_OFFICE_URL_ACS)
    val reponseSessionId: String = sipsResponse.getValue(SIPSCheckoutResponseParm.O3D_SESSION_ID)
    resultat = resultat.copy(code = if ("00" == responseCode) ResponseCode3DS.APPROVED else ResponseCode3DS.REFUSED, mdValue = reponseSessionId, url = reponseUrlAcs)

    transactionHandler.updateStatus3DS(vendorUuid, transactionUuid, resultat.code, responseCode)
    resultat
  }

  private[payment] def order3D(sessionData: SessionData, vendorUuid: Document, transactionUuid: Document, paymentConfig: PaymentConfig, paymentRequest: PaymentRequest): PaymentResult = {
    transactionHandler.updateStatus(vendorUuid, transactionUuid, null, TransactionStatus.VERIFICATION_THREEDS, null)
    val vendor = EsClient.load[Account](vendorUuid).get
    val transaction = EsClient.load[BOTransaction](transactionUuid).get
    val parametres = paymentConfig.cbParam.map(parse(_).extract[immutable.Map[String, String]]).getOrElse(Map())
    val formatDateAtos: SimpleDateFormat = new SimpleDateFormat("yyyyMM")

    val dir: File = new File(Settings.Sips.CertifDir, vendorUuid)
    val targetFile: File = new File(dir, "pathfile")
    val merchantCountry: String = parametres("sipsMerchantCountry")
    val merchantId: String = parametres("sipsMerchantId")
    val api: SIPSCheckoutApi = new SIPSCheckoutApi(targetFile.getAbsolutePath)
    val sipsRequest: SIPSDataObject = new SIPSCheckoutRequestParm(0)
    val sipsResponse: SIPSDataObject = new SIPSCheckoutResponseParm(0)
    sipsRequest.setValue("amount", "" + paymentRequest.amount)
    sipsRequest.setValue("merchant_country", merchantCountry)
    sipsRequest.setValue("merchant_id", merchantId)
    sipsRequest.setValue("transaction_id", "" + paymentRequest.transactionSequence)
    sipsRequest.setValue("o3d_session_id", sessionData.o3dSessionId.get)

    val botlog = new BOTransactionLog(uuid = newUUID, provider = "SIPS", direction = "OUT", transaction = transactionUuid, log = serialize(sipsRequest))
    EsClient.index(botlog, false)
    api.authent3D(sipsRequest, sipsResponse)
    val botlogIn = new BOTransactionLog(uuid = newUUID, provider = "SIPS", direction = "IN", transaction = transactionUuid, log = serialize(sipsResponse))
    EsClient.index(botlog, false)

    val responseCode: String = sipsResponse.getValue(SIPSCheckoutResponseParm.O3D_RESPONSE_CODE)
    var resultat: ThreeDSResult = new ThreeDSResult(
      code = if ("00" == responseCode) ResponseCode3DS.APPROVED else ResponseCode3DS.REFUSED,
      url = null,
      method = "GET",
      mdName = null,
      mdValue = null,
      pareqName = null,
      pareqValue = null,
      termUrlName = null,
      termUrlValue = s"${
        Settings.MogopayEndPoint
      }sips/3ds-callback/${
        sessionData.uuid
      }"
    )

    val diag_response_code: String = sipsResponse.getValue(SIPSCheckoutResponseParm.O3D_RESPONSE_CODE)
    val diag_time: String = sipsResponse.getValue(SIPSCheckoutResponseParm.TRANSACTION_TIME)
    val diag_date: String = sipsResponse.getValue(SIPSCheckoutResponseParm.TRANSACTION_DATE)
    val diag_certificate: String = sipsResponse.getValue(SIPSCheckoutResponseParm.TRANSACTION_CERTIFICATE)
    val merchant_id: String = sipsResponse.getValue(SIPSCheckoutResponseParm.MERCHANT_ID)
    val merchant_country: String = sipsResponse.getValue(SIPSCheckoutResponseParm.MERCHANT_COUNTRY)
    val transaction_id: String = sipsResponse.getValue(SIPSCheckoutResponseParm.TRANSACTION_ID)
    val avs_response_code: String = sipsResponse.getValue(SIPSCheckoutResponseParm.AVS_RESPONSE_CODE)
    val new_amount: String = sipsResponse.getValue(SIPSCheckoutResponseParm.AMOUNT)
    val currency_code: String = sipsResponse.getValue(SIPSCheckoutResponseParm.CURRENCY_CODE)
    val authorisation_id: String = sipsResponse.getValue(SIPSCheckoutResponseParm.AUTHORISATION_ID)
    val bank_response_code: String = sipsResponse.getValue(SIPSCheckoutResponseParm.BANK_RESPONSE_CODE)
    val dt: Calendar = makeCalendar(diag_date, diag_time);
    val paymentResult: PaymentResult = PaymentResult(
      transactionSequence = paymentRequest.transactionSequence,
      orderDate = if (diag_response_code == "00" && dt != null) dt.getTime else paymentRequest.orderDate,
      amount = paymentRequest.amount,
      cardType = paymentRequest.cardType,
      ccNumber = paymentRequest.ccNumber,
      expirationDate = paymentRequest.expirationDate,
      transactionDate = dt.getTime,
      cvv = paymentRequest.cvv,
      gatewayTransactionId = transaction_id,
      errorCodeOrigin = diag_response_code,
      errorMessageOrigin = Option(o3dCodes.getOrElse(diag_response_code, "")),
      bankErrorCode = bank_response_code,
      bankErrorMessage = Option(BankErrorCodes.getErrorMessage(bank_response_code)),
      transactionCertificate = diag_certificate,
      authorizationId = authorisation_id,
      status = if (diag_response_code == "00") PaymentStatus.COMPLETE else PaymentStatus.FAILED,
      token = null,
      data = null
    )
    transactionHandler.finishPayment(vendorUuid, transactionUuid, GlobalUtil.computeTransactionStatus(paymentResult.status), paymentResult, paymentResult.errorCodeOrigin)
    paymentResult
  }

  private[payment] def submit(vendorUuid: Document, transactionUuid: Document, paymentConfig: PaymentConfig, paymentRequest: PaymentRequest): PaymentResult = {
    val vendor = EsClient.load[Account](vendorUuid).get
    val transaction = EsClient.load[BOTransaction](transactionUuid).get
    val parametres = paymentConfig.cbParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())
    transactionHandler.updateStatus(vendorUuid, transactionUuid, null, TransactionStatus.PAYMENT_REQUESTED, null)
    val merchantCountry: String = parametres("sipsMerchantCountry")
    val merchantId: String = parametres("sipsMerchantId")
    val formatDateAtos: SimpleDateFormat = new SimpleDateFormat("yyyyMM")

    if (paymentConfig.paymentMethod eq CBPaymentMethod.EXTERNAL) {
      val dir: File = new File(Settings.Sips.CertifDir, vendorUuid)
      val targetFile: File = new File(dir, "pathfile")
      val merchantCountry: String = parametres("sipsMerchantCountry")
      val merchantId: String = parametres("sipsMerchantId")
      val api: SIPSApiWeb = new SIPSApiWeb(targetFile.getAbsolutePath)
      val sipsRequest = new SIPSCallParm
      sipsRequest.setValue("merchant_country", merchantCountry)
      sipsRequest.setValue("merchant_id", merchantId)
      sipsRequest.setValue("transaction_id", "" + paymentRequest.transactionSequence)
      sipsRequest.setValue("currency_code", "" + paymentRequest.currency.numericCode)
      sipsRequest.setValue("data", "NO_RESPONSE_PAGE")
      sipsRequest.setValue("amount", "" + paymentRequest.amount)
      sipsRequest.setValue("order_id", transactionUuid.split("-").mkString(""))
      sipsRequest.setValue("normal_return_url", Settings.MogopayEndPoint + "sips/done")
      sipsRequest.setValue("cancel_return_url", Settings.MogopayEndPoint + "sips/done")
      sipsRequest.setValue("automatic_response_url", Settings.MogopayEndPoint + "sips/callback/" + vendorUuid)
      val htmlToDsiplay = """\
			<HTML><HEAD><TITLE>SIPS - Paiement Securise sur Internet</TITLE></HEAD>
			<BODY bgcolor=#ffffff>
			<Font color=#000000>
			<center><H1>Paiement SIPS</H1></center><br><br>
                          			""" + api.sipsPaymentCallFunc(sipsRequest) +
        """			</BODY>
</HTML>
        """
      PaymentResult(
        transactionSequence = paymentRequest.transactionSequence,
        orderDate = new Date,
        amount = paymentRequest.amount,
        cardType = paymentRequest.cardType,
        ccNumber = paymentRequest.ccNumber,
        expirationDate = paymentRequest.expirationDate,
        transactionDate = null,
        cvv = paymentRequest.cvv,
        gatewayTransactionId = null,
        errorCodeOrigin = null,
        errorMessageOrigin = null,
        bankErrorCode = null,
        bankErrorMessage = None,
        transactionCertificate = null,
        authorizationId = null,
        status = null,
        token = null,
        data = htmlToDsiplay
      )
    }
    else {
      val dir: File = new File(Settings.Sips.CertifDir, vendorUuid)
      val targetFile: File = new File(dir, "pathfile")
      val merchantCountry: String = parametres("sipsMerchantCountry")
      val merchantId: String = parametres("sipsMerchantId")
      val api = new SIPSOfficeApi(targetFile.getAbsolutePath());
      val sipsRequest: SIPSDataObject = new SIPSOfficeRequestParm
      val sipsResponse: SIPSDataObject = new SIPSOfficeResponseParm
      val formatDateAtos: SimpleDateFormat = new SimpleDateFormat("yyyyMM")
      sipsRequest.setValue("merchant_country", merchantCountry)
      sipsRequest.setValue("merchant_id", merchantId)
      sipsRequest.setValue("transaction_id", "" + paymentRequest.transactionSequence)
      sipsRequest.setValue("currency_code", "" + paymentRequest.currency.numericCode)
      sipsRequest.setValue("amount", "" + paymentRequest.amount)
      sipsRequest.setValue("card_number", paymentRequest.ccNumber + "00")
      sipsRequest.setValue("cvv_flag", "1")
      sipsRequest.setValue("cvv_key", "" + paymentRequest.cvv)
      sipsRequest.setValue("card_validity", formatDateAtos.format(paymentRequest.expirationDate))
      sipsRequest.setValue("card_type", paymentRequest.cardType.toString)
      sipsRequest.setValue("return_context", "context")
      sipsRequest.setValue("order_id", transactionUuid.split("-").mkString(""))
      sipsRequest.setValue("capture_mode", "AUTHOR_CAPTURE")
      sipsRequest.setValue("capture_day", "0")
      sipsRequest.setValue("data", "")
      sipsRequest.setValue("order_validity", "")
      sipsRequest.setValue("security_indicator", "09")
      sipsRequest.setValue("alternate_certificate", "")
      val botlog = new BOTransactionLog(uuid = newUUID, provider = "SIPS", direction = "OUT", transaction = transactionUuid, log = serialize(sipsRequest))
      EsClient.index(botlog, false)
      api.asoAuthorTransaction(sipsRequest, sipsResponse)
      val botlogIn = new BOTransactionLog(uuid = newUUID, provider = "SIPS", direction = "IN", transaction = transactionUuid, log = serialize(sipsResponse))
      EsClient.index(botlog, false)


      val diag_response_code: String = sipsResponse.getValue(SIPSOfficeResponseParm.TRANSACTION_RESPCODE)
      val diag_time: String = sipsResponse.getValue(SIPSOfficeResponseParm.TRANSACTION_TIME)
      val diag_date: String = sipsResponse.getValue(SIPSOfficeResponseParm.TRANSACTION_DATE)
      val diag_certificate: String = sipsResponse.getValue(SIPSOfficeResponseParm.TRANSACTION_CERTIFICATE)
      val merchant_id: String = sipsResponse.getValue(SIPSOfficeResponseParm.MERCHANT_ID)
      val merchant_country: String = sipsResponse.getValue(SIPSOfficeResponseParm.MERCHANT_COUNTRY)
      val transaction_id: String = sipsResponse.getValue(SIPSOfficeResponseParm.TRANSACTION_ID)
      val avs_response_code: String = sipsResponse.getValue(SIPSOfficeResponseParm.AVS_RESPONSE_CODE)
      val new_amount: String = sipsResponse.getValue(SIPSOfficeResponseParm.AMOUNT)
      val currency_code: String = sipsResponse.getValue(SIPSOfficeResponseParm.CURRENCY_CODE)
      val transaction_status: String = sipsResponse.getValue(SIPSOfficeResponseParm.TRANSACTION_STATUS)
      val authorisation_id: String = sipsResponse.getValue(SIPSOfficeResponseParm.AUTHORISATION_ID)
      val bank_response_code: String = sipsResponse.getValue(SIPSOfficeResponseParm.BANK_RESPONSE_CODE)
      val codeErreur = diag_response_code

      val dt: Calendar = makeCalendar(diag_date, diag_time)

      val paymentResult = PaymentResult(
        transactionSequence = paymentRequest.transactionSequence,
        orderDate = if (dt != null) dt.getTime else new Date,
        amount = paymentRequest.amount,
        cardType = paymentRequest.cardType,
        ccNumber = paymentRequest.ccNumber,
        expirationDate = paymentRequest.expirationDate,
        transactionDate = null,
        cvv = paymentRequest.cvv,
        gatewayTransactionId = transaction_id,
        errorCodeOrigin = codeErreur,
        errorMessageOrigin = Some(""),
        bankErrorCode = bank_response_code,
        bankErrorMessage = Option(BankErrorCodes.getErrorMessage(bank_response_code)),
        transactionCertificate = diag_certificate,
        authorizationId = authorisation_id,
        status = if (bank_response_code == "00") PaymentStatus.COMPLETE else PaymentStatus.FAILED,
        token = null,
        data = null
      )
      transactionHandler.finishPayment(vendorUuid, transactionUuid, GlobalUtil.computeTransactionStatus(paymentResult.status), paymentResult, paymentResult.errorCodeOrigin)
      paymentResult
    }
  }

  private def makeCalendar(date: String, time: String): Calendar = {
    val df: SimpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss")
    val transTimeFormat: DecimalFormat = new DecimalFormat("000000")
    try {
      val transformDateTime: Date = df.parse(date + time)
      val calendar: Calendar = Calendar.getInstance
      calendar.setTime(transformDateTime)
      return calendar
    }
    catch {
      case e: Throwable => {
        return null
      }
    }
  }

  private def serialize(sipsParam: SIPSDataObject): String = {
    var result: String = ""
    val e: Enumeration[_] = sipsParam.getKeyEnum
    while (e.hasMoreElements) {
      val key: String = e.nextElement.asInstanceOf[String]
      val value: String = URLEncoder.encode(sipsParam.getValue(key), "UTF-8")
      if (result.length > 0) result += "&"
      result += key + "=" + value
    }
    result
  }

  private[payment] def cancel(vendorUuid: Document, transactionUuid: Document, paymentConfig: PaymentConfig, infosPaiement: CancelRequest): CancelResult = {
    val vendor = EsClient.load[Account](vendorUuid).get
    val parametres = paymentConfig.cbParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())
    val formatDateAtos: SimpleDateFormat = new SimpleDateFormat("yyyyMM")
    val dir: File = new File(Settings.Sips.CertifDir, vendorUuid)
    val targetFile: File = new File(dir, "pathfile")
    val merchantCountry: String = parametres("sipsMerchantCountry")
    val merchantId: String = parametres("sipsMerchantId")
    val api: SIPSOfficeApi = new SIPSOfficeApi(targetFile.getAbsolutePath)
    val sipsRequest: SIPSDataObject = new SIPSCheckoutRequestParm(0)
    val sipsResponse: SIPSDataObject = new SIPSCheckoutResponseParm(0)

    transactionHandler.updateStatus(vendorUuid, transactionUuid, null, TransactionStatus.CANCEL_REQUESTED, null)

    sipsRequest.setValue("merchant_country", merchantCountry)
    sipsRequest.setValue("merchant_id", merchantId)
    sipsRequest.setValue("payment_date", new SimpleDateFormat("yyyyMMdd").format(new Date))
    sipsRequest.setValue("transaction_id", infosPaiement.id)
    sipsRequest.setValue("currency_code", "" + infosPaiement.currency.numericCode)

    val botlog = new BOTransactionLog(uuid = newUUID, provider = "SIPS", direction = "OUT", transaction = transactionUuid, log = serialize(sipsRequest))
    EsClient.index(botlog, false)
    api.asoCancelTransaction(sipsRequest, sipsResponse)
    val botlogIn = new BOTransactionLog(uuid = newUUID, provider = "SIPS", direction = "IN", transaction = transactionUuid, log = serialize(sipsResponse))
    EsClient.index(botlog, false)

    val diag_response_code: String = sipsResponse.getValue(SIPSOfficeResponseParm.TRANSACTION_RESPCODE)
    val diag_time: String = sipsResponse.getValue(SIPSOfficeResponseParm.TRANSACTION_TIME)
    val diag_date: String = sipsResponse.getValue(SIPSOfficeResponseParm.TRANSACTION_DATE)
    val diag_certificate: String = sipsResponse.getValue(SIPSOfficeResponseParm.TRANSACTION_CERTIFICATE)
    val merchant_id: String = sipsResponse.getValue(SIPSOfficeResponseParm.MERCHANT_ID)
    val merchant_country: String = sipsResponse.getValue(SIPSOfficeResponseParm.MERCHANT_COUNTRY)
    val transaction_id: String = sipsResponse.getValue(SIPSOfficeResponseParm.TRANSACTION_ID)
    val avs_response_code: String = sipsResponse.getValue(SIPSOfficeResponseParm.AVS_RESPONSE_CODE)
    val new_amount: String = sipsResponse.getValue(SIPSOfficeResponseParm.AMOUNT)
    val currency_code: String = sipsResponse.getValue(SIPSOfficeResponseParm.CURRENCY_CODE)
    val transaction_status: String = sipsResponse.getValue(SIPSOfficeResponseParm.TRANSACTION_STATUS)
    val authorisation_id: String = sipsResponse.getValue(SIPSOfficeResponseParm.AUTHORISATION_ID)
    val bank_response_code: String = sipsResponse.getValue(SIPSOfficeResponseParm.BANK_RESPONSE_CODE)
    val codeErreur: Long = diag_response_code.toLong
    val codeErreurBank: Long = if (codeErreur == 0) bank_response_code.toLong else -1
    transactionHandler.updateStatus(vendorUuid, transactionUuid, null, if (codeErreur == 0 && codeErreurBank == 0) TransactionStatus.CANCEL_CONFIRMED else TransactionStatus.CANCEL_FAILED, null)
    if (codeErreur == 0 && codeErreurBank == 0)
      CancelResult(
        id = infosPaiement.id,
        status = PaymentStatus.CANCELED,
        errorCodeOrigin = codeErreurBank.toString,
        errorMessageOrigin = Some("")
      )
    else
      CancelResult(
        id = infosPaiement.id,
        status = PaymentStatus.CANCEL_FAILED,
        errorCodeOrigin = codeErreur.toString,
        errorMessageOrigin = Some("")
      )
  }
}


object SipsHandler {
  val o3dCodes: Map[String, String] = Map(
    "00" -> "Porteur authentifie",
    "02" -> "Problème technique sur l’ACS",
    "03" -> "Commerçant inconnu",
    "12" -> "Requête invalide, vérifier les paramètres transférés dans la requête",
    "40" -> "Utilisation d’une fonctionnalité non supportée.",
    "55" -> "Porteur non authentifie",
    "62" -> "By-pass du porteur sur l'ACS",
    "81" -> "Erreur interne au MPI lors du 1er appel au MPI",
    "82" -> "Erreur interne au MPI lors du 2nd appel au MPI",
    "84" -> "Réponse du Directory Server invalide (VERes invalid)",
    "85" -> "MPI injoignable lors du 1er appel au MPI",
    "86" -> "MPI injoignable lors du 2nd appel au MPI",
    "88" -> "Problème réseau",
    "92" -> "Erreur interne du Directory Server",
    "93" -> "Erreur interne de l’ACS",
    "95" -> "Erreur d’intégrité sur le message renvoyé par l’ACS",
    "96" -> "Message renvoyé par l’ACS invalide",
    "99" -> "Erreur technique au niveau du serveur 3D Office"
  )

  val errorMessages = Map(
    "00" -> "Autorisation acceptée",
    "02" -> "Demande d’autorisation par téléphone à la banque à cause d’un dépassement du plafond d’autorisation sur la carte, si vous êtes autorisé à forcer les transactions. (cf. Annexe L) Dans le cas contraire, vous obtiendrez un code 05.",
    "03" -> "Champ merchant_id invalide, vérifier la valeur renseignée dans la requête. Contrat de vente à distance inexistant, contacter votre banque.",
    "05" -> "Autorisation refusée",
    "12" -> "Transaction invalide, vérifier les paramètres transférés dans la requête.",
    "14" -> "coordonnées bancaires ou cryptogramme visuel invalides.",
    "24" -> "Opération impossible. L’opération que vous souhaitez réaliser n’est pas compatible avec l’état de la transaction.",
    "25" -> "Transaction non trouvée dans la base de données Sips",
    "30" -> "Erreur de format.",
    "34" -> "Suspicion de fraude",
    "40" -> "Fonction non supportée : l’opération que vous souhaitez réaliser ne fait pas partie de la liste des opérations auxquelles vous êtes autorisé sur le serveur Sips Office Server. Contactez le Centre d’assistance Technique.",
    "54" -> "Date de validité de la carte dépassée.",
    "63" -> "Règles de sécurité non respectées, transaction arrêtée",
    "75" -> "Porteur non authentifié 3-D Secure (composant Checkout uniquement)",
    "90" -> "Service temporairement indisponible",
    "94" -> "Transaction dupliquée : pour une journée donnée, le transaction_id a déjà été utilisé.",
    "99" -> "Problème temporaire au niveau du serveur Sips Office Server."
  )

}
