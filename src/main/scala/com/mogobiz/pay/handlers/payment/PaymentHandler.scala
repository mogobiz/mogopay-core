/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.payment

import java.util.{Calendar, Date, UUID}

import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.exceptions.Exceptions._
import com.mogobiz.pay.model.PaymentType.PaymentType
import com.mogobiz.pay.model.{BOTransaction, TransactionStatus, _}
import com.mogobiz.system.ActorSystemLocator
import com.mogobiz.utils.GlobalUtil
import spray.http.Uri
import spray.http.Uri.Query
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import com.mogobiz.json.JacksonConverter
import com.mogobiz.pay.codes.MogopayConstant
import com.mogobiz.pay.model.TransactionStatus._
import com.mogobiz.utils.GlobalUtil._
import com.typesafe.scalalogging.StrictLogging
import org.json4s.jackson.JsonMethods._

import scala.collection.mutable

case class ExternalPaymentResult(paymentUuid: String, redirection: Either[Uri, FormRedirection])

case class AuthorizedPaymentResult(paymentUuid : String,
                                   @JsonScalaEnumeration(classOf[PaymentStatusRef])
                                   status : PaymentStatus.PaymentStatus)

case class FormRedirection(html: String)

case class ValidatePaymentResult(@JsonScalaEnumeration(classOf[PaymentStatusRef])
                                 status: PaymentStatus.PaymentStatus,
                                 transactionId: Option[String],
                                 transactionDate: Option[Date],
                                 boShopTransaction: BOShopTransaction)

case class RefundPaymentResult(@JsonScalaEnumeration(classOf[PaymentStatusRef])
                               status: PaymentStatus.PaymentStatus,
                               transactionId: Option[String],
                               transactionDate: Option[Date],
                               boShopTransaction: BOShopTransaction)

trait CBProvider extends PaymentHandler {

  def validatePayment(boShopTransaction: BOShopTransaction): ValidatePaymentResult

  def refundPayment(boShopTransaction: BOShopTransaction): RefundPaymentResult
/*
  def cancelPayment(paymentUuid: String): CancelPaymentResult
*/
}

trait PaymentHandler extends StrictLogging {
  implicit val system = ActorSystemLocator()
  implicit val _      = system.dispatcher

  //def paymentType: PaymentType

  val DIR_OUT = "OUT"
  val DIR_IN = "IN"

  def startPayment(sessionData: SessionData): Either[FormRedirection, Uri]

  def createBOTransaction(sessionData: SessionData,
                          paymentRequest: PaymentRequest,
                          paymentType: PaymentType.PaymentType): BOTransaction = {
    val vendorId = sessionData.merchantId.getOrElse(throw new InvalidContextException("Merchant UUID not found in session data"))
    val vendor = accountHandler.load(vendorId).getOrElse(throw new InvalidContextException("Vendor not found"))
    val customer = sessionData.accountId.flatMap{ accountHandler.load(_)}
    val callbackUrl = sessionData.successURL.getOrElse(throw InvalidContextException("callbackUrl not found in session data"))
    val shippingData = sessionData.selectShippingCart.flatMap(_.shippingPriceByShopId.get(MogopayConstant.SHOP_MOGOBIZ))
    val transactionUUID = sessionData.transactionUuid.getOrElse(throw InvalidContextException("Transaction UUID not found in session data"))
    val paymentConfig: PaymentConfig = sessionData.paymentConfig.getOrElse(throw InvalidContextException("Payment Config not found in session data"))
    val email = sessionData.email.getOrElse(customer.map {_.email}.getOrElse(throw InvalidContextException("Email not found in session data")))

    val merchantConfirmation = false;

    var transaction = BOTransaction(transactionUUID,
      transactionUUID,
      Calendar.getInstance().getTime,
      None,
      vendor,
      customer,
      email,
      paymentRequest.cart.finalPrice + paymentRequest.cart.shippingPrice,
      paymentRequest.cart.rate,
      TransactionStatus.INITIATED,
      None,
      None,
      callbackUrl,
      sessionData.locale,
      paymentConfig,
      paymentType,
      shippingData,
      None,
      merchantConfirmation)

    boTransactionHandler.create(transaction)
    transaction
  }

