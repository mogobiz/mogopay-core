/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.codes

import com.mogobiz.pay.config.Settings

object MogopayConstant {
  val SHOP_MOGOBIZ = "MOGOBIZ"

  val UserOrPasswordIsNull          = "user.or.password.is.null"
  val UserUnknown                   = "user.unknown"
  val AccountInactive               = "account.inactive"
  val AccountAlreadyExist           = "account.already.exist"
  val AccountDoesNotExist           = "account.does.not.exist"
  val AccountPasswordError          = "account.password.error"
  val AccountTooManyAttempts        = "account.too.many.attempts"
  val AccountOwnerUnknown           = "account.owner.unknown"
  val AccountPasswordPatternInvalid = "account.password.invalid.pattern"
  val VerifyTimeout                 = "verify.timeout"
  val VerifyInvalidEmail            = "verify.invalid.email"
  val VerifyFail                    = "verify.fail"

  val UnknownPaymentError = "mogopay.errors.payment.unknow";

  val DateFormatWIthoutHour   = "dd/MM/yyyy"
  val DateFormat              = "dd/MM/yyyy HH:mm"
  val DateFormatMonthAndYear  = "MM/yyyy"
  val NumberAccountPerPage    = 50
  val NumerTransactionPerPage = 50
  val NumerEventPerPage       = 50

  val MaxAttempts      = Settings.AccountValidatePasswordMaxattempts
  val MaxAttemptsError = "too.many.attempts"

  // TIMEOUT paiement
  val Timeout = 300

  val CreditCardExpired = "expired"

  val Error   = "error"
  val Success = "success"

  val InvalidPassword             = "invalid.password"
  val UnknownError                = "error.unknown"
  val EmailRequired               = "error.email.required"
  val ThreedsRequired             = "error.3ds.required"
  val CreditCardTypeRequired      = "error.cc.type.required"
  val CreditCardNumRequired       = "error.cc.num.required"
  val CreditCardMonthRequired     = "error.cc.month.required"
  val CreditCardYearRequired      = "error.cc.year.required"
  val CreditCardCryptoRequired    = "error.cc.crypto.required"
  val CreditCardCryptoInvalid     = "error.cc.crypto.invalid"
  val CreditCardExpiryDateInvalid = "error.cc.expirydate.invalid"
  val CreditCardAmountRequired    = "error.cc.amount.required"
  val CreditCardRequired          = "error.cc.required"
  val InvalidPayboxConfig         = "error.configuration.paybox"
  val InvalidPaylineConfig        = "error.configuration.payline"
  val InvalidPaypalConfig         = "error.configuration.paypal"
  val InvalidSipsConfig           = "error.configuration.sips"
  val InvalidSystemPayConfig      = "error.configuration.systempay"
  val PaypalTokenError            = "error.paypal.token"
  val PaypalPayerIdError          = "error.paypal.payerid"
  val PaymentFailed               = "error.payment.failed"

  val InvalidMogopayAccount       = "error.mogopay.account.invalid"
  val InvalidAccount              = "error.account.invalid"
  val InvalidPhonePincode3        = "error.invalid.phone.pincode3"
  val SmsSendError                = "error.sms.send"
  val InvalidCardId               = "error.card.invalid"
  val InvalidCardNumber           = "error.invalid.card.number"
  val InvalidTransactionUuid      = "error.invalid.transaction.uuid"
  val InvalidTransactionAmount    = "error.invalid.transaction.amount"
  val PaymentNotConfirmed         = "error.payment.not.confirmed"
  val TransactionAlreadyConfirmed = "error.transaction.already.confirmed"

  val ERROR_PAYMENT_REQUEST_NOT_FOUND = "error.payment.request.not.found"
  val ERROR_EXTERNAL_PAYMENT_NOT_ALLOWED_WITH_MANY_SHOPS = "error.external.payment.not.allowed.with.many.shops"
  val ERROR_THREEDS_NOT_ALLOWED_WITH_MANY_SHOPS = "error.3DS.not.allowed.with.many.shops"
  val ERROR_THREEDS_REQUIRED = "error.3DS.required"
  val ERROR_CONFIRM_SHIPPING = "error.confirm.shipping"
}
