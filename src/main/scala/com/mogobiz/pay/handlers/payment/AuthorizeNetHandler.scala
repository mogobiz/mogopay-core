/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.payment

import java.math
import java.nio.charset.StandardCharsets
import java.util.{UUID, Date}

import akka.actor.ActorSystem
import akka.util.Timeout
import com.mogobiz.es.EsClient
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.config.{Environment, Settings}
import com.mogobiz.pay.exceptions.Exceptions._
import com.mogobiz.pay.model.Mogopay.CreditCardType.CreditCardType
import com.mogobiz.pay.model.Mogopay.{TransactionStatus, _}
import com.mogobiz.utils.GlobalUtil._
import com.mogobiz.utils.{CustomSslConfiguration, GlobalUtil}
import net.authorize.api.contract.v1.{TransactionRequestType, CreateTransactionRequest}
import net.authorize.{aim, ResponseCode, Merchant, TransactionType}
import net.authorize.sim._
import org.json4s.jackson.JsonMethods._
import spray.client.pipelining._
import spray.http.{HttpResponse, Uri, _}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util._

class AuthorizeNetHandler(handlerName: String) extends PaymentHandler with CustomSslConfiguration {
  PaymentHandler.register(handlerName, this)

  implicit val timeout: Timeout = 40.seconds
  import system.dispatcher

  val paymentType = PaymentType.CREDIT_CARD
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
  implicit val formats = new org.json4s.DefaultFormats {}

  val VENDOR_UUID = "vendor_uuid"
  val BOTX_UUID   = "botx_uuid"
  val PAYREQ_UUID   = "payreq_uuid"
  val SESSION_UUID   = "session_uuid"

  def startPayment(sessionData: SessionData): Either[String, Uri] = {
    val transactionRequestUUID = sessionData.transactionUuid.get
    val vendorId = sessionData.merchantId.get
    val paymentConfig = sessionData.paymentConfig.get
    val paymentRequest = sessionData.paymentRequest.get
    val id3d = sessionData.id3d.orNull
    val parameters = paymentConfig.cbParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())

    val transaction =
      if (id3d != null)
        EsClient.load[BOTransaction](Settings.Mogopay.EsIndex, transactionRequestUUID).get
      else
        transactionHandler.startPayment(vendorId, sessionData, transactionRequestUUID, paymentRequest,
          PaymentType.CREDIT_CARD, CBPaymentProvider.AUTHORIZENET).get

    transactionHandler.updateStatus(transactionRequestUUID, sessionData.ipAddress, TransactionStatus.PAYMENT_REQUESTED)

    paymentRequestHandler.save(paymentRequest, refresh = false)

