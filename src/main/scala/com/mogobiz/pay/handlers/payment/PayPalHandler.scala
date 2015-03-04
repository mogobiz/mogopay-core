package com.mogobiz.pay.handlers.payment

import java.net.{URLDecoder}
import java.util.{Date, Locale}
import javax.xml.datatype.{DatatypeFactory, XMLGregorianCalendar}

import akka.actor.ActorSystem
import com.mogobiz.pay.codes.MogopayConstant
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.es.EsClient
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.exceptions.Exceptions.{InvalidInputException, InvalidContextException, AccountDoesNotExistException, MogopayError}
import com.mogobiz.pay.implicits.Implicits
import com.mogobiz.utils.GlobalUtil
import com.mogobiz.utils.GlobalUtil._
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.session.Session
import org.json4s.jackson.JsonMethods._
import Implicits._
import spray.http.Uri
import spray.http.Uri.Query

import scala.collection.JavaConverters._
import scala.concurrent.{Await, Future}
import scala.util._
import spray.http._
import spray.client.pipelining._


class PayPalHandler(handlerName: String) extends PaymentHandler {
  PaymentHandler.register(handlerName, this)
  implicit val system = ActorSystem()

  import system.dispatcher

  // execution context for futures

  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  implicit val formats = new org.json4s.DefaultFormats {}

  /**
   * Return (Session, URL to redirect to)
   */
  def startPayment(sessionData: SessionData): Either[String, Uri] = {
    val paymentRequest = sessionData.paymentRequest.get
    val vendorId = sessionData.merchantId.get
    val successURL = Settings.Mogopay.EndPoint + "paypal/success"
    val failureURL = Settings.Mogopay.EndPoint + "paypal/fail"
    val paymentConfig = sessionData.paymentConfig.get
    val amount = sessionData.amount.get
    val maybeToken = getToken(vendorId, successURL, failureURL, paymentConfig, amount, paymentRequest)

    maybeToken map { token =>
      sessionData.token = maybeToken
      Right(Uri(Settings.PayPal.UrlExpresschout).withQuery(Map("cmd" -> "_express-checkout", "token" -> token)))
    } getOrElse (throw MogopayError(MogopayConstant.PaypalTokenError))
  }

  private def getToken(vendorId: String, successURL: String, failureURL: String,
                       paymentConfig: PaymentConfig, amount: Long,
                       paymentRequest: PaymentRequest): Option[String] = {
    accountHandler.load(vendorId) map { vendor =>
      val parameters: Map[String, String] = paymentConfig.paypalParam
        .map(parse(_).extract[Map[String, String]])
        .getOrElse(Map())
      val user: String = parameters.getOrElse("paypalUser", "")
      val password: String = parameters.getOrElse("paypalPassword", "")
      val signature: String = parameters.getOrElse("paypalSignature", "")

      val amount2 = amount.toDouble / 100.0
      val query = Query(
        "METHOD" -> "SetExpressCheckout",
        "USER" -> user,
        "PWD" -> password,
        "SIGNATURE" -> signature,
        "VERSION" -> Settings.PayPal.Version,
        "PAYMENTREQUEST_0_PAYMENTACTION" -> "SALE",
        "PAYMENTREQUEST_0_AMT" -> String.format(Locale.US, "%5.2f%n", amount2.asInstanceOf[AnyRef]),
        "PAYMENTREQUEST_0_CURRENCYCODE" -> paymentRequest.currency.code,
        "RETURNURL" -> successURL,
        "CANCELURL" -> failureURL)
      val response: Future[HttpResponse] = pipeline(Get(Uri(Settings.PayPal.UrlNvpApi).withQuery(query)))
      val tuples = fromHttResponse(response)
      val res = tuples map { tuples =>
        tuples.get("ACK") flatMap { ack =>
          if (ack.equals("Success") || ack.equals("SuccessWithWarning")) {
            val token = tuples("TOKEN")
            Some(URLDecoder.decode(token, "UTF-8"))
          } else None
        }
      }
      import scala.concurrent.duration._
      val result = Await.result(res, 30 seconds)
      result
    } getOrElse (throw AccountDoesNotExistException(""))
  }

