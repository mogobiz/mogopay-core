/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.payment

import java.io._
import java.net.URLDecoder
import java.security._
import java.security.spec.X509EncodedKeySpec
import java.text.SimpleDateFormat
import java.util.Date

import akka.util.Timeout
import com.mogobiz.es.EsClient
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.config.{Environment, Settings}
import com.mogobiz.pay.exceptions.Exceptions.{InvalidContextException, InvalidSignatureException}
import com.mogobiz.pay.handlers.UtilHandler
import com.mogobiz.pay.model.CreditCardType.CreditCardType
import com.mogobiz.pay.model.{TransactionStatus, _}
import com.mogobiz.utils.GlobalUtil._
import com.mogobiz.utils.{CustomSslConfiguration, GlobalUtil, Sha512}
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.io.pem.PemReader
import org.joda.time.format.ISODateTimeFormat
import org.json4s.jackson.JsonMethods._
import spray.client.pipelining._
import spray.http.{HttpResponse, Uri, _}
import sun.misc.BASE64Decoder

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, Future}
import scala.util._

class PayboxHandler(handlerName: String) extends PaymentHandler with CustomSslConfiguration {
  PaymentHandler.register(handlerName, this)

  implicit val timeout: Timeout = 40.seconds
  val paymentType               = PaymentType.CREDIT_CARD
/*
  def startPayment(sessionData: SessionData): Either[String, Uri] = {
    val (transactionUUID, vendor, paymentConfig, paymentRequest) = getContext(sessionData)

    val id3d       = sessionData.id3d.getOrElse(null)
    val parametres = getCreditCardConfig(paymentConfig)

    val transaction =
      if (id3d != null)
        boTransactionHandler.find(transactionUUID).get
      else
        transactionHandler.startPayment(vendor,
                                        sessionData,
                                        transactionUUID,
                                        paymentRequest,
                                        PaymentType.CREDIT_CARD,
                                        CBPaymentProvider.PAYBOX)

    val context = if (Settings.Env == Environment.DEV) "TEST" else "PRODUCTION"
    val ctxMode = context
    transactionHandler.updateStatus(transactionUUID, sessionData.ipAddress, TransactionStatus.PAYMENT_REQUESTED)
    var paymentResult: PaymentResult = PaymentResult(
        transactionSequence = paymentRequest.transactionSequence,
        orderDate = paymentRequest.orderDate,
        amount = paymentRequest.amount,
        ccNumber = paymentRequest.ccNumber,
        cardType = paymentRequest.cardType,
        expirationDate = paymentRequest.expirationDate,
        cvv = paymentRequest.cvv,
        gatewayTransactionId = transactionUUID,
        transactionDate = null,
        transactionCertificate = null,
        authorizationId = null,
        status = null,
        errorCodeOrigin = "",
        errorMessageOrigin = Some(""),
        data = "",
        bankErrorCode = "",
        bankErrorMessage = Some(""),
        token = "",
        errorShipment = None
    )
    val currency: Int      = paymentRequest.currency.numericCode
    val site: String       = parametres("payboxSite")
    val key: String        = parametres("payboxKey")
    val rank: String       = parametres("payboxRank")
    val idMerchant: String = parametres("payboxMerchantId")
    val transDate: String  = new SimpleDateFormat("yyyyMMddHHmmss").format(paymentRequest.orderDate)

    val action        = Settings.Paybox.MPIEndPoint
    val IdMerchant    = idMerchant
    val IdSession     = vendor.uuid + "--" + transactionUUID
    val Amount        = String.format("%010d", paymentRequest.amount.asInstanceOf[AnyRef])
    val XCurrency     = currency.toString
    val CCNumber      = paymentRequest.ccNumber
    val CVVCode       = paymentRequest.cvv
    val URLRetour     = s"${Settings.Mogopay.EndPoint}paybox/done-3ds/${sessionData.uuid}"
    val URLHttpDirect = s"${Settings.Mogopay.EndPoint}paybox/callback-3ds/${sessionData.uuid}"
    if (parametres("payboxContract") == "PAYBOX_DIRECT" || parametres("payboxContract") == "PAYBOX_DIRECT_PLUS") {
      val CCExpDate = new SimpleDateFormat("MMyy").format(paymentRequest.expirationDate)
      if (id3d == null && (paymentConfig.paymentMethod == CBPaymentMethod.THREEDS_REQUIRED || paymentConfig.paymentMethod == CBPaymentMethod.THREEDS_IF_AVAILABLE)) {
        val query = Map("IdMerchant=" -> idMerchant,
                        "IdSession"     -> IdSession,
                        "Amount"        -> Amount,
                        "Currency"      -> XCurrency,
                        "CCNumber"      -> CCNumber,
                        "CCExpDate"     -> CCExpDate,
                        "CVVCode"       -> CVVCode,
                        "URLRetour"     -> URLRetour,
                        "URLHttpDirect" -> URLHttpDirect)
        val form = s"""
              <html>
                  <head>
                  </head>
                  <body>
                <FORM id="formpaybox" ACTION = "$action" METHOD="POST">
                  <INPUT TYPE="hidden" NAME ="IdMerchant" VALUE = "${IdMerchant}"><br>
                  <INPUT TYPE="hidden" NAME ="IdSession" VALUE = "${IdSession}"><br>
                  <INPUT TYPE="hidden" NAME ="Amount" VALUE = "${Amount}"><br>
                  <INPUT TYPE="hidden" NAME ="Currency" VALUE = "${XCurrency}"><br>
                  <INPUT TYPE="hidden" NAME ="CCNumber" VALUE = "${CCNumber}"><br>
                  <INPUT TYPE="hidden" NAME ="CCExpDate" VALUE = "${CCExpDate}"><br>
                  <INPUT TYPE="hidden" NAME ="CVVCode" VALUE = "${CVVCode}"><br>
                  <INPUT TYPE="hidden" NAME ="URLRetour" VALUE = "${URLRetour}"><br>
                  <INPUT TYPE="hidden" NAME ="URLHttpDirect" VALUE = "${URLHttpDirect}"><br>
                </FORM>
                    <script>document.getElementById("formpaybox").submit();</script>
                  </body>
              </html>
              """
        // We redirect the user to the paybox payment form
        Left(form)
      } else {
        // Group payment allowed only when using DIRECT_PLUS in PAYBOX and in 2D mode only
        val query = scala.collection.mutable.Map(
            "NUMQUESTION" -> String.format("%010d", paymentRequest.transactionSequence.toInt.asInstanceOf[AnyRef]),
            "REFERENCE"   -> IdSession,
            "DATEQ"       -> new SimpleDateFormat("ddMMyyyyhhmmss").format(new Date()),
            "DEVISE"      -> currency.toString,
            "MONTANT"     -> Amount,
            "TYPE"        -> "00003",
            "SITE"        -> site,
            "RANG"        -> rank,
            "CLE"         -> key,
            "ACTIVITE"    -> "024",
            "PORTEUR"     -> CCNumber,
            "DATEVAL"     -> CCExpDate,
            "CVV"         -> CVVCode,
            "DIFFERE"     -> "000",
            //          "ARCHIVAGE" -> "",
            //          "NUMAPPEL" -> "",
            //          "NUMTRANS" -> "",
            //          "AUTORISATION" -> "",
            "VERSION" -> (if (parametres("payboxContract") == "PAYBOX_DIRECT") "00103" else "00104"))
        if (paymentConfig.paymentMethod == CBPaymentMethod.THREEDS_REQUIRED)
          query += "ID3D" -> id3d
        else if (id3d != "" && paymentConfig.paymentMethod == CBPaymentMethod.THREEDS_IF_AVAILABLE)
          query += "ID3D" -> id3d

        val botlog = new BOTransactionLog(uuid = newUUID,
                                          provider = "PAYBOX",
                                          direction = "OUT",
                                          transaction = transactionUUID,
                                          log = GlobalUtil.mapToQueryString(query.toMap),
                                          step = TransactionStep.START_PAYMENT)
        boTransactionLogHandler.save(botlog, false)

        val uri = Uri(Settings.Paybox.DirectEndPoint)

        //        query.put("TYPE", "00056")
        //        query.put("SITE", "1999888")
        //        query.put("RANG", "069")
        //        query.put("CLE", "200932363")
        //        query.put("MONTANT", "100")
        //        val refAbonne = ""+(new Date().getTime)
        //        query.put("REFABONNE", refAbonne)
        //        val request0 = Post(uri.path.toString()).withEntity(HttpEntity(ContentType(MediaTypes.`application/x-www-form-urlencoded`), GlobalUtil.mapToQueryStringNoEncode(query.toMap)))
        //        val response0 = pipeline.flatMap(_(request0))
        //
        //        val tuples0 = Await.result(GlobalUtil.fromHttResponse(response0), Duration.Inf)
        //        tuples0.foreach(println)
        //
        //        query.put("TYPE", "00053")
        //        query.put("MONTANT", Amount)
        val request =
          Post(uri.path.toString()).withEntity(HttpEntity(ContentType(MediaTypes.`application/x-www-form-urlencoded`),
                                                          GlobalUtil.mapToQueryStringNoEncode(query.toMap)))
        val response = sslPipeline(uri.authority.host).flatMap(_ (request))

        val tuples = Await.result(GlobalUtil.fromHttResponse(response), Duration.Inf)
        tuples.foreach(println)
        val bolog = new BOTransactionLog(uuid = newUUID,
                                         provider = "PAYBOX",
                                         direction = "IN",
                                         transaction = transactionUUID,
                                         log = GlobalUtil.mapToQueryStringNoEncode(tuples),
                                         step = TransactionStep.START_PAYMENT)
        boTransactionLogHandler.save(bolog, false)
        val errorCode    = tuples.getOrElse("CODEREPONSE", "")
        val errorMessage = tuples.get("COMMENTAIRE")
        paymentResult = paymentResult.copy(ccNumber = UtilHandler.hideCardNumber(paymentRequest.ccNumber, "X"),
                                           status = PaymentStatus.FAILED,
                                           errorCodeOrigin = errorCode,
                                           errorMessageOrigin = errorMessage,
                                           bankErrorCode = errorCode,
                                           bankErrorMessage = errorMessage)
        if (errorCode.equals("00000")) {
          val transaction   = tuples("NUMQUESTION")
          val authorisation = tuples("AUTORISATION")
          paymentResult = paymentResult.copy(status = PaymentStatus.COMPLETE,
                                             transactionDate = new Date(),
                                             gatewayTransactionId = transaction,
                                             authorizationId = authorisation,
                                             transactionCertificate = null)
        }
        val paymentResultWithShippingResult = transactionHandler.finishPayment(
            this,
            sessionData,
            transactionUUID,
            if (errorCode == "00000") TransactionStatus.PAYMENT_CONFIRMED else TransactionStatus.PAYMENT_REFUSED,
            paymentResult,
            sessionData.locale,
            Some(s"""NUMTRANS=${tuples("NUMTRANS")}&NUMAPPEL=${tuples("NUMAPPEL")}"""))
        // We redirect the user to the merchant website
        Right(finishPayment(sessionData, paymentResultWithShippingResult))
      }
    } else if (parametres("payboxContract") == "PAYBOX_SYSTEM") {
      val hmackey      = idMerchant
      val pbxtime      = ISODateTimeFormat.dateTimeNoMillis().print(org.joda.time.DateTime.now())
      val amountString = String.format("%010d", paymentRequest.amount.asInstanceOf[AnyRef])
      val queryList = List(
          "PBX_SITE"        -> site,
          "PBX_RANG"        -> rank,
          "PBX_IDENTIFIANT" -> key,
          "PBX_TOTAL"       -> amountString,
          "PBX_DEVISE"      -> s"${paymentRequest.currency.numericCode}",
          "PBX_CMD"         -> s"${vendor.uuid}--${transactionUUID}",
          "PBX_PORTEUR"     -> transaction.email.getOrElse(vendor.email),
          "PBX_RETOUR"      -> "AMOUNT:M;REFERENCE:R;AUTO:A;NUMTRANS:S;NUMAPPEL:T;TYPEPAIE:P;CARTE:C;THREEDS:G;CARTEFIN:J;DATEFIN:D;DTPBX:W;CODEREPONSE:E;EMPREINTE:H;SIGNATURE:K",
          "PBX_HASH"        -> "SHA512",
          "PBX_TIME"        -> pbxtime,
          "PBX_EFFECTUE"    -> s"${Settings.Mogopay.EndPoint}paybox/done/${sessionData.uuid}",
          "PBX_REFUSE"      -> s"${Settings.Mogopay.EndPoint}paybox/done/${sessionData.uuid}",
          "PBX_ANNULE"      -> s"${Settings.Mogopay.EndPoint}paybox/done/${sessionData.uuid}",
          "PBX_REPONDRE_A"  -> s"${Settings.Mogopay.EndPoint}paybox/callback/${sessionData.uuid}"
      )

      val queryString = mapToQueryStringNoEncode(queryList)
      val hmac        = Sha512.hmacDigest(queryString, hmackey)
      val botlog = BOTransactionLog(uuid = newUUID,
                                    provider = "PAYBOX",
                                    direction = "OUT",
                                    transaction = transactionUUID,
                                    log = queryString,
                                    step = TransactionStep.START_PAYMENT)
      boTransactionLogHandler.save(botlog, false)
      val action = Settings.Paybox.SystemEndPoint
      val query  = queryList.toMap

      //NUMTRANS=0003149638&NUMAPPEL=0005000280&NUMQUESTION=0000645802&SITE=5983130&RANG=01&AUTO=XXXXXX&CODEREPONSE=00000&COMMENTAIRE=Demande traitee avec succes&REFABONNE=&PORTEUR=&PAYS=???
      //NUMTRANS=0003149638&NUMAPPEL=0005000280&NUMQUESTION=0000645802&SITE=5983130&RANG=01&AUTO=XXXXXX&CODEREPONSE=00000&COMMENTAIRE=Demande traitee avec succes&REFABONNE=&PORTEUR=&PAYS=???
      val form = s"""
<html>
    <head>
    </head>
    <body>
    	Veuillez patienter...
	<FORM id="formpaybox" ACTION = "${action}" METHOD="POST">
	  <INPUT TYPE="hidden" NAME ="PBX_SITE" VALUE = "${site}"><br>
		<INPUT TYPE="hidden" NAME ="PBX_RANG" VALUE = "${rank}"><br>
		<INPUT TYPE="hidden" NAME ="PBX_IDENTIFIANT" VALUE =  "${key}"><br>
		<INPUT TYPE="hidden" NAME ="PBX_TOTAL" VALUE = "${query("PBX_TOTAL")}"><br>
		<INPUT TYPE="hidden" NAME ="PBX_DEVISE" VALUE = "${query("PBX_DEVISE")}"><br>
		<INPUT TYPE="hidden" NAME ="PBX_CMD" VALUE = "${query("PBX_CMD")}"><br>
		<INPUT TYPE="hidden" NAME ="PBX_PORTEUR" VALUE = "${query("PBX_PORTEUR")}"><br>
		<INPUT TYPE="hidden" NAME ="PBX_RETOUR" VALUE = "${query("PBX_RETOUR")}"><br>
		<INPUT TYPE="hidden" NAME ="PBX_HASH" VALUE = "SHA512"><br>
		<INPUT TYPE="hidden" NAME ="PBX_TIME" VALUE = "${query("PBX_TIME")}"><br>
		<INPUT TYPE="hidden" NAME ="PBX_EFFECTUE" VALUE = "${query("PBX_EFFECTUE")}"><br>
		<INPUT TYPE="hidden" NAME ="PBX_REFUSE" VALUE = "${query("PBX_REFUSE")}"><br>
		<INPUT TYPE="hidden" NAME ="PBX_ANNULE" VALUE = "${query("PBX_ANNULE")}"><br>
		<INPUT TYPE="hidden" NAME ="PBX_REPONDRE_A" VALUE = "${query("PBX_REPONDRE_A")}"><br>
		<INPUT TYPE="hidden" NAME ="PBX_HMAC" VALUE = "${hmac}"><br>
	</FORM>
    	<script>document.getElementById("formpaybox").submit();</script>
    </body>
</html>
"""
      // We redirect the user to the payment server
      Left(form)
    } else {
      throw InvalidContextException(s"""Invalid Paybox payment mode ${parametres("payboxContract")}""")
    }
  }

  def validatePayment(transaction: BOTransaction, amount: Long): Option[ValidatePaymentResult] = {
    //TODO à implémenter
    None
  }

  def refundPayment(transaction: BOTransaction, amount: Long): Option[ValidatePaymentResult] = {
    //TODO à implémenter
    None
  }

  def verifySha1(data: String, sign: String): Boolean = {
    val Charset                 = "UTF-8"
    val HashEncryptionAlgorithm = "SHA1withRSA"

    Security.addProvider(new BouncyCastleProvider())
    val decoder   = new BASE64Decoder()
    val signature = decoder.decodeBuffer(URLDecoder.decode(sign, Charset))
    try {
      val md = MessageDigest.getInstance("SHA-1")
      md.digest(data.getBytes(Charset))
    } catch {
      case e: NoSuchAlgorithmException => e.printStackTrace()
    }
    val sig = Signature.getInstance("SHA1withRSA")
    sig.initVerify(Settings.Paybox.PublicKey)
    sig.update(data.getBytes(Charset))
    sig.verify(signature)
  }

  def callbackPayment(sessionData: SessionData, params: Map[String, String], uri: String): Unit = {
    if (params("CODEREPONSE") == "00000") donePayment(sessionData, params, uri)
  }

  def donePayment(sessionData: SessionData, params: Map[String, String], uri: String): Uri = {
    val transactionUuid = sessionData.transactionUuid.get
    val vendorId        = sessionData.merchantId.get
    val transaction     = boTransactionHandler.find(transactionUuid).get
    val amount          = sessionData.amount
    val paymentRequest  = sessionData.paymentRequest.get
    val errorUrl        = sessionData.errorURL
    val successUrl      = sessionData.successURL
    if (transaction.status != TransactionStatus.PAYMENT_CONFIRMED && transaction.status != TransactionStatus.PAYMENT_REFUSED) {
      //val signature = params.getOrElse("SIGNATURE", throw new Exception("Unexpected payment chain"))
      val vendorAndUuid        = params("REFERENCE")
      val vendorAndUuidArray   = vendorAndUuid.split("--")
      val paramVendorId        = vendorAndUuidArray(0)
      val paramTransactionUuid = vendorAndUuidArray(1)
      val ccNumber =
        if (params.getOrElse("CARTEDEBUT", null) != null) params("CARTEDEBUT") + "XXXXXXXX" + params("CARTEFIN")
        else "XXXXXXXX"
      val simpleDateFormat = new SimpleDateFormat("ddMMyy")
      val expirationDate = try {
        simpleDateFormat.parse("01" + params("DATEFIN"))
      } catch {
        case e: Exception =>
          // The customer did not give his card number
          null
      }
      val dataToCheck = uri.substring(uri.indexOf("?") + 1, uri.indexOf("&SIGNATURE="))
      val signature   = uri.substring(uri.indexOf("&SIGNATURE=") + "&SIGNATURE=".length)
      val ok          = verifySha1(dataToCheck, signature) && paramTransactionUuid == transactionUuid && vendorId == paramVendorId
      if (ok) {
        val codeReponse = params("CODEREPONSE")
        val bankErrorCode =
          if (codeReponse == "00000") "00" else if (codeReponse.startsWith("001")) codeReponse.substring(3) else ""
        val paymentResult = PaymentResult(
            transactionSequence = paymentRequest.transactionSequence,
            orderDate = paymentRequest.orderDate,
            amount = paymentRequest.amount,
            ccNumber = ccNumber,
            cardType = PayboxHandler.toCardType(params.getOrElse("CARTE", CreditCardType.CB.toString)),
            expirationDate = expirationDate,
            cvv = "",
            gatewayTransactionId = transaction.uuid,
            transactionDate = new Date(),
            transactionCertificate = null,
            authorizationId = null,
            status = if (codeReponse == "00000") PaymentStatus.COMPLETE else PaymentStatus.FAILED,
            errorCodeOrigin = codeReponse,
            errorMessageOrigin =
              Some(PayboxHandler.errorMessages(if (codeReponse.startsWith("001")) "001xx" else codeReponse)),
            data = "",
            bankErrorCode = bankErrorCode,
            bankErrorMessage = Some(BankErrorCodes.getErrorMessage(bankErrorCode)),
            token = "",
            errorShipment = None
        )
        val creditCard = BOCreditCard(
            number = ccNumber,
            holder = None,
            expiryDate = paymentResult.expirationDate,
            cardType = paymentResult.cardType
        )
        boTransactionHandler.update(transaction.copy(creditCard = Some(creditCard)), false)

        val paymentResultWithShippingResult = transactionHandler.finishPayment(
            this,
            sessionData,
            transactionUuid,
            if (codeReponse == "00000") TransactionStatus.PAYMENT_CONFIRMED else TransactionStatus.PAYMENT_REFUSED,
            paymentResult,
            sessionData.locale,
            Some(GlobalUtil.mapToQueryString(params)))
        finishPayment(sessionData, paymentResultWithShippingResult)
      } else {
        throw InvalidSignatureException(s"$signature")
      }
    } else {
      val paymentResult = PaymentResult(
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
          status =
            if (transaction.status == TransactionStatus.PAYMENT_CONFIRMED) PaymentStatus.COMPLETE
            else PaymentStatus.FAILED,
          errorCodeOrigin = transaction.errorCodeOrigin.getOrElse(""),
          errorMessageOrigin = transaction.errorMessageOrigin,
          data = "",
          bankErrorCode = "",
          bankErrorMessage = Some(""),
          token = "",
          errorShipment = None
      )
      finishPayment(sessionData, paymentResult)
    }

  }

  def done3DSecureCheck(sessionData: SessionData, params: Map[String, String]): Uri = {
    val statusPBX            = params("StatusPBX")
    val vendorAndUuid        = params("IdSession")
    val vendorAndUuidArray   = vendorAndUuid.split("--")
    val paramVendorId        = vendorAndUuidArray(0)
    val paramTransactionUuid = vendorAndUuidArray(1)
    val transaction          = boTransactionHandler.find(paramTransactionUuid).get
    val id3d                 = params("ID3D")
    val transactionUuid      = sessionData.transactionUuid.get
    val vendorId             = sessionData.merchantId.get

    val paymentConfig  = sessionData.paymentConfig.get
    val paymentRequest = sessionData.paymentRequest.get
    val ctxMode        = if (Settings.Env == Environment.PROD) "PRODUCTION" else "TEST"
    if (vendorId != paramVendorId || transactionUuid != paramTransactionUuid) {
      throw InvalidContextException(s"Invalid vendorid $paramVendorId or transactionid $paramTransactionUuid")
    } else {
      if (ctxMode == "TEST" || transaction.paymentData.status3DS == ResponseCode3DS.APPROVED) {
        sessionData.id3d = Some(id3d)
        startPayment(sessionData) match {
          case Right(uri) => uri
          case Left(form) => throw InvalidContextException("Unexpected payment chaining")
        }
      } else if (paymentConfig.paymentMethod == CBPaymentMethod.THREEDS_IF_AVAILABLE) {
        sessionData.id3d = Some("") /* empty string for id3d means fallback to 2DS */
        startPayment(sessionData) match {
          case Right(uri) => uri
          case Left(form) => throw new InvalidContextException("Unexpected payment chaining")
        }
      } else {
        val errorCode = "12"
        val paymentResult = PaymentResult(
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
            errorCodeOrigin = errorCode,
            errorMessageOrigin = Some("ThreeDSecure required"),
            data = "",
            bankErrorCode = errorCode,
            bankErrorMessage = Some(BankErrorCodes.getErrorMessage(errorCode)),
            token = "",
            errorShipment = None
        )
        finishPayment(sessionData, paymentResult)
      }
    }
  }

  def callback3DSecureCheck(sessionData: SessionData, params: Map[String, String]): Unit = {
    val statusPBX          = params("StatusPBX")
    val vendorAndUuid      = params("IdSession")
    val vendorAndUuidArray = vendorAndUuid.split("--")
    val vendorId           = vendorAndUuidArray(0)
    val transactionUuid    = vendorAndUuidArray(1)
    val transaction        = boTransactionHandler.find(transactionUuid).get
    val id3d               = params("ID3D")

    if (params("3DSTATUS") == "Y" && params("3DSIGNVAL") == "Y" && params("3DENROLLED") == "Y" && params("3DERROR") == "0") {
      transaction.paymentData.copy(status3DS = Some(ResponseCode3DS.APPROVED))
    } else {
      transaction.paymentData.copy(status3DS = Some(ResponseCode3DS.REFUSED))
    }

    boTransactionHandler.update(transaction, refresh = false)
  }

  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
  implicit val formats                              = new org.json4s.DefaultFormats {}

  override def refund(paymentConfig: PaymentConfig,
                      boTx: BOTransaction,
                      amount: Long,
                      paymentResult: PaymentResult): RefundResult = {
    val parameters = getCreditCardConfig(paymentConfig)

    val gatewayData = GlobalUtil.queryStringToMap(boTx.gatewayData.getOrElse(""))

    val query: Map[String, String] = Map(
        "VERSION"     -> (if (parameters("payboxContract") == "PAYBOX_DIRECT") "00103" else "00104"),
        "SITE"        -> parameters("payboxSite"),
        "TYPE"        -> "00014", // 00014 for refund
        "RANG"        -> parameters("payboxRank"),
        "CLE"         -> parameters("payboxKey"),
        "NUMQUESTION" -> transactionSequenceHandler.nextTransactionId(boTx.vendor.get.uuid).toString.format("%010d"),
        "MONTANT"     -> boTx.amount.toString,
        "DEVISE"      -> boTx.currency.numericCode.toString,
        "REFERENCE"   -> (boTx.vendor.get.uuid + "--" + boTx.uuid),
        "NUMTRANS"    -> gatewayData("NUMTRANS"),
        "NUMAPPEL"    -> gatewayData("NUMAPPEL"),
        "ACTIVITE"    -> "024", // 024 is the default value
        "DATEQ"       -> new SimpleDateFormat("ddMMyyyyhhmmss").format(new Date())
    )

    val logOUT = new BOTransactionLog(uuid = newUUID,
                                      provider = "PAYBOX",
                                      direction = "OUT",
                                      transaction = boTx.uuid,
                                      log = GlobalUtil.mapToQueryString(query),
                                      step = TransactionStep.REFUND)
    EsClient.index(Settings.Mogopay.EsIndex, logOUT, false)

    val uri = Uri(Settings.Paybox.DirectEndPoint)
    val post = Post(uri).withEntity(
        HttpEntity(ContentType(MediaTypes.`application/x-www-form-urlencoded`), GlobalUtil.mapToQueryString(query)))
    val response = Await.result(sslPipeline(uri.authority.host).flatMap(_ (post)), Duration.Inf)

    val logIN = new BOTransactionLog(uuid = newUUID,
                                     provider = "PAYBOX",
                                     direction = "IN",
                                     transaction = boTx.uuid,
                                     log = response.entity.data.asString,
                                     step = TransactionStep.REFUND)
    EsClient.index(Settings.Mogopay.EsIndex, logIN, false)

    val responseCode = GlobalUtil.queryStringToMap(response.entity.data.asString)("CODEREPONSE")
    val status       = if (responseCode == "00000") PaymentStatus.REFUNDED else PaymentStatus.REFUND_FAILED
    RefundResult(status, responseCode, PayboxHandler.errorMessages.get(responseCode))
  }
  */

  override def startPayment(sessionData: SessionData): Either[FormRedirection, Uri] = throw new Exception("Not implemented")

}

