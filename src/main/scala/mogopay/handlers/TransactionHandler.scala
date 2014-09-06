package mogopay.handlers

import java.text.SimpleDateFormat
import java.util.{Calendar, Currency, Date}


import mogopay.actors.TransactionActor.Submit
import mogopay.codes.MogopayConstant
import mogopay.config.HandlersConfig._
import mogopay.config.{Settings}
import mogopay.es.EsClient
import mogopay.exceptions.Exceptions._
import mogopay.handlers.shipping._
import mogopay.model.Mogopay.CreditCardType.CreditCardType
import mogopay.model.Mogopay.ResponseCode3DS.ResponseCode3DS
import mogopay.model.Mogopay.TransactionStatus.TransactionStatus
import mogopay.config.Implicits._
import mogopay.model.Mogopay.CBPaymentProvider.CBPaymentProvider
import mogopay.model.Mogopay.PaymentType.PaymentType
import mogopay.model.Mogopay._
import mogopay.session.{Session}
import mogopay.util.GlobalUtil._
import mogopay.util.RSA
import org.json4s.JValue
import org.json4s.JsonAST.JNothing
import org.json4s.jackson.JsonMethods._
import mogopay.config.Implicits._
import scala.collection._
import scala.util._

class TransactionHandler {
  // TODO this method is not correctly implemented
  def searchByCustomer(uuid: String) = {
    EsClient.load[BOTransaction](uuid)
  }

  def init(secret: String, amount: Long, currencyCode: String,
           currencyRate: Double, extra: Option[String]): Try[String] = {
    (rateHandler findByCurrencyCode currencyCode map { rate: Rate =>
      val currency = Currency.getInstance(currencyCode)
      (accountHandler findBySecret secret map { vendor: Account =>
        if (!vendor.roles.contains(RoleName.MERCHANT)) {
          Failure(new NotAVendorAccountException)
        } else {
          val txSeqId = transactionSequenceHandler.nextTransactionId(vendor.uuid)
          val txReqUUID = newUUID

          val txCurrency = TransactionCurrency(currencyCode, currency.getNumericCode, currencyRate, rate.currencyFractionDigits)
          val txRequest = TransactionRequest(txReqUUID, txSeqId, amount, extra, txCurrency, vendor.uuid)

          EsClient.index(txRequest)

          Success(txReqUUID)
        }
      }).getOrElse(Failure(new AccountDoesNotExistError))
    }).getOrElse(Failure(new CurrencyCodeNotFoundException))
  }

  /*
			transaction.cbProvider = CBPaymentProvider.NONE;

   */
  def startPayment(vendorId: String, transactionUUID: String, paymentRequest: PaymentRequest,
                   paymentType: PaymentType, cbProvider: CBPaymentProvider) = {
    accountHandler.load(vendorId).map { account =>
      var transaction = BOTransaction(transactionUUID, transactionUUID, "", Option(new Date), paymentRequest.amount,
        paymentRequest.currency, TransactionStatus.INITIATED, new Date, None,
        BOPaymentData(paymentType, cbProvider, None, None, None, None, None),
        false,
        Option(paymentRequest.transactionEmail), None, None, Option(paymentRequest.transactionExtra),
        Option(paymentRequest.transactionDesc), None, Option(account), None, Nil)

      if (paymentType == PaymentType.CREDIT_CARD &&
        account.paymentConfig.map(_.paymentMethod) != Some(CBPaymentMethod.EXTERNAL)) {
        val creditCard = BOCreditCard(
          number = UtilHandler.hideCardNumber(paymentRequest.ccNumber, "X"),
          holder = None,
          expiryDate = paymentRequest.expirationDate,
          cardType = paymentRequest.cardType
        )
        transaction = transaction.copy(creditCard = Option(creditCard))
      }
      transaction = transaction.copy(
        paymentData = transaction.paymentData.copy(
          idCommandCB = Option(paymentRequest.id),
          dateCommandCB = Option(paymentRequest.orderDate)
        )
      )
      EsClient.index(transaction, false)
      Success(transaction)
    }.getOrElse(Failure(new Exception("transactionService.creerTransaction.vendeur.inconnu")))
  }

