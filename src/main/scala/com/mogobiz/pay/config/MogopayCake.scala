package com.mogobiz.pay.config

import com.ebiznext.mogopay.payment.SipsHandler
import com.mogobiz.pay.handlers._
import com.mogobiz.pay.handlers.connector.MiraklHandler
import com.mogobiz.pay.handlers.payment._
import com.mogobiz.pay.handlers.shipping.{EasyPostHandler, KialaShippingHandler}

trait MogopayCake {
  def authorizeNetHandler: AuthorizeNetHandler

  def accountAddressHandler: AccountAddressHandler

  def accountHandler: AccountHandler

  def backofficeHandler: BackofficeHandler

  def boCreditCardHandler: BOCreditCardHandler

  def boTransactionHandler: BOTransactionHandler

  def boShopTransactionHandler: BOShopTransactionHandler

  def boTransactionLogHandler: BOTransactionLogHandler

  def countryAdminHandler: CountryAdminHandler

  def countryImportHandler: CountryImportHandler

  def rateImportHandler: RateImportHandler

  def countryHandler: CountryHandler

  def creditCardHandler: CreditCardHandler

  def rateHandler: RateHandler

  def roleHandler: RoleHandler

  def shippingAddressHandler: ShippingAddressHandler

  def smsHandler: ClickatellSMSHandler

  def telephoneHandler: TelephoneHandler

  def transactionHandler: TransactionHandler

  def transactionRequestHandler: TransactionRequestHandler

  def transactionSequenceHandler: TransactionSequenceHandler

  def kialaShippingHandler: KialaShippingHandler

  def easyPostHandler: EasyPostHandler

  def systempayHandler: SystempayHandler

  def sipsHandler: SipsHandler

  def payPalHandler: PayPalHandler

  def applePayHandler: ApplePayHandler

  def paylineHandler: PaylineHandler

  def mogopayHandler: MogopayHandler

  def userHandler: UserHandler

  def payboxHandler: PayboxHandler

  def pdfHandler: PdfHandler

  def templateHandler: TemplateHandler

  def customPaymentHandler: PaymentHandler = null

  def miraklHandler : MiraklHandler
}

class DefaultMogopayCake extends MogopayCake {
  val accountAddressHandler            = new AccountAddressHandler
  val accountHandler                   = new AccountHandler
  val backofficeHandler                = new BackofficeHandler
  val boCreditCardHandler              = new BOCreditCardHandler
  val boTransactionHandler             = new BOTransactionHandler
  val boShopTransactionHandler         = new BOShopTransactionHandler
  val boTransactionLogHandler          = new BOTransactionLogHandler
  val countryAdminHandler              = new CountryAdminHandler
  val countryImportHandler             = new CountryImportHandler
  val rateImportHandler                = new RateImportHandler
  val countryHandler                   = new CountryHandler
  val creditCardHandler                = new CreditCardHandler
  val rateHandler                      = new RateHandler
  val roleHandler                      = new RoleHandler
  val shippingAddressHandler           = new ShippingAddressHandler
  val smsHandler: ClickatellSMSHandler = new ClickatellSMSHandler {}
  val telephoneHandler                 = new TelephoneHandler
  val transactionRequestHandler        = new TransactionRequestHandler
  val transactionSequenceHandler       = new TransactionSequenceHandler

  val kialaShippingHandler = new KialaShippingHandler
  val easyPostHandler      = new EasyPostHandler
  val userHandler          = new UserHandler
  val pdfHandler           = new PdfHandler
  val templateHandler      = new TemplateHandler

  val transactionHandler  = new TransactionHandler
  val authorizeNetHandler = new AuthorizeNetHandler("authorizenet")
  val payboxHandler       = new PayboxHandler("paybox")
  val systempayHandler    = new SystempayHandler("systempay")
  val sipsHandler         = new SipsHandler("sips")
  val payPalHandler       = new PayPalHandler("paypal")
  val applePayHandler     = new ApplePayHandler("applepay")
  val paylineHandler      = new PaylineHandler("payline")
  val mogopayHandler      = new MogopayHandler("mogopay")

  val miraklHandler       = new MiraklHandler

}
