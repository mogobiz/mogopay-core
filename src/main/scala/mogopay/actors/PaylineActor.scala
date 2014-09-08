package mogopay.actors

import akka.actor.Actor
import mogopay.config.HandlersConfig._
import mogopay.model.Mogopay.{Document, SessionData}


object PaylineActor {

  case class StartPayment(sessionData: SessionData)

  case class Done(sessionData: SessionData, params: Map[String, String])

  case class CallbackPayment(sessionId: SessionData, params: Map[String, String])

  case class ThreeDSCallback(sessionData: SessionData, params: Map[String, String])

}

class PaylineActor extends Actor {

  import PaylineActor._

  def receive: Receive = {
    case StartPayment(sessionData) => sender ! paylineHandler.startPayment(sessionData)
    case Done(sessionData, params) => sender ! paylineHandler.done(sessionData, params)
    case CallbackPayment(params, vendorUuid) => sender ! paylineHandler.callbackPayment(params, vendorUuid)
    case ThreeDSCallback(sessionData, params) => sender ! paylineHandler.threeDSCallback(sessionData, params)
  }
}