  def finishTransaction(boTransaction: BOTransaction, status: TransactionStatus.TransactionStatus, error: Option[String] = None): BOTransaction = {
    val newBOTransaction = PaymentHandler.updateTransactionStatus(boTransaction.copy(endDate = Some(new Date)), status, error)
    if (boTransaction.endDate.isEmpty) {
      // La transaction n'était pas terminé, on la termine
      transactionHandler.finishPayment(newBOTransaction)
    }
    else {
      // La transaction était déjà terminée, pas besoin de le refaire une 2° fois
      newBOTransaction
    }
  }

  def createBOShopTransaction(boTransaction: BOTransaction,
                              shopCart: ShopCartWithShipping,
                              paymentData: String,
                              status: ShopTransactionStatus.ShopTransactionStatus = ShopTransactionStatus.INITIATED): BOShopTransaction = {
    val shopTransaction = BOShopTransaction(UUID.randomUUID().toString,
      shopCart.shopId,
      boTransaction.transactionUUID,
      shopCart.finalPrice + shopCart.shippingPrice,
      shopCart.rate,
      status,
      None,
      boTransaction.paymentConfig,
      paymentData = paymentData,
      extra = JacksonConverter.serialize(shopCart),
      modifications = Nil,
      creditCard = None)

    boShopTransactionHandler.create(shopTransaction)
    shopTransaction
  }

  def updateBOShopTransactionStatus(boShopTransaction: BOShopTransaction,
                                    status: ShopTransactionStatus.ShopTransactionStatus,
                                    errorCode: Option[String]): BOShopTransaction = {
    updateBOShopTransactionStatusAndData(boShopTransaction, status, errorCode, boShopTransaction.paymentData)
  }

  def updateBOShopTransactionStatusAndData(boShopTransaction: BOShopTransaction,
                                           status: ShopTransactionStatus.ShopTransactionStatus,
                                           errorCode: Option[String],
                                           paymentData: String): BOShopTransaction = {
    val modStatus = ModificationStatus(
      uuid = newUUID,
      xdate = new Date,
      ipAddr = None,
      oldStatus = boShopTransaction.status,
      newStatus = status,
      comment = None
    )

    val newBOShopTransaction = boShopTransaction.copy(status = status,
      errorCode = errorCode,
      paymentData = paymentData,
      modifications = boShopTransaction.modifications :+ modStatus)

    boShopTransactionHandler.update(newBOShopTransaction)
    newBOShopTransaction
  }

  protected def createTransactionLog(boShopTransaction: BOShopTransaction, direction: String, transactionStep: TransactionShopStep.TransactionShopStep, data: List[String]): Unit = {
    val botlog = BOTransactionLog(newUUID,
      direction,
      data.mkString("<br/>"),
      boShopTransaction.paymentConfig.cbProvider.toString,
      boShopTransaction.transactionUUID,
      boShopTransaction.uuid,
      transactionStep)
    boTransactionLogHandler.save(botlog, false)
  }

  protected def getCreditCardConfig(paymentConfig: PaymentConfig): Map[String, String] = {
    import com.mogobiz.pay.implicits.Implicits._
    paymentConfig.cbParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())
  }
/*
  def refund(paymentConfig: PaymentConfig,
             boTx: BOTransaction,
             amount: Long,
             paymentResult: PaymentResult): RefundResult
*/

  protected def redirectionToCallback(boTransaction: BOTransaction) : Uri = {
    val query = Query(
      "status"  -> boTransaction.status.toString,
      "error"   -> boTransaction.error.getOrElse("")
    )
    val sep        = if (boTransaction.callbackUrl .indexOf('?') > 0) "&" else "?"
    Uri(boTransaction.callbackUrl + sep + GlobalUtil.mapToQueryString(query.toMap))
  }
/*
  def authorizePaymentIn2Step() : Boolean = false

  def validatePayment(transaction: BOTransaction, amount: Long): Option[ValidatePaymentResult]

  def refundPayment(transaction: BOTransaction, amount: Long): Option[ValidatePaymentResult]
*/
}

object PaymentHandler {
  private val handlers = mutable.Map[String, PaymentHandler]()

  def register(handler: (String, PaymentHandler)): Unit = {
    handlers.put(handler._1, handler._2)
  }

  def apply(handlerName: String): PaymentHandler = {
    handlers(handlerName)
  }

  def updateTransactionStatus(boTransaction: BOTransaction, status: TransactionStatus.TransactionStatus, error: Option[String] = None, msgError: Option[String] = None): BOTransaction = {
    val newBOTransaction = boTransaction.copy(status = status, transactionDate = Calendar.getInstance().getTime, error = error, msgError = msgError)
    boTransactionHandler.update(newBOTransaction)
    newBOTransaction
  }

}
