package com.mogobiz.pay.handlers.payment

import java.io.StringReader
import java.net.URLDecoder
import java.security.interfaces.RSAPublicKey
import java.security.{Signature, NoSuchAlgorithmException, MessageDigest, Security}
import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.ActorSystem
import akka.util.Timeout
import com.mogobiz.pay.handlers.UtilHandler
import com.mogobiz.pay.model.Mogopay.CreditCardType.CreditCardType
import net.authorize.sim._
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.config.Environment
import com.mogobiz.es.EsClient
import com.mogobiz.pay.exceptions.Exceptions.{InvalidContextException, InvalidSignatureException}
import com.mogobiz.pay.model.Mogopay.TransactionStatus
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.pay.config.Settings
import com.mogobiz.utils.{CustomSslConfiguration, GlobalUtil}
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMReader
import org.json4s.jackson.JsonMethods._
import com.mogobiz.utils.GlobalUtil._
import spray.http.{HttpResponse, Uri}
import sun.misc.BASE64Decoder

import scala.concurrent.Future
import spray.http._
import spray.client.pipelining._
import scala.concurrent.duration._

import scala.util._

class AuthorizeNetHandler(handlerName: String) extends PaymentHandler with CustomSslConfiguration {
  PaymentHandler.register(handlerName, this)

  implicit val timeout: Timeout = 40.seconds
  implicit val system = ActorSystem()

  import system.dispatcher

  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
  implicit val formats = new org.json4s.DefaultFormats {
  }

  def startPayment(sessionData: SessionData): Either[String, Uri] = {
    val transactionUUID = sessionData.transactionUuid.get
    val vendorId = sessionData.merchantId.get
    val paymentConfig = sessionData.paymentConfig.get
    val paymentRequest = sessionData.paymentRequest.get
    val id3d = sessionData.id3d.orNull
    val parameters = paymentConfig.cbParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())

    val transaction =
      if (id3d != null)
        EsClient.load[BOTransaction](Settings.Mogopay.EsIndex, transactionUUID).get
      else
        transactionHandler.startPayment(vendorId, sessionData.accountId, transactionUUID, paymentRequest,
          PaymentType.CREDIT_CARD, CBPaymentProvider.PAYBOX).get

    transactionHandler.updateStatus(vendorId, transactionUUID, null, TransactionStatus.PAYMENT_REQUESTED, null)

    val amount = paymentRequest.amount.toString
    val currency: Int = paymentRequest.currency.numericCode
    val apiLoginID: String = parameters("apiLoginID")
    val transactionKey: String = parameters("transactionKey")

