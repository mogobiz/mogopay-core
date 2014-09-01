package mogopay

import mogopay.actors.{BootedMogopaySystem, MogopayActors}

object Cli extends App with MogopayActors with BootedMogopaySystem
