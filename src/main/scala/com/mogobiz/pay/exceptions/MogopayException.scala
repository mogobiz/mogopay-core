/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.exceptions

import spray.http.{StatusCode, StatusCodes}

object Exceptions {

  abstract class MogopayMessagelessException(val code: StatusCode, val printTrace: Boolean = true) extends Exception()

  abstract class MogopayException(val code: StatusCode, message: String, val printTrace: Boolean = true)
      extends Exception(message)

  case class UnauthorizedException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class CreditCardDoesNotBelongToUserException(message: String)
      extends MogopayException(StatusCodes.Unauthorized, message)

  case class InvalidCardNumberException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class PaymentConfigIdNotFoundException(message: String)
      extends MogopayException(StatusCodes.Unauthorized, message)

  case class PaymentConfigNotFoundException() extends MogopayMessagelessException(StatusCodes.Unauthorized)

  case class PasswordPatternNotFoundException(message: String)
      extends MogopayException(StatusCodes.Unauthorized, message)

  case class PasswordDoesNotMatchPatternException(message: String)
      extends MogopayException(StatusCodes.Unauthorized, message)

  case class VendorNotFoundException() extends MogopayMessagelessException(StatusCodes.Unauthorized)

  case class AccountDoesNotExistException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class CannotRetrieveURLPrefixException(message: String)
      extends MogopayException(StatusCodes.Unauthorized, message)

  case class NotAVendorAccountException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class NoAddressFoundException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class NoPhoneNumberFoundException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class CreditCardDoesNotExistException(message: String)
      extends MogopayException(StatusCodes.Unauthorized, message)

  case class ShippingAddressDoesNotExistException(message: String)
      extends MogopayException(StatusCodes.Unauthorized, message)

  case class UserEmailNotAllowedAsMerchantException(message: String)
      extends MogopayException(StatusCodes.Unauthorized, message)

