package mogopay.util

import spray.http.HttpResponse

import scala.concurrent.{ExecutionContext, Future}

object GlobalUtil {
  def now = new java.util.Date()

  def newUUID = java.util.UUID.randomUUID().toString

  // From: http://stackoverflow.com/a/1227643/604041
  def caseClassToMap(cc: AnyRef) =
    (Map[String, Any]() /: cc.getClass.getDeclaredFields) {(a, f) =>
      f.setAccessible(true)
      a + (f.getName -> f.get(cc))
    }

  def mapToQueryString(m: Map[String, Any]) = {
    m.map { case (k, v) => s"$k=$v" }.mkString("&")
  }

   def fromHttResponse(response: Future[HttpResponse])(implicit ev:ExecutionContext): Future[Map[String, String]] = {
    response map { response =>
      val data = response.entity.asString.trim
      val pairs = data.split('&')
      val tuples = (pairs map { pair =>
        val tab = pair.split('=')
        tab(0) -> tab(1)
      }).toMap
      tuples
    }
  }

}