package com.mogobiz.pay.services

import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.implicits.Implicits
import Implicits._
import com.mogobiz.pay.model.Mogopay.Rate
import com.wordnik.swagger.annotations._
import spray.http.StatusCodes
import spray.routing.Directives

@Api(value = "/api/pay/rate", description = "Currency rating operations.", produces="application/json", position=2)
class RateService extends Directives with DefaultComplete {

  val route = pathPrefix("rate") {
    list ~
      format
  }

  @ApiOperation(value = "Return all rates", notes = "", nickname = "list", httpMethod = "get", produces="application/json")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "List of rates.")
  ))
  def list = path("list") {
    get {
      dynamic {
        handleCall(rateHandler.list,
          (rates: Seq[Rate]) => complete(StatusCodes.OK -> rates))
      }
    }
  }

  def format = path("format") {
    get {
      parameters('amount.as[Long], 'currency, 'country) { (amount, currency, country) =>
        handleCall(rateHandler.format(amount, currency, country),
          (res: Option[String]) => complete(StatusCodes.OK -> res))
      }
    }
  }
}