object PayboxHandler {

  val errorMessages = Map(
      "00000" -> "Opération ré́ussie.",
      "00016" -> "Abonné déjà existant (inscription nouvel abonné).",
      "00001" -> "La connexion au centre d‟autorisation a échoué.",
      "00017" -> "Abonné inexistant.",
      "001xx" -> "Paiement refusé par le centre d‟autorisation",
      "00018" -> "Transaction non trouvée.",
      "00002" -> "Une erreur de cohérence est survenue.",
      "00019" -> "Réservé.",
      "00003" -> "Erreur Paybox.",
      "00020" -> "Cryptogramme visuel non présent.",
      "00004" -> "Numéro de porteur invalide.",
      "00021" -> "Carte non autorisée.",
      "00005" -> "Numéro de question invalide.",
      "00022" -> "Réservé.",
      "00006" -> "Accès refusé ou site / rang incorrect.",
      "00023" -> "Réservé.",
      "00007" -> "Date invalide.",
      "00024" -> "Erreur de chargement de la clé ->Réservé Usage Futur.",
      "00008" -> "Date de fin de validité incorrecte.",
      "00025" -> "Signature manquante ->Réservé Usage Futur.",
      "00009" -> "Type d‟opération invalide.",
      "00026" -> "Clé manquante mais la signature est présente ->Réservé Usage Futur.",
      "00010" -> "Devise inconnue.",
      "00027" -> "Erreur OpenSSL durant la vérification de la signature ->Réservé Usage Futur.",
      "00011" -> "Montant incorrect.",
      "00028" -> "Signature invalide ->Réservé Usage Futur.",
      "00012" -> "Référence commande invalide.",
      "00097" -> "Timeout de connexion atteint.",
      "00013" -> "Cette version n‟est plus soutenue.",
      "00098" -> "Erreur de connexion interne.",
      "00014" -> "Trame reçue incohérente.",
      "00099" -> "Incohérence entre la question et la réponse. Refaire une nouvelle tentative ultérieurement.",
      "00015" -> "Erreur d‟accès aux données précédemment référencées."
  )

