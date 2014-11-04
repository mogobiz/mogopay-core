package com.mogobiz.pay.actors

import akka.actor.Actor
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.model.Mogopay.SessionData
import com.mogobiz.session.Session

import scala.util.Try

object MogopayActor {

  case class StartPayment(sessionData: SessionData)
  case class Authenticate(sessionData: SessionData)

}

class MogopayActor extends Actor {

  import MogopayActor._

  def receive: Receive = {
    case StartPayment(sessionData) => sender ! Try(mogopayHandler.startPayment(sessionData))
    case Authenticate(sessionData) => sender ! Try(mogopayHandler.authenticate(sessionData))
  }
}