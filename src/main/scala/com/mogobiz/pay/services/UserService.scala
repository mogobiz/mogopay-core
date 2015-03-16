package com.mogobiz.pay.services

import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.implicits.Implicits
import Implicits._
import com.wordnik.swagger.annotations._
import spray.http.StatusCodes
import spray.routing.Directives

@ApiModel( value = "RegisterResponse", description = "Person resource representation" )
case class RegisterResponse(callback_success: String, callback_error: String, merchant_id: String,
                            userEmail: String, userPassword: String, mogopay_token: String)

@Api(value = "/api/pay/user", description = ".", produces = "application/json", position = 1)
class UserService extends Directives with DefaultComplete {


  val route = {
    pathPrefix("user") {
      register
    }
  }

  @ApiOperation(nickname = "", httpMethod = "GET",
    value =
      """
      """, notes = "", produces = "application/json")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "callback_success",
      value = "", required = true, dataType = "string", paramType = "query"),
    new ApiImplicitParam(name = "callback_error",
      value = "", required = true, dataType = "string", paramType = "query"),
    new ApiImplicitParam(name = "merchant_id",
      value = "", required = true, dataType = "string", paramType = "query"),
    new ApiImplicitParam(name = "email",
      value = "", required = true, dataType = "string", paramType = "query"),
    new ApiImplicitParam(name = "password",
      value = "", required = true, dataType = "string", paramType = "query")
  ))
  @ApiResponses(Array(
//    new ApiResponse(code = 200, response = RegisterResponse.class, message = "")
  ))
  def register = path("register") {
    get {
      val params = parameters('callback_success, 'callback_error, 'merchant_id, 'email, 'password)
      params { (callback_success, callback_error, merchant_id, email, password) =>
        handleCall(userHandler.register(callback_success, callback_error, merchant_id, email, password),
          (data: Map[String, String]) => complete(StatusCodes.OK -> data))
      }
    }
  }
}
