/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.payment

import java.io.File
import java.text.{NumberFormat, SimpleDateFormat}
import java.util.{List => _, _}

import com.mogobiz.es.EsClient
import com.mogobiz.json.JacksonConverter
import com.mogobiz.pay.codes.MogopayConstant
import com.mogobiz.pay.common._
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.exceptions.Exceptions._
import com.mogobiz.pay.handlers.UtilHandler
import com.mogobiz.pay.handlers.shipping.ShippingHandler
import com.mogobiz.pay.implicits.Implicits._
import com.mogobiz.pay.model.CBPaymentProvider.CBPaymentProvider
import com.mogobiz.pay.model.CreditCardType.CreditCardType
import com.mogobiz.pay.model.PaymentType.PaymentType
import com.mogobiz.pay.model.ResponseCode3DS.ResponseCode3DS
import com.mogobiz.pay.model.TransactionStatus.TransactionStatus
import com.mogobiz.pay.model.{CBPaymentProvider, ParamRequest, TransactionStatus, _}
import com.mogobiz.utils.EmailHandler.{Attachment, Mail}
import com.mogobiz.utils.GlobalUtil._
import com.mogobiz.utils.{EmailHandler, GlobalUtil, SymmetricCrypt}
import org.apache.commons.lang.LocaleUtils
import org.elasticsearch.common.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTime
import org.json4s.JsonAST.{JField, JObject}
import org.json4s._
import org.json4s.jackson.JsonMethods._
import Settings.Mail.Smtp.MailSettings
import org.json4s.jackson.Serialization._

import scala.collection.{Map, _}
import scala.util._

case class Submit(sessionData: SessionData,
                  params: SubmitParams,
                  actionName: Option[String],
                  csrfToken: Option[String])

case class SubmitParams(successURL: String,
                        errorURL: String,
                        cardinfoURL: Option[String],
                        authURL: Option[String],
                        cvvURL: Option[String],
                        transactionUUID: String,
                        amount: Long,
                        merchantId: String,
                        transactionType: String,
                        customerCVV: Option[String],
                        ccNum: Option[String],
                        customerEmail: Option[String],
                        customerPassword: Option[String],
                        transactionDescription: Option[String],
                        gatewayData: Option[String],
                        ccMonth: Option[String],
                        ccYear: Option[String],
                        ccType: Option[String],
                        ccStore: Option[Boolean],
                        private val _payers: Option[String],
                        groupTxUUID: Option[String],
                        locale: Option[String]) {
  def payers: Map[String, Long] =
    _payers.map { payers =>
      queryStringToMap(payers, sep = ",", elementsSep = ":").mapValues(_.toLong).map(identity)
    }.getOrElse(Map())
}

class TransactionHandler {

  def retrieveHandler(cbProvider : CBPaymentProvider) : CBProvider = cbProvider match {
      //TODO décommenter au fur et à mesure
    //case CBPaymentProvider.AUTHORIZENET => authorizeNetHandler
    //case CBPaymentProvider.SYSTEMPAY => systempayHandler
    case CBPaymentProvider.PAYLINE => paylineHandler
    /*case CBPaymentProvider.PAYBOX => payboxHandler
    case CBPaymentProvider.SIPS => sipsHandler
    case CBPaymentProvider.CUSTOM => {
      if (customPaymentHandler != null) customPaymentHandler
      else throw new InvalidPaymentHandlerException("Custom Payment Handler not found")
    }*/
    case _ => throw new InvalidPaymentHandlerException("Unable to found Paym Handler for " + cbProvider.toString)
  }

  def validatePayment(boShopTransaction: BOShopTransaction) : ValidatePaymentResult = {
    retrieveHandler(boShopTransaction.paymentConfig.cbProvider).validatePayment(boShopTransaction)
  }

  def refundPayment(boShopTransaction: BOShopTransaction) : RefundPaymentResult = {
    retrieveHandler(boShopTransaction.paymentConfig.cbProvider).refundPayment(boShopTransaction)
  }

  def init(sessionData: SessionData, params: ParamRequest.TransactionInit, cart: Cart): String = {
    //    (rateHandler findByCurrencyCode params.currencyCode map { rate: Rate =>
    (accountHandler findBySecret params.merchantSecret map { vendor: Account =>
          if (!vendor.roles.contains(RoleName.MERCHANT))
            throw NotAVendorAccountException("")
          else {
            val txRequest = createTxReqForInit(vendor,
                                               params,
                                               cart.rate,
                                               None,
                                               params.groupPaymentExpirationDate,
                                               params.groupPaymentRefundPercentage)
            transactionRequestHandler.save(txRequest, false)
            txRequest.uuid
          }
        }).getOrElse(throw AccountDoesNotExistException("Invalid merchant secret"))
  }

