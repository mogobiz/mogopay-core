/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay

import scala.concurrent.Future

object SprayTestsNoMagnet {

  trait Route

  def host[T](hostnames: T)(implicit ev: T => String): Route => Route = (route: Route) => route

  implicit def stringToString(input: String): String = ???

  val tmpHost: Route => Route = host("mogobiz.io")

  tmpHost {
    new Route {}
  }

}

object SprayTests {

  trait Route

  sealed trait HostMagnet {
    def apply(): (Route) => Route
  }

  object HostMagnet {
    implicit def from[T](obj: T)(implicit ev: T => String) =
      new HostMagnet {
        def apply() = (route: Route) => {
          ev(obj);
          route
        }
      }
  }

  def host(magnet: HostMagnet) = magnet()

  implicit def stringToString(input: String): String = ???

  // ma route
  val route = host("mogobiz.io") {
    new Route {}
  }
}

object SprayTests2 {

  trait Route

  sealed trait HostMagnet {
    def apply(): (Route) => Route
  }

  object HostMagnet {
    implicit def from[T](obj: T)(implicit ev: T => String) =
      new HostMagnet {
        def apply() = (route: Route) => route
      }
  }

  def host(magnet: HostMagnet) = magnet()

  implicit def stringToString(input: String): String = ???

  type StatusCode = Int

  // En commentaire ci-dessous ce que l'on cherche à faire avec les magnet
  // Car impossible sans magnet car complete(Future[T]) de vient complete(Future) dans la JVM
  // ce qui interdit de surcharger les fonctions par le type paramétré.
  //  def complete[T](code: StatusCode, response: T) = ???
  //  def complete[T](code: StatusCode, headers: List[(String, String)], response: T) = ???
  //  def complete[T](code: Future[StatusCode]) = ???
  //  def complete[T](code: Future[String]) = ???

  sealed trait CompleteMagnet {
    type Result

    def apply(): Result
  }

  object CompleteMagnet {
    implicit def fromStatusAndResponse[T](tuple: (StatusCode, T)) =
      new CompleteMagnet {
        type Result = Unit

        def apply(): Result = ???
      }

    implicit def fromFutureStatus[T](future: Future[StatusCode]) =
      new CompleteMagnet {
        type Result = Int

        def apply(): Result = ???
      }

    implicit def fromFutureResponse[T](future: Future[String]) =
      new CompleteMagnet {
        type Result = Int

        def apply(): Result = ???
      }

  }

  def complete(magnet: CompleteMagnet): magnet.Result = magnet()

  // ma route
  val route = host("mogobiz.io") {
    new Route {
      complete(100, "")
    }
  }
}
