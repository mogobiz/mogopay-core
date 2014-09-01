trait Route


def host[T](hostnames: T)(implicit ev: T => String): Route => Route = (route: Route) => route

implicit def stringToString(input: String): String = ???

host("hello") {
  new Route {}
}
