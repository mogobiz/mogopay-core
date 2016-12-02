/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.payment

import java.net.URLDecoder
import java.util.{Date, Locale}
import javax.xml.datatype.{DatatypeFactory, XMLGregorianCalendar}

import akka.actor.ActorSystem
import akka.util.Timeout
import com.mogobiz.es.EsClient
import com.mogobiz.pay.codes.MogopayConstant
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.exceptions.Exceptions.{AccountDoesNotExistException, InvalidContextException, InvalidInputException, MogopayError}
import com.mogobiz.pay.model._
import com.mogobiz.system.ActorSystemLocator
import com.mogobiz.utils.GlobalUtil._
import com.mogobiz.utils.{CustomSslConfiguration, GlobalUtil}
import org.json4s.jackson.JsonMethods._
import spray.client.pipelining._
import spray.http.Uri.Query
import spray.http.{Uri, _}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util._

class PayPalHandler(handlerName: String) extends PaymentHandler with CustomSslConfiguration {
  PaymentHandler.register(handlerName, this)

  implicit val timeout: Timeout = 40.seconds

  // execution context for futures

  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  implicit val formats = new org.json4s.DefaultFormats {}

  val paymentType = PaymentType.PAYPAL

  /**
    * Return (Session, URL to redirect to)
    */
  /*
  def startPayment(sessionData: SessionData): Either[String, Uri] = {
    val (transactionUUID, vendor, paymentConfig, paymentRequest) = getContext(sessionData)
    val successURL                                               = Settings.Mogopay.EndPoint + s"paypal/success/${sessionData.uuid}"
    val failureURL                                               = Settings.Mogopay.EndPoint + s"paypal/fail/${sessionData.uuid}"
    val amount                                                   = sessionData.amount.get
    val ipAddress                                                = sessionData.ipAddress
    transactionHandler
      .startPayment(vendor, sessionData, transactionUUID, paymentRequest, PaymentType.PAYPAL, CBPaymentProvider.NONE)
    val maybeToken =
      getToken(transactionUUID, vendor, ipAddress, successURL, failureURL, paymentConfig, amount, paymentRequest)

    maybeToken map { token =>
      sessionData.token = maybeToken
      Right(Uri(Settings.PayPal.UrlExpresschout).withQuery(Map("cmd" -> "_express-checkout", "token" -> token)))
    } getOrElse (throw MogopayError(MogopayConstant.PaypalTokenError))
  }

  def validatePayment(transaction: BOTransaction, amount: Long): Option[ValidatePaymentResult] = {
    //TODO à implémenter
    None
  }

  def refundPayment(transaction: BOTransaction, amount: Long): Option[ValidatePaymentResult] = {
    //TODO à implémenter
    None
  }

  private def getToken(transactionUUID: String,
                       vendor: Account,
                       ipAddress: Option[String],
                       successURL: String,
                       failureURL: String,
                       paymentConfig: PaymentConfig,
                       amount: Long,
                       paymentRequest: PaymentRequest): Option[String] = {
    val parameters: Map[String, String] =
      paymentConfig.paypalParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())
    val user: String      = parameters.getOrElse("paypalUser", "")
    val password: String  = parameters.getOrElse("paypalPassword", "")
    val signature: String = parameters.getOrElse("paypalSignature", "")
    transactionHandler.updateStatus(transactionUUID, ipAddress, TransactionStatus.PAYMENT_REQUESTED)

    val amount2 = amount.toDouble / 100.0
    val query = Query("METHOD" -> "SetExpressCheckout",
                      "USER"                           -> user,
                      "PWD"                            -> password,
                      "SIGNATURE"                      -> signature,
                      "VERSION"                        -> Settings.PayPal.Version,
                      "PAYMENTREQUEST_0_PAYMENTACTION" -> "SALE",
                      "PAYMENTREQUEST_0_AMT"           -> String.format(Locale.US, "%5.2f%n", amount2.asInstanceOf[AnyRef]),
                      "PAYMENTREQUEST_0_CURRENCYCODE"  -> paymentRequest.currency.code,
                      "RETURNURL"                      -> successURL,
                      "CANCELURL"                      -> failureURL)
    val uri: Uri                       = Uri(Settings.PayPal.UrlNvpApi)
    val response: Future[HttpResponse] = sslPipeline(uri.authority.host).flatMap(_ (Get(uri.withQuery(query))))
    val tuples                         = fromHttResponse(response)
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
  }

  /*
   * Returns the redirection URL
   */
  def fail(sessionData: SessionData, tokenFromParams: String): Uri = {
    val transactionUuid = sessionData.transactionUuid.orNull
    val token           = sessionData.token.get
    if (token != tokenFromParams) {
      throw InvalidContextException(s"$tokenFromParams unknown")
    } else {
      val paymentResult = PaymentResult("",
                                        new Date,
                                        sessionData.amount.get,
                                        "",
                                        CreditCardType.OTHER,
                                        new Date,
                                        "",
                                        transactionUuid,
                                        new Date,
                                        "",
                                        "",
                                        PaymentStatus.FAILED,
                                        "",
                                        Some(""),
                                        "",
                                        "",
                                        Some(""),
                                        token,
                                        None)
      val paymentResultWithShippingResult = transactionHandler.finishPayment(this,
                                                                             sessionData,
                                                                             transactionUuid,
                                                                             TransactionStatus.PAYMENT_REFUSED,
                                                                             paymentResult,
                                                                             sessionData.locale)
      finishPayment(sessionData, paymentResultWithShippingResult)
    }
  }

