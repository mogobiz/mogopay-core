package mogopay.actors


import akka.actor.Actor
import mogopay.config.HandlersConfig._
import mogopay.model.Mogopay.SessionData

import scala.util.Try

object PayboxActor {

  case class StartPayment(sessionData: SessionData)

  case class CallbackPayment(sessionData: SessionData, params: Map[String, String])

  case class Done(sessionData: SessionData, params: Map[String, String], uri:String)

  case class Done3DSecureCheck(sessionData: SessionData, params: Map[String, String])

  case class Callback3DSecureCheck(sessionData: SessionData, params: Map[String, String])

}

class PayboxActor extends Actor {

  import PayboxActor._

  def receive: Receive = {
    case StartPayment(sessionData) => sender ! Try(payboxHandler.startPayment(sessionData))
    case CallbackPayment(sessionData, params) => sender ! Try(payboxHandler.callbackPayment(sessionData, params))
    case Done(sessionData, params, uri) => sender ! Try(payboxHandler.donePayment(sessionData, params, uri))
    case Done3DSecureCheck(sessionData, params) => sender ! Try(payboxHandler.done3DSecureCheck(sessionData, params))
    case Callback3DSecureCheck(sessionData, params) => sender ! Try(payboxHandler.callback3DSecureCheck(sessionData, params))
  }
}



