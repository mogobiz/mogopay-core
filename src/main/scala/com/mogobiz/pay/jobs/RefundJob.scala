/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.jobs

import akka.actor.{ Actor, ActorSystem, Props }
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.config.Settings
import scala.concurrent.duration._

object RefundJob {
  def start(system: ActorSystem) {
    import system.dispatcher

    if (Settings.Jobs.Interval.Refund > 0) {
      system.scheduler.schedule(
        initialDelay = Settings.Jobs.Delay.Refund seconds,
        interval = Settings.Jobs.Interval.Refund seconds,
        receiver = system.actorOf(Props[RefundJob]),
        message = ""
      )
    }
  }
}

class RefundJob extends Actor {
  def receive = {
    case _ =>
      println(" == RefundJob: start.")
      transactionHandler.refundGroupPayments()
      println(" == RefundJob: done.")
  }
}