  def createTxReqForInit(merchant: Account,
                         params: ParamRequest.TransactionInit,
                         rate: CartRate,
                         groupTxUUID: Option[String],
                         groupPaymentExpirationDate: Option[Long],
                         groupPaymentRefundPercentage: Option[Int]): TransactionRequest = {
    val txSeqId   = transactionSequenceHandler.nextTransactionId(merchant.uuid)
    val txReqUUID = newUUID

    TransactionRequest(txReqUUID,
                       txSeqId,
                       groupTxUUID,
                       groupPaymentExpirationDate,
                       groupPaymentRefundPercentage.getOrElse(100),
                       params.transactionAmount,
                       rate,
                       merchant.uuid)
  }
/*
  def updateStatus(transactionUUID: String,
                   ipAddress: Option[String],
                   newStatus: TransactionStatus,
                   comment: Option[String] = None,
                   paymentResult: Option[PaymentResult] = None,
                   gatewayData: Option[String] = None) : BOTransaction = {
    boTransactionHandler.find(transactionUUID).map { transaction =>
      val modStatus = ModificationStatus(
          uuid = newUUID,
          xdate = new Date,
          ipAddr = ipAddress.orElse(transaction.modifications.collectFirst({ case e => e.ipAddr }).flatten),
          oldStatus = Option(transaction.status),
          newStatus = Option(newStatus),
          comment = comment.map {Some(_)}.getOrElse(paymentResult.map{pr => Option(pr.errorCodeOrigin)}.getOrElse(None))
      )

      val newPaymentData = paymentResult.map { pr =>
        transaction.paymentData.copy(
          transactionId = Option(pr.gatewayTransactionId),
          authorizationId = Option(pr.authorizationId)
        )
      }.getOrElse(transaction.paymentData)

      val transactionDate = paymentResult.map { pr =>
        if (pr.transactionDate != null) Option(pr.transactionDate)
        else transaction.transactionDate
      }.getOrElse(transaction.transactionDate)

      val newTx = transaction.copy(
          status = newStatus,
          endDate = computeEndDate(newStatus),
          transactionDate = transactionDate,
          authorizationId = paymentResult.map { _.authorizationId }.getOrElse(transaction.authorizationId),
          errorCodeOrigin = paymentResult.map { pr => Option(pr.errorCodeOrigin) }.getOrElse(transaction.errorCodeOrigin),
          errorMessageOrigin = paymentResult.map { _.errorMessageOrigin }.getOrElse(transaction.errorMessageOrigin),
          paymentData = newPaymentData,
          gatewayData = gatewayData.map { Option(_) }.getOrElse(transaction.gatewayData),
          modifications = transaction.modifications :+ modStatus
      )

      boTransactionHandler.update(newTx, refresh = false)
      newTx
    }.getOrElse(throw new TransactionNotFoundException(transactionUUID))
  }

  def updateStatus3DS(transactionUUID: String,
                      ipAddress: Option[String],
                      status3DS: ResponseCode3DS,
                      codeRetour: String) {
    val maybeTx = boTransactionHandler.find(transactionUUID)
    maybeTx.map { transaction =>
      val modification = ModificationStatus(
          uuid = newUUID,
          xdate = new Date,
          oldStatus = Option(transaction.status),
          newStatus = Option(TransactionStatus.THREEDS_TESTED),
          comment = Option(codeRetour),
          ipAddr = ipAddress.orElse(transaction.modifications.collectFirst({ case e => e.ipAddr }).flatten)
      )

      val newTx = transaction.copy(
          status = TransactionStatus.THREEDS_TESTED,
          endDate = computeEndDate(TransactionStatus.THREEDS_TESTED),
          paymentData = transaction.paymentData.copy(status3DS = Option(status3DS)),
          modifications = transaction.modifications :+ modification
      )

      boTransactionHandler.update(newTx, false)
    }.getOrElse(Failure(TransactionNotFoundException(s"$transactionUUID")))
  }
*/
  // called by other handlers
  def finishPayment(boTransaction: BOTransaction): BOTransaction = {

    val status = boTransaction.status

    val boShopTransaction = boShopTransactionHandler.findByShopIdAndTransactionUuid(MogopayConstant.SHOP_MOGOBIZ, boTransaction.transactionUUID)

    // commit du shipping
    val (finalTransaction, finalShopTransaction) = if (status == TransactionStatus.PAYMENT_AUTHORIZED) {
      Try(ShippingHandler.confirmShippingPrice(boTransaction.shippingData)) match {
        case Success(shippingData) => {
          val transactionWithShippingData = shippingData.map { shippingData =>
            val finalTransWithShippingInfo = boTransaction.copy(shippingData = Some(shippingData))
            boTransactionHandler.update(finalTransWithShippingInfo)
            finalTransWithShippingInfo
          }.getOrElse(boTransaction)

          boShopTransaction.map { boShopTransaction =>
            val validationResult = validatePayment(boShopTransaction)
             if (validationResult.status == PaymentStatus.COMPLETE) {
               val updatedTransaction = PaymentHandler.updateTransactionStatus(transactionWithShippingData, TransactionStatus.COMPLETED, None)
               (updatedTransaction, Some(validationResult.boShopTransaction))
             }
            else {
              val refundResult = refundPayment(boShopTransaction)
               val updatedTransaction = if (refundResult.status == PaymentStatus.REFUNDED)
                PaymentHandler.updateTransactionStatus(transactionWithShippingData, TransactionStatus.REFUNDED, None)
              else PaymentHandler.updateTransactionStatus(transactionWithShippingData, TransactionStatus.REFUNDED_FAILED, None)
               (updatedTransaction, Some(refundResult.boShopTransaction))
            }
          }.getOrElse((transactionWithShippingData, boShopTransaction))
        }
        case Failure(f) => {
          logger.error(f.getMessage)
          (PaymentHandler.updateTransactionStatus(boTransaction, TransactionStatus.SHIPMENT_ERROR, Some(MogopayConstant.ERROR_CONFIRM_SHIPPING), Some(f.getMessage)), boShopTransaction)
        }
      }
    } else (boTransaction, boShopTransaction)

    finalShopTransaction.map {boShopTransaction =>
      // On envoie les mails uniquements pour le shop mogopay
      notifyPaymentFinished(finalTransaction, boShopTransaction)
      notifySuccessRefund(finalTransaction, boShopTransaction)
    }
    finalTransaction
  }

  def notifyPaymentFinished(transaction: BOTransaction, boShopTransaction: BOShopTransaction): Unit = {
    val locale = transaction.locale
    val localeOrEn = locale.getOrElse("en");
    val vendor = transaction.vendor
    val jsonString = BOTransactionJsonTransform.transform(transaction, boShopTransaction, LocaleUtils.toLocale(localeOrEn))
    try {
      val (subject, body) = templateHandler.mustache(Some(vendor), "mail-order", locale, jsonString)
      EmailHandler.Send(
          Mail(
            vendor.email -> s"""${vendor.firstName
            .getOrElse("")} ${vendor.lastName.getOrElse("")}""",
              Seq(transaction.email),
              Seq.empty,
              Seq.empty,
              subject,
              body,
              Some(body),
              None
          ))
    } catch {
      case e: Throwable => if (!Settings.Mogopay.Anonymous) throw e
    }
    if (transaction.status == TransactionStatus.COMPLETED) {
      try {
        val (subject, body) = templateHandler.mustache(Some(vendor), "mail-bill", locale, jsonString)
        val bill = transaction.customer.map { customer =>
          val f = download(transaction, boShopTransaction, "A4", localeOrEn)
          Attachment(f, transaction.transactionUUID + ".pdf")
        }
        EmailHandler.Send(
            Mail(
              vendor.email -> s"""${vendor.firstName
              .getOrElse("")} ${vendor.lastName.getOrElse("")}""",
                Seq(transaction.email),
                Seq.empty,
                Seq.empty,
                subject,
                body,
                Some(body),
                bill
            ))
      } catch {
        case e: Throwable => if (!Settings.Mogopay.Anonymous) throw e
      }
    }
  }

