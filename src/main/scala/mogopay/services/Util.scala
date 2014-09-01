package mogopay.services

import mogopay.exceptions.Exceptions._
import spray.http.StatusCode
import spray.http.StatusCodes
import spray.routing.RequestContext
import spray.routing.directives.PathDirectives
import spray.routing.directives.MethodDirectives
import com.google.i18n.phonenumbers.NumberParseException

object Util {
  def getPath(pathString: String)(f: RequestContext => Unit) = {
    PathDirectives.path(pathString) {
      MethodDirectives.get { f }
    }
  }

  def toHTTPResponse(t: Throwable): StatusCode = t match {
    case CreditCardDoesNotBelongToUserException() => StatusCodes.Unauthorized
    case InvalidCardNumberException() => StatusCodes.BadRequest
    case PaymentConfigIdNotFoundException() => StatusCodes.NotFound
    case PaymentConfigNotFoundException() => StatusCodes.NotFound
    case PasswordPatternNotFoundException() => StatusCodes.NotFound
    case PasswordDoesNotMatchPatternException() => StatusCodes.BadRequest
    case VendorNotFoundException() => StatusCodes.NotFound
    case AccountDoesNotExistError() => StatusCodes.NotFound
    case CannotRetrieveURLPrefixException() => StatusCodes.NotFound
    case NotAVendorAccountException() => StatusCodes.Unauthorized
    case NoAddressFoundException() => StatusCodes.NotFound
    case NoPhoneNumberFoundException() => StatusCodes.NotFound
    case CreditCardDoesNotExistException() => StatusCodes.NotFound
    case ShippingAddressDoesNotExistException() => StatusCodes.NotFound
    case UserEmailNotAllowedAsMerchantException() => StatusCodes.Unauthorized
    case VendorNotProvidedError() => StatusCodes.BadRequest
    case AccountAddressDoesNotExistException() => StatusCodes.NotFound
    case TelephoneDoesNotExistException() => StatusCodes.NotFound
    case NoAccountIdProvidedException() => StatusCodes.BadRequest
    case PaymentConfigDoesNotExistException() => StatusCodes.NotFound
    case AccountWithSameEmailAddressAlreadyExistsError() => StatusCodes.Conflict
    case NoPasswordProvidedError() => StatusCodes.BadRequest
    case NoActiveShippingAddressFound() => StatusCodes.NotFound
    case CountryDoesNotExistException() => StatusCodes.NotFound
    case CurrencyCodeNotFoundException() => StatusCodes.NotFound
    case TransactionNotFoundException() => StatusCodes.NotFound
    case UnexpectedAmountException() => StatusCodes.BadRequest
    case TransactionTimeoutException(_) => StatusCodes.RequestTimeout
    case PaymentNotConfirmedException(_) => StatusCodes.BadRequest
    case TransactionAlreadyConfirmedException(_) => StatusCodes.Conflict
    case LackingInfoForMerchantException(_) => StatusCodes.BadRequest
    case TooManyLoginAttemptsException() => StatusCodes.TooManyRequests
    case InactiveAccountException() => StatusCodes.BadRequest
    case InvalidPasswordErrorException() => StatusCodes.BadRequest
    case InvalidMerchantAccountException() => StatusCodes.BadRequest
    case BOTransactionNotFoundException()  => StatusCodes.NotFound
    case MogopayError(message: String)  => StatusCodes.BadRequest
    case InactiveMerchantException()  => StatusCodes.BadRequest
    case UserOrPasswordIsNullError()  => StatusCodes.BadRequest
    case AccountAlreadyExistsException()  => StatusCodes.Conflict
    case NotACreditCardTransactionException() => StatusCodes.BadRequest
    case SomeParameterIsMissingException(_) => StatusCodes.BadRequest
    case e: NumberParseException => StatusCodes.BadRequest
    case _ => StatusCodes.InternalServerError
  }
}
