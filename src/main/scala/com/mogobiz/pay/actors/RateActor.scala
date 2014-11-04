package com.mogobiz.pay.actors

import akka.actor.Actor
import com.mogobiz.pay.config.MogopayHandlers._

import scala.util.Try

object RateActor {

  case class ListRates()

  case class Format(amount: Long, currency: String, country: String)

}

class RateActor extends Actor {

  import RateActor._

  def receive: Receive = {
    case ListRates => sender ! Try(rateHandler.list)
    case Format(x, y, z) => sender ! Try(rateHandler.format(x, y, z))
  }
}