  /*
   * Returns the redirection URL
   */
  def fail(sessionData: SessionData, tokenFromParams: String): Uri = {
    val transactionUuid = sessionData.transactionUuid.orNull
    val token = sessionData.token.get
    if (token != tokenFromParams) {
      throw InvalidContextException(s"$tokenFromParams unknown")
    } else {
      val pr = PaymentResult("", new Date, sessionData.amount.get, "", CreditCardType.OTHER, new Date, "", transactionUuid, new Date,
        "", "", PaymentStatus.FAILED, "", Some(""), "", "", Some(""), token)
      finishPayment(sessionData, pr)
    }
  }

  def success(sessionData: SessionData, tokenFromParams: String): Uri = {
    val paymentConfig: PaymentConfig = sessionData.paymentConfig.get
    val token = sessionData.token.get

    if (token != tokenFromParams) {
      throw InvalidInputException(s"$tokenFromParams")
    } else {
      val transactionUUID = sessionData.transactionUuid.get
      val vendorId = sessionData.merchantId.get
      val paymentRequest = sessionData.paymentRequest.get

      val maybePayerId = getPayerId(token, paymentConfig)
      maybePayerId match {
        case None | Some("") =>
          throw MogopayError(MogopayConstant.PaypalPayerIdError)
        case Some(payerId) =>
          if (paymentConfig == null) {
            throw MogopayError(MogopayConstant.InvalidPaypalConfig)
          } else {
            transactionHandler.startPayment(vendorId, transactionUUID, paymentRequest, PaymentType.PAYPAL, CBPaymentProvider.NONE)
            val paymentResult = submit(vendorId, transactionUUID, paymentConfig, paymentRequest, token, payerId)
            finishPayment(sessionData, paymentResult)
          }
      }
    }
  }


  private def getPayerId(token: String, paymentConfig: PaymentConfig): Option[String] = {
    val parameters: Map[String, String] =
      paymentConfig.paypalParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())

    val user = parameters("paypalUser").toString
    val password = parameters("paypalPassword").toString
    val signature = parameters("paypalSignature").toString

    val query = Query(
      "METHOD" -> "GetExpressCheckoutDetails",
      "USER" -> user,
      "PWD" -> password,
      "SIGNATURE" -> signature,
      "VERSION" -> Settings.PayPal.Version,
      "TOKEN" -> token)
    val response: Future[HttpResponse] = pipeline(Get(Uri(Settings.PayPal.UrlNvpApi).withQuery(query)))
    val tuples = GlobalUtil.fromHttResponse(response)
    val res = tuples map { tuples =>
      tuples.get("ACK") flatMap { ack =>
        if (ack.equals("Success") || ack.equals("SuccessWithWarning")) {
          val payerId = tuples("PAYERID")
          Some(URLDecoder.decode(payerId, "UTF-8"))
        } else None
      }
    }
    import scala.concurrent.duration._

