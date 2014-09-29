package mogopay.handlers.payment

import mogopay.codes.MogopayConstant
import mogopay.model.Mogopay._
import mogopay.util.GlobalUtil
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
