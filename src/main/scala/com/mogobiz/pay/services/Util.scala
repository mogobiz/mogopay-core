package com.mogobiz.pay.services

import com.mogobiz.pay.exceptions.Exceptions._
import spray.http.StatusCode
import spray.http.StatusCodes
import spray.routing.RequestContext
import spray.routing.directives.PathDirectives
import spray.routing.directives.MethodDirectives
import com.google.i18n.phonenumbers.NumberParseException

object Util {
  def getPath(pathString: String)(f: RequestContext => Unit) = {
    PathDirectives.path(pathString) {
      MethodDirectives.get {
        f
      }
    }
  }

  def toHTTPResponse(t: Throwable): StatusCode = t match {
    case e:MogopayException => e.code
    case _ => StatusCodes.InternalServerError
  }
}