  def notifySuccessRefund(transaction: BOTransaction, boShopTransaction: BOShopTransaction): Unit = {
    if (transaction.status == TransactionStatus.REFUNDED) {
      val locale = transaction.locale
      val vendor = transaction.vendor

      try {
        val jsonString =
          BOTransactionJsonTransform.transform(transaction, boShopTransaction, LocaleUtils.toLocale(locale.getOrElse("en")))
        val (subject, body) = templateHandler.mustache(Some(vendor), "mail-refund", locale, jsonString)
        EmailHandler.Send(
            Mail(
              vendor.email -> s"""${vendor.firstName
              .getOrElse("")} ${vendor.lastName.getOrElse("")}""",
                Seq(transaction.email),
                Seq.empty,
                Seq.empty,
                subject,
                body,
                Some(body),
                None
            ))
      } catch {
        case e: Throwable => if (!Settings.Mogopay.Anonymous) throw e
      }
    }
  }

  protected def toCardType(xtype: String): CreditCardType = {
    import com.mogobiz.pay.model.CreditCardType._
    val `type`: String = if (xtype == null) "CB" else xtype.toUpperCase
    `type` match {
      case "CB"                            => CB
      case "VISA" | "VISA_ELECTRON"        => VISA
      case "AMEX"                          => AMEX
      case x if x.startsWith("MASTERCARD") => MASTER_CARD
      case _                               => CB
    }
  }
/*
  protected def computeEndDate(status: TransactionStatus): Option[Date] = {
    import com.mogobiz.pay.model.TransactionStatus._
    status match {
      case PAYMENT_CONFIRMED | PAYMENT_REFUSED | CANCEL_CONFIRMED | CUSTOMER_REFUNDED => Some(new Date())
      case _                                                                          => None
    }
  }
  */

  def verify(secret: String, amount: Option[Long], transactionUUID: String): (BOTransaction, TransactionStatus) = {
    val transaction =
      boTransactionHandler.find(transactionUUID).getOrElse(throw TransactionNotFoundException(s"$transactionUUID"))

    if (transaction.vendor.secret != secret)
      throw InvalidMerchantAccountException("")

    val validatedTx =
      if (amount.forall(transaction.amount == _)) transaction else throw UnexpectedAmountException(s"$amount")

    val txDate     = validatedTx.endDate.map(_.getTime).getOrElse(-1L)
    val now        = new Date().getTime
    val duration   = (now - txDate) / 1000
    val durationOK = duration < Settings.TransactionDuration
    if (!durationOK) {
      throw TransactionTimeoutException(MogopayConstant.Timeout.toString)
    } else if (transaction.merchantConfirmation) {
      throw TransactionAlreadyConfirmedException(MogopayConstant.TransactionAlreadyConfirmed)
    } else {
      val boShopTransaction = boShopTransactionHandler.findByShopIdAndTransactionUuid(MogopayConstant.SHOP_MOGOBIZ, transaction.transactionUUID)
      val expectedSuccessStatus = boShopTransaction.map { shop => TransactionStatus.COMPLETED}.getOrElse(TransactionStatus.PAYMENT_AUTHORIZED) // S'il n'y a pas de produit du shop mogobiz, on ne fait que des autorisations. La validation du paiement vient après
      if (transaction.status != expectedSuccessStatus && transaction.status != TransactionStatus.REFUNDED) {
        (transaction, expectedSuccessStatus)
      } else {
        val newTx = transaction.copy(merchantConfirmation = true)
        boTransactionHandler.update(newTx)
        (newTx, expectedSuccessStatus)
      }
    }
  }

  def shippingPrices(cart: Cart, accountId: String): (Option[AccountAddress], ShippingDataList) = {
    val maybeCustomer = accountHandler.load(accountId)

    val customer = maybeCustomer.getOrElse(throw AccountDoesNotExistException(s"$accountId"))

    val address = shippingAddressHandler.findByAccount(customer.uuid).find(_.active)

    address.map { addr =>
      val internationalUnauthorized = cart.compagnyAddress.map { compagnyAddr =>
        !compagnyAddr.shippingInternational && addr.address.country.getOrElse("") != compagnyAddr.country
      }.getOrElse(false)

      if (internationalUnauthorized) (Some(addr.address), ShippingDataList(Some(ShippingPriceError.INTERNATIONAL_SHIPPING_NOT_ALLOWED), Nil))
      else (Some(addr.address), ShippingHandler.computePrice(addr, cart))
    }.getOrElse((None, ShippingDataList(None, Nil)))
  }

  def selectShippingPrice(sessionData: SessionData, accountId: String, selectShippingDataByShop: Map[String, String]): Option[SelectShippingCart] = {
    shippingAddressHandler.findByAccount(accountId).find(_.active).map { clientAddress =>
      val internationalUnauthorized = sessionData.cart.map { cart =>
        cart.compagnyAddress.map { compagnyAddr =>
          !compagnyAddr.shippingInternational && clientAddress.address.country.getOrElse("") != compagnyAddr.country
        }.getOrElse(false)
      }.getOrElse(false)

      if (internationalUnauthorized) None
      else {
        val shippingCart = sessionData.shippingCart.getOrElse(throw InvalidContextException("The shippings list wasn't computed."))
        if (shippingCart.hasError) throw InvalidContextException("The shippings list must be computed without error.")

        val shippingPriceByShopId = shippingCart.shippingPricesByShopId.flatMap { shopIdAndShippingPrice =>
          val shopId = shopIdAndShippingPrice._1
          val shippingDataList = shopIdAndShippingPrice._2
          if (shippingDataList.empty) None
          else {
            val selectShippingDataId = selectShippingDataByShop.get(shopId).getOrElse(throw NoShippingPriceFound())
            Some((shopId -> shippingDataList.findById(selectShippingDataId).getOrElse(throw NoShippingPriceFound())))
          }
        }
        val selectShippingCart = Some(SelectShippingCart(clientAddress.address, shippingPriceByShopId))
        sessionData.selectShippingCart = selectShippingCart
        selectShippingCart
      }
    }.getOrElse(throw NoActiveShippingAddressFound())
  }

