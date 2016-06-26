/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.jobs

import akka.actor.{Actor, ActorSystem, Props}
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.config.Settings
import scala.concurrent.duration._

object CleanAccountsJob {
  def start(system: ActorSystem) {
    import system.dispatcher

    if (Settings.Jobs.Interval.CleanAccounts > 0) {
      system.scheduler.schedule(
          initialDelay = Settings.Jobs.Delay.CleanAccounts seconds,
          interval = Settings.Jobs.Interval.CleanAccounts seconds,
          receiver = system.actorOf(Props[CleanAccountsJob]),
          message = ""
      )
    }
  }
}

class CleanAccountsJob extends Actor {
  def receive = {
    case _ =>
      println(" == CleanAccountsJob: start.")
      accountHandler.recycle()
      println(" == CleanAccountsJob: done.")
  }
}
