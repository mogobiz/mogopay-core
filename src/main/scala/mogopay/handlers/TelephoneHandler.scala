package mogopay.handlers

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil._
import mogopay.exceptions.Exceptions._
import mogopay.handlers.UtilHandler._
import mogopay.model.Mogopay._
import scala.slick.driver.PostgresDriver.simple._
import scala.util._

case class PhoneVerification(isValid: Boolean,
                             nationalFormat: Option[String] = None,
                             internationalFormat: Option[String] = None,
                             phoneType: Option[PhoneNumberType] = None)

object TelephoneHandler {
  //  def find(id: Long): Option[Telephone] = dbTransaction { implicit session =>
  //    telephones.where(_.id === id).firstOption
  //  }
  //
  //  def checkPhoneNumber(phone: String, country: String): PhoneVerification = {
  //    val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
  //
  //    try {
  //      val phoneNumber = phoneUtil.parse(phone, country)
  //      if (!phoneUtil.isValidNumberForRegion(phoneNumber, country)) {
  //        PhoneVerification(isValid = false)
  //      } else {
  //        PhoneVerification(isValid = true,
  //          Some(phoneUtil.format(phoneNumber, PhoneNumberFormat.NATIONAL)),
  //          Some(phoneUtil.format(phoneNumber, PhoneNumberFormat.INTERNATIONAL)),
  //          Some(phoneUtil.getNumberType(phoneNumber)))
  //      }
  //    } catch {
  //      case _: Throwable => PhoneVerification(isValid = false)
  //    }
  //  }
  //
  //  def findPhone(id: Long): Option[Telephone] = dbTransaction { implicit session =>
  //    for {
  //      addr  <- accountAddresses.where(_.id === id).firstOption
  //      telId <- addr.telephoneId
  //      tel   <- telephones.where(_.id === 1L).firstOption
  //    } yield tel
  //  }
  //
  //  def save(tel: Telephone): Telephone = dbTransaction { implicit session =>
  //    val id = (telephones returning telephones.map(_.id)) += tel
  //    tel.copy(id = Some(id))
  //  }
  //
  //  def delete(id: Long): Try[Unit] = dbTransaction { implicit session =>
  //    val r = telephones.where(_.id === id).delete
  //    if (r == 0) Failure(new TelephoneDoesNotExistException)
  //    else        Success(())
  //  }
}
