/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.services

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import com.mogobiz.pay.config.DefaultComplete
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import com.mogobiz.pay.config.MogopayHandlers.handlers.userHandler

class UserService extends Directives with DefaultComplete {

  val route = {
    pathPrefix("user") {
      register
    }
  }

  lazy val register = path("register") {
    get {
      val params = parameters('callback_success,
                              'callback_error,
                              'merchant_id,
                              'email,
                              'password)
      params {
        (callback_success, callback_error, merchant_id, email, password) =>
          handleCall(
            userHandler.register(callback_success,
                                 callback_error,
                                 merchant_id,
                                 email,
                                 password),
            (data: Map[String, String]) => complete(StatusCodes.OK -> data))
      }
    }
  }
}
