package com.mogobiz.pay.handlers.payment

import com.mogobiz.pay.codes.MogopayConstant
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.utils.GlobalUtil
import spray.http.Uri
import spray.http.Uri.Query

import scala.collection.mutable

trait PaymentHandler {
  /**
   * Returns the redirection page's URL
   */
  protected def finishPayment(sessionData: SessionData,
                              paymentResult: PaymentResult): Uri = {
    val errorURL = sessionData.errorURL.getOrElse("")
    val successURL = sessionData.successURL.getOrElse("")
    val transactionUUID = sessionData.transactionUuid.getOrElse("")
    val transactionSequence = if (sessionData.paymentRequest.isDefined) sessionData.paymentRequest.get.transactionSequence else ""
    val success = paymentResult.status == PaymentStatus.COMPLETE

    val query = Query(
      "result" -> (if (success) MogopayConstant.Success else MogopayConstant.Error),
      "transaction_id" -> transactionUUID,
      "transaction_sequence" -> transactionSequence,
      "transaction_type" -> "CREDIT_CARD",
      "error_code_bank" -> paymentResult.bankErrorCode,
      "error_message_bank" -> paymentResult.bankErrorMessage.getOrElse(""),
      "error_code_provider" -> paymentResult.errorCodeOrigin,
      "error_message_provider" -> paymentResult.errorMessageOrigin.getOrElse("")
    )
    sessionData.finished = true
    val redirectTo = if (success) successURL else errorURL
    val sep = if (redirectTo.indexOf('?') > 0) "&" else "?"
    Uri(redirectTo + sep + GlobalUtil.mapToQueryString(query.toMap))
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
