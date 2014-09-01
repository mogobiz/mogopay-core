package mogopay.services

import org.specs2.mutable._
import spray.testkit.Specs2RouteTest
import spray.routing.Directives
import mogopay.actors.{MogopaySystem, MogopayActors}

class cAccountServiceSpec extends Specification with Directives with Specs2RouteTest with MogopayRoutes with MogopayActors with MogopaySystem {
  "The DemoService" should {
    "return a greeting for GET requests to the root path" in {
      Get("/alreadyExistEmail?email=hayssams") ~> routes ~> check {
        responseAs[String] must contain("KO")
      }
    }


    "leave GET requests to other paths unhandled" in {
      Get("/unknownService") ~> routes ~> check {
        handled must beFalse
      }
    }
  }
}