  /**
    * @param submit
    * @return (ServiceName, methodName)
    *
    *         1. External Payment
    *         callback_success, callback_error, merchant_id, transaction_id, transaction_type, transaction_amount
    *         2. Custom Payment
    *         callback_success, callback_error, merchant_id, transaction_id, transaction_type, transaction_amount,
    *         card_type, card_month,card_year,card_cvv
    *         3. Mogopay Payment
    *         3.1 First URL (amount only)
    *         callback_success, callback_error, merchant_id, transaction_id, transaction_type, transaction_amount
    *         callback_cardinfo, callback_cvv, callback_auth
    *         3.2 second URL (come back from auth screen) - sent here when user was not authenticated
    *         callback_success, callback_error, merchant_id, transaction_id, transaction_type, transaction_amount
    *         callback_cardinfo, callback_cvv, callback_auth
    *         user_email, user_password
    *         3.3 third URL (come back from cvv screen) - sent here once user is authenticated
    *         callback_success, callback_error, merchant_id, transaction_id, transaction_type, transaction_amount
    *         callback_cardinfo, callback_cvv, callback_auth
    *         card_cvv
    */
  def submit(submit: Submit): (String, String) = {
    val mogopayAuth = false
    /*
            String merchantId = MogopayUtil.extractStringParam(params["merchant_id"]);
            String transactionType = MogopayUtil.extractStringParam(params["transaction_type"]);
     */
    var transactionUUID = Option(submit.params.transactionUUID)
    var errorURL        = Option(submit.params.errorURL)
    var successURL      = Option(submit.params.successURL)
    var transactionType = Option(submit.params.transactionType)
    var amount          = Option(submit.params.amount)
    val cardinfoURL = submit.params.cardinfoURL
    val authURL     = submit.params.authURL
    val cvvURL      = submit.params.cvvURL
    val sessionData = submit.sessionData
    var cardStore = submit.params.ccStore.getOrElse(false)

    // The first time a user come, mogopay is false & cardinfo is true.
    // This definitely set the mogopay status for the whole session.

    val vendor =
      if (sessionData.accountId.isDefined && successURL.isEmpty && errorURL.isEmpty && cardinfoURL.isEmpty && authURL.isEmpty) {
        // user is authenticated and is coming back from the CVV screen.
        transactionUUID = sessionData.transactionUuid
        errorURL = sessionData.errorURL
        successURL = sessionData.successURL
        transactionType = sessionData.transactionType
        amount = sessionData.amount
        accountHandler.load(sessionData.merchantId.get).getOrElse(throw VendorNotFoundException())
      } else {
        accountHandler.load(submit.params.merchantId).getOrElse(throw VendorNotFoundException())
      }

    val transactionRequest = transactionRequestHandler
      .find(transactionUUID.get)
      .getOrElse(throw TransactionRequestNotFoundException(s"${transactionUUID.get}"))

    if (transactionRequest.vendorUuid != vendor.uuid)
      throw TransactionRequestWasInitiatedByAnotherMerchantException()

    if (sessionData.payers.size > 1 && transactionRequest.groupPaymentExpirationDate.isEmpty)
      throw NotAGroupPaymentException()

    //    if (sessionData.payers.size < 2 && transactionRequest.groupPaymentExpirationDate.isDefined)
    //      throw MissingPayersForGroupPaymentException()
    //
    if (transactionRequest.amount != amount.get) {
      throw UnexpectedAmountException(s"${amount.get}")
    }

    val cart = sessionData.cart.getOrElse { throw InvalidContextException("Cart isn't set.") }
    var selectedShippingCart = sessionData.selectShippingCart.getOrElse(throw InvalidContextException("Shipping price cannot be None"))

    val shopCarts = cart.shopCarts.map { shopCart =>
      val shippingCart = selectedShippingCart.shippingPriceByShopId.get(shopCart.shopId)
      val shippingPrice = shippingCart.map {_.price}.getOrElse(0L)
      new ShopCartWithShipping(shopCart, shippingPrice)
    }
    val cartWithShipping = new CartWithShipping(cart, shopCarts)

    transactionRequestHandler.delete(transactionRequest.uuid, refresh = false)

    val transaction: Option[BOTransaction] = boTransactionHandler.find(transactionUUID.get)
    if (transaction.isDefined)
      throw TheBOTransactionAlreadyExistsException(s"${transactionUUID.get}")

    def checkParameters(vendor: Account): Boolean = {
      def checkBCParameters(paymentConfig: PaymentConfig): Boolean = {
        (CBPaymentProvider.NONE != paymentConfig.cbProvider
            && paymentConfig.cbParam.isDefined
            && paymentConfig.cbParam.exists(_ != ""))
      }

      val isMerchant = vendor.roles.contains(RoleName.MERCHANT)
      if (isMerchant && vendor.paymentConfig != null && vendor.status == AccountStatus.ACTIVE) {
        val paymentConfig = vendor.paymentConfig
        paymentConfig.exists(checkBCParameters)
      } else {
        false
      }
    }

    if (amount.getOrElse(0L) <= 0 && !mogopayAuth)
      throw SomeParameterIsMissingException("Not a mogopay payment and amount is missing.")
    if (transactionUUID.isEmpty)
      throw SomeParameterIsMissingException("transactionUUID is missing.")
    if (successURL.isEmpty)
      throw SomeParameterIsMissingException("successURL is missing.")
    if (errorURL.isEmpty)
      throw SomeParameterIsMissingException("errorURL is missing.")
    if (!checkParameters(vendor))
      throw SomeParameterIsMissingException("Invalid vendor parameters.")

    sessionData.amount = amount
    sessionData.transactionUuid = transactionUUID
    sessionData.errorURL = errorURL
    sessionData.successURL = successURL
    sessionData.cardinfoURL = submit.params.cardinfoURL
    sessionData.cvvURL = submit.params.cvvURL
    sessionData.transactionType = transactionType
    sessionData.merchantId = Some(vendor.uuid)
    sessionData.paymentConfig = vendor.paymentConfig
    sessionData.locale = submit.params.locale
    if (submit.params.customerEmail.isDefined) sessionData.email = submit.params.customerEmail
    sessionData.password = submit.params.customerPassword

    if (!sessionData.mogopay)
      sessionData.mogopay = cardStore && (sessionData.authenticated || submit.params.customerPassword.nonEmpty || (submit.params.ccNum.isEmpty && submit.params.customerCVV.nonEmpty))

    sessionData.accountId.map { customerId =>
      // User is a mogopay user, he has authenticated and is coming back from the cardinfo screen
      if (submit.params.ccNum.nonEmpty && cardStore) {
        val customer = accountHandler.load(customerId).orNull
        // Mogopay avec une nouvele carte
        val ccNum            = submit.params.ccNum.orNull
        val ccMonth          = submit.params.ccMonth.orNull
        val ccYear           = submit.params.ccYear.orNull
        val ccType           = toCardType(submit.params.ccType.orNull)
        val simpleDateFormat = new SimpleDateFormat("ddMMyy")
        val expiryDate       = simpleDateFormat.parse(s"01$ccMonth$ccYear")
        val cc = CreditCard(GlobalUtil.newUUID,
                            SymmetricCrypt.encrypt(ccNum, Settings.Mogopay.Secret, "AES"),
                            submit.params.customerEmail.getOrElse(""),
                            expiryDate,
                            ccType,
                            UtilHandler.hideCardNumber(ccNum, "X"),
                            customer.uuid)
        val cust2 = customer.copy(creditCards = List(cc))
        accountHandler.update(cust2, refresh = false)
      }
    }
    //    if (cardinfoURL.nonEmpty && authURL.nonEmpty && successURL.nonEmpty && errorURL.nonEmpty &&
    //      cvvURL.nonEmpty && submit.params.customerEmail.isEmpty && submit.params.customerCVV.isEmpty) {
    //      // Mogopay has to determine what to do.
    //      // if user is authenticated, then route him to cvv
    //      // if user is not authenticated then route him to auth screen
    //      // from auth screen, he will be routed to cardinfo_url if the card is not present in his account
    //      if (sessionData.authenticated) {
    //        (null, cvvURL.get)
    //      }
    //      else {
    //        (null, authURL.get)
    //      }
    //
    //    }
    if (sessionData.mogopay) {
      // this is a mogopay payment
      if (transactionType.getOrElse("CREDIT_CARD") == "CREDIT_CARD") {
        // only credit card payments are supported through mogopay
        if (submit.params.customerCVV.nonEmpty) {
          val customer  = accountHandler.load(sessionData.accountId.get).orNull
          val card      = customer.creditCards.head
          val cardNum   = SymmetricCrypt.decrypt(card.number, Settings.Mogopay.CardSecret, "AES")
          val cardMonth = new SimpleDateFormat("MM").format(card.expiryDate)
          val cardYear  = new SimpleDateFormat("yyyy").format(card.expiryDate)
          val paymentRequest = initPaymentRequest(vendor.paymentConfig,
                                                  transactionType,
                                                  mogopay = true,
                                                  cartWithShipping,
                                                  submit.params.customerCVV.orNull,
                                                  cardNum,
                                                  cardMonth,
                                                  cardYear,
                                                  card.cardType)
          sessionData.paymentRequest = Some(paymentRequest)

          // user is already authenticated. We check start the mogopayment
          // the user has been authenticated at this step.
          //forward(controller: "mogopay", action: "startPayment", params: params + [xtoken: sessionData.csrfToken])
          //Success((buildNewSubmit(submit, newSession), "startPayment"))
          ("mogopay", "start")
        } else {
          // User submitted a password we authenticate him
          // we redirect the user to authentication screen
          // but we need first to recreate the transaction request
          transactionRequestHandler.save(transactionRequest, refresh = false)
          ("mogopay", "authenticate")
        }
      } else {
        throw NotACreditCardTransactionException(s"$transactionType")
      }
    } else {
      val paymentRequest = initPaymentRequest(vendor.paymentConfig,
                                              transactionType,
                                              mogopay = false,
                                              cartWithShipping,
                                              submit.params.customerCVV.orNull,
                                              submit.params.ccNum.orNull,
                                              submit.params.ccMonth.orNull,
                                              submit.params.ccYear.orNull,
                                              toCardType(submit.params.ccType.orNull))
      sessionData.paymentRequest = Some(paymentRequest)
      val handler = if (submit.sessionData.transactionType.contains("CREDIT_CARD")) {
        val providerName = sessionData.paymentConfig.get.cbProvider.toString.toLowerCase
        if (providerName == "custom") {
          val params =
            sessionData.paymentConfig.get.cbParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())
          params("customProviderName")
        } else {
          providerName
        }
      } else {
        sessionData.transactionType.get.toLowerCase
      }
      (handler, "start")
    }
  }

  //
  def download(transactionUuid: String, pageFormat: String, langCountry: String): File = {
    EsClient.load[BOTransaction](Settings.Mogopay.EsIndex, transactionUuid).map { boTransaction =>
      boShopTransactionHandler.findByShopIdAndTransactionUuid(MogopayConstant.SHOP_MOGOBIZ, boTransaction.transactionUUID).map { boShopTransaction =>
        download(boTransaction, boShopTransaction, pageFormat, langCountry)
      }.getOrElse(throw new TransactionNotFoundException(transactionUuid))
    }.getOrElse(throw new TransactionNotFoundException(transactionUuid))
  }

  def download(transaction: BOTransaction, boShopTransaction: BOShopTransaction, pageFormat: String, langCountry: String): File = {
    if (transaction.status == TransactionStatus.COMPLETED) {
      val jsonString = BOTransactionJsonTransform.transform(transaction, boShopTransaction, LocaleUtils.toLocale(langCountry))
      val (subject, body) =
        templateHandler.mustache(Some(transaction.vendor), "download-bill", Some(langCountry), jsonString)
      pdfHandler.convertToPdf(pageFormat, body)
    } else throw new PaymentNotConfirmedException(transaction.transactionUUID)
  }

  protected def initPaymentRequest(paymentConfig: Option[PaymentConfig],
                                   transactionType: Option[String],
                                   mogopay: Boolean,
                                   cart: CartWithShipping,
                                   ccCrypto: String,
                                   card_number: String,
                                   card_month: String,
                                   card_year: String,
                                   card_type: CreditCardType): PaymentRequest = {

    val cc_num = if (card_number != null) card_number.replaceAll(" ", "") else null
    val externalPages: Boolean = paymentConfig.exists(_.paymentMethod == CBPaymentMethod.EXTERNAL)
    val paymentRequest: PaymentRequest = PaymentRequest(cart)

    if (transactionType.getOrElse("CREDIT_CARD") == "CREDIT_CARD" && (!externalPages || mogopay)) {
      if (card_type == null) {
        throw SomeParameterIsMissingException(MogopayConstant.CreditCardTypeRequired)
      } else if (cc_num == null) {
        throw SomeParameterIsMissingException(MogopayConstant.CreditCardNumRequired)
      } else if (card_month == null) {
        throw SomeParameterIsMissingException(MogopayConstant.CreditCardMonthRequired)
      } else if (card_year == null) {
        throw SomeParameterIsMissingException(MogopayConstant.CreditCardYearRequired)
      } else if (ccCrypto == null) {
        throw SomeParameterIsMissingException(MogopayConstant.CreditCardCryptoRequired)
      } else {
        // Construction et controle de la date de validite
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, card_month.toInt - 1)
        cal.set(Calendar.YEAR, card_year.toInt)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.MONTH))

        // ce n'est pas à nous de contrôler la date de validité. C'est au prestataire de paiement
        val cc_date = cal.getTime
        if (cc_date.compareTo(new Date()) < 0) {
          throw SomeParameterIsMissingException(MogopayConstant.CreditCardExpiryDateInvalid)
        } else {
          paymentRequest.copy(
              cardType = Some(card_type),
              ccNumber = Some(cc_num), // we don't store the credit card number (for security purpose)
              expirationDate = Some(cc_date),
              cvv = Some(ccCrypto)
          )
        }
      }
    }
    else paymentRequest
  }

  def initGroupPayment(token: String): (Account, TransactionRequest, String, String, String) = {
    val decryptedToken = SymmetricCrypt.decrypt(token, Settings.Mogopay.Secret, "AES")
    val (expirationDate, txUUID, customerUUID, groupTxUUID, successURL, failureURL) =
      decryptedToken.split('|').toList match {
        case a :: b :: c :: d :: e :: f :: Nil => (a, b, c, d, e, f)
        case _                                 => throw new InvalidTokenException("")
      }

    if ((new Date).after(new Date(java.lang.Long.parseLong(expirationDate)))) {
      throw new TokenExpiredException
    }

    val txReq   = transactionRequestHandler.find(txUUID).getOrElse(throw new TransactionNotFoundException(txUUID))
    val account = accountHandler.find(customerUUID).getOrElse(throw new AccountDoesNotExistException(""))

    (account, txReq, groupTxUUID, successURL, failureURL)
  }

  def serializeCart(cart: CartWithShipping): String = {
    JacksonConverter.serialize(cart)
  }
}

