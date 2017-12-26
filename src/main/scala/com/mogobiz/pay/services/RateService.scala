/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.services

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.model.Rate

class RateService extends Directives with DefaultComplete {

  val route = pathPrefix("rate") {
    list ~
      format
  }

  lazy val list = path("list") {
    get {
      import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
      import com.mogobiz.json.JacksonConverter._
      handleCall(rateHandler.list,
                 (rates: Seq[Rate]) => complete(StatusCodes.OK -> rates))
    }
  }

  lazy val format = path("format") {
    get {
      parameters('amount.as[Long], 'currency, 'country) {
        (amount, currency, country) =>
          handleCall(rateHandler.format(amount, currency, country),
                     (res: Option[String]) => complete(StatusCodes.OK -> res))
      }
    }
  }
}
