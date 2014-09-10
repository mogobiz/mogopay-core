package mogopay.jobs

import akka.actor.{Actor, ActorSystem, Props}
import mogopay.config.HandlersConfig._
import mogopay.config.Settings
import scala.concurrent.duration._

object CleanAccountsJob {
  def start(system: ActorSystem) {
    import system.dispatcher
    system.scheduler.schedule(
      initialDelay = Settings.Jobs.Delay.recycleAccount seconds,
      interval     = Settings.Jobs.Interval.cleanAccounts seconds,
      receiver     = system.actorOf(Props[CleanAccountsJob]),
      message      = ""
    )
  }
}

class CleanAccountsJob extends Actor {
  def receive = {
    case _ => accountHandler.recycle
  }
}
