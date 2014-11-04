package com.mogobiz.pay.jobs

import akka.actor.{Actor, ActorSystem, Props}
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.settings.Settings
import scala.concurrent.duration._

object CleanTransactionRequestsJob {
  def start(system: ActorSystem) {
    import system.dispatcher

    if (Settings.Jobs.Interval.cleanTransactionRequests > 0) {
      system.scheduler.schedule(
        initialDelay = Settings.Jobs.Delay.cleanTransactionRequests seconds,
        interval = Settings.Jobs.Interval.cleanTransactionRequests seconds,
        receiver = system.actorOf(Props[CleanTransactionRequestsJob]),
        message = "")
    }
  }
}

class CleanTransactionRequestsJob extends Actor {
  def receive = {
    case _ =>
      println(" == CleanTransactionRequestsJob: start.")
      transactionRequestHandler.recycle
      println(" == CleanTransactionRequestsJob: done.")
  }
}