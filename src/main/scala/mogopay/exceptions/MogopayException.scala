package mogopay.exceptions

import spray.http.{StatusCode, StatusCodes}

object Exceptions {

  abstract class MogopayException(val code: StatusCode, message: String) extends Exception(message)

  case class CreditCardDoesNotBelongToUserException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class InvalidCardNumberException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class PaymentConfigIdNotFoundException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class PaymentConfigNotFoundException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class PasswordPatternNotFoundException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class PasswordDoesNotMatchPatternException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class VendorNotFoundException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class AccountDoesNotExistException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class CannotRetrieveURLPrefixException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class NotAVendorAccountException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class NoAddressFoundException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class NoPhoneNumberFoundException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class CreditCardDoesNotExistException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class ShippingAddressDoesNotExistException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class UserEmailNotAllowedAsMerchantException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class VendorNotProvidedError(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class AccountAddressDoesNotExistException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class TelephoneDoesNotExistException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class NoAccountIdProvidedException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class PaymentConfigDoesNotExistException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class AccountWithSameEmailAddressAlreadyExistsError(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class NoPasswordProvidedError(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class NoActiveShippingAddressFound(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class CountryDoesNotExistException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class CurrencyCodeNotFoundException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class AccountNotActiveException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class TransactionNotFoundException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class InvalidTransactionTypeException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class BOTransactionNotFoundException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class UnexpectedAmountException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class TransactionTimeoutException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class PaymentNotConfirmedException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class TransactionAlreadyConfirmedException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class MogopayError(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class LackingInfoForMerchantException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class TooManyLoginAttemptsException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class InactiveAccountException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class InactiveMerchantException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class InvalidPasswordErrorException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class InvalidMerchantAccountException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class UserOrPasswordIsNullError(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class AccountAlreadyExistsException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class NotACreditCardTransactionException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class SomeParameterIsMissingException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class PasswordsDoNotMatchError(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class InvalidEmailException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class TokenExpiredException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class InvalidTokenException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  // Paybox
  case class InvalidSignatureException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)

  case class InvalidInputException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)
  case class InvalidContextException(message: String) extends MogopayException(StatusCodes.Unauthorized, message)
  case class NotAvailablePaymentGatewayException(message: String) extends MogopayException(StatusCodes.ServiceUnavailable, message)

}