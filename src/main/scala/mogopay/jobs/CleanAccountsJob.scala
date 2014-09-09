package mogopay.jobs

import akka.actor.{Actor, ActorSystem, Props}
import mogopay.config.HandlersConfig._
import scala.concurrent.duration._

object CleanAccountsJob {
  def start(system: ActorSystem) {
    import system.dispatcher
    val job = system.actorOf(Props[CleanAccountsJob])
    system.scheduler.schedule(0 second, 5 seconds, job, "")
  }
}

class CleanAccountsJob extends Actor {
  def receive = {
    case _ => accountHandler.recycle
  }
}