    val result = Await.result(res, 30 seconds)
    result
  }

  private def submit(vendorId: String, transactionUUID: String, paymentConfig: PaymentConfig,
                     infosPaiement: PaymentRequest, token: String, payerId: String): PaymentResult = {
    accountHandler.load(vendorId).map {
      account =>
        val parameters = paymentConfig.paypalParam.map(parse(_).extract[Map[String, String]])
          .getOrElse(Map())
        transactionHandler.updateStatus(vendorId, transactionUUID, null, TransactionStatus.PAYMENT_REQUESTED, null)
        val transaction: BOTransaction = EsClient.load[BOTransaction](Settings.Mogopay.EsIndex, transactionUUID).orNull
        val user: String = parameters("paypalUser")
        val password = parameters("paypalPassword")
        val signature = parameters("paypalSignature")

        val paymentResult = PaymentResult(
          transactionSequence = infosPaiement.transactionSequence,
          orderDate = infosPaiement.orderDate,
          amount = infosPaiement.amount,
          ccNumber = infosPaiement.ccNumber,
          cardType = infosPaiement.cardType,
          expirationDate = infosPaiement.expirationDate,
          cvv = infosPaiement.cvv,
          gatewayTransactionId = transactionUUID,
          transactionDate = null,
          transactionCertificate = null,
          authorizationId = null,
          status = null,
          errorCodeOrigin = null,
          errorMessageOrigin = None,
          data = null,
          bankErrorCode = null,
          bankErrorMessage = None,
          token = token
        )

        val amount = infosPaiement.amount.toDouble / 100.0
        val paramMap = Map(
          "USER" -> user,
          "PWD" -> password,
          "SIGNATURE" -> signature,
          "VERSION" -> Settings.PayPal.Version,
          "METHOD" -> "DoExpressCheckoutPayment",
          "PAYMENTREQUEST_0_PAYMENTACTION" -> "SALE",
          "PAYMENTREQUEST_0_AMT" -> String.format(Locale.US, "%5.2f%n", amount.asInstanceOf[AnyRef]),
          "TOKEN" -> token,
          "PAYERID" -> payerId,
          "PAYMENTREQUEST_0_CURRENCYCODE" -> infosPaiement.currency.code)

        val bot1 = BOTransactionLog(
          uuid = newUUID,
          direction = "OUT",
          log = paramMap.map(t => t._1 + "=" + t._2).reduce(_ + "&" + _),
          provider = "PAYPAL",
          transaction = transaction.uuid
        )
        boTransactionLogHandler.save(bot1)

        val response: Future[HttpResponse] = pipeline(Get(Uri(Settings.PayPal.UrlNvpApi).withQuery(Query(paramMap))))
        val tuples = fromHttResponse(response)
        val res = tuples map { tuples =>
          val bot2 = BOTransactionLog(
            uuid = newUUID,
            direction = "IN",
            log = tuples.map(t => t._1 + "=" + t._2).reduce(_ + "&" + _),
            provider = "PAYPAL",
            transaction = transaction.uuid
          )
          boTransactionLogHandler.save(bot2)
          val ack = tuples("ACK")
          if (ack.equals("Success") || ack.equals("SuccessWithWarning")) {
            val transactionId = tuples.get("PAYMENTINFO_0_TRANSACTIONID").orNull
            val cal: XMLGregorianCalendar = DatatypeFactory.newInstance()
              .newXMLGregorianCalendar(URLDecoder.decode(tuples.get("PAYMENTINFO_0_ORDERTIME").orNull, "UTF-8"))
            val c2 = cal.toGregorianCalendar
            val updatedPaymentResult = paymentResult.copy(
              status = PaymentStatus.COMPLETE,
              transactionDate = c2.getTime(),
              gatewayTransactionId = transactionId,
              transactionCertificate = null
            )
            transactionHandler.finishPayment(vendorId, transactionUUID, TransactionStatus.PAYMENT_CONFIRMED, paymentResult, ack)
            updatedPaymentResult
          } else {
            val errorCode = tuples.get("L_ERRORCODE0").orNull
            val errorCodeMessage = URLDecoder.decode(tuples.get("L_SHORTMESSAGE0").orNull, "UTF-8")

            val updatedPaymentResult = paymentResult.copy(
              status = PaymentStatus.FAILED,
              errorCodeOrigin = errorCode,
              errorMessageOrigin = Option(errorCodeMessage)
            )

            transactionHandler.finishPayment(vendorId, transactionUUID, TransactionStatus.PAYMENT_REFUSED,
              paymentResult, null)

            updatedPaymentResult
          }
        }
        import scala.concurrent.duration._
        Await.result(res, 30 seconds)
    }.getOrElse {
      throw AccountDoesNotExistException("")
    }
  }
}