    if (true) {
      val relayURL = s"${Settings.Mogopay.EndPoint}authorizenet/relay"
      val cancelURL = s"${Settings.Mogopay.EndPoint}authorizenet/cancel"

      val fingerprint = Fingerprint.createFingerprint(apiLoginID, transactionKey, 0, amount)
      val x_fp_sequence = fingerprint.getSequence
      val x_fp_timestamp = fingerprint.getTimeStamp
      val x_fp_hash = fingerprint.getFingerprintHash

      val form = {
        <form name="authorizenet" id="authorizenet" action="https://test.authorize.net/gateway/transact.dll" method="post">
          <input type="text" name="x_amount" value={amount} />
          <input type="hidden" name="x_login" value={apiLoginID} />
          <input type="hidden" name="x_fp_sequence" value={x_fp_sequence.toString} />
          <input type="hidden" name="x_fp_timestamp" value={x_fp_timestamp.toString} />
          <input type="hidden" name="x_fp_hash" value={x_fp_hash} />
          <input type="hidden" name="x_version" value="3.1"/>
          <input type="hidden" name="x_method" value="CC"/>
          <input type="hidden" name="x_type" value="AUTH_CAPTURE"/>
          <input type="hidden" name="x_show_form" value="payment_form" />
          <input type="hidden" name="x_test_request" value="false" />
          <input TYPE="hidden" name="x_relay_response" value="true" />
          <input type="hidden" name="x_relay_url" value={relayURL} />
          <input type="hidden" name="foo" value="bar" />
          <input type="hidden" name="x_cancel_url" value={cancelURL} />
          <input type="submit" name="submit_button" value="Submit" />
          <input type="hidden" name="vendor_uuid" value={vendorId} />
        </form>
        <script>document.getElementById('authorizenet').submit();</script>
      }

      val query = Map(
        "amount" -> amount,
        "apiLoginID" -> apiLoginID,
        "transactionKey" -> GlobalUtil.hideStringExceptLastN(transactionKey, 3),
        "currency" -> currency,
        "cc_holder" -> paymentRequest.holder,
        "cc_number" -> UtilHandler.hideCardNumber(paymentRequest.ccNumber, "X"),
        "cc_cvv" -> paymentRequest.cvv,
        "cc_expirationDate" -> paymentRequest.expirationDate
      )

      val log1 = new BOTransactionLog(uuid = newUUID, provider = "AUTHORIZENET", direction = "OUT",
        transaction = transactionUUID, log = GlobalUtil.mapToQueryString(query))
      EsClient.index(Settings.Mogopay.EsIndex, log1, false)

      Left(form.mkString)

      /*
      val CCExpDate = new SimpleDateFormat("MMyy").format(paymentRequest.expirationDate)
      if (id3d == null && (paymentConfig.paymentMethod == CBPaymentMethod.THREEDS_REQUIRED || paymentConfig.paymentMethod == CBPaymentMethod.THREEDS_IF_AVAILABLE)) {
        val query = Map(
          "IdMerchant=" -> idMerchant,
          "IdSession" -> IdSession,
          "Amount" -> Amount,
          "Currency" -> XCurrency,
          "CCNumber" -> CCNumber,
          "CCExpDate" -> CCExpDate,
          "CVVCode" -> CVVCode,
          "URLRetour" -> URLRetour,
          "URLHttpDirect" -> URLHttpDirect)
        val form =
          s"""
              <html>
                  <head>
                  </head>
                  <body>
                <FORM id="formpaybox" ACTION = "${action}" METHOD="POST">
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
        val query = scala.collection.mutable.Map(
          "NUMQUESTION" -> String.format("%010d", paymentRequest.transactionSequence.toInt.asInstanceOf[AnyRef]),
          "REFERENCE" -> IdSession,
          "DATEQ" -> new SimpleDateFormat("ddMMyyyyhhmmss").format(new Date()),
          "DEVISE" -> currency.toString,
          "MONTANT" -> Amount,
          "TYPE" -> "00003",
          "SITE" -> site,
          "RANG" -> rank,
          "CLE" -> key,
          "ACTIVITE" -> "024",
          "PORTEUR" -> CCNumber,
          "DATEVAL" -> CCExpDate,
          "CVV" -> CVVCode,
          "DIFFERE" -> "000",
          //          "ARCHIVAGE" -> "",
          //          "NUMAPPEL" -> "",
          //          "NUMTRANS" -> "",
          //          "AUTORISATION" -> "",
          "VERSION" -> (if (parameters("payboxContract") == "PAYBOX_DIRECT") "00103" else "00104"))
        if (paymentConfig.paymentMethod == CBPaymentMethod.THREEDS_REQUIRED)
          query += "ID3D" -> id3d
        else if (id3d != "" && paymentConfig.paymentMethod == CBPaymentMethod.THREEDS_IF_AVAILABLE)
          query += "ID3D" -> id3d

        val botlog = new BOTransactionLog(uuid = newUUID, provider = "PAYBOX", direction = "OUT", transaction = transactionUUID, log = GlobalUtil.mapToQueryString(query.toMap))
        EsClient.index(Settings.Mogopay.EsIndex, botlog, false)

        val uri = Uri(Settings.Paybox.DirectEndPoint)
        val host = uri.authority.host

        val logRequest: HttpRequest => HttpRequest = { r => println(r); r}
        val logResponse: HttpResponse => HttpResponse = { r => println(r); r}
        val pipeline: Future[SendReceive] =
          for (
            Http.HostConnectorInfo(connector, _) <-
            IO(Http) ? Http.HostConnectorSetup(host.toString, 443, sslEncryption = true)(system, sslEngineProvider)
          ) yield logRequest ~> sendReceive(connector) ~> logResponse

        val request = Post(uri.path.toString()).withEntity(HttpEntity(ContentType(MediaTypes.`application/x-www-form-urlencoded`), GlobalUtil.mapToQueryStringNoEncode(query.toMap)))
        val response = pipeline.flatMap(_(request))

        val tuples = Await.result(GlobalUtil.fromHttResponse(response), Duration.Inf)

        val bolog = new BOTransactionLog(uuid = newUUID, provider = "PAYBOX", direction = "IN", transaction = transactionUUID, log = GlobalUtil.mapToQueryStringNoEncode(tuples))
        EsClient.index(Settings.Mogopay.EsIndex, bolog, false)
        val errorCode = tuples.getOrElse("CODEREPONSE", "")
        val errorMessage = tuples.get("COMMENTAIRE")
        paymentResult = paymentResult.copy(
          ccNumber = UtilHandler.hideCardNumber(paymentResult.ccNumber, "X"),
          status = PaymentStatus.FAILED,
          errorCodeOrigin = errorCode,
          errorMessageOrigin = errorMessage,
          bankErrorCode = errorCode,
          bankErrorMessage = errorMessage)
        if (errorCode.equals("00000")) {
          val transaction = tuples("NUMQUESTION")
          val authorisation = tuples("AUTORISATION")
          paymentResult = paymentResult.copy(
            status = PaymentStatus.COMPLETE,
            transactionDate = new Date(),
            gatewayTransactionId = transaction,
            authorizationId = authorisation,
            transactionCertificate = null)
        }
        transactionHandler.finishPayment(vendorId, transactionUUID, if (errorCode == "00000") TransactionStatus.PAYMENT_CONFIRMED else TransactionStatus.PAYMENT_REFUSED, paymentResult, errorCode)
        // We redirect the user to the merchant website
        Right(finishPayment(sessionData, paymentResult))
      }
      */
    } else if (parameters("payboxContract") == "PAYBOX_SYSTEM") {
      /*
      val hmackey = idMerchant
      val pbxtime = ISODateTimeFormat.dateTimeNoMillis().print(org.joda.time.DateTime.now())
      val amountString = String.format("%010d", paymentRequest.amount.asInstanceOf[AnyRef])
      val queryList = List(
        "PBX_SITE" -> site,
        "PBX_RANG" -> rank,
        "PBX_IDENTIFIANT" -> key,
        "PBX_TOTAL" -> amountString,
        "PBX_DEVISE" -> s"${paymentRequest.currency.numericCode}",
        "PBX_CMD" -> s"${vendorId}--${transactionUUID}",
        "PBX_PORTEUR" -> transaction.email.getOrElse(vendor.email),
        "PBX_RETOUR" -> "AMOUNT:M;REFERENCE:R;AUTO:A;NUMTRANS:T;TYPEPAIE:P;CARTE:C;CARTEDEBUT:N;THREEDS:G;CARTEFIN:J;DATEFIN:D;DTPBX:W;CODEREPONSE:E;EMPREINTE:H;SIGNATURE:K",
        "PBX_HASH" -> "SHA512",
        "PBX_TIME" -> pbxtime,
        "PBX_EFFECTUE" -> s"${Settings.Mogopay.EndPoint}paybox/done",
        "PBX_REFUSE" -> s"${Settings.Mogopay.EndPoint}paybox/done",
        "PBX_ANNULE" -> s"${Settings.Mogopay.EndPoint}paybox/done",
        "PBX_REPONDRE_A" -> s"${Settings.Mogopay.EndPoint}paybox/callback/${sessionData.uuid}"
      )


      val queryString = mapToQueryStringNoEncode(queryList)
      val hmac = Sha512.hmacDigest(queryString, hmackey)
      val botlog = BOTransactionLog(uuid = newUUID, provider = "PAYBOX", direction = "OUT", transaction = transactionUUID, log = queryString)
      EsClient.index(Settings.Mogopay.EsIndex, botlog, false)
      val action = Settings.Paybox.SystemEndPoint
      val query = queryList.toMap

      //NUMTRANS=0003149638&NUMAPPEL=0005000280&NUMQUESTION=0000645802&SITE=5983130&RANG=01&AUTO=XXXXXX&CODEREPONSE=00000&COMMENTAIRE=Demande traitee avec succes&REFABONNE=&PORTEUR=&PAYS=???
      //NUMTRANS=0003149638&NUMAPPEL=0005000280&NUMQUESTION=0000645802&SITE=5983130&RANG=01&AUTO=XXXXXX&CODEREPONSE=00000&COMMENTAIRE=Demande traitee avec succes&REFABONNE=&PORTEUR=&PAYS=???
      val form =
        s"""
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
      */
      ???
    }
    else {
      throw InvalidContextException( s"""Invalid Paybox payment mode ${parameters("payboxContract")}""")
    }
  }

  def relay(sessionData: SessionData, params: Map[String, String]) = {
    val log = new BOTransactionLog(uuid = newUUID, provider = "AUTHORIZENET", direction = "IN",
      transaction = sessionData.transactionUuid.getOrElse("None"), log = mapToQueryString(params))
    EsClient.index(Settings.Mogopay.EsIndex, log, false)

    val action = "http://763bf2d2.ngrok.com/pay/authorizenet/finish"
    val form = {
      <form action={action} id="redirectForm" method="GET">
        {params.map { case (name, value) =>
          <input type="hidden" name={name} value={value} />
        }}
      </form>
      <script>document.getElementById('redirectForm').submit();</script>
    }.mkString.replaceAll("\"", "'")

    // TODO Sanitize the values
    println(form)
    form
  }

  def finish(sessionData: SessionData, params: Map[String, String]) = {
    val paymentConfig = sessionData.paymentConfig.get
    val cbParams = paymentConfig.cbParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())

    val paymentRequest = sessionData.paymentRequest.get

    val amount = paymentRequest.amount.toString
    val currency: Int = paymentRequest.currency.numericCode
    val apiLoginID: String = cbParams("apiLoginID")
    val transactionKey: String = cbParams("transactionKey")

    val query = Map(
      "amount" -> amount,
      "apiLoginID" -> apiLoginID,
      "transactionKey" -> GlobalUtil.hideStringExceptLastN(transactionKey, 3),
      "currency" -> currency,
      "cc_holder" -> paymentRequest.holder,
      "cc_number" -> UtilHandler.hideCardNumber(paymentRequest.ccNumber, "X"),
      "cc_cvv" -> paymentRequest.cvv,
      "cc_expirationDate" -> paymentRequest.expirationDate
    )

    val transaction: BOTransaction = sessionData.transactionUuid.flatMap { txUUID =>
      EsClient.load[BOTransaction](Settings.Mogopay.EsIndex, txUUID)
    }.get

    val responseCodeToStatus = Map("1" -> PaymentStatus.COMPLETE, "2" -> PaymentStatus.FAILED, "4" -> PaymentStatus.PENDING)
    val status: PaymentStatus.Value = responseCodeToStatus.getOrElse(params("x_response_code"), PaymentStatus.FAILED)

    val paymentResult = PaymentResult(
      transactionSequence = paymentRequest.transactionSequence,
      orderDate = paymentRequest.orderDate,
      amount = paymentRequest.amount,
      ccNumber = hideStringExceptLastN("X" * 8 + params("x_account_number"), 4, "X"),
      cardType = PayboxHandler.toCardType(params.getOrElse("CARTE", CreditCardType.CB.toString)),
      expirationDate = paymentRequest.expirationDate,
      cvv = "",
      gatewayTransactionId = transaction.uuid,
      transactionDate = new Date(),
      transactionCertificate = null,
      authorizationId = null,
      status = status,
      errorCodeOrigin = params("x_response_code"),
      errorMessageOrigin = Some(AuthorizeNetHandler.errorMessages(params("x_response_code"))),
      data = "",
      bankErrorCode = "",
      bankErrorMessage = None,
      token = ""
    )

    transactionHandler.finishPayment(params("vendor_uuid"),
      sessionData.transactionUuid.getOrElse(""),
      if (status == PaymentStatus.COMPLETE) TransactionStatus.PAYMENT_CONFIRMED else TransactionStatus.PAYMENT_REFUSED,
      paymentResult,
      params("x_response_code"))
    finishPayment(sessionData, paymentResult)
  }

  def cancel(sessionData: SessionData) = {
    val transaction: BOTransaction = EsClient.load[BOTransaction](Settings.Mogopay.EsIndex, sessionData.transactionUuid.getOrElse("???")).orNull
    val paymentResult = PaymentResult(
      newUUID, null, -1L, "", null, null, "", "", null, "", "", PaymentStatus.FAILED,
      transaction.errorCodeOrigin.getOrElse(""),
      transaction.errorMessageOrigin, "", "", Some(""), "")

    transactionHandler.finishPayment(sessionData.merchantId.getOrElse(""),
      sessionData.transactionUuid.getOrElse(""),
      TransactionStatus.PAYMENT_REFUSED,
      paymentResult,
      "")
    finishPayment(sessionData, paymentResult)
  }
}

object AuthorizeNetHandler {
  val errorMessages = Map(
    "1" -> "Approved",
    "2" -> "Declined",
    "3" -> "Error",
    "4" -> "Held for review"
  )

  def toCardType(xtype: String): CreditCardType = {
    val typ = if (xtype == null) "CB" else xtype.toUpperCase()
    if (typ == "CB") {
      CreditCardType.CB
    } else if (typ == "VISA") {
      CreditCardType.VISA
    } else if (typ == "AMEX") {
      CreditCardType.AMEX
    } else if (typ.indexOf("MASTERCARD") >= 0) {
      CreditCardType.MASTER_CARD
    } else if (typ == "VISA_ELECTRON") {
      CreditCardType.VISA
    } else {
      CreditCardType.CB
    }
  }
}