object BOTransactionJsonTransform {

  private def getFieldAsString(obj: JValue) = obj match {
    case JString(v) => Some(v)
    case _          => None
  }

  private def getFieldAsBigInt(obj: JValue) = obj match {
    case JInt(v) => Some(v)
    case _       => None
  }

  private def getFieldAsBool(obj: JValue) = obj match {
    case JBool(v) => Some(v)
    case _        => None
  }

  def transformAsJValue(transaction: BOTransaction, shopTransaction: BOShopTransaction, locale: Locale) = {
    val jsonTransaction = Extraction.decompose(transaction)
    val jsonShopTransaction = Extraction.decompose(shopTransaction)
    transformBOTransaction(jsonTransaction, locale) merge
      transformShopBOTransaction(jsonShopTransaction, locale) merge JObject(
        JField("templateImagesUrl", JString(Settings.TemplateImagesUrl)))
  }

  def transform(transaction: BOTransaction, shopTransaction: BOShopTransaction, locale: Locale) = {
    compact(render(transformAsJValue(transaction, shopTransaction, locale)))
  }

  private def transformBOTransaction(obj: JValue, locale: Locale): JValue = {
    obj match {
      case t: JObject => {
        val currencyCode   = getFieldAsString(t \ "currency" \ "code").getOrElse("")
        val fractionDigits = getFieldAsBigInt(t \ "currency" \ "fractionDigits").getOrElse(BigInt(2))

        val filterChildren = t.obj.filter { child: JField =>
          child match {
            case JField("transactionDate", _)          => true
            case _                          => false
          }
        }
        val transformChildren = filterChildren.map { child: JField =>
          child match {
            case JField("transactionDate", JString(transactionDate)) =>
              JField("transactionDate", getDateAsMillis(transactionDate))
            case f: JField                                => f
          }
        }
        JObject(transformChildren)
      }
      case v: JValue => v
    }
  }

