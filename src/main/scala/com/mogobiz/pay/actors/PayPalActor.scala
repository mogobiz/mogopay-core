package com.mogobiz.pay.actors

import akka.actor.Actor
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.model.Mogopay.SessionData
import com.mogobiz.session.Session

import scala.util.Try

object PayPalActor {

  case class StartPayment(sessionData: SessionData)

  case class Fail(sessionData: SessionData, token: String)

  case class SuccessPP(sessionData: SessionData, token: String)

}

class PayPalActor extends Actor {

  import PayPalActor._

  def receive: Receive = {
    case StartPayment(sessionData) => sender ! Try(payPalHandler.startPayment(sessionData))
    case Fail(sessionData, token) => sender ! Try(payPalHandler.fail(sessionData, token))
    case SuccessPP(sessionData, token) => sender ! Try(payPalHandler.success(sessionData, token))
  }
}