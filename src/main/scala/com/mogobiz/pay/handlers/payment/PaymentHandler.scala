/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.payment

import java.util.{ Date, UUID }

import akka.actor.Props
import com.mogobiz.pay.codes.MogopayConstant
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.config.{ Environment, Settings }
import com.mogobiz.pay.exceptions.Exceptions._
import com.mogobiz.pay.handlers.EmailHandler.Mail
import com.mogobiz.pay.handlers.EmailingActor
import com.mogobiz.pay.handlers.shipping.ShippingHandler
import com.mogobiz.pay.model.Mogopay.PaymentType.PaymentType
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.pay.model.ParamRequest
import com.mogobiz.system.ActorSystemLocator
import com.mogobiz.utils.{ GlobalUtil, SymmetricCrypt }
import org.apache.commons.lang.LocaleUtils
import spray.http.Uri
import spray.http.Uri.Query

import scala.collection.mutable
import scala.util.{ Failure, Success, Try }

trait PaymentHandler {
  implicit val system = ActorSystemLocator()
  implicit val _ = system.dispatcher

  def paymentType: PaymentType

  def refund(paymentConfig: PaymentConfig, boTx: BOTransaction, amount: Long, paymentResult: PaymentResult): RefundResult
  /**
   * Returns the redirection page's URL
   */
  protected def finishPayment(sessionData: SessionData, paymentResult: PaymentResult): Uri = {
    val errorURL = sessionData.errorURL.getOrElse("")
    val successURL = sessionData.successURL.getOrElse("")
    val transactionUUID = sessionData.transactionUuid.getOrElse("")
    val transactionSequence = if (sessionData.paymentRequest.isDefined) sessionData.paymentRequest.get.transactionSequence else ""
    val success = paymentResult.status == PaymentStatus.COMPLETE

    val errorShipment = if (success) {
      val transaction = boTransactionHandler.find(transactionUUID)
        .getOrElse(throw BOTransactionNotFoundException(s"$transactionUUID"))

      Try(ShippingHandler.confirmShippingPrice(sessionData.selectShippingPrice)) match {
        case Success(_) => None
        case Failure(f) => {
          val newTx = transaction.copy(
            errorCodeOrigin = Option("SHIPMENT_ERROR"),
            errorMessageOrigin = Some(f.getMessage)
          )
          boTransactionHandler.update(newTx, false)
          //TODO faire l'appel à l'annulation du paiement
          refund(sessionData.paymentConfig.get, newTx, sessionData.amount.get, paymentResult)
          Some(f.getMessage)
        }
      }
    } else None

    val query = Query(
      "result" -> (if (success && errorShipment.isEmpty) MogopayConstant.Success else MogopayConstant.Error),
      "transaction_id" -> transactionUUID,
      "transaction_sequence" -> transactionSequence,
      "transaction_type" -> paymentType.toString,
      "error_code_bank" -> paymentResult.bankErrorCode,
      "error_message_bank" -> paymentResult.bankErrorMessage.getOrElse(""),
      "error_code_provider" -> paymentResult.errorCodeOrigin,
      "error_message_provider" -> paymentResult.errorMessageOrigin.getOrElse(""),
      "error_shipment" -> errorShipment.getOrElse("")
    )

    if (success && sessionData.payers.nonEmpty) {
      val payers = sessionData.payers
      val tx = boTransactionHandler.find(transactionUUID).getOrElse(throw new BOTransactionNotFoundException(transactionUUID))
      val merchantUUID = tx.vendor.getOrElse(throw new VendorNotFoundException).uuid
      val paymentConfig = accountHandler.find(merchantUUID)
        .getOrElse(throw new VendorNotFoundException())
        .paymentConfig
        .getOrElse(throw new PaymentConfigNotFoundException())
      val firstPayer: Account = tx.customer.getOrElse(throw new NoCustomerSetForTheBOTrasaction)
      handleGroupPayment(payers, tx, merchantUUID, paymentConfig, firstPayer, sessionData.locale)
    }

    sessionData.finished = true
    val redirectTo = if (success) successURL else errorURL
    val sep = if (redirectTo.indexOf('?') > 0) "&" else "?"
    Uri(redirectTo + sep + GlobalUtil.mapToQueryString(query.toMap))
  }

