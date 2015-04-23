package com.mogobiz.pay.handlers.payment

import java.net.URLDecoder
import java.util.{Date, Locale}

import akka.actor.ActorSystem
import com.mogobiz.es.EsClient
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.exceptions.Exceptions._
import com.mogobiz.pay.model.Mogopay.CreditCardType
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.pay.config.Settings
import com.mogobiz.utils.GlobalUtil._
import net.authorize.Environment
import net.authorize.api.contract.v1.PaymentType
import net.authorize.api.contract.v1.{CreditCardType => _, _}
import net.authorize.api.controller.CreateTransactionController
import net.authorize.api.controller.base.ApiOperationBase
import net.authorize.sim.Fingerprint
import org.json4s.jackson.JsonMethods._
import spray.client.pipelining._
import spray.http.Uri.Query
import spray.http.{Uri, _}

import scala.concurrent.{Await, Future}
import scala.util._
import scala.concurrent.duration._

class ApplePayHandler(handlerName: String) extends PaymentHandler {
  PaymentHandler.register(handlerName, this)
//  implicit val system = ActorSystem()

  import system.dispatcher

  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  implicit val formats = new org.json4s.DefaultFormats {}

  /**
   * Returns a String to print, or a URL to redirect to
   */
  def startPayment(sessionData: SessionData): Either[String, Uri] = {
    val paymentRequest = sessionData.paymentRequest.get
    val amount = sessionData.amount.get

    val paymentConfig      = sessionData.paymentConfig.getOrElse(throw new PaymentConfigNotFoundException())
    val authorizeNetParams = paymentConfig.authorizeNetParam.map(parse(_).extract[Map[String, String]]).orElse(throw new MissingAuthorizeNetParamException)
    val anetAPILoginID     = authorizeNetParams.get("anetAPILoginID")
    val anetTransactionKey = authorizeNetParams.get("anetTransactionKey")

    val appleMerchAuthenticationType = new MerchantAuthenticationType()
    appleMerchAuthenticationType.setName(anetAPILoginID)
    appleMerchAuthenticationType.setTransactionKey(anetTransactionKey)

    ApiOperationBase.setEnvironment(Settings.ApplePay.env)
    ApiOperationBase.setMerchantAuthentication(appleMerchAuthenticationType)

    val op = new OpaqueDataType()
    op.setDataDescriptor("COMMON.APPLE.INAPP.PAYMENT")
    op.setDataValue(Settings.ApplePay.token.getOrElse(paymentRequest.gatewayData))

    val paymentType = new PaymentType()
    paymentType.setOpaqueData(op)

    val txnRequest = new TransactionRequestType()
    txnRequest.setTransactionType(TransactionTypeEnum.AUTH_CAPTURE_TRANSACTION.value())
    txnRequest.setPayment(paymentType)
    txnRequest.setAmount(BigDecimal.long2bigDecimal(amount).bigDecimal)

    val apiRequest = new CreateTransactionRequest()
    apiRequest.setTransactionRequest(txnRequest)

    val controller = new CreateTransactionController(apiRequest)
    controller.execute()

    val response = controller.getApiResponse
    if (response == null) {
      throw new NoResponseFromAuthorizeNetException()
    } else {
      val result = response.getTransactionResponse
      if (result.getResponseCode == "1") {
        val successURL: String = sessionData.successURL.getOrElse(throw new NoSuccessURLProvided)
        val successResponse = Await.result(pipeline(Get(Uri(successURL))), Duration.Inf)
        Right(successResponse.entity.asString)
      } else {
        throw new AuthorizeNetErrorException(result.getResponseCode)
      }
    }
  }
}
