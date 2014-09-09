package mogopay.jobs

import akka.actor.{Actor, ActorSystem, Props}
import mogopay.config.HandlersConfig._
import scala.concurrent.duration._

object CleanTransactionRequestsJob {
  def start(system: ActorSystem) {
    import system.dispatcher
    val job = system.actorOf(Props[CleanTransactionRequestsJob])
    system.scheduler.schedule(0 second, 5 seconds, job, "")
  }
}

class CleanTransactionRequestsJob extends Actor {
  def receive = {
    case _ => transactionRequestHandler.recycle
  }
}