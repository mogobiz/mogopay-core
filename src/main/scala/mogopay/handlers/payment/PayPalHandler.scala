package mogopay.handlers.payment

import java.net.{URLDecoder}
import java.util.{Date, Locale}
import javax.xml.datatype.{DatatypeFactory, XMLGregorianCalendar}

import akka.actor.ActorSystem
import mogopay.codes.MogopayConstant
import mogopay.config.HandlersConfig._
import mogopay.config.Settings
import mogopay.es.EsClient
import mogopay.exceptions.Exceptions.{AccountDoesNotExistError, MogopayError}
import mogopay.util.GlobalUtil
import mogopay.util.GlobalUtil._
import mogopay.model.Mogopay._
import mogopay.session.Session
import org.json4s.jackson.JsonMethods._
import mogopay.config.Implicits._
import spray.http.Uri
import spray.http.Uri.Query

import scala.collection.JavaConverters._
import scala.concurrent.{Await, Future}
import scala.util._
import spray.http._
import spray.client.pipelining._


class PayPalHandler(handlerName:String) extends PaymentHandler {
  PaymentHandler.register(handlerName, this)
  implicit val system = ActorSystem()

  import system.dispatcher

  // execution context for futures

  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  implicit val formats = new org.json4s.DefaultFormats {}

  /**
   * Return (Session, URL to redirect to)
   */
  def startPayment(sessionData: SessionData): Try[Either[String, Uri]] = {
    val paymentRequest = sessionData.paymentRequest.get
    val vendorId = sessionData.vendorId.get
    val successURL = Settings.MogopayEndPoint + "paypal/success"
    val failureURL = Settings.MogopayEndPoint + "paypal/fail"
    val paymentConfig = sessionData.paymentConfig.get
    val amount = sessionData.amount.get
    val tryToken = getToken(vendorId, successURL, failureURL, paymentConfig, amount, paymentRequest)

    tryToken match {
      case Failure(t) => Failure(t)
      case Success(token) =>
        if (token.isEmpty || token == Some("")) {
          Failure(new MogopayError(MogopayConstant.PaypalTokenError))
        } else {
          sessionData.token = token
          Success(Right(Uri(Settings.PayPal.UrlExpresschout).withQuery(Map("cmd" -> "_express-checkout", "token" -> token.get))))
        }
    }
  }

  private def getToken(vendorId: String, successURL: String, failureURL: String,
                       paymentConfig: PaymentConfig, amount: Long,
                       paymentRequest: PaymentRequest): Try[Option[String]] = {
    accountHandler.load(vendorId) map { vendor =>
      val parameters: Map[String, String] = paymentConfig.paypalParam
        .map(parse(_).extract[Map[String, String]])
        .getOrElse(Map())
      val user: String = parameters.getOrElse("paypalUser", "")
      val password: String = parameters.getOrElse("paypalPassword", "")
      val signature: String = parameters.getOrElse("paypalSignature", "")

      val amount2 = amount.toDouble /100.0
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
      Success(result)
    } getOrElse Failure(new AccountDoesNotExistError)
  }

  /*
   * Returns the redirection URL
   */
  def fail(sessionData: SessionData, tokenFromParams: String): Try[Uri] = {
    val transactionUuid = sessionData.transactionUuid.orNull
    val token = sessionData.token.get
    if (token != tokenFromParams) {
      Failure(new Exception)
    } else {
      val pr = PaymentResult("", new Date, sessionData.amount.get, "", CreditCardType.OTHER, new Date, "", transactionUuid, new Date,
        "", "", PaymentStatus.FAILED, "", Some(""), "", "", Some(""), token)
      Success(finishPayment(sessionData, pr))
    }
  }

  def success(sessionData: SessionData, tokenFromParams: String): Try[Uri] = {
    val paymentConfig: PaymentConfig = sessionData.paymentConfig.get
    val token = sessionData.token.get

    if (token != tokenFromParams) {
      Failure(new Exception)
    } else {
      val transactionUUID = sessionData.transactionUuid.get
      val vendorId = sessionData.vendorId.get
      val paymentRequest = sessionData.paymentRequest.get

      val maybePayerId = getPayerId(token, paymentConfig)
      maybePayerId match {
        case None | Some("") =>
          Failure(new MogopayError(MogopayConstant.PaypalPayerIdError))
        case Some(payerId) =>
          if (paymentConfig == null) {
            Failure(new MogopayError(MogopayConstant.InvalidPaypalConfig))
          } else {
            transactionHandler.startPayment(vendorId, transactionUUID, paymentRequest, PaymentType.PAYPAL, CBPaymentProvider.NONE)
            val tryPaymentResult = submit(vendorId, transactionUUID, paymentConfig, paymentRequest, token, payerId)
            tryPaymentResult.map(pr => finishPayment(sessionData, pr))
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
                     infosPaiement: PaymentRequest, token: String, payerId: String): Try[PaymentResult] = {
    accountHandler.load(vendorId).map {
      account =>
        val parameters = paymentConfig.paypalParam.map(parse(_).extract[Map[String, String]])
          .getOrElse(Map())
        transactionHandler.updateStatus(vendorId, transactionUUID, null, TransactionStatus.PAYMENT_REQUESTED, null)
        val transaction: BOTransaction = EsClient.load[BOTransaction](transactionUUID).orNull
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

        val amount = infosPaiement.amount.toDouble /100.0
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
        val result = Await.result(res, 30 seconds)
        Success(result)
    }.getOrElse {
      Failure(new AccountDoesNotExistError)
    }
  }
}