  private def handleGroupPayment(payers: Map[String, Long], firstPayerBOTx: BOTransaction, merchantId: String,
    paymentConfig: PaymentConfig, firstPayer: Account, locale: Option[String]): Unit = if (payers.size > 1) {
    val groupTxUUID = firstPayerBOTx.uuid
    boTransactionHandler.update(firstPayerBOTx.copy(groupTransactionUUID = Some(groupTxUUID)), refresh = false)

    payers.filter(_._1 != firstPayer.email).foreach {
      case (email, amount) =>
        val account = accountHandler.findByEmail(email, Some(merchantId)).getOrElse {
          val newAccount = Account(
            uuid = UUID.randomUUID().toString,
            email = email,
            password = null,
            owner = Some(merchantId),
            secret = "",
            status = AccountStatus.INACTIVE
          )
          accountHandler.save(newAccount, false)
          newAccount
        }

        val merchant = accountHandler.find(merchantId).get
        val params = ParamRequest.TransactionInit(merchant.secret, amount, None,
          firstPayerBOTx.groupPaymentExpirationDate, Some(firstPayerBOTx.groupPaymentRefundPercentage))
        val txReq = transactionHandler.createTxReqForInit(merchant, params, firstPayerBOTx.currency, Some(groupTxUUID),
          firstPayerBOTx.groupPaymentExpirationDate, Some(firstPayerBOTx.groupPaymentRefundPercentage))
        transactionRequestHandler.save(txReq, refresh = false)

        val groupPaymentInfo = paymentConfig.groupPaymentInfo.getOrElse(throw new NoGroupPaymentInfoSpecifiedException)

        val token = {
          val expirationDate = firstPayerBOTx.groupPaymentExpirationDate.getOrElse(throw NoExpirationTimeSpecifiedException())
          val expirationTime: Long = new Date((new Date).getTime + expirationDate).getTime
          val clearToken = s"$expirationTime|${txReq.uuid}|${account.uuid}|$groupTxUUID|${groupPaymentInfo.successURL}|${groupPaymentInfo.failureURL}"
          SymmetricCrypt.encrypt(clearToken, Settings.Mogopay.Secret, "AES")
        }

        if (Settings.Env == Environment.DEV) println(s"==== Group payment token: $token")

        val url = groupPaymentInfo.returnURLforNextPayers
        val uri = Uri(url).withQuery(("token", token))

        def sendEmail() {
          val merchant = accountHandler.find(merchantId).getOrElse(throw new VendorNotFoundException())
          val paymentConfig = merchant.paymentConfig.getOrElse(throw new PaymentConfigNotFoundException())

          val template = templateHandler.loadTemplateByVendor(Option(merchant), "mail-group-payment", locale)

          val country = firstPayer.country.getOrElse(throw new NoCountrySpecifiedException).code.toLowerCase
          val jsonTx = BOTransactionJsonTransform.transform(firstPayerBOTx, LocaleUtils.toLocale(country))

          val amount = rateHandler.format(txReq.amount.toFloat / 100, txReq.currency.code, country).getOrElse(
            throw new RateNotFoundException(txReq.currency.code))
          val payerName = firstPayer.firstName.getOrElse(firstPayer.lastName.getOrElse(firstPayer.email))
          val data =
            s"""
              |{
              |  "firstPayer":  "$payerName",
              |  "url":         "$uri",
              |  "amount":      "$amount",
              |  "transaction": $jsonTx
              |}
              |""".stripMargin
          val (subject, body) = templateHandler.mustache(template, data)

          val senderName = merchant.paymentConfig.get.senderName
          val senderEmail = merchant.paymentConfig.get.senderEmail

          val emailingActor = system.actorOf(Props[EmailingActor])
          emailingActor ! Mail(
            from = (senderEmail.getOrElse(""), senderName.get),
            to = Seq(account.email),
            subject = subject,
            message = body)
        }
        sendEmail()
    }
  }

  def startPayment(sessionData: SessionData): Either[String, Uri]

  def createThreeDSNotEnrolledResult(): PaymentResult = {
    PaymentResult(
      transactionSequence = GlobalUtil.newUUID,
      orderDate = null,
      amount = -1L,
      ccNumber = "",
      cardType = null,
      expirationDate = null,
      cvv = "",
      gatewayTransactionId = "",
      transactionDate = null,
      transactionCertificate = "",
      authorizationId = "",
      status = PaymentStatus.FAILED,
      errorCodeOrigin = "12",
      errorMessageOrigin = Some("ThreeDSecure required"),
      data = "",
      bankErrorCode = "12",
      bankErrorMessage = Some(BankErrorCodes.getErrorMessage("12")),
      token = ""
    )
  }
}

object PaymentHandler {
  private val handlers = mutable.Map[String, PaymentHandler]()

  def register(handler: (String, PaymentHandler)): Unit = {
    handlers.put(handler._1, handler._2)
  }

  def apply(handlerName: String): PaymentHandler = {
    handlers(handlerName)
  }

}
