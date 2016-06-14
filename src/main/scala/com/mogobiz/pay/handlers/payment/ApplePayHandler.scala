/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.payment

import akka.http.scaladsl.model.{ HttpMethods, HttpRequest, Uri }
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.util.Timeout
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.exceptions.Exceptions._
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.utils.HttpRequestor
import net.authorize.api.contract.v1.{ CreditCardType => _, _ }
import net.authorize.api.controller.CreateTransactionController
import net.authorize.api.controller.base.ApiOperationBase
import org.json4s.jackson.JsonMethods._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util._

class ApplePayHandler(handlerName: String) extends PaymentHandler with HttpRequestor {
  PaymentHandler.register(handlerName, this)
  implicit val timeout: Timeout = 40.seconds

  val paymentType = com.mogobiz.pay.model.Mogopay.PaymentType.CREDIT_CARD

  implicit val formats = new org.json4s.DefaultFormats {}

  /**
   * Returns a String to print, or a URL to redirect to
   */
  def startPayment(sessionData: SessionData): Either[String, Uri] = {
    val paymentRequest = sessionData.paymentRequest.get
    val amount = sessionData.amount.get

    val paymentConfig = sessionData.paymentConfig.getOrElse(throw new PaymentConfigNotFoundException())
    val authorizeNetParams = paymentConfig.applePayParam.map(parse(_).extract[Map[String, String]]).orElse(throw new MissingAuthorizeNetParamException)
    val anetAPILoginID = authorizeNetParams.get("anetAPILoginID")
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
        val successURL: String = sessionData.successURL.getOrElse(throw new NoSuccessURLProvided)
        val request = HttpRequest(
          method = HttpMethods.GET,
          uri = Uri(successURL))
        val successData = Await.result(doRequest(request).flatMap(rep => Unmarshal(rep.entity).to[String]), Duration.Inf)

        Right(successData)
      } else {
        throw new AuthorizeNetErrorException(result.getResponseCode)
      }
    }
  }

  override def refund(paymentConfig: PaymentConfig, boTx: BOTransaction, amount: Long, paymentResult: PaymentResult): RefundResult = ???
}
