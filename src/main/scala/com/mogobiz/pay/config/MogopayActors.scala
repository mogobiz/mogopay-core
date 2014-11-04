package com.mogobiz.pay.config

import akka.actor.Props
import com.mogobiz.pay.actors._


/**
 * This trait contains the actors that make up our application; it can be mixed in with
 * ``BootedCore`` for running code or ``TestKit`` for unit and integration tests.
 */
trait MogopayActors {
  this: MogopaySystem =>

  val accountActor = system.actorOf(Props[AccountActor])
  val backofficeActor = system.actorOf(Props[BackofficeActor])
  val countryActor = system.actorOf(Props[CountryActor])
  val rateActor = system.actorOf(Props[RateActor])
  val transactionActor = system.actorOf(Props[TransactionActor])
  val systempayActor = system.actorOf(Props[SystempayActor])
  val payPalActor = system.actorOf(Props[PayPalActor])
  val payboxActor = system.actorOf(Props[PayboxActor])
  val paylineActor = system.actorOf(Props[PaylineActor])
  val mogopayActor = system.actorOf(Props[MogopayActor])
  val sipsActor = system.actorOf(Props[SipsActor])
  val userActor = system.actorOf(Props[UserActor])
}
