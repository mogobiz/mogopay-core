package mogopay.handlers.payment

import mogopay.model.Mogopay._
import mogopay.codes.MogopayConstant
import mogopay.util.GlobalUtil._
import mogopay.config.Implicits._
import spray.http.Uri
import spray.http.Uri.{Authority, Path, Query}

class PaymentHandler {
  /**
   * Returns the redirection page's URL
   */
  protected def finishPayment(sessionData: SessionData,
                              paymentResult: PaymentResult): Uri = {
    val errorURL = sessionData.errorURL.getOrElse("")
    val successURL = sessionData.successURL.getOrElse("")
    val transactionUUID = sessionData.transactionUuid.getOrElse("")
    val success = paymentResult.status == PaymentStatus.COMPLETE

    val query = Query(
      "result" -> (if (success) MogopayConstant.Success else MogopayConstant.Error),
      "transaction_id" -> transactionUUID,
      "transaction_type" -> "CREDIT_CARD",
      "error_code_bank" -> paymentResult.bankErrorCode,
      "error_message_bank" -> paymentResult.bankErrorMessage.getOrElse(""),
      "error_code_provider" -> paymentResult.errorCodeOrigin,
      "error_message_provider" -> paymentResult.errorMessageOrigin.getOrElse("")
    )
    sessionData.finished = true
    val redirectTo = if (success) successURL else errorURL
    Uri(redirectTo).withQuery(query)
  }
}