  def updateStatus(vendorId: String, transactionUUID: String, ipAddress: String, newStatus: TransactionStatus, comment: String): Try[Unit] = {
    val maybeTx = EsClient.load[BOTransaction](transactionUUID)
    maybeTx.map { transaction =>
      val modStatus: ModificationStatus = ModificationStatus(
        uuid = newUUID,
        xdate = new Date,
        ipAddr = Option(ipAddress),
        oldStatus = Option(transaction.status),
        newStatus = Option(newStatus),
        comment = Option(comment)
      )

      val newTx = transaction.copy(
        status = newStatus,
        endDate = computeEndDate(newStatus),
        modifications = transaction.modifications :+ modStatus
      )

      EsClient.update(newTx, true, false)
      Success()
    }.getOrElse(Failure(new TransactionNotFoundException))
  }

  def updateStatus3DS(vendorId: String, transactionUUID: String, status3DS: ResponseCode3DS, codeRetour: String) {
    val maybeTx = EsClient.load[BOTransaction](transactionUUID)
    maybeTx.map { transaction =>
      val modification = ModificationStatus(
        uuid = newUUID,
        xdate = new Date,
        oldStatus = Option(transaction.status),
        newStatus = Option(TransactionStatus.THREEDS_TESTED),
        comment = Option(codeRetour),
        ipAddr = None
      )

      val newTx = transaction.copy(
        status = TransactionStatus.THREEDS_TESTED,
        endDate = computeEndDate(TransactionStatus.THREEDS_TESTED),
        paymentData = transaction.paymentData.copy(status3DS = Option(status3DS)),
        modifications = transaction.modifications :+ modification
      )

      EsClient.update(newTx, true, false)
    }.getOrElse(Failure(new BOTransactionNotFoundException))
  }

  def finishPayment(vendorId: String, transactionUUID: String, newStatus: TransactionStatus,
                    paymentResult: PaymentResult, returnCode: String): Try[Unit] = {
    try {
      val transaction = EsClient.load[BOTransaction](transactionUUID)
      transaction.map { transaction: BOTransaction =>
        val modification = ModificationStatus(newUUID, new Date, None, Option(transaction.status), Option(newStatus), Option(returnCode))
        val newTx = transaction.copy(
          status = newStatus,
          endDate = computeEndDate(newStatus),
          transactionUUID = paymentResult.id,
          authorizationId = paymentResult.authorizationId,
          errorCodeOrigin = Option(paymentResult.errorCodeOrigin),
          errorMessageOrigin = paymentResult.errorMessageOrigin,
          modifications = transaction.modifications :+ modification
        )

        if (paymentResult.transactionDate != null)
          EsClient.update(newTx.copy(transactionDate = Option(paymentResult.transactionDate)), true, false)
        else
          EsClient.index(newTx, false)
        Success()
      }.getOrElse(Failure(new BOTransactionNotFoundException))
    } catch {
      case e: Throwable => Failure(e)
    }
  }

  private def toCardType(xtype: String): CreditCardType = {
    import CreditCardType._
    val `type`: String = if (xtype == null) "CB" else xtype.toUpperCase
    `type` match {
      case "CB" => CB
      case "VISA" | "VISA_ELECTRON" => VISA
      case "AMEX" => AMEX
      case x if (x.startsWith("MASTERCARD")) => MASTER_CARD
      case _ => CB
    }
  }

  private def computeEndDate(status: TransactionStatus): Option[Date] = {
    import TransactionStatus._
    status match {
      case PAYMENT_CONFIRMED | PAYMENT_REFUSED | CANCEL_CONFIRMED | CUSTOMER_REFUNDED => Some(new Date())
      case _ => None
    }
  }

  def verify(secret: String, amount: Option[Long], transactionUUID: String): Try[BOTransaction] = {
    val maybeVendor = accountHandler.findBySecret(secret)

    val account: Try[Account] = maybeVendor match {
      case None => Failure(new AccountDoesNotExistError)
      case Some(a) => Success(a)
    }

    val vendorUUID = account match {
      case Failure(t) => Failure(t)
      case Success(v) if v.roles.contains(RoleName.MERCHANT) => Success(v.uuid)
      case _ => Failure(new NotAVendorAccountException)
    }

    val maybeTransaction: Try[Option[BOTransaction]] = vendorUUID match {
      case Failure(t) => Failure(t)
      case Success(uuid) => Success(EsClient.load[BOTransaction](transactionUUID))
    }

    val transaction = maybeTransaction match {
      case Failure(t) => Failure(t)
      case Success(None) => Failure(new TransactionNotFoundException)
      case Success(Some(tx)) => Success(tx)
    }

    val validatedTx = transaction match {
      case Failure(t) => Failure(t)
      case Success(tx) =>
        if (amount.map(tx.amount == _).getOrElse(true))
          Success(tx)
        else
          Failure(UnexpectedAmountException())
    }

    validatedTx match {
      case Failure(t) => Failure(t)
      case Success(transaction) =>
        val txDate = transaction.endDate.map(_.getTime).getOrElse(-1L)
        val now = new Date().getTime
        val duration = (now - txDate) / 1000
        val durationOK = duration < Settings.TransactionDuration
        if (!durationOK) {
          Failure(new TransactionTimeoutException(MogopayConstant.Timeout.toString))
        } else if (transaction.status != TransactionStatus.PAYMENT_CONFIRMED) {
          Failure(new PaymentNotConfirmedException(MogopayConstant.PaymentNotConfirmed))
        } else if (transaction.merchantConfirmation) {
          Failure(new TransactionAlreadyConfirmedException(MogopayConstant.TransactionAlreadyConfirmed))
        } else {
          val newTx = transaction.copy(merchantConfirmation = true)
          EsClient.update(newTx, false, false)
          Success(newTx)
        }
    }
  }

