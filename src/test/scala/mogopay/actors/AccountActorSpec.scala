package mogopay.actors

import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.ActorSystem
import org.specs2.mutable.SpecificationLike

class AccountActorSpec extends TestKit(ActorSystem()) with SpecificationLike with MogopayActors with MogopaySystem with ImplicitSender {
  import AccountActor._

  // force tests to be executed sequentially
  sequential

  "Registration should" >> {

    "Check if mail already exist" in {
      accountActor ! DoesAccountExistByEmail("hayssams@mogobiz.com", None)
      expectMsg("OK")
      success
    }

    "Check if mail does not exist" in {
      accountActor ! DoesAccountExistByEmail("hayssams@ailleurs.com", None)
      expectMsg("KO")
      success
    }
  }

}
