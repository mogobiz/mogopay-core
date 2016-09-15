/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */
package com.mogobiz.pay.services

import akka.actor.{Actor, Props}
import akka.event.Logging
import com.mogobiz.mirakl.PaymentModel.{DebitOrderList, RefundOrderList}
import com.mogobiz.pay.config.DefaultComplete
import spray.http.StatusCodes
import spray.routing.Directives
import com.mogobiz.pay.config.MogopayHandlers.handlers.miraklHandler
import com.mogobiz.pay.implicits.Implicits._
import com.mogobiz.system.ActorSystemLocator

import scala.util.{Success, Try}

class MiraklService extends Directives with DefaultComplete {

  val route = {
    logRequestResponse("REST API", Logging.InfoLevel)
    {
      pathPrefix("mirakl") {
        debitCustomer ~
          refundCustomer
      }
    }
  }

  lazy val debitCustomer = path("debitCustomer") {
    post {
      entity(as[DebitOrderList]) { orders =>
        handleCall(Try{
          val system = ActorSystemLocator()
          val miraklActor = system.actorOf(Props[MiraklActor])
          miraklActor ! DebitCustomerMessage(orders)
          true
        }, (res: Try[Boolean]) => res match {
          case Success(_) => complete(StatusCodes.NoContent)
          case _ => complete(StatusCodes.NotImplemented)
        })
      }
    }
  }

  lazy val refundCustomer = path("refundCustomer") {
    post {
      entity(as[RefundOrderList]) { orders =>
        handleCall(Try{
          val system = ActorSystemLocator()
          val miraklActor = system.actorOf(Props[MiraklActor])
          miraklActor ! RefundCustomerMessage(orders)
          true
        }, (res: Try[Boolean]) => res match {
          case Success(_) => complete(StatusCodes.NoContent)
          case _ => complete(StatusCodes.NotImplemented)
        })
      }
    }
  }
}

class MiraklActor extends Actor {
  def receive = {
    case msg: DebitCustomerMessage =>
      miraklHandler.debitCustomer(msg.orders)
    case msg: RefundCustomerMessage =>
      miraklHandler.refundCustomer(msg.orders)
  }
}

case class DebitCustomerMessage(orders: DebitOrderList)

case class RefundCustomerMessage(orders: RefundOrderList)
