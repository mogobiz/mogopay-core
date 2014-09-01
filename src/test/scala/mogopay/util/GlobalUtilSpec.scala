package mogopay.util

import org.specs2.mutable.Specification
import mogopay.util.GlobalUtil._

class GlobalUtilSpec extends Specification {
  "caseClassToMap" should {
    "make a Map out of a case class" in {
      case class A(foo: String, bar: Int)
      val a = A("aze", 10)
      caseClassToMap(a) must havePairs("foo" -> "aze", "bar" -> 10)
    }
  }

  "mapToQueryString" should {
    "make a query string out of a Map" in {
      val m = Map("foo" -> 1, "bar" -> "2")
      mapToQueryString(m) must_== "foo=1&bar=2"
    }
  }
}