  case class VendorNotProvidedError(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class AccountAddressDoesNotExistException(message: String)
      extends MogopayException(StatusCodes.Unauthorized, message)

  case class TelephoneDoesNotExistException(message: String)
      extends MogopayException(StatusCodes.Unauthorized, message)

  case class NoAccountIdProvidedException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class PaymentConfigDoesNotExistException(message: String)
      extends MogopayException(StatusCodes.Unauthorized, message)

  case class AccountWithSameEmailAddressAlreadyExistsError(message: String)
      extends MogopayException(StatusCodes.Unauthorized, message)

  case class AccountWithSameCompanyAlreadyExistsError(message: String)
      extends MogopayException(StatusCodes.Unauthorized, message)

  case class NoPasswordProvidedError(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class NoShippingPriceFound() extends MogopayMessagelessException(StatusCodes.Unauthorized)

  case class SelectedShippingPriceNotFound() extends MogopayMessagelessException(StatusCodes.Unauthorized)

  case class NoActiveShippingAddressFound() extends MogopayMessagelessException(StatusCodes.Unauthorized)

  case class CountryDoesNotExistException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class CurrencyCodeNotFoundException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class AccountNotActiveException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class TransactionRequestNotFoundException(message: String)
      extends MogopayException(StatusCodes.NotFound, message)

  case class TransactionNotFoundException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class InvalidTransactionTypeException(message: String)
      extends MogopayException(StatusCodes.Unauthorized, message)

  case class TheBOTransactionAlreadyExistsException(message: String)
      extends MogopayException(StatusCodes.Unauthorized, message)

  case class UnexpectedAmountException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class TransactionTimeoutException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class PaymentNotConfirmedException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class TransactionAlreadyConfirmedException(message: String)
      extends MogopayException(StatusCodes.Unauthorized, message)

  case class MogopayError(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class LackingInfoForMerchantException(message: String)
      extends MogopayException(StatusCodes.Unauthorized, message)

  case class TooManyLoginAttemptsException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class InactiveAccountException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class InactiveMerchantException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class InvalidPasswordErrorException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class InvalidMerchantAccountException(message: String)
      extends MogopayException(StatusCodes.Unauthorized, message)

  case class UserOrPasswordIsNullError(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class AccountAlreadyExistsException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class NotACreditCardTransactionException(message: String)
      extends MogopayException(StatusCodes.Unauthorized, message)

  case class SomeParameterIsMissingException(message: String) extends MogopayException(StatusCodes.BadRequest, message)

  case class PasswordsDoNotMatchException(message: String) extends MogopayException(StatusCodes.BadRequest, message)

  case class InvalidEmailException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class TokenExpiredException() extends MogopayMessagelessException(StatusCodes.Gone)

  case class InvalidTokenException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class InvalidSignatureException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class InvalidPhoneNumberException(message: String) extends MogopayException(StatusCodes.BadRequest, message)

  case class InvalidDateFormatException(message: String) extends MogopayException(StatusCodes.BadRequest, message)

  case class AccountNotConfirmedException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class InvalidInputException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class InvalidContextException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class NotAvailablePaymentGatewayException(message: String)
      extends MogopayException(StatusCodes.ServiceUnavailable, message)

  case class NoSuccessURLProvided() extends MogopayMessagelessException(StatusCodes.InternalServerError)

  case class NoResponseFromAuthorizeNetException() extends MogopayMessagelessException(StatusCodes.InternalServerError)

  case class AuthorizeNetErrorException(errorCode: String)
      extends MogopayException(StatusCodes.InternalServerError, errorCode)

  case class MissingAuthorizeNetParamException() extends MogopayMessagelessException(StatusCodes.BadRequest)

  case class InvalidPaymentMethodException()
      extends MogopayException(StatusCodes.BadRequest, "Invalid payment method.")

  case class NoCustomerSetForTheBOTrasaction() extends MogopayMessagelessException(StatusCodes.NotFound)

  case class NoReturnURLSpecifiedException() extends MogopayMessagelessException(StatusCodes.NotFound)

  case class NoExpirationTimeSpecifiedException() extends MogopayMessagelessException(StatusCodes.NotFound)

  case class NoGroupPaymentInfoSpecifiedException() extends MogopayMessagelessException(StatusCodes.NotFound)

  case class NoCountrySpecifiedException() extends MogopayMessagelessException(StatusCodes.NotFound)

  case class RateNotFoundException(message: String) extends MogopayException(StatusCodes.NotFound, message)

  case class RefundNotSupportedException() extends MogopayMessagelessException(StatusCodes.NotFound)

  case class TransactionIdNotFoundException() extends MogopayMessagelessException(StatusCodes.BadRequest)

  case class RefundException(message: String) extends MogopayException(StatusCodes.BadRequest, message)

  case class InvalidPaymentHandlerException(message: String) extends MogopayException(StatusCodes.BadRequest, message)

  case class MissingGroupPaymentInfoValues() extends MogopayMessagelessException(StatusCodes.BadRequest)

  case class PaymentAlreadyRefundedException() extends MogopayMessagelessException(StatusCodes.BadRequest)

  case class NoRefundPercentageSpecifiedException() extends MogopayMessagelessException(StatusCodes.BadRequest)

  case class NotAGroupPaymentException() extends MogopayMessagelessException(StatusCodes.BadRequest)

  case class MissingPayersForGroupPaymentException() extends MogopayMessagelessException(StatusCodes.BadRequest)

  case class TransactionRequestWasInitiatedByAnotherMerchantException()
      extends MogopayMessagelessException(StatusCodes.BadRequest)

  case class TheRefundAmountIsHigherThanTheInitialAmountException()
      extends MogopayMessagelessException(StatusCodes.BadRequest)

  case class CompanyNotSpecifiedException() extends MogopayMessagelessException(StatusCodes.BadRequest)

  case class NoCBProviderSpecified() extends MogopayMessagelessException(StatusCodes.BadRequest)

  case class NoCBPaymentMethodSpecified() extends MogopayMessagelessException(StatusCodes.BadRequest)

  case class ShippingException() extends MogopayMessagelessException(StatusCodes.BadRequest, false)

}
