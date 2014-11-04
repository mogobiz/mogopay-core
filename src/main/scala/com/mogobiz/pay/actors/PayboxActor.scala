package com.mogobiz.pay.actors


import akka.actor.Actor
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.model.Mogopay.SessionData

import scala.util.Try

object PayboxActor {

  case class StartPayment(sessionData: SessionData)

  case class CallbackPayment(sessionData: SessionData, params: Map[String, String], uri:String)

  case class Done(sessionData: SessionData, params: Map[String, String], uri:String)

  case class Done3DSecureCheck(sessionData: SessionData, params: Map[String, String])

  case class Callback3DSecureCheck(sessionData: SessionData, params: Map[String, String])

}

class PayboxActor extends Actor {

  import PayboxActor._

  def receive: Receive = {
    case StartPayment(sessionData) => sender ! Try(payboxHandler.startPayment(sessionData))
    case CallbackPayment(sessionData, params, uri) => sender ! Try(payboxHandler.callbackPayment(sessionData, params, uri))
    case Done(sessionData, params, uri) => sender ! Try(payboxHandler.donePayment(sessionData, params, uri))
    case Done3DSecureCheck(sessionData, params) => sender ! Try(payboxHandler.done3DSecureCheck(sessionData, params))
    case Callback3DSecureCheck(sessionData, params) => sender ! Try(payboxHandler.callback3DSecureCheck(sessionData, params))
  }
}



