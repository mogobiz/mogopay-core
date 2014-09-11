package mogopay.actors

import akka.actor.Actor
import mogopay.config.HandlersConfig._
import mogopay.model.Mogopay.SessionData
import mogopay.session.Session

object MogopayActor {

  case class StartPayment(sessionData: SessionData)
  case class Authenticate(sessionData: SessionData)

}

class MogopayActor extends Actor {

  import MogopayActor._

  def receive: Receive = {
    case StartPayment(sessionData) => sender ! mogopayHandler.startPayment(sessionData)
    case Authenticate(sessionData) => sender ! mogopayHandler.authenticate(sessionData)
  }
}