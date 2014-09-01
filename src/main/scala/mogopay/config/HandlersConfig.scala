package mogopay.config

import mogopay.handlers._
import mogopay.handlers.payment._
import mogopay.handlers.shipping._

object HandlersConfig {
  val accountAddressHandler = new AccountAddressHandler
  val accountHandler = new AccountHandler
  val backofficeHandler = new BackofficeHandler
  val boCreditCardHandler = new BOCreditCardHandler
  val boTransactionLogHandler = new BOTransactionLogHandler
  val countryAdminHandler = new CountryAdminHandler
  val countryImportHandler = new CountryImportHandler
  val countryHandler = new CountryHandler
  val creditCardHandler = new CreditCardHandler
  val rateHandler = new RateHandler
  val roleHandler = new RoleHandler
  val shippingAddressHandler = new ShippingAddressHandler
  val smsHandler: ClickatellSMSHandler = new ClickatellSMSHandler {}
  val transactionHandler = new TransactionHandler
  val transactionRequestHandler = new TransactionRequestHandler
  val transactionSequenceHandler = new TransactionSequenceHandler

  val kialaShippingHandler = new KialaShippingHandler
  val systempayHandler = new SystempayHandler
  val systempayPaymentHandler = new SystempayClient
  val payPalHandler = new PayPalHandler
  val userHandler = new UserHandler
  val payboxHandler = new PayboxHandler
}