  private def transformShopBOTransaction(obj: JValue, locale: Locale): JValue = {
    obj match {
      case t: JObject => {
        val currencyCode   = getFieldAsString(t \ "currency" \ "code").getOrElse("")
        val fractionDigits = getFieldAsBigInt(t \ "currency" \ "fractionDigits").getOrElse(BigInt(2))

        val filterChildren = t.obj.filter { child: JField =>
          child match {
            case JField("uuid", _)          => false
            case JField("shopId", _)          => false
            case JField("currency", _)      => false
            case JField("modifications", _) => false
            case JField("dateCreated", _)   => false
            case JField("lastUpdated", _)   => false
            case _                          => true
          }
        }
        val transformChildren = filterChildren.map { child: JField =>
          child match {
            case JField("status", status: JValue) =>
              JField("status", JString(getFieldAsString(status \ "name").getOrElse("")))
            case JField("amount", JInt(amount)) =>
              JField("amount", formatPrice(locale, amount, currencyCode, fractionDigits))
            case JField("endDate", JString(endDate)) => JField("endDate", getDateAsMillis(endDate))
            case JField("paymentData", paymentData: JValue) =>
              JField("paymentData", transformBOPaymentData(paymentData))
            case JField("extra", JString(extra)) =>
              JField("cart", transformShopCart(parse(extra).extract[ShopCartWithShipping], locale))
            case JField("creditCard", creditCard: JValue) => JField("creditCard", transformBOCreditCard(creditCard))
            case JField("vendor", vendor: JValue)         => JField("vendor", transformAccount(vendor))
            case JField("customer", customer: JValue)     => JField("customer", transformAccount(customer))
            case f: JField                                => f
          }
        }
        JObject(transformChildren)
      }
      case v: JValue => v
    }
  }

