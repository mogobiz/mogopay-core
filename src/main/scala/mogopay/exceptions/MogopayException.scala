package mogopay.exceptions

object Exceptions {
  type MogopayException = Exception

  case class CreditCardDoesNotBelongToUserException() extends Exception

  case class InvalidCardNumberException() extends Exception

  case class PaymentConfigIdNotFoundException() extends Exception

  case class PaymentConfigNotFoundException() extends Exception

  case class PasswordPatternNotFoundException() extends Exception

  case class PasswordDoesNotMatchPatternException() extends Exception

  case class VendorNotFoundException() extends Exception

  case class AccountDoesNotExistError() extends Exception

  case class CannotRetrieveURLPrefixException() extends Exception

  case class NotAVendorAccountException() extends Exception

  case class NoAddressFoundException() extends Exception

  case class NoPhoneNumberFoundException() extends Exception

  case class CreditCardDoesNotExistException() extends Exception

  case class ShippingAddressDoesNotExistException() extends Exception

  case class UserEmailNotAllowedAsMerchantException() extends Exception

  case class VendorNotProvidedError() extends Exception

  case class AccountAddressDoesNotExistException() extends Exception

  case class TelephoneDoesNotExistException() extends Exception

  case class NoAccountIdProvidedException() extends Exception

  case class PaymentConfigDoesNotExistException() extends Exception

  case class AccountWithSameEmailAddressAlreadyExistsError() extends Exception

  case class NoPasswordProvidedError() extends Exception

  case class NoActiveShippingAddressFound() extends Exception

  case class CountryDoesNotExistException() extends Exception

  case class CurrencyCodeNotFoundException() extends Exception

  case class AccountNotActiveException() extends Exception

  case class TransactionNotFoundException() extends Exception

  case class BOTransactionNotFoundException() extends Exception

  case class UnexpectedAmountException() extends Exception

  case class TransactionTimeoutException(message: String) extends Exception(message)

  case class PaymentNotConfirmedException(message: String) extends Exception(message)

  case class TransactionAlreadyConfirmedException(message: String) extends Exception(message)

  case class MogopayError(message: String) extends Exception(message)

  case class LackingInfoForMerchantException(message: String) extends Exception(message)

  case class TooManyLoginAttemptsException() extends Exception

  case class InactiveAccountException() extends Exception

  case class InactiveMerchantException() extends Exception

  case class InvalidPasswordErrorException() extends Exception

  case class InvalidMerchantAccountException() extends Exception

  case class UserOrPasswordIsNullError() extends Exception

  case class AccountAlreadyExistsException() extends Exception

  case class NotACreditCardTransactionException() extends Exception

  case class SomeParameterIsMissingException(message: String) extends Exception(message)

  case class PasswordsDontMatchError() extends Exception

}