  def toCardType(xtype: String): CreditCardType = {
    val typ = if (xtype == null) "CB" else xtype.toUpperCase()
    if (typ == "CB") {
      return CreditCardType.CB
    } else if (typ == "VISA") {
      return CreditCardType.VISA
    } else if (typ == "AMEX") {
      return CreditCardType.AMEX
    } else if (typ.indexOf("MASTERCARD") >= 0) {
      return CreditCardType.MASTER_CARD
    } else if (typ == "VISA_ELECTRON") {
      return CreditCardType.VISA
    } else {
      return CreditCardType.CB
    }
  }

  def main(args: Array[String]): Unit = {
    val Charset = "UTF-8"
    val sig     = Signature.getInstance("SHA1withRSA")
    val sign =
      "Olwo4inZvWiVIqfoQus8Y1xBNSAHcPJnrH4Qh7fX7DOwWeAlQUjHh0XoIX6La4eks6AiocuZSlx8Ey4xTiOt6A/W%2BJhpIz61eqz5d%2BHF2Oh/I0DkjKwSnSmVMVUgOu48YRDbsI05CR3EX162HBe0l2XYc8eZ3OYHk2CwgPPr3Uk%3D"
    val decoder   = new BASE64Decoder()
    val signature = decoder.decodeBuffer(URLDecoder.decode(sign, Charset))
    val lines     = scala.io.Source.fromFile("/Applications/jcf/data/mogopay/paybox/paybox.pem").mkString
    println(lines)
    val pem = lines.replace("-----BEGIN PUBLIC KEY-----\n", "").replace("-----END PUBLIC KEY-----", "")
    println(pem)

    val reader = new StringReader(
        scala.io.Source.fromFile("/Applications/jcf/data/mogopay/paybox/paybox.pem").mkString)
    val pemreader          = new PemReader(reader)
    val x509EncodedKeySpec = new X509EncodedKeySpec(pemreader.readPemObject().getContent)
    val kf                 = KeyFactory.getInstance("RSA")
    sig.initVerify(kf.generatePublic(x509EncodedKeySpec))
    val message =
      "AMOUNT=3300&REFERENCE=d7b864c8-4567-4603-abd4-5f85e9ff56e6--d32b6abd-b8a6-41f9-bbab-cdfb0c1048bf&AUTO=XXXXXX&NUMTRANS=13949577&TYPEPAIE=CARTE&CARTE=VISA&CARTEDEBUT=&CARTEFIN=&DATEFIN=2207&DTPBX=27012016&CODEREPONSE=00000&EMPREINTE=5B434C778490889697170E225029F56AFF19CA47"
    sig.update(message.getBytes(Charset))
    val ok = sig.verify(signature)
    println(ok)
  }
}
