package com.mogobiz.pay.config

import com.ebiznext.mogopay.payment.SipsHandler
import com.mogobiz.pay.handlers._
import com.mogobiz.pay.handlers.payment._
import com.mogobiz.pay.handlers.shipping._

object  MogopayHandlers {
  val authorizeNetHandler = new AuthorizeNetHandler("authorizenet")
  val accountAddressHandler = new AccountAddressHandler
  val accountHandler = new AccountHandler
  val backofficeHandler = new BackofficeHandler
  val boCreditCardHandler = new BOCreditCardHandler
  val boTransactionHandler = new BOTransactionHandler
  val boTransactionLogHandler = new BOTransactionLogHandler
  val countryAdminHandler = new CountryAdminHandler
  val countryImportHandler = new CountryImportHandler
  val rateImportHandler = new RateImportHandler
  val countryHandler = new CountryHandler
  val creditCardHandler = new CreditCardHandler
  val rateHandler = new RateHandler
  val roleHandler = new RoleHandler
  val shippingAddressHandler = new ShippingAddressHandler
  val smsHandler: ClickatellSMSHandler = new ClickatellSMSHandler {}
  val telephoneHandler = new TelephoneHandler
  val transactionHandler = new TransactionHandler
  val transactionRequestHandler = new TransactionRequestHandler
  val transactionSequenceHandler = new TransactionSequenceHandler

  val noShippingHandler = new NoShippingHandler
  val kialaShippingHandler = new KialaShippingHandler
  val systempayHandler = new SystempayHandler("systempay")
  val sipsHandler = new SipsHandler("sips")
  val payPalHandler = new PayPalHandler("paypal")
  val applePayHandler = new ApplePayHandler("applepay")
  val paylineHandler = new PaylineHandler("payline")
  val mogopayHandler = new MogopayHandler("mogopay")
  val userHandler = new UserHandler
  val payboxHandler = new PayboxHandler("paybox")
  val pdfHandler = new PdfHandler
  val templateHandler = new TemplateHandler

}
