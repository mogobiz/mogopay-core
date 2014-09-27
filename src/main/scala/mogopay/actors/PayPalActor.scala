package mogopay.actors

import akka.actor.Actor
import mogopay.config.HandlersConfig._
import mogopay.model.Mogopay.SessionData
import mogopay.session.Session

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