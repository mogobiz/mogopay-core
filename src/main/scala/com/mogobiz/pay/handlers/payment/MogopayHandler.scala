/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.payment

import java.text.SimpleDateFormat

import com.mogobiz.pay.config.Settings
import com.sksamuel.elastic4s.ElasticDsl._
import com.mogobiz.pay.codes.MogopayConstant
import com.mogobiz.es.EsClient
import com.mogobiz.pay.exceptions.Exceptions.InvalidTransactionTypeException
import com.mogobiz.pay.model._
import org.apache.shiro.crypto.hash.Sha256Hash
import spray.http.Uri

import scala.util.{Either, Left}

class MogopayHandler(handlerName: String) extends PaymentHandler {
  PaymentHandler.register(handlerName, this)
  val paymentType = PaymentType.CREDIT_CARD
/*
  def authenticate(sessionData: SessionData): Left[String, Nothing] = {
    val ownerFilter =
      sessionData.merchantId.map { vendorId =>
        termFilter("owner", vendorId)
      } getOrElse {
        missingFilter("owner") existence true includeNull true
      }

    val req = search in Settings.Mogopay.EsIndex -> "Account" postFilter {
      and(
          termFilter("status", AccountStatus.ACTIVE),
          termFilter("email", sessionData.email.get),
          termFilter("password", new Sha256Hash(sessionData.password.get)),
          ownerFilter
      ) cache (false)
    }
    val account =
      if (sessionData.authenticated) EsClient.load[Account](Settings.Mogopay.EsIndex, sessionData.accountId.get)
      else EsClient.search[Account](req)
    account map { account =>
      sessionData.authenticated = true
      sessionData.accountId = Some(account.uuid)
      val cards = account.creditCards
      if (cards.isEmpty) {
        val form = s"""
            <html>
              <head>
              </head>
              <body>
                <form id="formpay" action="${sessionData.cardinfoURL.get}" method="POST" >
                <input type="hidden" name="result" value="${MogopayConstant.Error}" />
                <input type="hidden" name="transaction_id" value="${sessionData.transactionUuid}" />
                <input type="hidden" name="transaction_amount" value="${sessionData.amount.get}" />
                <input type="hidden" name="transaction_type" value="CREDIT_CARD" />
                <input type="hidden" name="error_code" value="${MogopayConstant.CreditCardNumRequired}" />
                </form>
                <script>document.getElementById("formpay").submit();</script>
              </body>
            </html>"""
        Left(form)
      } else {
        val card = cards(0)
        val form = s"""
            <html>
              <head>
              </head>
              <body>
                <form id="formpay" action="${sessionData.cvvURL.get}" method="POST" >
                <input type="hidden" name="result" value="${MogopayConstant.Success}" />
                <input type="hidden" name="transaction_id" value="${sessionData.transactionUuid}" />
                <input type="hidden" name="transaction_type" value="CREDIT_CARD" />
                <input type="hidden" name="card_type" value="${card.cardType}" />
                <input type="hidden" name="card_expiry_date" value="${new SimpleDateFormat("MMyyyy")
          .format(card.expiryDate)}" />
                <input type="hidden" name="card_expiry_month" value="${new SimpleDateFormat("MM")
          .format(card.expiryDate)}" />
                <input type="hidden" name="card_expiry_year" value="${new SimpleDateFormat("yyyy")
          .format(card.expiryDate)}" />
                <input type="hidden" name="card_number" value="${card.hiddenNumber}" />
                <input type="hidden" name="card_holder" value="${card.holder}" />
                </form>
                <script>document.getElementById("formpay").submit();</script>
              </body>
            </html>"""
        Left(form)
      }
    } getOrElse {
      val form = s"""
            <html>
              <head>
              </head>
              <body>
                <form id="formpay" action="${sessionData.errorURL.get}" method="POST" >
                <input type="hidden" name="result" value="${MogopayConstant.Error}" />
                <input type="hidden" name="transaction_id" value="${sessionData.transactionUuid}" />
                <input type="hidden" name="transaction_type" value="CREDIT_CARD" />
                <input type="hidden" name="error_code" value="${MogopayConstant.InvalidPassword}" />
                </form>
                <script>document.getElementById("formpay").submit();</script>
              </body>
            </html>"""
      Left(form)

    }
  }

  def startPayment(sessionData: SessionData): Either[String, Uri] = {
    if (sessionData.transactionType.getOrElse("CREDIT_CARD") == "CREDIT_CARD") {
      val cbProvider = sessionData.paymentConfig.get.cbProvider.toString.toLowerCase()
      PaymentHandler(cbProvider).startPayment(sessionData)
    } else {
      throw new InvalidTransactionTypeException(sessionData.transactionType.get)
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
