package mogopay.actors

import akka.actor.Actor
import mogopay.config.HandlersConfig._
import mogopay.model.Mogopay.{Document, SessionData}


object SipsActor {
  case class StartPayment(sessionData: SessionData)
  case class Done(sessionData: SessionData, params: Map[String, String])
  case class CallbackPayment(params: Map[String, String], vendorUuid : Document)
  case class ThreeDSCallback(sessionData: SessionData, params: Map[String, String])
}

class SipsActor extends Actor {
  import SipsActor._

  def receive: Receive = {
    case StartPayment(sessionData)    => sender ! sipsHandler.startPayment(sessionData)
    case Done(sessionData, params)            => sender ! sipsHandler.done(sessionData, params)
    case CallbackPayment(params, vendorUuid)           => sender ! sipsHandler.callbackPayment(params, vendorUuid)
    case ThreeDSCallback(sessionData, params) => sender ! sipsHandler.threeDSCallback(sessionData, params)
  }
}