    val amount = paymentRequest.amount.toString
    val currency: Int = paymentRequest.currency.numericCode
    val cbParam = paymentConfig.cbParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())
    val apiLoginID = cbParam("apiLoginID")
    val transactionKey = cbParam("transactionKey")
    val fingerprint = Fingerprint.createFingerprint(apiLoginID, transactionKey, 0, amount)

    val relayURL = s"${Settings.Mogopay.EndPoint}authorizenet/relay/${sessionData.uuid}" // without port because Authorize.net doesn't hit "exotic" ports :)
    val cancelURL = s"${Settings.Mogopay.EndPoint}authorizenet/cancel"
    val formAction = Settings.AuthorizeNet.formAction

    if (paymentConfig.paymentMethod == CBPaymentMethod.EXTERNAL) {
      val x_fp_sequence = fingerprint.getSequence
      val x_fp_timestamp = fingerprint.getTimeStamp
      val x_fp_hash = fingerprint.getFingerprintHash

      val form = {
        <form name="authorizenet" id="authorizenet" action={formAction} method="post">
          <input type="hidden" name="x_amount" value={(amount.toFloat / 100).toString}/>
          <input type="hidden" name="x_login" value={apiLoginID}/>
          <input type="hidden" name="x_fp_sequence" value={x_fp_sequence.toString}/>
          <input type="hidden" name="x_fp_timestamp" value={x_fp_timestamp.toString}/>
          <input type="hidden" name="x_fp_hash" value={x_fp_hash}/>
          <input type="hidden" name="x_version" value="3.1"/>
          <input type="hidden" name="x_method" value="CC"/>
          <input type="hidden" name="x_type" value="AUTH_CAPTURE"/>
          <input type="hidden" name="x_show_form" value="payment_form"/>
          <input type="hidden" name="x_test_request" value="false"/>
          <input TYPE="hidden" name="x_relay_response" value="true"/>
          <input type="hidden" name="x_relay_url" value={relayURL}/>
          <input type="hidden" name="x_cancel_url" value={cancelURL}/>
          <input type="submit" name="submit_button" value="Submit"/>
          <input type="hidden" name={VENDOR_UUID} value={vendorId}/>
          <input type="hidden" name={BOTX_UUID} value={transaction.uuid}/>
          <input type="hidden" name={PAYREQ_UUID} value={paymentRequest.uuid}/>
          <input type="hidden" name={SESSION_UUID} value={sessionData.uuid}/>
        </form>
        <script>document.getElementById('authorizenet').submit();</script>
      }

      if (Settings.Env == Environment.DEV) { // Just `open /tmp/authorizenet-form.html` to start the payment
        java.nio.file.Files.write(java.nio.file.Paths.get("/tmp/authorizenet-form.html"),
          form.mkString.getBytes(StandardCharsets.UTF_8))
      }

      val query = Map(
        "amount" -> amount,
        "apiLoginID" -> apiLoginID,
        "transactionKey" -> GlobalUtil.hideStringExceptLastN(transactionKey, 3),
        "currency" -> currency
      )

      val log1 = new BOTransactionLog(uuid = newUUID, provider = "AUTHORIZENET", direction = "OUT",
        transaction = transactionRequestUUID, log = GlobalUtil.mapToQueryString(query), step = TransactionStep.START_PAYMENT)
      EsClient.index(Settings.Mogopay.EsIndex, log1, false)

      Left(form.mkString)
    } else if (paymentConfig.paymentMethod == CBPaymentMethod.THREEDS_NO) {
      val query = Map(
        "amount" -> amount,
        "apiLoginID" -> apiLoginID,
        "transactionKey" -> GlobalUtil.hideStringExceptLastN(transactionKey, 3),
        "currency" -> currency,
        "cc_holder" -> paymentRequest.holder,
        "cc_number" -> GlobalUtil.hideStringExceptLastN(paymentRequest.ccNumber, 4, "X"),
        "cc_cvv" -> paymentRequest.cvv,
        "cc_expirationDate" -> paymentRequest.expirationDate
      )

      val (cardNumberDefaultValue, expirationDateDefaultValue) = if (Settings.Env == Environment.DEV) {
        ("4007000000027", "0219")
      } else {
        ("", "")
      }

      val form = {
        <form id="authorizenet" action={formAction} method="post">
          <label>CreditCardNumber</label>
          <input type="text" class="text" name="x_card_num" size="15" value={cardNumberDefaultValue}/>
          <label>Exp.</label>
          <input type="text" class="text" name="x_exp_date" size="4" value={expirationDateDefaultValue}/>
          <label>Amount</label>
          <input type="text" class="text" name="x_amount" size="9" readonly="readonly" value={amount}/>
          <input type="hidden" name="x_invoice_num" value={System.currentTimeMillis.toString}/>
          <input type="hidden" name="x_relay_url" value={relayURL}/>
          <input type="hidden" name="x_bInin" value={apiLoginID}/>
          <input type="hidden" name="x_fp_sequence" value={fingerprint.getSequence.toString}/>
          <input type="hidden" name="x_fp_timestamp" value={fingerprint.getTimeStamp.toString}/>
          <input type="hidden" name="x_fp_hash" value={fingerprint.getFingerprintHash}/>
          <input type="hidden" name="x_version" value="3.1"/>
          <input type="hidden" name="x_method" value="CC"/>
          <input type="hidden" name="x_type" value="AUTH_CAPTURE"/>
          <input type="hidden" name="x_amount" value={amount}/>
          <input type="hidden" name="x_test_request" value="FALSE"/>
          <input type="hidden" name={VENDOR_UUID} value={vendorId}/>
          <input type="hidden" name={BOTX_UUID} value={transaction.uuid}/>
          <input type="hidden" name={PAYREQ_UUID} value={paymentRequest.uuid}/>
          <input type="hidden" name={SESSION_UUID} value={sessionData.uuid}/>
          <input type="submit" name="buy_button" value="BUY"/>
          {if (Settings.Env == Environment.DEV) {
            <script>document.getElementById('authorizenet').submit();</script>
          }}
        </form>
      }

      if (Settings.Env == Environment.DEV) { // Just `open /tmp/authorizenet-form.html` to start the payment
        java.nio.file.Files.write(java.nio.file.Paths.get("/tmp/authorizenet-form.html"),
          form.mkString.getBytes(StandardCharsets.UTF_8))
      }

      val log = new BOTransactionLog(uuid = newUUID, provider = "AUTHORIZENET", direction = "OUT",
        transaction = transactionRequestUUID, log = GlobalUtil.mapToQueryString(query), step = TransactionStep.START_PAYMENT)
      EsClient.index(Settings.Mogopay.EsIndex, log, false)

      Left(form.mkString)
    } else {
      throw InvalidPaymentMethodException()
    }
  }

  def relay(sessionData: SessionData, params: Map[String, String]) = {
    val action = s"${Settings.Mogopay.EndPoint}authorizenet/finish/${sessionData.uuid}"
    val form = {
      <form action={action} id="redirectForm" method="GET">
        {params.map { case (name, value) =>
          <input type="hidden" name={name} value={value}/>
        }}
      </form>
      <script>document.getElementById('redirectForm').submit();</script>
    }.mkString

    form // TODO Sanitize the values?
  }

  def finish(sessionData: SessionData, params: Map[String, String]) = {
    val log = new BOTransactionLog(uuid = newUUID, provider = "AUTHORIZENET", direction = "IN",
      transaction = sessionData.transactionUuid.getOrElse("None"), log = mapToQueryString(params), step = TransactionStep.FINISH)
    EsClient.index(Settings.Mogopay.EsIndex, log, false)

    val paymentConfig = sessionData.paymentConfig.get
    val cbParam = paymentConfig.cbParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())

    val paymentRequest = sessionData.paymentRequest.get

    val amount = paymentRequest.amount.toString
    val currency: Int = paymentRequest.currency.numericCode
    val apiLoginID: String = cbParam("apiLoginID")
    val transactionKey: String = cbParam("transactionKey")

