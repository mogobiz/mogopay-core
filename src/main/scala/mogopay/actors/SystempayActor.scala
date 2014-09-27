package mogopay.actors

import akka.actor.Actor
import mogopay.config.HandlersConfig._
import mogopay.model.Mogopay.SessionData
import mogopay.session.Session

import scala.util.Try

object SystempayActor {

  case class StartPayment(sessionData: SessionData)

  case class Done(sessionData: SessionData, params: Map[String, String])

  case class CallbackPayment(params: Map[String, String])

  case class ThreeDSCallback(sessionData: SessionData, params: Map[String, String])

}

class SystempayActor extends Actor {

  import SystempayActor._

  def receive: Receive = {
    case StartPayment(sessionData) => sender ! Try(systempayHandler.startPayment(sessionData))
    case Done(sessionData, params) => sender ! Try(systempayHandler.done(sessionData, params))
    case CallbackPayment(params) => sender ! Try(systempayHandler.callbackPayment(params))
    case ThreeDSCallback(sessionData, params) => sender ! Try(systempayHandler.threeDSCallback(sessionData, params))
  }
}
