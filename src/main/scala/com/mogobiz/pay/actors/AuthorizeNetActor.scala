package com.mogobiz.pay.actors


import akka.actor.Actor
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.model.Mogopay.SessionData

import scala.util.Try

object AuthorizeNetActor {
  case class StartPayment(sessionData: SessionData)

//  case class CallbackPayment(sessionData: SessionData, params: Map[String, String], uri:String)

  case class Done(sessionData: SessionData, params: Map[String, String], uri:String)

//  case class Done3DSecureCheck(sessionData: SessionData, params: Map[String, String])

//  case class Callback3DSecureCheck(sessionData: SessionData, params: Map[String, String])

  case class Relay(sessionData: SessionData, params: Map[String, String])

  case class Finish(sessionData: SessionData, params: Map[String, String])

  case class Cancel(sessionData: SessionData)
}

class AuthorizeNetActor extends Actor {
  import AuthorizeNetActor._

  def receive: Receive = {
    case StartPayment(sessionData) => sender ! Try(authorizeNetHandler.startPayment(sessionData))
//    case CallbackPayment(sessionData, params, uri) => sender ! Try(authorizeNetHandler.callbackPayment(sessionData, params, uri))
    case Done(sessionData, params, uri) => sender ! ??? //Try(authorizeNetHandler.done(sessionData, params))
//    case Done3DSecureCheck(sessionData, params) => sender ! Try(authorizeNetHandler.done3DSecureCheck(sessionData, params))
//    case Callback3DSecureCheck(sessionData, params) => sender ! Try(authorizeNetHandler.callback3DSecureCheck(sessionData, params))
    case Relay(sessionData, params) => sender ! Try(authorizeNetHandler.relay(sessionData, params))
    case Finish(sessionData, params) => sender ! Try(authorizeNetHandler.finish(sessionData, params))
    case Cancel(sessionData) => sender ! Try(authorizeNetHandler.cancel(sessionData))
  }
}