  def success(sessionData: SessionData, tokenFromParams: String): Uri = {
    val paymentConfig: PaymentConfig = sessionData.paymentConfig.get
    val token                        = sessionData.token.get

    if (token != tokenFromParams) {
      throw InvalidInputException(s"$tokenFromParams")
    } else {
      val transactionUUID = sessionData.transactionUuid.get
      val vendorId        = sessionData.merchantId.get
      val paymentRequest  = sessionData.paymentRequest.get

      val maybePayerId = getPayerId(token, paymentConfig)
      maybePayerId match {
        case None | Some("") =>
          throw MogopayError(MogopayConstant.PaypalPayerIdError)
        case Some(payerId) =>
          if (paymentConfig == null) {
            throw MogopayError(MogopayConstant.InvalidPaypalConfig)
          } else {
            val paymentResult = submit(vendorId,
                                       transactionUUID,
                                       paymentConfig,
                                       paymentRequest,
                                       token,
                                       payerId,
                                       sessionData,
                                       TransactionStep.SUCCESS)
            finishPayment(sessionData, paymentResult)
          }
      }
    }
  }

  private def getPayerId(token: String, paymentConfig: PaymentConfig): Option[String] = {
    val parameters: Map[String, String] =
      paymentConfig.paypalParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())

    val user      = parameters("paypalUser").toString
    val password  = parameters("paypalPassword").toString
    val signature = parameters("paypalSignature").toString

    val query = Query("METHOD" -> "GetExpressCheckoutDetails",
                      "USER"      -> user,
                      "PWD"       -> password,
                      "SIGNATURE" -> signature,
                      "VERSION"   -> Settings.PayPal.Version,
                      "TOKEN"     -> token)
    val response: Future[HttpResponse] = pipeline(Get(Uri(Settings.PayPal.UrlNvpApi).withQuery(query)))
    val tuples                         = GlobalUtil.fromHttResponse(response)
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

