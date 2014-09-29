package mogopay.util

import java.net.URLEncoder

import mogopay.handlers.payment.BankErrorCodes
import mogopay.model.Mogopay.{PaymentResult, TransactionStatus, PaymentStatus}
import mogopay.model.Mogopay.TransactionStatus.TransactionStatus
import mogopay.model.Mogopay.PaymentStatus.PaymentStatus
import spray.http.HttpResponse

import scala.concurrent.{ExecutionContext, Future}

object GlobalUtil {
  def now = new java.util.Date()

  def newUUID = java.util.UUID.randomUUID().toString

  // From: http://stackoverflow.com/a/1227643/604041
  def caseClassToMap(cc: AnyRef) =
    (Map[String, Any]() /: cc.getClass.getDeclaredFields) { (a, f) =>
      f.setAccessible(true)
      a + (f.getName -> f.get(cc))
    }

  def mapToQueryString(m: Map[String, Any]): String = {
    mapToQueryString(m.toList)
  }

  def mapToQueryStringNoEncode(m: Map[String, Any]): String = {
    mapToQueryStringNoEncode(m.toList)
  }

  def mapToQueryString(m: List[(String, Any)]): String = {
    m.map { case (k, v) =>
      println(s"$k=$v")
      s"$k=" + URLEncoder.encode(if (v == null) "" else v.toString, "UTF-8")
    }.mkString("&")
  }

  def mapToQueryStringNoEncode(m: List[(String, Any)]): String = {
    m.map { case (k, v) =>
      println(s"$k=$v")
      s"$k=$v"
    }.mkString("&")
  }

  def fromHttResponse(response: Future[HttpResponse])(implicit ev: ExecutionContext): Future[Map[String, String]] = {
    response map { response =>
      val data = response.entity.asString.trim
      println(s"data $data")
      val pairs = data.split('&')
      val tuples = (pairs map { pair =>
        val tab = pair.split('=')
        tab(0) -> (if (tab.length == 1) "" else tab(1))
      }).toMap
      tuples
    }
  }

  def computeTransactionStatus(paymentStatus: PaymentStatus): TransactionStatus = {
    paymentStatus match {
      case PaymentStatus.CANCEL_FAILED => TransactionStatus.CANCEL_FAILED
      case PaymentStatus.CANCELED => TransactionStatus.CANCEL_CONFIRMED
      case PaymentStatus.COMPLETE => TransactionStatus.PAYMENT_CONFIRMED
      case PaymentStatus.FAILED => TransactionStatus.PAYMENT_REFUSED
      case PaymentStatus.INVALID => TransactionStatus.PAYMENT_REFUSED
      case PaymentStatus.PENDING => TransactionStatus.PAYMENT_REQUESTED
      case _ => null
    }
  }

  def createThreeDSNotEnrolledResult(): PaymentResult = {
    PaymentResult(
      transactionSequence = newUUID,
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