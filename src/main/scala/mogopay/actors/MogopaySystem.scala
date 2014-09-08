package mogopay.actors

import akka.actor.{ActorSystem, Props}

/**
 * Core is type containing the ``system: ActorSystem`` member. This enables us to use it in our
 * apps as well as in our tests.
 */
trait MogopaySystem {
  implicit def system: ActorSystem
}

/**
 * This trait implements ``System`` by starting the required ``ActorSystem`` and registering the
 * termination handler to stop the system when the JVM exits.
 */
trait BootedMogopaySystem extends MogopaySystem {
  /**
   * Construct the ActorSystem we will use in our application
   */
  implicit lazy val system = ActorSystem("mogopay")

  /**
   * Ensure that the constructed ActorSystem is shut down when the JVM shuts down
   */
  sys.addShutdownHook(system.shutdown())
}

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
  val sipsActor = system.actorOf(Props[SipsActor])
  val userActor = system.actorOf(Props[UserActor])
}
