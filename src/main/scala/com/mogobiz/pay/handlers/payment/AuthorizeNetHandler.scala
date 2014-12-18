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
import com.mogobiz.pay.exceptions.Exceptions.{InvalidPaymentMethodException, InvalidContextException, InvalidSignatureException}
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

    val fingerprint = Fingerprint.createFingerprint(apiLoginID, transactionKey, 0, amount)

    if (paymentConfig.paymentMethod == CBPaymentMethod.EXTERNAL) {
      val relayURL = s"${Settings.Mogopay.EndPoint}authorizenet/relay"
      val cancelURL = s"${Settings.Mogopay.EndPoint}authorizenet/cancel"

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
    } else if (paymentConfig.paymentMethod == CBPaymentMethod.THREEDS_NO) {
      val relayResponseUrl = s"${Settings.Mogopay.EndPoint}authorizenet/relay"
      val action = "https://test.authorize.net/gateway/transact.dll"

      val params = Map(
        "apiLoginId" -> apiLoginID,
        "transactionKey" -> transactionKey,
        "x_card_num" -> sessionData,
        "x_exp_date" -> "",
        "x_amount" -> amount,
        "x_invoice_num" -> System.currentTimeMillis.toString,
        "x_relay_url" -> relayResponseUrl,
        "x_login" -> apiLoginID,
        "x_fp_sequence" -> fingerprint.getSequence.toString,
        "x_fp_timestamp" -> fingerprint.getTimeStamp.toString,
        "x_fp_hash" -> fingerprint.getFingerprintHash,
        "x_version" -> "3.1",
        "x_method" -> "CC",
        "x_type" -> "AUTH_CAPTURE",
        "x_amount" -> amount,
        "x_test_request" -> "FALSE",
        "notes" -> "extra hot please",
        "buy_button" -> "BUY"
      ).toList

      val form = {
        <form id="authorizenet" action="https://test.authorize.net/gateway/transact.dll" method="post">
          <label>CreditCardNumber</label>
          <input type="text" class="text" name="x_card_num" size="15" />
          <label>Exp.</label>
          <input type="text" class="text" name="x_exp_date" size="4" />
          <label>Amount</label>
          <input type="text" class="text" name="x_amount" size="9" readonly="readonly" value={amount} />
          <input type="hidden" name="x_invoice_num" value={System.currentTimeMillis.toString} />
          <input type="hidden" name="x_relay_url" value={relayResponseUrl} />
          <input type="hidden" name="x_login" value={apiLoginID} />
          <input type="hidden" name="x_fp_sequence" value={fingerprint.getSequence.toString} />
          <input type="hidden" name="x_fp_timestamp" value={fingerprint.getTimeStamp.toString} />
          <input type="hidden" name="x_fp_hash" value={fingerprint.getFingerprintHash} />
          <input type="hidden" name="x_version" value="3.1" />
          <input type="hidden" name="x_method" value="CC" />
          <input type="hidden" name="x_type" value="AUTH_CAPTURE" />
          <input type="hidden" name="x_amount" value={amount} />
          <input type="hidden" name="x_test_request" value="FALSE" />
          <input type="hidden" name="notes" value="extra hot please" />
          <input type="hidden" name="vendor_uuid" value={vendorId} />
          <input type="submit" name="buy_button" value="BUY" />
        </form>
        <script>document.getElementById("authorizenet").submit();</script>
      }

      Left(form.mkString)
    } else {
      throw InvalidPaymentMethodException()
    }
  }

  def relay(sessionData: SessionData, params: Map[String, String]) = {
    val action = "http://7ceb5c55.ngrok.com/pay/authorizenet/finish"
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
    val log = new BOTransactionLog(uuid = newUUID, provider = "AUTHORIZENET", direction = "IN",
      transaction = sessionData.transactionUuid.getOrElse("None"), log = mapToQueryString(params))
    EsClient.index(Settings.Mogopay.EsIndex, log, false)

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
    val log = new BOTransactionLog(uuid = newUUID, provider = "AUTHORIZENET", direction = "IN",
      transaction = sessionData.transactionUuid.getOrElse("None"), log = "")
    EsClient.index(Settings.Mogopay.EsIndex, log, false)

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
