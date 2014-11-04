package com.mogobiz.pay.implicits

import com.mogobiz.pay.model.Mogopay.SessionData
import com.mogobiz.session.Session
import org.json4s.DefaultFormats
import spray.httpx.Json4sJacksonSupport

object Implicits extends Json4sJacksonSupport {
  implicit val json4sJacksonFormats = DefaultFormats

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
