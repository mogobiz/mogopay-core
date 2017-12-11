/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */
package com.mogobiz.pay.services

import com.mogobiz.system.ActorSystemLocator
import org.scalatest.{FlatSpec, Matchers}
import spray.http._
import spray.routing.HttpService
import spray.testkit.ScalatestRouteTest

class MiraklServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with HttpService {

  def actorRefFactory = system // connect the DSL to the test ActorSystem
  ActorSystemLocator(system)
  
  val DEBIT_CUSTOMER_PATH = "/mirakl/debitCustomer"
  val JSON_ORDERS_DEBIT_CUSTOMER = "{\n  \"order\" : [ {\n    \"amount\" : 108.08,\n    \"currency_iso_code\" : \"EUR\",\n    \"customer_id\" : \"bfa1e006-b3dd-47df-96fd-2c58fbbf9531\",\n    \"order_commercial_id\" : \"2d44f3f3-f651-4743-9c38-1a3f64a4f1f3\",\n    \"order_id\" : \"2d44f3f3-f651-4743-9c38-1a3f64a4f1f3-A\",\n    \"order_lines\" : {\n      \"order_line\" : [ {\n        \"offer_id\" : \"2094\",\n        \"order_line_amount\" : 108.08,\n        \"order_line_id\" : \"2d44f3f3-f651-4743-9c38-1a3f64a4f1f3-A-1\",\n        \"order_line_quantity\" : 1\n      } ]\n    },\n    \"shop_id\" : \"2003\"\n  } ]\n}"
  val JSON_ORDERS_DEBIT_CUSTOMER_MIRAKL_TEST = "{\n  \"order\" : [ {\n    \"customer_id\" : \"Testing debit connector. Please ignore.\"\n  } ]\n}"

  val miraklService = new MiraklService
  "The Mirakl service" should "return a MethodNotAllowed error for GET requests to the debit customer path " in {
    Get(DEBIT_CUSTOMER_PATH) ~> HttpService.sealRoute(miraklService.route) ~> check {
      status === StatusCodes.MethodNotAllowed
      responseAs[String] === "HTTP method not allowed, supported methods: POST"
    }
  }

  it should "return a MethodNotAllowed error for PUT requests to the debit customer path " in {
    Put(DEBIT_CUSTOMER_PATH) ~> HttpService.sealRoute(miraklService.route) ~> check {
      status === StatusCodes.MethodNotAllowed
      responseAs[String] === "HTTP method not allowed, supported methods: POST"
    }
  }

  it should "return a MethodNotAllowed error for DELETE requests to the debit customer path " in {
    Delete(DEBIT_CUSTOMER_PATH) ~> HttpService.sealRoute(miraklService.route) ~> check {
      status === StatusCodes.MethodNotAllowed
      responseAs[String] === "HTTP method not allowed, supported methods: POST"
    }
  }

  it should "return a BadRequest error for Post requests without json entity to the debit customer path " in {
    Post(DEBIT_CUSTOMER_PATH) ~> HttpService.sealRoute(miraklService.route) ~> check {
      status === StatusCodes.BadRequest
      responseAs[String] === "Request entity expected but not supplied"
    }
  }

  it should "return a BadRequest error for Post requests with xml entity to the debit customer path " in {
    Post(DEBIT_CUSTOMER_PATH, HttpEntity(MediaTypes.`application/xml`, "<p>cool</p>")) ~>
      HttpService.sealRoute(miraklService.route) ~> check {
      status === StatusCodes.UnsupportedMediaType
      responseAs[String] === "There was a problem with the requests Content-Type:\nExpected 'application/json'"
    }
  }

  it should "return a success for Post requests with json entity to the debit customer path " in {
    Post(DEBIT_CUSTOMER_PATH, HttpEntity(MediaTypes.`application/json`, JSON_ORDERS_DEBIT_CUSTOMER)) ~>
      HttpService.sealRoute(miraklService.route) ~> check {
      responseAs[String] === "There was a problem with the requests Content-Type:\nExpected 'application/json'"
      status === StatusCodes.NoContent
    }
  }

  it should "return a success for Post requests from Mirakl to test access " in {
    Post(DEBIT_CUSTOMER_PATH, HttpEntity(MediaTypes.`application/json`, JSON_ORDERS_DEBIT_CUSTOMER_MIRAKL_TEST)) ~>
      HttpService.sealRoute(miraklService.route) ~> check {
      status === StatusCodes.NoContent
    }
  }
}
