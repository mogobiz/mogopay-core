/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.payment

import akka.util.Timeout
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.exceptions.Exceptions._
import com.mogobiz.pay.model._
import com.mogobiz.utils.CustomSslConfiguration
import net.authorize.api.contract.v1.{CreditCardType => _, _}
import net.authorize.api.controller.CreateTransactionController
import net.authorize.api.controller.base.ApiOperationBase
import org.json4s.jackson.JsonMethods._
import spray.client.pipelining._
import spray.http.{Uri, _}

import scala.Either
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util._

class ApplePayHandler(handlerName: String) extends PaymentHandler with CustomSslConfiguration {
  PaymentHandler.register(handlerName, this)
  implicit val timeout: Timeout = 40.seconds

  val paymentType = com.mogobiz.pay.model.PaymentType.CREDIT_CARD

  implicit val formats = new org.json4s.DefaultFormats {}

  /**
    * Returns a String to print, or a URL to redirect to
    *//*
  def startPayment(sessionData: SessionData): Either[String, Uri] = {
    val paymentRequest = sessionData.paymentRequest.get
    val amount         = sessionData.amount.get

    val paymentConfig = sessionData.paymentConfig.getOrElse(throw new PaymentConfigNotFoundException())
    val authorizeNetParams = paymentConfig.applePayParam
      .map(parse(_).extract[Map[String, String]])
      .orElse(throw new MissingAuthorizeNetParamException)
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

    val thePaymentType = new PaymentType()
    thePaymentType.setOpaqueData(op)

    val txnRequest = new TransactionRequestType()
    txnRequest.setTransactionType(TransactionTypeEnum.AUTH_CAPTURE_TRANSACTION.value())
    txnRequest.setPayment(thePaymentType)
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
        val successURL: String                            = sessionData.successURL.getOrElse(throw new NoSuccessURLProvided)
        val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
        val successResponse                               = Await.result(pipeline(Get(Uri(successURL))), Duration.Inf)
        Right(successResponse.entity.asString)
      } else {
        throw new AuthorizeNetErrorException(result.getResponseCode)
      }
    }
  }

  def validatePayment(transaction: BOTransaction, amount: Long): Option[ValidatePaymentResult] = {
    //TODO à implémenter
    None
  }

  def refundPayment(transaction: BOTransaction, amount: Long): Option[ValidatePaymentResult] = {
    //TODO à implémenter
    None
  }

  override def refund(paymentConfig: PaymentConfig,
                      boTx: BOTransaction,
                      amount: Long,
                      paymentResult: PaymentResult): RefundResult = ???
                      */
  override def startPayment(sessionData: SessionData): Either[FormRedirection, Uri] = throw new Exception("Not implemented")
}