  private def transformBOPaymentData(obj: JValue): JValue = {
    obj match {
      case t: JObject => {
        val transformChildren = t.obj.map { child: JField =>
          child match {
            case JField("paymentType", paymentType: JValue) =>
              JField("paymentType", JString(getFieldAsString(paymentType \ "name").getOrElse("")))
            case JField("cbProvider", cbProvider: JValue) =>
              JField("cbProvider", JString(getFieldAsString(cbProvider \ "name").getOrElse("")))
            case JField("orderDate", JString(orderDate)) => JField("orderDate", getDateAsMillis(orderDate))
            case JField("status3DS", status3DS: JValue) =>
              JField("status3DS", JString(getFieldAsString(status3DS \ "name").getOrElse("")))
            case f: JField => f
          }
        }
        JObject(transformChildren)
      }
      case v: JValue => v
    }
  }

  private def transformBOCreditCard(obj: JValue): JValue = {
    obj match {
      case t: JObject => {
        val transformChildren = t.obj.map { child: JField =>
          child match {
            case JField("cardType", cardType: JValue) =>
              JField("cardType", JString(getFieldAsString(cardType \ "name").getOrElse("")))
            case JField("expiryDate", JString(expiryDate)) => JField("expiryDate", getDateAsMillis(expiryDate))
            case f: JField                                 => f
          }
        }
        JObject(transformChildren)
      }
      case v: JValue => v
    }
  }

  private def transformAccount(obj: JValue): JValue = {
    obj match {
      case a: JObject => {
        val filterChildren = a.obj.filter { child: JField =>
          child match {
            case JField("uuid", _)              => false
            case JField("password", _)          => false
            case JField("loginFailedCount", _)  => false
            case JField("waitingPhoneSince", _) => false
            case JField("waitingEmailSince", _) => false
            case JField("extra", _)             => false
            case JField("lastLogin", _)         => false
            case JField("paymentConfig", _)     => false
            case JField("country", _)           => false
            case JField("roles", _)             => false
            case JField("owner", _)             => false
            case JField("emailingToken", _)     => false
            case JField("secret", _)            => false
            case JField("creditCards", _)       => false
            case JField("walletId", _)          => false
            case JField("dateCreated", _)       => false
            case JField("lastUpdated", _)       => false
            case _                              => true
          }
        }
        val transformChildren = filterChildren.map { child: JField =>
          child match {
            case JField("civility", civility: JValue) =>
              JField("civility", JString(getFieldAsString(civility \ "name").getOrElse("")))
            case JField("status", status: JValue) =>
              JField("status", JString(getFieldAsString(status \ "name").getOrElse("")))
            case JField("address", address: JValue)      => JField("address", transformAccountAddress(address))
            case JField("birthDate", JString(birthDate)) => JField("birthDate", getDateAsMillis(birthDate))
            case JField("shippingAddresses", shippingAddresses: JValue) =>
              JField("shippingAddress", transformShippingAddress(shippingAddresses))
            case f: JField => f
          }
        }
        JObject(transformChildren)
      }
      case v: JValue => v
    }
  }

  private def transformAccountAddress(obj: JValue): JValue = {
    obj match {
      case a: JObject => {
        val filterChildren = a.obj.filter { child: JField =>
          child match {
            case JField("company", _)        => false
            case JField("geoCoordinates", _) => false
            case _                           => true
          }
        }
        val transformChildren = filterChildren.map { child: JField =>
          child match {
            case JField("civility", civility: JValue) =>
              JField("civility", JString(getFieldAsString(civility \ "name").getOrElse("")))
            case JField("status", status: JValue) =>
              JField("status", JString(getFieldAsString(status \ "name").getOrElse("")))
            case JField("telephone", telephone: JValue) => JField("telephone", transformTelephone(telephone))
            case f: JField                              => f
          }
        }
        JObject(transformChildren)
      }
      case v: JValue => v
    }
  }

  private def transformTelephone(obj: JValue): JValue = {
    obj match {
      case a: JObject => {
        val transformChildren = a.obj.map { child: JField =>
          child match {
            case JField("status", status: JValue) =>
              JField("status", JString(getFieldAsString(status \ "name").getOrElse("")))
            case f: JField => f
          }
        }
        JObject(transformChildren)
      }
      case v: JValue => v
    }
  }

  private def transformShippingAddress(obj: JValue): JValue = {
    obj match {
      case a: JArray => {
        a.find { c: JValue =>
          getFieldAsBool(c \ "active").getOrElse(false)
        }.map { c: JValue =>
          transformAccountAddress(c)
        }.getOrElse(JNothing)
      }
      case v: JValue => v
    }
  }

  private def transformShopCart(shopCart: ShopCartWithShipping, locale: Locale): JValue = {
    val currencyCode: String = shopCart.rate.code
    val fractionDigits: Int  = shopCart.rate.fractionDigits
    JObject(
      JField("shipping", formatPrice(locale, shopCart.shippingPrice, currencyCode, fractionDigits)),
      JField("price", formatPrice(locale, shopCart.price, currencyCode, fractionDigits)),
      JField("taxAmount", formatPrice(locale, shopCart.taxAmount, currencyCode, fractionDigits)),
      JField("endPrice", formatPrice(locale, shopCart.endPrice, currencyCode, fractionDigits)),
      JField("reduction", formatPrice(locale, shopCart.reduction, currencyCode, fractionDigits)),
      JField("finalPrice", formatPrice(locale, shopCart.finalPrice, currencyCode, fractionDigits)),
      JField("cartItems", JArray(shopCart.cartItems.map { cartItem =>
        transformCartItem(cartItem, locale, currencyCode, fractionDigits)
      })),
      JField("coupons", JArray(shopCart.coupons.map { coupon =>
        transformCoupon(coupon, locale, currencyCode, fractionDigits)
      }))
    ).merge(Extraction.decompose(shopCart.customs))
  }

