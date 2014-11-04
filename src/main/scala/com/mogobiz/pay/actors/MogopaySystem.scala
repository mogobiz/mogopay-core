package com.mogobiz.pay.actors

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

