/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.exceptions.Exceptions._
import com.mogobiz.pay.model.AccountWithChanges
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.utils.GlobalUtil._
import com.mogobiz.utils.{ GlobalUtil, RSA }
import org.apache.shiro.crypto.hash.Sha256Hash
import scalikejdbc.DBSession

import scala.collection.mutable
import scala.util._

class UserHandler {
  val USER_OR_PASSWORD_IS_NULL = "101"
  val USER_UNKNOWN = "102"
  val ACCOUNT_INACTIVE = "103"
  val ACCOUNT_ALREADY_EXIST = "103"
  val ACCOUNT_DOES_NOT_EXIST = "104"
  val ACCOUNT_PASSWORD_ERROR = "105"
  val ACCOUNT_TOO_MANY_ATTEMPTS = "106"
  val VERIFY_TIMEOUT = "151"
  val VERIFY_INVALID_EMAIL = "152"
  val VERIFY_FAIL = "153"
  val SUCCESS = "0"

  def register(successURL: String, errorURL: String, merchantId: String,
    email: String, password: String): Map[String, String] = {
    val transactionalBlock = { implicit session: DBSession =>
      val vendor = accountHandler.load(merchantId).getOrElse(throw AccountDoesNotExistException(""))
      if (!vendor.roles.contains(RoleName.MERCHANT)) throw NotAVendorAccountException("")

      if (accountHandler.findByEmail(email, Some(merchantId)).nonEmpty) {
        throw AccountAlreadyExistsException("")
      } else {
        val clearData = email + "" + System.currentTimeMillis() + "" + newUUID + "" + SUCCESS
        val encodedData = RSA.encrypt(clearData, Settings.RSA.publicKey)
        val extra: Map[String, String] = Map(
          "callback_success" -> successURL,
          "callback_error" -> errorURL,
          "merchant_id" -> merchantId,
          "userEmail" -> email,
          "userPassword" -> password,
          "mogopay_token" -> encodedData
        )

        val account = Account(uuid = newUUID,
          email = email,
          company = None,
          website = None,
          password = new Sha256Hash(password).toHex,
          civility = None,
          firstName = None,
          lastName = None,
          birthDate = None,
          address = None,
          status = AccountStatus.ACTIVE,
          loginFailedCount = 0,
          waitingPhoneSince = -1L,
          waitingEmailSince = -1L,
          extra = Some(extra.mkString(", ")),
          lastLogin = None,
          paymentConfig = None,
          country = None,
          roles = Nil,
          owner = None,
          emailingToken = None,
          shippingAddresses = Nil,
          secret = null,
          creditCards = Nil)

        (accountHandler.save(account), extra)
      }
    }

    val successBlock = { result: (AccountWithChanges, Map[String, String]) =>
      accountHandler.notifyESChanges(result._1.changes)
      result._2
    }

    GlobalUtil.runInTransaction(transactionalBlock, successBlock)
  }
}