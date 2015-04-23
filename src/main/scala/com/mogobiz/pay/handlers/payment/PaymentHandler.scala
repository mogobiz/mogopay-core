package com.mogobiz.pay.handlers.payment

import java.util.{Date, UUID}

import akka.actor.{Props, ActorSystem}
import com.mogobiz.pay.codes.MogopayConstant
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.config.{Environment, Settings}
import com.mogobiz.pay.exceptions.Exceptions._
import com.mogobiz.pay.handlers.{EmailingActor, EmailHandler}
import com.mogobiz.pay.handlers.EmailHandler.Mail
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.pay.model.ParamRequest
import com.mogobiz.utils.{SymmetricCrypt, GlobalUtil}
import spray.http.Uri
import spray.http.Uri.Query

import scala.collection.mutable

trait PaymentHandler {
  implicit val system = ActorSystem()

  /**
   * Returns the redirection page's URL
   */
  protected def finishPayment(sessionData: SessionData,
                              paymentResult: PaymentResult): Uri = {
    val errorURL = sessionData.errorURL.getOrElse("")
    val successURL = sessionData.successURL.getOrElse("")
    val transactionUUID = sessionData.transactionUuid.getOrElse("")
    val transactionSequence = if (sessionData.paymentRequest.isDefined) sessionData.paymentRequest.get.transactionSequence else ""
    val success = paymentResult.status == PaymentStatus.COMPLETE

    val query = Query(
      "result" -> (if (success) MogopayConstant.Success else MogopayConstant.Error),
      "transaction_id" -> transactionUUID,
      "transaction_sequence" -> transactionSequence,
      "transaction_type" -> "CREDIT_CARD",
      "error_code_bank" -> paymentResult.bankErrorCode,
      "error_message_bank" -> paymentResult.bankErrorMessage.getOrElse(""),
      "error_code_provider" -> paymentResult.errorCodeOrigin,
      "error_message_provider" -> paymentResult.errorMessageOrigin.getOrElse("")
    )

    if (success) {
      val payers = sessionData.payers
      val tx = boTransactionHandler.find(transactionUUID).getOrElse(throw new BOTransactionNotFoundException(transactionUUID))
      val merchantUUID = tx.vendor.getOrElse(throw new VendorNotFoundException).uuid
      val paymentConfig = accountHandler.find(merchantUUID)
        .getOrElse(throw new VendorNotFoundException())
        .paymentConfig
        .getOrElse(throw new PaymentConfigNotFoundException())
      val firstPayer: Account = tx.customer.getOrElse(throw new NoCustomerSetForTheBOTrasaction)
      handleGroupPayment(payers, tx, merchantUUID, paymentConfig, firstPayer)
    }

    sessionData.finished = true
    val redirectTo = if (success) successURL else errorURL
    val sep = if (redirectTo.indexOf('?') > 0) "&" else "?"
    Uri(redirectTo + sep + GlobalUtil.mapToQueryString(query.toMap))
  }

  private def handleGroupPayment(payers: Map[String, Long], firstPayerBOTx: BOTransaction, merchantId: String,
                                 paymentConfig: PaymentConfig, firstPayer: Account): Unit = if (payers.size > 1) {
    val groupTxUUID = firstPayerBOTx.uuid
    boTransactionHandler.update(firstPayerBOTx.copy(groupTransactionUUID = Some(groupTxUUID)), refresh = false)

    val payersAccounts = payers.filter(_._1 != firstPayer.email).foreach { case (email, amount) =>
      val account = accountHandler.findByEmail(email, Some(merchantId)).getOrElse {
        val newAccount = Account(
          uuid     = UUID.randomUUID().toString,
          email    = email,
          password = null,
          owner    = Some(merchantId),
          secret   = "",
          status   = AccountStatus.INACTIVE
        )
        accountHandler.save(newAccount, false)
        newAccount
      }

      val merchant = accountHandler.find(merchantId).get
      val params = ParamRequest.TransactionInit(merchant.secret, amount, firstPayerBOTx.currency.code,
        firstPayerBOTx.currency.rate, firstPayerBOTx.extra, None)
      val txReq = transactionHandler.createTxReqForInit(account, params, Some(groupTxUUID))
      transactionRequestHandler.save(txReq, refresh = false)

      val token = {
        val clearToken = s"${new Date((new Date).getTime + 7 * 24 * 3600 * 1000).getTime}|${txReq.uuid}|${account.uuid}|$groupTxUUID" // todo make the time customizable
        SymmetricCrypt.encrypt(clearToken, Settings.Mogopay.Secret, "AES")
      }

      if (Settings.Env == Environment.DEV) println(s"==== Group payment token: $token")

      val url = Uri("http://foo.com").withQuery(("token", token)) //todo: cutomize the url

      def sendEmail() {
        val merchant = accountHandler.find(merchantId).getOrElse(throw new VendorNotFoundException())
        val paymentConfig = merchant.paymentConfig.getOrElse(throw new PaymentConfigNotFoundException())

        val template = templateHandler.loadTemplateByVendor(Option(merchant), "group-payment.mustache")

        val jsonTx = BOTransactionJsonTransform.transform(firstPayerBOTx, firstPayer.country.map(_.code).getOrElse("fr")) // make "FR" configurable

        val payerName = firstPayer.firstName.getOrElse(firstPayer.lastName.getOrElse(firstPayer.email))
        val data = //todo: add transaction extra + payers
          s"""
              |{
              |  "firstPayer":  "$payerName",
              |  "url":         "$url",
              |  "transaction": $jsonTx
              |}
              |""".stripMargin
        val (subject, body) = templateHandler.mustache(template, data)

        val senderName = merchant.paymentConfig.get.senderName
        val senderEmail = merchant.paymentConfig.get.senderEmail

        val emailingActor = system.actorOf(Props[EmailingActor])
        emailingActor ! Mail(
          from = (senderEmail.getOrElse(""), senderName.get),
          to = Seq(account.email),
          subject = subject,
          message = body)
      }
      sendEmail()
    }
  }

  def startPayment(sessionData: SessionData): Either[String, Uri]

  def createThreeDSNotEnrolledResult(): PaymentResult = {
    PaymentResult(
      transactionSequence = GlobalUtil.newUUID,
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

object PaymentHandler {
  private val handlers = mutable.Map[String, PaymentHandler]()

  def register(handler: (String, PaymentHandler)): Unit = {
    handlers.put(handler._1, handler._2)
  }

  def apply(handlerName: String): PaymentHandler = {
    handlers(handlerName)
  }

}