  protected def submit(vendorId: String,
                       transactionUUID: String,
                       paymentConfig: PaymentConfig,
                       paymentRequest: PaymentRequest,
                       token: String,
                       payerId: String,
                       sessionData: SessionData,
                       step: TransactionStep): PaymentResult = {
    accountHandler
      .load(vendorId)
      .map { account =>
        val parameters                 = paymentConfig.paypalParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())
        val transaction: BOTransaction = EsClient.load[BOTransaction](Settings.Mogopay.EsIndex, transactionUUID).orNull
        val user: String               = parameters("paypalUser")
        val password                   = parameters("paypalPassword")
        val signature                  = parameters("paypalSignature")

        val paymentResult = PaymentResult(
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
            errorCodeOrigin = null,
            errorMessageOrigin = None,
            data = null,
            bankErrorCode = null,
            bankErrorMessage = None,
            token = token,
            errorShipment = None
        )

        val amount = paymentRequest.amount.toDouble / 100.0
        val paramMap = Map("USER" -> user,
                           "PWD"                            -> password,
                           "SIGNATURE"                      -> signature,
                           "VERSION"                        -> Settings.PayPal.Version,
                           "METHOD"                         -> "DoExpressCheckoutPayment",
                           "PAYMENTREQUEST_0_PAYMENTACTION" -> "SALE",
                           "PAYMENTREQUEST_0_AMT"           -> String.format(Locale.US, "%5.2f%n", amount.asInstanceOf[AnyRef]),
                           "TOKEN"                          -> token,
                           "PAYERID"                        -> payerId,
                           "PAYMENTREQUEST_0_CURRENCYCODE"  -> paymentRequest.currency.code)

        val bot1 = BOTransactionLog(
            uuid = newUUID,
            direction = "OUT",
            log = paramMap.map(t => t._1 + "=" + t._2).reduce(_ + "&" + _),
            provider = "PAYPAL",
            transaction = transaction.uuid,
            step = step
        )
        boTransactionLogHandler.save(bot1)

        val response: Future[HttpResponse] = pipeline(Get(Uri(Settings.PayPal.UrlNvpApi).withQuery(Query(paramMap))))
        val tuples                         = fromHttResponse(response)
        val res = tuples map { tuples =>
          val bot2 = BOTransactionLog(
              uuid = newUUID,
              direction = "IN",
              log = tuples.map(t => t._1 + "=" + t._2).reduce(_ + "&" + _),
              provider = "PAYPAL",
              transaction = transaction.uuid,
              step = step
          )
          boTransactionLogHandler.save(bot2)
          val ack = tuples("ACK")
          if (ack.equals("Success") || ack.equals("SuccessWithWarning")) {
            val transactionId = tuples.get("PAYMENTINFO_0_TRANSACTIONID").orNull
            val cal: XMLGregorianCalendar = DatatypeFactory
              .newInstance()
              .newXMLGregorianCalendar(URLDecoder.decode(tuples.get("PAYMENTINFO_0_ORDERTIME").orNull, "UTF-8"))
            val c2 = cal.toGregorianCalendar
            val updatedPaymentResult = paymentResult.copy(
                errorCodeOrigin = ack,
                status = PaymentStatus.COMPLETE,
                transactionDate = c2.getTime(),
                gatewayTransactionId = transactionId,
                transactionCertificate = null
            )
            transactionHandler.finishPayment(this,
                                             sessionData,
                                             transactionUUID,
                                             TransactionStatus.PAYMENT_CONFIRMED,
                                             paymentResult,
                                             sessionData.locale)
          } else {
            val errorCode        = tuples.get("L_ERRORCODE0").orNull
            val errorCodeMessage = URLDecoder.decode(tuples.get("L_SHORTMESSAGE0").orNull, "UTF-8")

            val updatedPaymentResult = paymentResult.copy(
                status = PaymentStatus.FAILED,
                errorCodeOrigin = errorCode,
                errorMessageOrigin = Option(errorCodeMessage)
            )

            transactionHandler.finishPayment(this,
                                             sessionData,
                                             transactionUUID,
                                             TransactionStatus.PAYMENT_REFUSED,
                                             paymentResult,
                                             null,
                                             sessionData.locale)
          }
        }
        import scala.concurrent.duration._
        Await.result(res, 30 seconds)
      }
      .getOrElse {
        throw AccountDoesNotExistException("")
      }
  }

  override def refund(paymentConfig: PaymentConfig,
                      boTx: BOTransaction,
                      amount: Long,
                      paymentResult: PaymentResult): RefundResult = {
    val parameters: Map[String, String] =
      paymentConfig.paypalParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())

    val user      = parameters("paypalUser").toString
    val password  = parameters("paypalPassword").toString
    val signature = parameters("paypalSignature").toString

    val query = Query("METHOD" -> "RefundTransaction",
                      "TRANSACTIONID" -> paymentResult.gatewayTransactionId,
                      "REFUNDTYPE"    -> "Full")
    val response: Future[HttpResponse] = pipeline(Get(Uri(Settings.PayPal.UrlNvpApi).withQuery(query)))
    val tuples                         = GlobalUtil.fromHttResponse(response)
    val res = tuples map { tuples =>
      tuples.get("ACK") flatMap { ack =>
        if (ack.equals("Success") || ack.equals("SuccessWithWarning")) {
          val refundTrId = tuples("REFUNDTRANSACTIONID")
          Some(URLDecoder.decode(refundTrId, "UTF-8"))
        } else None
      }
    }
    import scala.concurrent.duration._

    val result = Await.result(res, 30 seconds)
    val status = if (result.isDefined) PaymentStatus.REFUNDED else PaymentStatus.REFUND_FAILED
    RefundResult(status, result.getOrElse(""), None)
  }
  */

  override def startPayment(sessionData: SessionData): Either[FormRedirection, Uri] = throw new Exception("Not implemented")

}

object Test extends App with CustomSslConfiguration {
  implicit val timeout: Timeout = 40.seconds
  ActorSystemLocator(ActorSystem("Test"))
  implicit val _ = ActorSystemLocator().dispatcher
  val uri: Uri   = Uri("https://api-3t.sandbox.paypal.com/nvp")
  val response: Future[HttpResponse] = sslPipeline(uri.authority.host).flatMap(
      _ (Get("https://api-3t.sandbox.paypal.com/nvp?METHOD=SetExpressCheckout&USER=hayssams-facilitator_api1.yahoo.com&PWD=1365940711&SIGNATURE=An5ns1Kso7MWUdW4ErQKJJJ4qi4-AIvKXMZ8RRQl6BBiVO5ISM9ECdEG&VERSION=78&PAYMENTREQUEST_0_PAYMENTACTION=SALE&PAYMENTREQUEST_0_AMT=27.50%0A&PAYMENTREQUEST_0_CURRENCYCODE=EUR&RETURNURL=http://mogobiz.ebiznext.com:80/api/pay/paypal/success/23601e5c-f921-4c09-8c16-d73c12fbfd38&CANCELURL=http://mogobiz.ebiznext.com:80/api/pay/paypal/fail/23601e5c-f921-4c09-8c16-d73c12fbfd38")))
  response.foreach(x => println(x.entity.toString))
}