  private def transformCartItem(cartItem: CartItem,
                                locale: Locale,
                                currencyCode: String,
                                fractionDigits: Int): JValue = {
    JObject(
        JField("name", JString(cartItem.name)),
        JField("picture", JString(cartItem.picture)),
        JField("shopUrl", JString(cartItem.shopUrl)),
        JField("quantity", JInt(cartItem.quantity)),
        JField("price", formatPrice(locale, cartItem.price, currencyCode, fractionDigits)),
        JField("endPrice", formatPrice(locale, cartItem.endPrice, currencyCode, fractionDigits)),
        JField("tax", JDouble(cartItem.tax)),
        JField("taxAmount", formatPrice(locale, cartItem.taxAmount, currencyCode, fractionDigits)),
        JField("totalPrice", formatPrice(locale, cartItem.totalPrice, currencyCode, fractionDigits)),
        JField("totalEndPrice", formatPrice(locale, cartItem.totalEndPrice, currencyCode, fractionDigits)),
        JField("totalTaxAmount", formatPrice(locale, cartItem.totalTaxAmount, currencyCode, fractionDigits)),
        JField("salePrice", formatPrice(locale, cartItem.salePrice, currencyCode, fractionDigits)),
        JField("saleEndPrice", formatPrice(locale, cartItem.saleEndPrice, currencyCode, fractionDigits)),
        JField("saleTaxAmount", formatPrice(locale, cartItem.saleTaxAmount, currencyCode, fractionDigits)),
        JField("saleTotalPrice", formatPrice(locale, cartItem.saleTotalPrice, currencyCode, fractionDigits)),
        JField("saleTotalEndPrice", formatPrice(locale, cartItem.saleTotalEndPrice, currencyCode, fractionDigits)),
        JField("saleTotalTaxAmount", formatPrice(locale, cartItem.saleTotalTaxAmount, currencyCode, fractionDigits)),
        JField("registeredCartItems", JArray(cartItem.registeredCartItems.toList.map { registeredCartItem =>
          transformRegisteredCartItem(registeredCartItem, locale, currencyCode, fractionDigits)
        })),
        JField("downloadableLink",
               if (cartItem.downloadableLink != null) JString(cartItem.downloadableLink) else JNothing)
    ).merge(Extraction.decompose(cartItem.customs))
  }

  private def transformRegisteredCartItem(registeredCartItem: RegisteredCartItem,
                                          locale: Locale,
                                          currencyCode: String,
                                          fractionDigits: Int): JValue = {
    JObject(
        JField("email", JString(registeredCartItem.email)),
        JField("firstname", registeredCartItem.firstname.map { firstname =>
          JString(firstname)
        }.getOrElse(JNothing)),
        JField("lastname", registeredCartItem.lastname.map { lastname =>
          JString(lastname)
        }.getOrElse(JNothing)),
        JField("phone", registeredCartItem.phone.map { phone =>
          JString(phone)
        }.getOrElse(JNothing)),
        JField("birthdate", registeredCartItem.birthdate.map { birthdate =>
          getDateAsMillis(birthdate)
        }.getOrElse(JNothing))
    ).merge(Extraction.decompose(registeredCartItem.customs))
  }

  private def transformCoupon(coupon: Coupon, locale: Locale, currencyCode: String, fractionDigits: Int): JValue = {
    JObject(
        JField("code", JString(coupon.code)),
        JField("startDate", coupon.startDate.map { date =>
          getDateAsMillis(date)
        }.getOrElse(JNothing)),
        JField("endDate", coupon.endDate.map { date =>
          getDateAsMillis(date)
        }.getOrElse(JNothing)),
        JField("price", formatPrice(locale, coupon.price, currencyCode, fractionDigits)),
        JField("customs", Extraction.decompose(coupon.customs))
    )
  }

  private def transformExtra(locale: Locale,
                             currencyCode: String,
                             fractionDigits: BigInt): PartialFunction[JField, JField] = {
    case JField("salePrice", JInt(salePrice)) =>
      JField("salePrice", formatPrice(locale, salePrice, currencyCode, fractionDigits))
    case JField("saleEndPrice", JInt(saleEndPrice)) =>
      JField("saleEndPrice", formatPrice(locale, saleEndPrice, currencyCode, fractionDigits))
    case JField("totalPrice", JInt(totalPrice)) =>
      JField("totalPrice", formatPrice(locale, totalPrice, currencyCode, fractionDigits))
    case JField("totalEndPrice", JInt(totalEndPrice)) =>
      JField("totalEndPrice", formatPrice(locale, totalEndPrice, currencyCode, fractionDigits))
    case JField("saleTotalPrice", JInt(saleTotalPrice)) =>
      JField("saleTotalPrice", formatPrice(locale, saleTotalPrice, currencyCode, fractionDigits))
    case JField("saleTotalEndPrice", JInt(saleTotalEndPrice)) =>
      JField("saleTotalEndPrice", formatPrice(locale, saleTotalEndPrice, currencyCode, fractionDigits))
    case JField("startDate", JString(startDate)) => JField("startDate", getDateAsMillis(startDate))
    case JField("endDate", JString(endDate))     => JField("endDate", getDateAsMillis(endDate))
    case JField("birthdate", JString(birthdate)) => JField("birthdate", getDateAsMillis(birthdate))
  }

  private def getDateAsMillis(value: String) = {
    val date = Try(ISODateTimeFormat.dateTime().parseDateTime(value)) match {
      case Success(d) => d
      case _          => ISODateTimeFormat.dateTimeNoMillis().parseDateTime(value)
    }
    JDouble(date.getMillis())
  }

  private def getDateAsMillis(value: DateTime) = {
    JDouble(value.getMillis())
  }

  private def formatPrice(locale: Locale, amount: BigInt, currencyCode: String, fractionDigits: BigInt) = {
    val numberFormat = NumberFormat.getCurrencyInstance(locale)
    numberFormat.setCurrency(Currency.getInstance(currencyCode))
    JString(numberFormat.format(amount.toLong / Math.pow(10, fractionDigits.toLong)))
  }

  private def formatPrice(locale: Locale, amount: Long, currencyCode: String, fractionDigits: Int) = {
    val numberFormat = NumberFormat.getCurrencyInstance(locale)
    numberFormat.setCurrency(Currency.getInstance(currencyCode))
    JString(numberFormat.format(amount / Math.pow(10, fractionDigits.toLong)))
  }
}
