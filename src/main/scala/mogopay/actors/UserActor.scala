package mogopay.actors

import akka.actor.Actor
import mogopay.config.HandlersConfig._

import scala.util.Try

object UserActor {

  case class Register(successURL: String, errorURL: String, merchantId: String, email: String, password: String)

}

class UserActor extends Actor {

  import UserActor._

  def receive: Receive = {
    case Register(callback_success, callback_error, merchant_id, email, password) =>
      sender ! Try(userHandler.register(callback_success, callback_error, merchant_id, email, password))
  }
}