/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.implicits

import com.mogobiz.pay.model.SessionData
import com.mogobiz.session.Session
import org.json4s.{Formats, DefaultFormats}
import org.json4s.ext.JodaTimeSerializers
import spray.httpx.Json4sJacksonSupport

object Implicits extends Json4sJacksonSupport {
  implicit val json4sJacksonFormats: Formats = DefaultFormats ++ JodaTimeSerializers.all

  implicit class MogopaySession(session: Session) {
    def sessionData: SessionData = {
      val res = session.get("data").getOrElse {
        val sessionData = SessionData(session.id)
        session += ("data" -> sessionData)
        sessionData
      }
      res.asInstanceOf[SessionData]
    }
  }

}
