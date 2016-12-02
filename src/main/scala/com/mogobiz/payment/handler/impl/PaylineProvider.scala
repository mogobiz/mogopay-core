package com.mogobiz.payment.handler.impl

/**
  * Created by yoannbaudy on 22/11/2016.
  */
class PaylineProvider {
}

  /*
  val PROVIDER_PAYLINE = "PAYLINE"
  val STEP_START_EXTERNAL_PAYMENT = "START_EXTERNAL_PAYMENT"
  val STEP_PROCESS_PAYMENT_RETURN = "PROCESS_PAYMENT_RETURN"
  val STEP_AUTHORIZE_PAYMENT = "AUTHORIZE_PAYMENT"
  val STEP_VERIFY_ENROLLMENT = "VERIFY_ENROLLMENT"
  val STEP_VALIDATE_PAYMENT = "VALIDATE_PAYMENT"
  val SUCCESS_CODE = "00000"

  val ACTION_AUTHORISATION: String            = "100"
  val ACTION_VALIDATION: String               = "201"
  val ACTION_REFUND: String                   = "421"
  val MODE_COMPTANT: String                   = "CPT"
  val WEB_PAYMENT_API_SERVICE_NAME: QName                      = new QName("http://impl.ws.payline.experian.com", "WebPaymentAPI")


  override def routes(): Route = {
    pathPrefix("testPayline") {
      path("done" / Segment) { paymentUuid =>
        get {
          handleCall(this.processPaylineCallback(paymentUuid), (data: Uri) =>
              redirect(data, StatusCodes.TemporaryRedirect)
          )
        }
      } ~
        path("callback"/ Segment) { paymentUuid =>
          get {
            handleCall(this.processPaylineCallback(paymentUuid), (data: Uri) =>
              complete(StatusCodes.OK)
            )
          }
        } ~
        path("3ds"/ Segment) { paymentUuid =>
          post {
            entity(as[FormData]) { formData =>
              handleCall(this.processPayline3DSCallback(paymentUuid, formData.fields.toMap), (data: Uri) =>
                redirect(data, StatusCodes.TemporaryRedirect)
              )
            }
          }
        }
      }
  }

  override def startExternalPayment(config: PaylinePaymentConfig, ref: String, amount: Long, currency: Currency): ExternalPaymentResult = {
    val paymentData = PaylinePaymentData(config, ref, amount, currency)

    val boPayment = createBOPayment(ref, serialiseData(paymentData))

    val payment: Payment = new Payment
    payment.setAmount(amount.toString)
    payment.setCurrency(currency.numericCode.toString)
    payment.setAction(ACTION_AUTHORISATION)
    payment.setMode(MODE_COMPTANT)
    payment.setContractNumber(config.contractNumber)

    val order : Order = new Order
    order.setRef(ref)
    order.setAmount(payment.getAmount)
    order.setCurrency(payment.getCurrency)
    order.setDate(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date))

    val contractList = new SelectedContractList()
    contractList.getSelectedContract.add(payment.getContractNumber)

    val request: DoWebPaymentRequest = new DoWebPaymentRequest
    request.setVersion(setting.version)
    request.setPayment(payment)
    request.setSelectedContractList(contractList)
    request.setReturnURL(setting.localRouteEndpoint + "/testPayline/done/" + boPayment.uuid)
    request.setCancelURL(request.getReturnURL)
    request.setNotificationURL(setting.localRouteEndpoint + "/testPayline/callback" + boPayment.uuid)
    request.setOrder(order)
    request.setSecurityMode(setting.securityMode)
    request.setLanguageCode(setting.languageCode)
    request.setBuyer(null)
    request.setPrivateDataList(null)
    request.setRecurring(null)
    request.setCustomPaymentPageCode(config.customPaymentPageCode.orNull)
    request.setCustomPaymentTemplateURL(config.customPaymentTemplateURL.orNull)

    createBOPaymentLog(boPayment, DIR_OUT, PROVIDER_PAYLINE, STEP_START_EXTERNAL_PAYMENT, transformDoWebPaymentRequestAsLog(request))

    val response: DoWebPaymentResponse = createWebPaymentAPIProxy(config).doWebPayment(request)

    createBOPaymentLog(boPayment, DIR_IN, PROVIDER_PAYLINE, STEP_START_EXTERNAL_PAYMENT, transformDoWebPaymentResponseAsLog(response))

    if (response != null && response.getResult != null) {
      if (response.getResult.getCode == SUCCESS_CODE) {
        updateBOPayment(boPayment, serialiseData(paymentData.copy(token = Some(response.getToken))))
        ExternalPaymentResult(boPayment.uuid, Uri(response.getRedirectURL))
      }
      else
        throw NotAvailablePaymentGatewayException(response.getResult.getCode)
    }
    else
      throw NotAvailablePaymentGatewayException("Unkown")
  }

  override def authorizedPayment(config: PaylinePaymentConfig, ref: String, amount: Long, currency: Currency, params: AuthorizedPaymentParam): Either[AuthorizedPaymentResult, ThreeDSRedirection] = {
    val paymentData = PaylinePaymentData(config, ref, amount, currency, Some(params.asInstanceOf[AuthorizedPaymentParamImpl]))

    val boPayment = createBOPayment(ref, serialiseData(paymentData))

    if (config.threeDSUse == ThreeDSUse.THREEDS_REQUIRED || config.threeDSUse == ThreeDSUse.THREEDS_IF_AVAILABLE) {
      val threeDSResult = callVerifyEnrollment(paymentData, boPayment)
      if (threeDSResult.threeDSOK) {
        val html = s"""
            <html>
              <head>
              </head>
              <body>
                Redirection...
                <form id="formpay" action="${threeDSResult.url}" method="${threeDSResult.method}" >
                <input type="hidden" name="${threeDSResult.pareqName}" value="${threeDSResult.pareqValue}" />
                <input type="hidden" name="${threeDSResult.termUrlName}" value="${threeDSResult.termUrlValue}" />
                <input type="hidden" name="${threeDSResult.mdName}" value="${threeDSResult.mdValue}" />
                </form>
                <script>document.getElementById("formpay").submit();</script>
              </body>
            </html>"""
        Right(ThreeDSRedirection(html))
      }
      else if (config.threeDSUse == ThreeDSUse.THREEDS_IF_AVAILABLE)
        Left(callAuthorizedPayment(paymentData, boPayment, None, None))
      else {
        val newStatus = PaymentStatus.THREEDS_REQUIRED
        updateBOPaymentStatus(boPayment, newStatus, serialiseData(paymentData))
        Left(AuthorizedPaymentResultImpl(boPayment.uuid, newStatus))
      }
    }
    else Left(callAuthorizedPayment(paymentData, boPayment, None, None))
  }

  override def validatePayment(config: PaylinePaymentConfig, paymentUuid: String, amount: Long, currency: Currency): ValidatePaymentResult = {
    findBOPayment(paymentUuid).map { boPayment =>
      val paymentData = deserialiseData(boPayment.extra)
      paymentData.transactionId.map { transactionId =>
        val config = paymentData.config

        val payment: Payment = new Payment
        payment.setAmount(amount.toString)
        payment.setCurrency(currency.numericCode.toString)
        payment.setAction(ACTION_VALIDATION)
        payment.setMode(MODE_COMPTANT)
        payment.setContractNumber(config.contractNumber)

        val request = new DoCaptureRequest()
        request.setTransactionID(transactionId)
        request.setPayment(payment)

        createBOPaymentLog(boPayment, DIR_OUT, PROVIDER_PAYLINE, STEP_VALIDATE_PAYMENT, transformDoCaptureRequestAsLog(request))

        val response = createDirectPaymentAPIProxy(config).doCapture(request)

        createBOPaymentLog(boPayment, DIR_IN, PROVIDER_PAYLINE, STEP_VALIDATE_PAYMENT, transformDoCaptureResponseAsLog(response))

        val newStatus = if (response != null && response.getResult != null && response.getResult.getCode == SUCCESS_CODE)
          PaymentStatus.VALIDATED_PARTIALLY
        else PaymentStatus.VALIDATION_FAILED

        updateBOPaymentStatus(boPayment, newStatus, serialiseData(paymentData))

        ValidatePaymentResultImpl(newStatus)
      }.getOrElse {
        val error = s"Unabled to validate payment $paymentUuid. The transactionId is undefined"
        logger.error(error)
        throw NotAvailablePaymentGatewayException(error)
      }
    }.getOrElse {
      val error = s"Unabled to validate payment $paymentUuid. The payment is not found"
      logger.error(error)
      throw NotAvailablePaymentGatewayException(error)
    }
  }

  override def refundPayment(config: PaylinePaymentConfig, paymentUuid: String, amount: Long, currency: Currency): RefundPaymentResult = {
    findBOPayment(paymentUuid).map { boPayment =>
      val paymentData = deserialiseData(boPayment.extra)
      paymentData.transactionId.map { transactionId =>
        val config = paymentData.config

        val payment: Payment = new Payment
        payment.setAmount(amount.toString)
        payment.setCurrency(currency.numericCode.toString)
        payment.setAction(ACTION_REFUND)
        payment.setMode(MODE_COMPTANT)
        payment.setContractNumber(config.contractNumber)

        val request = new DoRefundRequest
        request.setVersion(setting.version)
        request.setTransactionID(transactionId)
        request.setPayment(payment)

        createBOPaymentLog(boPayment, DIR_OUT, PROVIDER_PAYLINE, STEP_VALIDATE_PAYMENT, transformDoRefundRequestAsLog(request))

        val response = createDirectPaymentAPIProxy(config).doRefund(request)

        createBOPaymentLog(boPayment, DIR_IN, PROVIDER_PAYLINE, STEP_VALIDATE_PAYMENT, transformDoRefundResponseAsLog(response))

        val newStatus = if (response != null && response.getResult != null && response.getResult.getCode == SUCCESS_CODE)
          PaymentStatus.REFUNDED_PARTIALLY
        else PaymentStatus.REFUND_FAILED

        updateBOPaymentStatus(boPayment, newStatus, serialiseData(paymentData))

        RefundPaymentResultImpl(newStatus)
      }.getOrElse {
        val error = s"Unabled to refund payment $paymentUuid. The transactionId is undefined"
        logger.error(error)
        throw NotAvailablePaymentGatewayException(error)
      }
    }.getOrElse {
      val error = s"Unabled to refund payment $paymentUuid. The payment is not found"
      logger.error(error)
      throw NotAvailablePaymentGatewayException(error)
    }
  }

  override def cancelPayment(config: PaylinePaymentConfig, paymentUuid: String): CancelPaymentResult = {
    findBOPayment(paymentUuid).map { boPayment =>
      val paymentData = deserialiseData(boPayment.extra)
      paymentData.transactionId.map { transactionId =>
        val config = paymentData.config

        val request = new DoResetRequest
        request.setVersion(setting.version)
        request.setTransactionID(transactionId)

        createBOPaymentLog(boPayment, DIR_OUT, PROVIDER_PAYLINE, STEP_VALIDATE_PAYMENT, transformDoResetRequestAsLog(request))

        val response = createDirectPaymentAPIProxy(config).doReset(request)

        createBOPaymentLog(boPayment, DIR_IN, PROVIDER_PAYLINE, STEP_VALIDATE_PAYMENT, transformDoResetResponseAsLog(response))

        val newStatus = if (response != null && response.getResult != null && response.getResult.getCode == SUCCESS_CODE)
          PaymentStatus.CANCELED
        else PaymentStatus.CANCEL_FAILED

        updateBOPaymentStatus(boPayment, newStatus, serialiseData(paymentData))

        CancelPaymentResultImpl(newStatus)
      }.getOrElse {
        val error = s"Unabled to cancel payment $paymentUuid. The transactionId is undefined"
        logger.error(error)
        throw NotAvailablePaymentGatewayException(error)
      }
    }.getOrElse {
      val error = s"Unabled to cancel payment $paymentUuid. The payment is not found"
      logger.error(error)
      throw NotAvailablePaymentGatewayException(error)
    }
  }

  def callAuthorizedPayment(paymentData: PaylinePaymentData, boPayment: BOPayment, paylineMd: Option[String], paylinePares: Option[String]): AuthorizedPaymentResult = {
    val config = paymentData.config
    val params = paymentData.cbParams.get

    val payment: Payment = new Payment
    payment.setAmount(paymentData.amount.toString)
    payment.setCurrency(paymentData.currency.numericCode.toString)
    payment.setAction(ACTION_AUTHORISATION)
    payment.setMode(MODE_COMPTANT)
    payment.setContractNumber(config.contractNumber)

    val card: Card = new Card
    card.setNumber(params.creditCardNumber)
    card.setType(fromCreditCardType(params.creditCardType))
    card.setExpirationDate(new SimpleDateFormat("MMyy").format(params.creditCardExpirationDate))
    card.setCvx(params.creditCardCrypto)

    val order : Order = new Order
    order.setRef(paymentData.ref)
    order.setAmount(payment.getAmount)
    order.setCurrency(payment.getCurrency)
    order.setDate(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date))

    val authen: Authentication3DSecure = if (paylineMd.isDefined && paylinePares.isDefined) {
      val authen = new Authentication3DSecure
      authen.setMd(paylineMd.get)
      authen.setPares(paylinePares.get)
      authen
    } else null

    val request: DoAuthorizationRequest = new DoAuthorizationRequest
    request.setPayment(payment)
    request.setCard(card)
    request.setOrder(order)
    request.setAuthentication3DSecure(authen)

    createBOPaymentLog(boPayment, DIR_OUT, PROVIDER_PAYLINE, STEP_AUTHORIZE_PAYMENT, transformDoAuthorizationRequestAsLog(request))

    val response: DoAuthorizationResponse = createDirectPaymentAPIProxy(config).doAuthorization(request)

    createBOPaymentLog(boPayment, DIR_IN, PROVIDER_PAYLINE, STEP_AUTHORIZE_PAYMENT, transformDoAuthorizationResponseAsLog(response))

    val newStatus = if (response != null && response.getResult != null) {
      if (response.getResult.getCode == SUCCESS_CODE) PaymentStatus.AUTHORIZED
      else PaymentStatus.REFUSED
    }
    else PaymentStatus.FAILED
    val transactionId = if (response != null && response.getTransaction != null) Option(response.getTransaction.getId) else None

    updateBOPaymentStatus(boPayment, newStatus, serialiseData(paymentData.copy(transactionId = transactionId)))

    AuthorizedPaymentResultImpl(boPayment.uuid, newStatus)
  }

  def callVerifyEnrollment(paymentData: PaylinePaymentData, boPayment: BOPayment): ThreeDSResult = {
    val config = paymentData.config
    val params = paymentData.cbParams.get

    val payment: Payment = new Payment
    payment.setAmount(paymentData.amount.toString)
    payment.setCurrency(paymentData.currency.numericCode.toString)
    payment.setAction(ACTION_AUTHORISATION)
    payment.setMode(MODE_COMPTANT)
    payment.setContractNumber(config.contractNumber)

    val card: Card = new Card
    card.setNumber(params.creditCardNumber)
    card.setType(fromCreditCardType(params.creditCardType))
    card.setExpirationDate(new SimpleDateFormat("MMyy").format(params.creditCardExpirationDate))
    card.setCvx(params.creditCardCrypto)

    val request: VerifyEnrollmentRequest = new VerifyEnrollmentRequest
    request.setPayment(payment)
    request.setCard(card)
    request.setOrderRef(paymentData.ref)

    createBOPaymentLog(boPayment, DIR_OUT, PROVIDER_PAYLINE, STEP_VERIFY_ENROLLMENT, transformVerifyEnrollmentRequestAsLog(request))

    val response: VerifyEnrollmentResponse = createDirectPaymentAPIProxy(config).verifyEnrollment(request)

    createBOPaymentLog(boPayment, DIR_OUT, PROVIDER_PAYLINE, STEP_VERIFY_ENROLLMENT, transformVerifyEnrollmentResponseAsLog(response))

    val threeDSOk = if (response != null && response.getResult != null)
      response.getResult.getCode == SUCCESS_CODE || response.getResult.getCode == "03000"
    else false

    ThreeDSResult(threeDSOk,
      response.getActionUrl,
      response.getActionMethod,
      response.getMdFieldName,
      response.getMdFieldValue,
      response.getPareqFieldName,
      response.getPareqFieldValue,
      response.getTermUrlName,
      setting.localRouteEndpoint + "/testPayline/3ds/" + boPayment.uuid)
  }

  protected def fromCreditCardType(creditType: CreditCardType.CreditCardType): String = creditType match {
    case CreditCardType.MASTER_CARD => "MASTERCARD"
    case CreditCardType.AMEX => "AMEX"
    case CreditCardType.SWITCH => "SWITCH"
    case _ => "CB"
  }

  protected def processPaylineCallback(paymentUuid: String) : Uri = {
    findBOPayment(paymentUuid).map { boPayment =>
      val paymentData = deserialiseData(boPayment.extra)
      if (paymentData.token.isDefined) {
        val token = paymentData.token.get
        val request: GetWebPaymentDetailsRequest = new GetWebPaymentDetailsRequest
        request.setVersion(setting.version)
        request.setToken(token)

        createBOPaymentLog(boPayment, DIR_OUT, PROVIDER_PAYLINE, STEP_PROCESS_PAYMENT_RETURN, transformGetWebPaymentDetailsRequestAsLog(request))

        val response : GetWebPaymentDetailsResponse = createWebPaymentAPIProxy(paymentData.config).getWebPaymentDetails(request)

        createBOPaymentLog(boPayment, DIR_IN, PROVIDER_PAYLINE, STEP_PROCESS_PAYMENT_RETURN, transformGetWebPaymentDetailsResponseAsLog(response))

        val newStatus = if (response != null && response.getResult != null && response.getResult.getCode == SUCCESS_CODE) PaymentStatus.COMPLETED
        else  PaymentStatus.FAILED
        val transactionId = if (response != null && response.getTransaction != null) Option(response.getTransaction.getId) else None

        updateBOPaymentStatus(boPayment, newStatus, serialiseData(paymentData.copy(transactionId = transactionId)))
      }
      else
        logger.error(s"Unabled to process payline callback for payment $paymentUuid. The token is not found")
      paymentData.config.redirectionUrl
    }.getOrElse {
      val error = s"Unabled to process payline callback for payment $paymentUuid. The payment is not found"
      logger.error(error)
      throw NotAvailablePaymentGatewayException(error)
    }
  }

  protected def processPayline3DSCallback(paymentUuid: String, params: Map[String, String]) : Uri = {
    findBOPayment(paymentUuid).map { boPayment =>
      val paymentData = deserialiseData(boPayment.extra)

      callAuthorizedPayment(paymentData, boPayment, Option(params("MD")), Option(params("PaRes")))
      paymentData.config.redirectionUrl
    }.getOrElse {
      val error = s"Unabled to process payline 3DS callback for payment $paymentUuid. The payment is not found"
      logger.error(error)
      throw NotAvailablePaymentGatewayException(error)
    }
  }

  protected def serialiseData(data: PaylinePaymentData): String = {
    JacksonConverter.serialize(data)
  }

  protected def deserialiseData(json: String): PaylinePaymentData = {
    JacksonConverter.deserialize[PaylinePaymentData](json)
  }

  protected def transformDoResetRequestAsLog(data: DoResetRequest) : List[String] = {
    if (data == null) Nil
    else List(
      "transaction.id=" + data.getTransactionID
    )
  }

  protected def transformDoResetResponseAsLog(data: DoResetResponse) : List[String] = {
    if (data == null) Nil
    else transformResultAsLog(data.getResult) ++ transformTransactionAsLog(data.getTransaction)
  }

  protected def transformDoRefundRequestAsLog(data: DoRefundRequest) : List[String] = {
    if (data == null) Nil
    else List(
      "version=" + data.getVersion,
      "transaction.id=" + data.getTransactionID
    ) ++ transformPaymentAsLog(data.getPayment)
  }

  protected def transformDoRefundResponseAsLog(data: DoRefundResponse) : List[String] = {
    if (data == null) Nil
    else transformResultAsLog(data.getResult) ++ transformTransactionAsLog(data.getTransaction)
  }

  protected def transformDoCaptureRequestAsLog(data: DoCaptureRequest) : List[String] = {
    if (data == null) Nil
    else transformPaymentAsLog(data.getPayment) :+ ("transaction.id=" + data.getTransactionID)
  }

  protected def transformDoCaptureResponseAsLog(data: DoCaptureResponse) : List[String] = {
    if (data == null) Nil
    else transformResultAsLog(data.getResult) ++ transformTransactionAsLog(data.getTransaction)
  }

  protected def transformVerifyEnrollmentRequestAsLog(data: VerifyEnrollmentRequest) : List[String] = {
    if (data == null) Nil
    else transformPaymentAsLog(data.getPayment) ++ transformCardAsLog(data.getCard) :+ ("orderRef=" + data.getOrderRef)
  }

  protected def transformVerifyEnrollmentResponseAsLog(data: VerifyEnrollmentResponse) : List[String] = {
    if (data == null) Nil
    else List(
      "response.actionUrl=" + data.getActionUrl,
      "response.actionMethod=" + data.getActionMethod,
      "response.mdFieldName=" + data.getMdFieldName,
      "response.mdFieldValue=" + data.getMdFieldValue,
      "response.pareqFieldName=" + data.getPareqFieldName,
      "response.pareqFieldValue=" + data.getPareqFieldValue,
      "response.termUrlName=" + data.getTermUrlName,
      "response.termUrlValue=" + data.getTermUrlValue
    ) ++ transformResultAsLog(data.getResult)
  }

  protected def transformDoAuthorizationRequestAsLog(data: DoAuthorizationRequest) : List[String] = {
    if (data == null) Nil
    else transformPaymentAsLog(data.getPayment) ++
      transformCardAsLog(data.getCard) ++
      transformOrderAsLog(data.getOrder) ++
      transformAuthentication3DSecureAsLog(data.getAuthentication3DSecure)
  }

  protected def transformDoAuthorizationResponseAsLog(data: DoAuthorizationResponse) : List[String] = {
    if (data == null) Nil
    else transformResultAsLog(data.getResult) ++
      transformCardOutAsLog(data.getCard) ++
      transformTransactionAsLog(data.getTransaction)
  }

  protected def transformGetWebPaymentDetailsRequestAsLog(data: GetWebPaymentDetailsRequest) : List[String] = {
    if (data == null) Nil
    else List(
      "version=" + data.getVersion,
      "token=" + data.getToken
    )
  }

  protected def transformGetWebPaymentDetailsResponseAsLog(data: GetWebPaymentDetailsResponse) : List[String] = {
    if (data == null) Nil
    else transformResultAsLog(data.getResult) ++
      transformAuthorizationAsLog(data.getAuthorization) ++
      transformCardOutAsLog(data.getCard) ++
      transformTransactionAsLog(data.getTransaction) ++
      transformAuthentication3DSecureAsLog(data.getAuthentication3DSecure)
  }

  protected def transformAuthorizationAsLog(data: Authorization) : List[String] = {
    if (data == null) Nil
    else List(
      "result.authorization.number=" + data.getNumber,
      "result.authorization.date=" + data.getDate
    )
  }

  protected def transformCardOutAsLog(data: CardOut) : List[String] = {
    if (data == null) Nil
    else List(
      "result.card.expirationdate=" + data.getExpirationDate,
      "result.card.number=" + data.getNumber,
      "result.card.type=" + data.getType,
      "result.card.cardholder=" + data.getCardholder
    )
  }

  protected def transformTransactionAsLog(data: Transaction) : List[String] = {
    if (data == null) Nil
    else List(
      "result.transaction.id=" + data.getId,
      "result.transaction.date=" + data.getDate,
      "result.transaction.isPossibleFraud=" + data.getIsPossibleFraud,
      "result.transaction.isDuplicated=" + data.getIsDuplicated
    )
  }

  protected def transformDoWebPaymentResponseAsLog(data: DoWebPaymentResponse) : List[String] = {
    if (data == null) Nil
    else List(
      "result.token=" + data.getToken,
      "result.redirectURL=" + data.getRedirectURL
    ) ++ transformResultAsLog(data.getResult)
  }

  protected def transformResultAsLog(data: Result) : List[String] = {
    if (data == null) Nil
    else List(
      "result.code=" + data.getCode,
      "result.shortMessage=" + data.getShortMessage,
      "result.longMessage=" + data.getLongMessage
    )
  }

  protected def transformDoWebPaymentRequestAsLog(data: DoWebPaymentRequest) : List[String] = {
    if (data == null) Nil
    else List(
      "returnURL=" + data.getReturnURL,
      "languageCode=" + data.getLanguageCode,
      "securityMode=" + data.getSecurityMode,
      "customPaymentPageCode=" + data.getCustomPaymentPageCode,
      "customPaymentTemplateURL=" + data.getCustomPaymentTemplateURL
    ) ++ transformPaymentAsLog(data.getPayment) ++ transformOrderAsLog(data.getOrder)
  }

  protected def transformPaymentAsLog(data: Payment) : List[String] = {
    if (data == null) Nil
    else List(
      "payment.amount=" + data.getAmount,
      "payment.currency=" + data.getCurrency,
      "payment.contractNumbner=" + data.getContractNumber,
      "payment.action=" + data.getAction,
      "payment.mode=" + data.getMode
    )
  }

  protected def transformOrderAsLog(data: Order) : List[String] = {
    if (data == null) Nil
    else List(
      "order.ref=" + data.getRef,
      "order.amount=" + data.getAmount,
      "order.currency=" + data.getCurrency,
      "order.date=" + data.getDate
    )
  }

  protected def transformCardAsLog(data: Card) : List[String] = {
    if (data == null) Nil
    else List(
      "card.number=" + UtilHandler.hideCardNumber(data.getNumber, "X"),
      "card.type=" + data.getType,
      "card.expirationDate=" + data.getExpirationDate,
      "card.cvx=XXX"
    )
  }

  protected def transformAuthentication3DSecureAsLog(data: Authentication3DSecure) : List[String] = {
    if (data == null) Nil
    else List(
      "authentication3DSecure.typeSecurisation=" + data.getTypeSecurisation,
      "authentication3DSecure.vadsResult=" + data.getVadsResult,
      "authentication3DSecure.md=" + data.getMd,
      "authentication3DSecure.pares=" + data.getPares
    )
  }

  protected def createWebPaymentAPIProxy(config: PaylinePaymentConfig) : WebPaymentAPI = {
    val url: URL                  = classOf[PaylineProvider].getResource("/wsdl/WebPaymentAPI_v4.38.wsdl")
    val ss: WebPaymentAPI_Service = new WebPaymentAPI_Service(url, WEB_PAYMENT_API_SERVICE_NAME)
    val proxy: WebPaymentAPI = ss.getWebPaymentAPI
    val proxyAsBindingProvider : BindingProvider = proxy.asInstanceOf[BindingProvider]
    val requestContext = proxyAsBindingProvider.getRequestContext
    requestContext.put("javax.xml.ws.security.auth.username", config.account)
    requestContext.put("javax.xml.ws.security.auth.password", config.key)
    requestContext.put("javax.xml.ws.service.endpoint.address", setting.webEndpoint)
    requestContext.put(TrustedSSLFactory.JaxwsSslSockeetFactory, TrustedSSLFactory.getTrustingSSLSocketFactory)
    requestContext.put(NaiveHostnameVerifier.JaxwsHostNameVerifier, new NaiveHostnameVerifier)
    requestContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true.asInstanceOf[Object])

    val binding: Binding = proxyAsBindingProvider.getBinding
    val handlerList      = binding.getHandlerChain
    //handlerList.add(new TraceHandler(transaction, "PAYLINE")) //TODO à faire
    binding.setHandlerChain(handlerList)
    proxy
  }

  private def createDirectPaymentAPIProxy(config: PaylinePaymentConfig): DirectPaymentAPI = {
    val url: URL                          = classOf[PaylineProvider].getResource("/wsdl/DirectPaymentAPI_v4.38.wsdl")
    val service: DirectPaymentAPI_Service = new DirectPaymentAPI_Service(url)
    val proxy: DirectPaymentAPI           = service.getDirectPaymentAPI
    val proxyAsBindingProvider : BindingProvider = proxy.asInstanceOf[BindingProvider]
    val requestContext = proxyAsBindingProvider.getRequestContext
    requestContext.put("javax.xml.ws.security.auth.username", config.account)
    requestContext.put("javax.xml.ws.security.auth.password", config.key)
    requestContext.put("javax.xml.ws.service.endpoint.address", setting.directEndpoint)
    requestContext.put(TrustedSSLFactory.JaxwsSslSockeetFactory, TrustedSSLFactory.getTrustingSSLSocketFactory)
    requestContext.put(NaiveHostnameVerifier.JaxwsHostNameVerifier, new NaiveHostnameVerifier)
    proxy
  }

}

case class PaylinePaymentConfig(threeDSUse : ThreeDSUse.ThreeDSUse,
                                account: String,
                                key: String,
                                contractNumber: String,
                                redirectionUrl: String,
                                customPaymentPageCode: Option[String],
                                customPaymentTemplateURL: Option[String]) extends CBPaymentConfig

case class GlobalPaylineSettings(version: String,
                                 directEndpoint: String,
                                 webEndpoint: String,
                                 securityMode: String,
                                 languageCode: String,
                                 localRouteEndpoint: String)

case class PaylinePaymentData(config: PaylinePaymentConfig,
                              ref: String,
                              amount: Long,
                              currency: Currency,
                              cbParams: Option[AuthorizedPaymentParamImpl] = None,
                              token: Option[String] = None,
                              transactionId: Option[String] = None)

case class ThreeDSResult(threeDSOK: Boolean,
                         url: String,
                         method: String,
                         mdName: String,
                         mdValue: String,
                         pareqName: String,
                         pareqValue: String,
                         termUrlName: String,
                         termUrlValue: String)

object PaylineProviderTest extends App with BootedMogobizSystem {
  es.Settings
  Settings

  ActorSystemLocator(system)

  val settings = GlobalPaylineSettings("3",
    "https://homologation.payline.com/V4/services/DirectPaymentAPI",
    "https://homologation.payline.com/V4/services/WebPaymentAPI",
    "SSL",
    "eng",
    "http://localhost:12000"
  )

  implicit val bOPaymentStorage = new BOPaymentStorageHandlerImpl
  implicit val bOPaymentLogStorage = new BOPaymentLogStorageHandlerImpl

  val paylineProvider = new PaylineProvider(settings)

  val routesServices = system.actorOf(Props(new RoutedHttpService(paylineProvider.getRoutes())))

  IO(Http)(system) ! Http.Bind(routesServices, interface = "localhost", port = 12000)

  val config3DSRequis = PaylinePaymentConfig(ThreeDSUse.THREEDS_REQUIRED,
                                    "26399702760590",
                                    "SH0gPsNhvHmePmlZz3Mj",
                                    "1234567", "http://www.google.fr", None, None)
  val config3DSOptional = config3DSRequis.copy(threeDSUse = ThreeDSUse.THREEDS_IF_AVAILABLE)
  val configNo3DS = config3DSRequis.copy(threeDSUse = ThreeDSUse.THREEDS_NO)

  val ref = "TEST YO"
  val amount = 10000
  val currency = Currency("EUR", 978)


  val cal = Calendar.getInstance()
  cal.add(Calendar.YEAR, 1)
  val params3DS = AuthorizedPaymentParamImpl("4970101122334455", CreditCardType.CB, cal.getTime, "123")
  val params2DS = AuthorizedPaymentParamImpl("1111222233334444", CreditCardType.CB, cal.getTime, "123")
*/

  /*
    println("TEST payment externe")
    println(paylineProvider.startExternalPayment(config3DSRequis, ref, amount, currency))

  println("TEST carte 3DS avec 3DS requis")
  paylineProvider.authorizedPayment(config3DSRequis, ref, amount, currency, params3DS) match {
    case Left(r : AuthorizedPaymentResult) => println(r.status)
    case Right(r : ThreeDSRedirection) => println(r.html)
  }

  println("TEST carte 2DS avec 3DS requis")
  paylineProvider.authorizedPayment(config3DSRequis, ref, amount, currency, params2DS) match {
    case Left(r : AuthorizedPaymentResult) => println(r.status)
    case Right(r : ThreeDSRedirection) => println(r.html)
  }

  println("TEST carte 3DS avec 3DS optionel")
  paylineProvider.authorizedPayment(config3DSOptional, ref, amount, currency, params3DS) match {
    case Left(r : AuthorizedPaymentResult) => println(r.status)
    case Right(r : ThreeDSRedirection) => println(r.html)
  }

  println("TEST carte 2DS avec 3DS optionel")
  paylineProvider.authorizedPayment(config3DSOptional, ref, amount, currency, params2DS) match {
    case Left(r : AuthorizedPaymentResult) => println(r.status)
    case Right(r : ThreeDSRedirection) => println(r.html)
  }

  println("TEST carte 3DS sans 3DS")
  paylineProvider.authorizedPayment(configNo3DS, ref, amount, currency, params3DS) match {
    case Left(r : AuthorizedPaymentResult) => println(r.status)
    case Right(r : ThreeDSRedirection) => println(r.html)
  }

  println("TEST carte 2DS sans 3DS")
  paylineProvider.authorizedPayment(configNo3DS, ref, amount, currency, params2DS) match {
    case Left(r : AuthorizedPaymentResult) => println(r.status)
    case Right(r : ThreeDSRedirection) => println(r.html)
  }
*/
/*
  println("TEST Remboursement")
  paylineProvider.authorizedPayment(configNo3DS, ref, amount, currency, params3DS) match {
    case Left(r : AuthorizedPaymentResult) =>
      println(paylineProvider.validatePayment(configNo3DS, r.paymentUuid, amount, currency))
      println(paylineProvider.refundPayment(configNo3DS, r.paymentUuid, amount, currency))
    case Right(r : ThreeDSRedirection) => println(r.html)
  }
  */
/*
  println("TEST annulation")
  paylineProvider.authorizedPayment(configNo3DS, ref, amount, currency, params3DS) match {
    case Left(r : AuthorizedPaymentResult) => println(paylineProvider.cancelPayment(configNo3DS, r.paymentUuid))
    case Right(r : ThreeDSRedirection) => println(r.html)
  }
  */
//}