package mogopay.config

import mogopay.model.Mogopay.SessionData
import mogopay.session.Session
import spray.httpx.Json4sJacksonSupport
import org.json4s.DefaultFormats

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
