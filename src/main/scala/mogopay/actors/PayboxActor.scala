package mogopay.actors


import akka.actor.Actor
import mogopay.config.HandlersConfig._
import mogopay.model.Mogopay.SessionData

object PayboxActor {
  case class StartPayment(sessionData: SessionData)

  case class CallbackPayment(sessionData: SessionData, params: Map[String, String])
  case class Done(sessionData: SessionData, params: Map[String, String])
  case class Done3DSecureCheck(sessionData: SessionData, params: Map[String, String])
  case class Callback3DSecureCheck(sessionData: SessionData, params: Map[String, String])
}
class PayboxActor extends Actor {
  import PayboxActor._

  def receive: Receive = {
    case StartPayment(sessionData)    =>                sender ! payboxHandler.startPayment(sessionData)
    case CallbackPayment(sessionData, params) =>        sender ! payboxHandler.callbackPayment(sessionData, params)
    case Done(sessionData, params) =>                   sender ! payboxHandler.donePayment(sessionData, params)
    case Done3DSecureCheck(sessionData, params) =>      sender ! payboxHandler.done3DSecureCheck(sessionData, params)
    case Callback3DSecureCheck(sessionData, params) =>  sender ! payboxHandler.callback3DSecureCheck(sessionData, params)
  }
}



