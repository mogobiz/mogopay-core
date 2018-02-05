/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.implicits

import com.mogobiz.pay.model.Mogopay.SessionData
import com.mogobiz.session.Session
import org.json4s.{DefaultFormats, Formats}
import org.json4s.ext.JodaTimeSerializers
import de.heikoseeberger.akkahttpjson4s.Json4sSupport

object Implicits extends Json4sSupport {
  implicit val json4sJacksonFormats
    : Formats = DefaultFormats ++ JodaTimeSerializers.all

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
