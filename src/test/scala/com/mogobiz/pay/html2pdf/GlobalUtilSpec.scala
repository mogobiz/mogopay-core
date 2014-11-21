package com.mogobiz.pay.html2pdf

import org.specs2.mutable.Specification
import com.mogobiz.utils.GlobalUtil._

import scala.util.{Failure, Success}

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

  "hideStringExceptLastN" should {
    "hide the string except the last N characters" in {
      hideStringExceptLastN("", Integer.MAX_VALUE) must_== Success("")
      hideStringExceptLastN("abc", 0) must_== "***"
      hideStringExceptLastN("abc", 1) must_== "**c"
      hideStringExceptLastN("abc", 2) must_== "*bc"
      hideStringExceptLastN("abc", 3) must_== "abc"
    }
  }
}