package mogopay.jobs

import akka.actor.{Actor, ActorSystem, Props}
import mogopay.config.HandlersConfig._
import mogopay.config.Settings
import scala.concurrent.duration._

object CleanTransactionRequestsJob {
  def start(system: ActorSystem) {
    import system.dispatcher
    system.scheduler.schedule(
      initialDelay = Settings.Jobs.Delay.transactionRequest seconds,
      interval     = Settings.Jobs.Interval.cleanTransactionRequests seconds,
      receiver     = system.actorOf(Props[CleanTransactionRequestsJob]),
      message      = "")
  }
}

class CleanTransactionRequestsJob extends Actor {
  def receive = {
    case _ => transactionRequestHandler.recycle
  }
}