package mogopay.services

import akka.actor.ActorRef
import akka.util.Timeout
import akka.pattern.ask
import mogopay.actors.UserActor._
import mogopay.config.Implicits._
import spray.http.StatusCodes
import spray.routing.Directives

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util._

class UserService(actor: ActorRef)(implicit executionContext: ExecutionContext) extends Directives with DefaultComplete {
  implicit val timeout = Timeout(10.seconds)

  val route = {
    pathPrefix("user") {
      register
    }
  }

  lazy val register = path("register") {
    get {
      val params = parameters('callback_success, 'callback_error, 'merchant_id, 'email, 'password)
      params { (callback_success, callback_error, merchant_id, email, password) =>
        val message = Register(callback_success, callback_error, merchant_id, email, password)
        onComplete((actor ? message).mapTo[Try[Map[String, String]]]) { call =>
          handleComplete(call, (data: Map[String, String]) => complete(StatusCodes.OK -> data))
        }
      }
    }
  }
}
