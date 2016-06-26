/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.analytics

import shapeless.HNil
import spray.http.{HttpMethods, HttpRequest}
import spray.routing.Directives._
import spray.routing._

object AnalyticsDirective {
  def analytics: Directive0 = {
    val request: Directive1[HttpRequest] = extract(_.request)
    request.flatMap[HNil] { request =>
      val params = request.uri.query.toMultiMap
      if (request.method == HttpMethods.POST) {
        request.entity.data
      } else {}
      pass
    }
  }
}
