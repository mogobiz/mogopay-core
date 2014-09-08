package mogopay.session

import java.util.UUID

import mogopay.config.Settings
import spray.http.DateTime

import scala.collection.mutable.Map

case class Session(data: Session.Data = Map((Settings.SessionCookieName, UUID.randomUUID.toString)),
                   expires: Option[DateTime] = Some(DateTime.now + (Settings.SessionMaxAge * 1000)),
                   maxAge: Option[Long] = Some(Settings.SessionMaxAge),
                   domain: Option[String] = None,
                   path: Option[String] = Some("/"),
                   secure: Boolean = false,
                   httpOnly: Boolean = true,
                   extension: Option[String] = None) {
  private var dirty: Boolean = false

  def clear() = {
    val theId = id
    data.clear()
    this += Settings.SessionCookieName ->  theId
  }
  def isDirty = dirty

  def get(key: String): Option[Any] = data.get(key)

  def isEmpty: Boolean = data.isEmpty

  def contains(key: String): Boolean = data.contains(key)

  def -=(key: String): Session = synchronized {
    dirty = true
    data -= key
    this
  }

  def +=(kv: (String, Any)): Session = synchronized {
    dirty = true
    data += kv
    this
  }

  def apply(key: String): Any = data(key)

  val id = data(Settings.SessionCookieName).asInstanceOf[String]
}

object Session {
  type Data = Map[String, Any]
}
