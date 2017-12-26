/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.analytics

import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.http.scaladsl.server.{Directive0, Directive1}

object AnalyticsDirective {
  def analytics: Directive0 = {
    val request: Directive1[HttpRequest] = extract(_.request)
    request.flatMap { request =>
      val params = request.uri.query.toMultiMap
      if (request.method == HttpMethods.POST) {
        request.entity.data
      } else {}
      pass
    }
  }
}
