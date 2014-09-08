package mogonotify.services

import akka.actor.ActorRef
import akka.util.Timeout
import mogonotify.actors.NotificationActor._
import mogopay.config.Implicits._

import mogonotify.model.MogoNotify.Platform._
import mogonotify.model.MogoNotify._
import spray.http.HttpResponse
import spray.routing.Directives
import scala.concurrent.duration._
import scala.concurrent.{Await, Future, ExecutionContext}
import akka.pattern.ask


/*
  case class Register(store: String, regId: String, clientId: String, platform: Platform, lang: String)

  case class Unregister(store: String, regId: String)

  case class Notify(regIds: List[String], payload: String)

 */
class NotificationService(notificationActor: ActorRef)(implicit executionContext: ExecutionContext) extends Directives {
  implicit val timeout = Timeout(10.seconds)

  val route = get {
    pathPrefix("oauth") {
      pathPrefix("github") {
        register ~
          unregister ~
          notification
      }
    }
  }

  lazy val register = path("register") {
    get {
      parameters('store, 'deviceUuid, 'regId, 'clientId.?, 'platform.as[Platform], 'lang).as(Register) { register =>
        complete {
          (notificationActor ? register).mapTo[Boolean]
        }
      }
    }
  }

  lazy val unregister = path("unregister") {
    get {
      parameters('store, 'regId).as(Unregister) { unregister =>
        complete {
          (notificationActor ? unregister).mapTo[Boolean]
        }
      }
    }
  }

  lazy val notification = path("notify") {
    get {
      parameterMultiMap { params =>
        val notification = Notify(params("store")(0), params("regId"), params("payload")(0))
        complete {
          val response = (notificationActor ? notification).mapTo[HttpResponse]
          response.map(_.status)
        }
      }
    }
  }

}
