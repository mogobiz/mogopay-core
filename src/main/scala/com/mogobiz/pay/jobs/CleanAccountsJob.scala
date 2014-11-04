package com.mogobiz.pay.jobs

import akka.actor.{Actor, ActorSystem, Props}
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.settings.Settings
import scala.concurrent.duration._

object CleanAccountsJob {
  def start(system: ActorSystem) {
    import system.dispatcher

    if (Settings.Jobs.Interval.cleanAccounts > 0) {
      system.scheduler.schedule(
        initialDelay = Settings.Jobs.Delay.cleanAccounts seconds,
        interval     = Settings.Jobs.Interval.cleanAccounts seconds,
        receiver     = system.actorOf(Props[CleanAccountsJob]),
        message      = ""
      )
    }
  }
}

class CleanAccountsJob extends Actor {
  def receive = {
    case _ =>
      println(" == CleanAccountsJob: start.")
      accountHandler.recycle
      println(" == CleanAccountsJob: done.")
  }
}
