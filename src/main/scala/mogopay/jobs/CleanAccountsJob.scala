package mogopay.jobs

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import mogopay.config.HandlersConfig._

object CleanAccountsJob {
  def start(system: ActorSystem) {
    val scheduler = QuartzSchedulerExtension(system)
    val job = system.actorOf(Props[CleanAccountsJob])
    scheduler.schedule("CleanAccounts", job, "")
  }
}

class CleanAccountsJob extends Actor {
  def receive = {
    case _ => accountHandler.recycle
  }
}
