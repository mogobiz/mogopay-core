package mogopay.handlers.payment

import java.text.SimpleDateFormat

import com.sksamuel.elastic4s.ElasticDsl._
import mogopay.codes.MogopayConstant
import mogopay.config.Settings
import mogopay.es.EsClient
import mogopay.model.Mogopay.{Account, AccountStatus, SessionData}
import org.apache.shiro.crypto.hash.Sha256Hash
import mogopay.config.HandlersConfig._
import spray.http.Uri

import scala.util.{Try, Failure, Left, Success}

class MogopayHandler(handlerName: String) extends PaymentHandler {
  PaymentHandler.register(handlerName, this)

  def authenticate(sessionData: SessionData): Try[Left[String, Nothing]] = {
    val ownerFilter =
      sessionData.merchantId.map {
        vendorId => termFilter("owner", vendorId)
      } getOrElse {
        missingFilter("owner") existence true includeNull true
      }


    val req = select in Settings.ElasticSearch.Index -> "Account" filter {
      and(
        termFilter("status", AccountStatus.ACTIVE),
        termFilter("email", sessionData.email.get),
        termFilter("password", new Sha256Hash(sessionData.password.get)),
        ownerFilter
      ) cache (false)
    }
    val account = EsClient.search[Account](req)
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
        Success(Left(form))
      }
      else {
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
                <input type="hidden" name="card_expiry_date" value="${new SimpleDateFormat("MMyyyy").format(card.expiryDate)}" />
                <input type="hidden" name="card_expiry_month" value="${new SimpleDateFormat("MM").format(card.expiryDate)}" />
                <input type="hidden" name="card_expiry_year" value="${new SimpleDateFormat("yyyy").format(card.expiryDate)}" />
                <input type="hidden" name="card_number" value="${card.hiddenNumber}" />
                <input type="hidden" name="card_holder" value="${card.holder}" />
                </form>
                <script>document.getElementById("formpay").submit();</script>
              </body>
            </html>"""
        Success(Left(form))
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
      Success(Left(form))

    }
  }

  def startPayment(sessionData: SessionData): Try[Either[String, Uri]] = {
    if (sessionData.transactionType.getOrElse("CREDIT_CARD") == "CREDIT_CARD") {
      val cbProvider = sessionData.paymentConfig.get.cbProvider.toString.toLowerCase()
      PaymentHandler(cbProvider).startPayment(sessionData)
    }
    else {
      Failure(new Exception("Invalid Transaction Type"))
    }
  }
}
