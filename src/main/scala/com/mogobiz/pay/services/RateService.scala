/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.services

import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.implicits.Implicits
import com.mogobiz.pay.implicits.Implicits._
import com.mogobiz.pay.model.Rate
import spray.http.StatusCodes
import spray.routing.Directives

class RateService extends Directives with DefaultComplete {

  val route = pathPrefix("rate") {
    list ~
    format
  }

  lazy val list = path("list") {
    get {
      dynamic {
        handleCall(rateHandler.list, (rates: Seq[Rate]) => complete(StatusCodes.OK -> rates))
      }
    }
  }

  lazy val format = path("format") {
    get {
      parameters('amount.as[Long], 'currency, 'country) { (amount, currency, country) =>
        handleCall(rateHandler.format(amount, currency, country),
                   (res: Option[String]) => complete(StatusCodes.OK -> res))
      }
    }
  }
}
