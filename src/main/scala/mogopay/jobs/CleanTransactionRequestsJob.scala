package mogopay.jobs

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import mogopay.config.HandlersConfig._

object CleanTransactionRequestsJob {
  def start(system: ActorSystem) {
    val scheduler = QuartzSchedulerExtension(system)
    val job = system.actorOf(Props[CleanTransactionRequestsJob])
    scheduler.schedule("CleanTransactionRequests", job, "")
  }
}

class CleanTransactionRequestsJob extends Actor {
  def receive = {
    case _ => transactionRequestHandler.recycle
  }
}