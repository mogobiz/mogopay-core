package mogopay.actors

import akka.actor.Actor
import mogopay.config.HandlersConfig._

object RateActor {

  case class ListRates()

  case class Format(amount: Long, currency: String, country: String)

}

class RateActor extends Actor {

  import RateActor._

  def receive: Receive = {
    case ListRates => sender ! rateHandler.list
    case Format(x, y, z) => sender ! rateHandler.format(x, y, z)
  }
}
