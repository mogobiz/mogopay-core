package mogopay.services


import spray.routing.Directives
import mogopay.config.Implicits._

import mogopay.session.SessionESDirectives._

class SampleService extends Directives with DefaultComplete {

  import akka.util.Timeout
  import scala.concurrent.duration._

  implicit val timeout = Timeout(2.seconds)

  val route = {
    pathPrefix("sample") {
      sample
    } ~
      pathPrefix("sample2") {
        sample2
      }
  }
  lazy val sample2 = path("sample2") {
    get {
      session { session =>
        complete {
          session.get("hello")
        }
      }
    }
  }

  class XXX(val x: Long)

  case class Coucou(id: Long, name: String) extends XXX(id + 1)

  case class Hehe(c: Coucou, ac: Array[Coucou])

  lazy val sample = path("sample") {
    get {
      session { session =>
        session +=("hello", "world")
        setSession(session) {
          complete {
            List(Hehe(Coucou(10, "hesllo"), Array(Coucou(10, "hesllo"), Coucou(101, "hesllo2"))))
          }
        }
      }
    }
  }
}
