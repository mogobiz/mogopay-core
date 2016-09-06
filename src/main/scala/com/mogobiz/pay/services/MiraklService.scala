package com.mogobiz.pay.services

import akka.actor.{Actor, Props}
import akka.event.Logging
import com.mogobiz.mirakl.PaymentModel.DebitOrderList
import com.mogobiz.pay.config.DefaultComplete
import spray.http.StatusCodes
import spray.routing.Directives
import com.mogobiz.pay.config.MogopayHandlers.handlers.miraklHandler
import com.mogobiz.pay.implicits.Implicits._
import com.mogobiz.system.ActorSystemLocator

import scala.util.{Success, Try}

/**
 * Created by yoannbaudy on 01/09/16.
 */
class MiraklService extends Directives with DefaultComplete {

  val route = {
    logRequestResponse("REST API", Logging.InfoLevel)
    {
      pathPrefix("mirakl") {
        debitCustomer
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
}

class MiraklActor extends Actor {
  def receive = {
    case msg: DebitCustomerMessage =>
      miraklHandler.debitCustomer(msg.orders)

  }
}

case class DebitCustomerMessage(orders: DebitOrderList)
