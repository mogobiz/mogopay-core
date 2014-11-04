package com.mogobiz.pay.actors

import akka.actor.Actor
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.model.Mogopay.{Document, SessionData}

import scala.util.Try


object PaylineActor {

  case class StartPayment(sessionData: SessionData)

  case class Done(sessionData: SessionData, params: Map[String, String])

  case class CallbackPayment(sessionId: SessionData, params: Map[String, String])

  case class ThreeDSCallback(sessionData: SessionData, params: Map[String, String])

}

class PaylineActor extends Actor {

  import PaylineActor._

  def receive: Receive = {
    case StartPayment(sessionData) => sender ! Try(paylineHandler.startPayment(sessionData))
    case Done(sessionData, params) => sender ! Try(paylineHandler.done(sessionData, params))
    case CallbackPayment(params, vendorUuid) => sender ! Try(paylineHandler.callbackPayment(params, vendorUuid))
    case ThreeDSCallback(sessionData, params) => sender ! Try(paylineHandler.threeDSCallback(sessionData, params))
  }
}
