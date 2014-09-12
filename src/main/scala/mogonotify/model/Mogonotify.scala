package mogonotify.model

import java.util.{Calendar, Date}


import mogopay.config.Settings
import spray.httpx.unmarshalling.{MalformedContent, FromStringDeserializer}

import scala.util.control.NonFatal

object MogoNotify {
  type Document = String

  object Platform extends Enumeration {
    type Platform = Value
    val ANDROID = Value("ANDROID")
    val IOS = Value("IOS")
  }

  import Platform._

  implicit def PlatformUnmarshaller = new FromStringDeserializer[Platform] {
    def apply(value: String) =
      try
        Right(Platform.withName(value))
      catch {
        case NonFatal(ex) => Left(MalformedContent(s"Cannot parse: $value", ex))
      }
  }

  import mogonotify.model.MogoNotify.Platform._

  case class Device(uuid: String,
                    deviceUuid: String,
                    storeCode: String,
                    regId: String,
                    platform: Platform,
                    lang: String,
                    clientId: Option[String] = None,
                    var dateCreated: Date = Calendar.getInstance().getTime,
                    var lastUpdated: Date = Calendar.getInstance().getTime)

  case class Notification[T](uuid: String,
                             store: String,
                             regIds: List[String],
                             lang: String,
                             payload: T,
                             var dateCreated: Date = Calendar.getInstance().getTime,
                             var lastUpdated: Date = Calendar.getInstance().getTime)


  object Device {
    def isAndroid(regId: String) = !isIOS(regId)

    def isIOS(regId: String) = regId.length == Settings.ApnsTokenSize
  }

}