//    val query = Map(
//      "amount" -> amount,
//      "apiLoginID" -> apiLoginID,
//      "transactionKey" -> GlobalUtil.hideStringExceptLastN(transactionKey, 3),
//      "currency" -> currency,
//      "cc_holder" -> paymentRequest.holder,
//      "cc_number" -> GlobalUtil.hideStringExceptLastN(paymentRequest.ccNumber, 4, "X"),
//      "cc_cvv" -> paymentRequest.cvv,
//      "cc_expirationDate" -> paymentRequest.expirationDate
//    )
//    val log1 = new BOTransactionLog(uuid = newUUID, provider = "AUTHORIZENET", direction = "OUT",
//      transaction = transactionRequestUUID, log = GlobalUtil.mapToQueryString(query))
//    EsClient.index(Settings.Mogopay.EsIndex, log1, false)

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
      cardType = AuthorizeNetHandler.toCardType(params.getOrElse("CARTE", CreditCardType.CB.toString)),
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

    val anetTransactionId = params("x_trans_id")
    val gatewayData = s"$anetTransactionId"

    transactionHandler.finishPayment(sessionData.transactionUuid.getOrElse(""),
      if (status == PaymentStatus.COMPLETE) TransactionStatus.PAYMENT_CONFIRMED else TransactionStatus.PAYMENT_REFUSED,
      paymentResult,
      params("x_response_code"), sessionData.locale, Some(gatewayData))
    finishPayment(sessionData, paymentResult)
  }

  def cancel(sessionData: SessionData) = {
    val log = new BOTransactionLog(uuid = newUUID, provider = "AUTHORIZENET", direction = "IN",
      transaction = sessionData.transactionUuid.getOrElse("None"), log = "", step = TransactionStep.CANCEL)
    EsClient.index(Settings.Mogopay.EsIndex, log, false)

    val transaction: BOTransaction = EsClient.load[BOTransaction](Settings.Mogopay.EsIndex, sessionData.transactionUuid.getOrElse("???")).orNull
    val paymentResult = PaymentResult(
      newUUID, null, -1L, "", null, null, "", "", null, "", "", PaymentStatus.FAILED,
      transaction.errorCodeOrigin.getOrElse(""),
      transaction.errorMessageOrigin, "", "", Some(""), "")

    transactionHandler.finishPayment(sessionData.transactionUuid.getOrElse(""),
      TransactionStatus.PAYMENT_REFUSED, paymentResult, "", sessionData.locale)
    finishPayment(sessionData, paymentResult)
  }

  def refund(paymentConfig: PaymentConfig, boTx: BOTransaction, amount: java.math.BigDecimal): RefundResult = {
    import net.authorize.Environment
    import net.authorize.data.creditcard.CreditCard

    val cbParam = paymentConfig.cbParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())

    val apiLoginID = cbParam("apiLoginID")
    val transactionKey = cbParam("transactionKey")
    val merchant = Merchant.createMerchant(Environment.SANDBOX,
      apiLoginID, transactionKey)

    val anetTransactionId = boTx.gatewayData.getOrElse(throw new TransactionIdNotFoundException)
    val creditCard = CreditCard.createCreditCard()
    creditCard.setCreditCardNumber(boTx.creditCard.get.number.substring(9))

    val authCaptureTransaction = merchant.createAIMTransaction(
      TransactionType.CREDIT, amount)
    authCaptureTransaction.setTransactionId(anetTransactionId)
    authCaptureTransaction.setCreditCard(creditCard)

    val queryOUT = Map(
      "apiLoginID" -> apiLoginID,
      "transactionKey" -> transactionKey,
      "anetTransactionId" -> anetTransactionId,
      "creditCard" -> creditCard.getCreditCardNumber,
      "amount" -> amount
    )
    val logOUT = new BOTransactionLog(uuid = newUUID, provider = "AUTHORIZENET", direction = "OUT",
      transaction = boTx.uuid, log = GlobalUtil.mapToQueryString(queryOUT), step = TransactionStep.REFUND)
    EsClient.index(Settings.Mogopay.EsIndex, logOUT, false)

    val result: net.authorize.Result[Transaction] = merchant
      .postTransaction(authCaptureTransaction)
      .asInstanceOf[net.authorize.Result[Transaction]]

    val response = result.asInstanceOf[aim.Result[Transaction]]

    val responseMap = Map(
      "responseCode" -> response.getResponseCode,
      "getReasonResponseCode" -> response.getReasonResponseCode,
      "responseText" -> response.getResponseText
    )
    val logIN = new BOTransactionLog(uuid = newUUID, provider = "AUTHORIZENET", direction = "IN",
      transaction = boTx.uuid, log = GlobalUtil.mapToQueryString(responseMap), step = TransactionStep.REFUND)
    EsClient.index(Settings.Mogopay.EsIndex, logIN, false)

    val responseCode = response.getResponseCode.getCode
    val reasonResponseCode = response.getReasonResponseCode.getResponseReasonCode
    val status = if (responseCode == 1) PaymentStatus.REFUNDED else PaymentStatus.REFUND_FAILED
    RefundResult(status, s"$responseCode-$reasonResponseCode", Option(response.getResponseText))
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
    val `type` = if (xtype == null) "CB" else xtype.toUpperCase
    if (`type` == "VISA") {
      CreditCardType.VISA
    } else if (`type` == "AMEX") {
      CreditCardType.AMEX
    } else if (`type`.indexOf("MASTERCARD") >= 0) {
      CreditCardType.MASTER_CARD
    } else if (`type` == "VISA_ELECTRON") {
      CreditCardType.VISA
    } else {
      CreditCardType.CB
    }
  }
}