  def shippingPrices(currencyCode: String, transactionExtra: String,
                     accountId: String): Try[Seq[ShippingPrice]] = {
    val maybeCustomer = accountHandler.load(accountId)

    val customer = maybeCustomer match {
      case None => Failure(new AccountDoesNotExistError)
      case Some(a) => Success(a)
    }

    val maybeAddress: Try[Option[ShippingAddress]] = customer match {
      case Failure(t) => Failure(t)
      case Success(c) => Success(shippingAddressHandler.findByAccount(c.uuid).find(_.active).headOption)
    }

    val address = maybeAddress match {
      case Failure(t) => Failure(t)
      case Success(None) => Failure(new NoActiveShippingAddressFound)
      case Success(Some(x)) => Success(x)
    }

    address match {
      case Failure(t) => Failure(t)
      case Success(addr) => Success(computePrice(addr, currencyCode, parse(transactionExtra)))
    }
  }

  def shippingPrice(prices: Seq[ShippingPrice], provider: String,
                    service: String, rateType: String): Option[ShippingPrice] = {
    prices.find { price =>
      price.provider.equals(provider) && price.service.equals(service) && price.rateType.equals(rateType)
    }
  }

  /**
   * @param submit
   * @return (ServiceName, methodName)
   */
  def submit(submit: Submit): Try[(String, String)] = {
    val mogopayAuth = false
    /*
            String merchantId = MogopayUtil.extractStringParam(params["merchant_id"]);
            String transactionType = MogopayUtil.extractStringParam(params["transaction_type"]);
     */
    var transactionUUID = submit.params.transactionUUID
    var errorURL = submit.params.errorURL
    var successURL = submit.params.successURL
    var transactionType = submit.params.transactionType
    var amount = submit.params.amount
    var cardinfoURL = submit.params.cardinfoURL
    val sessionData = submit.session.sessionData

    val vendor =
      if (sessionData.customerId.isDefined && successURL.isEmpty && errorURL.isEmpty && cardinfoURL.isEmpty) {
        transactionUUID = sessionData.transactionUuid.get
        errorURL = sessionData.errorURL
        successURL = sessionData.successURL
        transactionType = sessionData.transactionType.orNull
        amount = sessionData.amount.getOrElse(0L)
        EsClient.load[Account](sessionData.vendorId.get).orNull
      } else if (submit.params.merchantId.isDefined) {
        EsClient.load[Account](submit.params.merchantId.get).orNull
      } else {
        null
      }

    if (vendor == null) {
      return Failure(new AccountDoesNotExistError)
    }

    val transactionRequest: TransactionRequest = EsClient.load[TransactionRequest](transactionUUID).orNull
    if (transactionRequest == null) {
      return Failure(new TransactionNotFoundException)
    }
    if (transactionRequest.amount != amount) {
      return Failure(new UnexpectedAmountException)
    }

    var transactionExtra = transactionRequest.extra.orNull

    val listShipping = sessionData.accountId.map { accountId =>
      shippingPrices(transactionRequest.currency.code, transactionExtra, accountId)
    } getOrElse (Success(Seq[ShippingPrice]()))

    var selectedShippingPrice: Option[ShippingPrice] = None
    listShipping map { listShipping =>
      if (listShipping.length > 0) {
        if (sessionData.selectShippingPrice.isEmpty) {
          return Failure(new Exception)
        } else {
          val sp = sessionData.selectShippingPrice.get
          selectedShippingPrice = shippingPrice(listShipping, sp.provider, sp.service, sp.rateType)

          if (selectedShippingPrice.isEmpty) return Failure(new Exception)
        }
      }
    }

    if (selectedShippingPrice.isDefined) {
      val cart0: JValue = parse(transactionExtra) merge parse( s""""
        {
        "shipping" : ${selectedShippingPrice.get.price}
        }
        """)

      val cart1 = cart0 merge parse( s"""
        {
        "finalPrice" : ${(cart0 \ "finalPrice").extract[Long] + selectedShippingPrice.get.price}
        }
      """)
      transactionExtra = render(cart1).toString
    }

    val transactionCurrency: TransactionCurrency = transactionRequest.currency
    //transactionRequestHandler.update(tr.copy(currency = null)) TODO: uncomment this
    EsClient.delete[TransactionRequest](transactionRequest.uuid)

    val transaction: Option[BOTransaction] = EsClient.load[BOTransaction](transactionUUID)
    if (transaction.isDefined) return Failure(new BOTransactionNotFoundException)

    def checkParameters(vendor: Account): Boolean = {
      def checkBCParameters(paymentConfig: PaymentConfig): Boolean = {
        (CBPaymentProvider.NONE != paymentConfig.cbProvider
          && paymentConfig.cbParam != None
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

    if (amount <= 0 && !mogopayAuth)
      return Failure(new SomeParameterIsMissingException("Not a mogopay payment and amount is missing."))
    if (transactionUUID.isEmpty)
      return Failure(new SomeParameterIsMissingException("transactionUUID is missing."))
    if (successURL.isEmpty)
      return Failure(new SomeParameterIsMissingException("successURL is missing."))
    if (errorURL.isEmpty)
      return Failure(new SomeParameterIsMissingException("errorURL is missing."))
    if (!checkParameters(vendor))
      return Failure(new SomeParameterIsMissingException("Invalid vendor parameters."))

    sessionData.amount = Some(amount)
    sessionData.transactionUuid = Some(transactionUUID)
    sessionData.errorURL = errorURL
    sessionData.successURL = successURL
    sessionData.cardinfoURL = submit.params.cardinfoURL
    sessionData.transactionType = Some(transactionType)
    sessionData.vendorId = Some(vendor.uuid)
    sessionData.paymentConfig = vendor.paymentConfig
    sessionData.cardSave = submit.params.saveCard.getOrElse(false)

    sessionData.mogopay = submit.params.customerPassword.nonEmpty || (submit.params.ccNum.isEmpty && submit.params.customerCVV.nonEmpty)
    if (sessionData.mogopay) {
      // this is a mogopay payment
      if (transactionType == "CREDIT_CARD") {
        // only credit card payments are supported through mogopay
        if (submit.params.customerCVV.nonEmpty) {
          val paymentRequest = initPaymentRequest(vendor, transactionType, true, transactionRequest.tid,
            transactionExtra, transactionCurrency, sessionData, submit.params.transactionDescription.orNull,
            submit.params.customerCVV.orNull, submit.params.ccNum.orNull, submit.params.ccMonth.orNull, submit.params.ccYear.orNull,
            submit.params.ccType.orNull)
          paymentRequest match {
            case Failure(t) => Failure(t)
            case Success(payment) => {
              sessionData.paymentRequest = Some(payment)
              sessionData.password = submit.params.customerPassword
              if (submit.params.customerCVV.nonEmpty) {
                // user is already authenticated. We check start the mogopayment
                // the user has been authenticated at this step.
                //forward(controller: "mogopay", action: "startPayment", params: params + [xtoken: sessionData.csrfToken])
                //Success((buildNewSubmit(submit, newSession), "startPayment"))
                Success(("mogopay", "start-payment"))
              }
              else {
                // User submitted a password we authenticate him
                // we redirect the user to authentication screen
                // but we need first to recreate the transaction request
                EsClient.index(transactionRequest, false)
                //forward(controller: "mogopay", action: "authenticate", params: params + [xtoken: sessionData.csrfToken])
                //Success((build{ewSubmit(submit, newSession), "authenticate"))
                Success(("mogopay", "authenticate"))
              }
            }
          }
        }
        else {
          Failure(SomeParameterIsMissingException("CVV is missing"))
        }
      }
      else {
        return Failure(new NotACreditCardTransactionException)
      }
    } else {
      val paymentRequest = initPaymentRequest(vendor, transactionType, false, transactionRequest.tid,
        transactionExtra, transactionCurrency, sessionData, submit.params.transactionDescription.orNull,
        submit.params.customerCVV.orNull, submit.params.ccNum.orNull, submit.params.ccMonth.orNull, submit.params.ccYear.orNull,
        submit.params.ccType.orNull)

      paymentRequest match {
        case Failure(t) => Failure(t)
        case Success(payment) => {
          sessionData.paymentRequest = Some(payment)
          val handler = if (submit.session.sessionData.transactionType == Some("CREDIT_CARD")) {
            sessionData.paymentConfig.get.cbProvider.toString.toLowerCase
          } else {
            sessionData.transactionType.get.toLowerCase
          }
          Success((handler, "start-payment"))
        }
      }
    }
  }

  def computePrice(address: ShippingAddress, currencyCode: String, cart: JValue): Seq[ShippingPrice] = {
    val servicesList: Seq[ShippingService] = Seq(kialaShippingHandler)

    servicesList.map { service =>
      service.calculatePrice(address, currencyCode, cart)
    }.flatten
  }

  private def initPaymentRequest
  (vendor: Account, transactionType: String, mogopay: Boolean,
   transactionSequence: Long, transactionExtra: String,
   transactionCurrency: TransactionCurrency, sessionData: SessionData,
   transactionDesc: String, ccCrypto: String, card_number: String,
   card_month: String, card_year: String, card_type: String)
  : Try[PaymentRequest] = {
    var errors: mutable.Seq[Exception] = mutable.Seq()

    var cc_type: CreditCardType = null
    val transactionEmail: String = sessionData.email.orNull
    var cc_num: String = null
    var cc_month: String = null
    var cc_year: String = null

    if (mogopay) {
      val account: Account = EsClient.load[Account](sessionData.customerId.get).orNull
      val card = EsClient.load[Account](account.uuid).map(_.creditCards.headOption.orNull).orNull
      if (card != null) {
        cc_type = card.cardType
        val env = Settings.environment
        //TODO are we using the right env RSA Key ?
        cc_num = RSA.decrypt(card.number, Settings.RSA.privateKey)
        cc_month = new SimpleDateFormat("MM").format(card.expiryDate)
        cc_year = new SimpleDateFormat("yyyy").format(card.expiryDate)
      } else {
        return Failure(new SomeParameterIsMissingException(MogopayConstant.InvalidAccount))
      }
    } else {
      cc_type = toCardType(card_type)
      cc_num = if (card_number != null) card_number.replaceAll(" ", "") else card_number
      cc_month = card_month
      cc_year = card_year
    }

    val amount: Long = sessionData.amount.getOrElse(0L)
    val externalPages: Boolean = vendor.paymentConfig.orNull.paymentMethod == CBPaymentMethod.EXTERNAL
    var paymentProvider = CBPaymentProvider.NONE
    var paymentRequest: PaymentRequest = PaymentRequest("-1", null, -1L, "", "",
      null, null, "", "", "", "", "", "",
      sessionData.csrfToken.orNull, transactionCurrency)

    if (transactionType == "CREDIT_CARD" && (!externalPages || mogopay)) {
      paymentProvider = vendor.paymentConfig.orNull.cbProvider
      if (cc_type == null) {
        return Failure(new SomeParameterIsMissingException(MogopayConstant.CreditCardTypeRequired))
      }
      else if (cc_num == null) {
        return Failure(new SomeParameterIsMissingException(MogopayConstant.CreditCardNumRequired))
      }
      else if (cc_month == null) {
        return Failure(new SomeParameterIsMissingException(MogopayConstant.CreditCardMonthRequired))
      }
      else if (cc_year == null) {
        return Failure(new SomeParameterIsMissingException(MogopayConstant.CreditCardYearRequired))
      }
      else if (ccCrypto == null) {
        return Failure(new SomeParameterIsMissingException(MogopayConstant.CreditCardCryptoRequired))
      }
      else {
        // Construction et controle de la date de validite
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, cc_month.toInt - 1)
        cal.set(Calendar.YEAR, cc_year.toInt)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.MONTH))
        // ce n'est pas à nous de contrôler la date de validité. C'est au prestataire de paiement
        val cc_date = cal.getTime
        if (cc_date.compareTo(new Date()) < 0) {
          return Failure(new SomeParameterIsMissingException(MogopayConstant.CreditCardExpiryDateInvalid))
        }
        else {
          paymentRequest = paymentRequest.copy(
            cardType = cc_type,
            ccNumber = cc_num,
            expirationDate = cc_date,
            cvv = ccCrypto
          )
        }
      }
    }

    paymentRequest = paymentRequest.copy(
      transactionEmail = transactionEmail,
      transactionExtra = transactionExtra,
      id = transactionSequence.toString,
      orderDate = new Date,
      amount = amount
    )
    Success(paymentRequest)
  }
}
