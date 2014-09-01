package mogonotify.handlers

import java.io.IOException
import java.security.cert.X509Certificate
import javapns.devices.implementations.basic.BasicDevice
import javapns.notification._
import javax.net.ssl.{KeyManager, SSLContext, X509TrustManager}

import mogonotify.model.MogoNotify.Platform.Platform
import mogonotify.model.MogoNotify.Platform.Platform

import scala.annotation.tailrec
import scala.concurrent.duration._
import mogopay.util.JacksonConverter
import akka.actor.ActorSystem
import com.sksamuel.elastic4s.ElasticDsl._
import mogonotify.model.MogoNotify.{Platform, Device, Notification}
import mogopay.config.Settings
import mogopay.es.EsClient
import spray.client.pipelining._
import spray.http._

import scala.concurrent.{Await, Future}

class NotificationHandler {
  def register(device: Device): Boolean = {
    val req = search in Settings.DB.INDEX -> "Device" filter {
      and(
        termFilter("deviceUuid", device.deviceUuid),
        termFilter("storeCode", device.storeCode)
      )
    }
    // We delete the existing device if any && upsert
    EsClient.update(device)
  }

  def unregister(storeCode: String, regId: String): Boolean = {
    val req = search in Settings.DB.INDEX types "Device" filter {
      and(
        termFilter("regId", regId),
        termFilter("storeCode", storeCode)
      )
    }
    val devices = EsClient.search[Device](req)
    devices.foreach(d => EsClient.delete[Device](d.uuid))
    true
  }

  def notify[T](notification: Notification[T]): Future[HttpResponse] = {
    val (androids, ioss) = notification.regIds.partition(regId => Device.isAndroid(regId))
    gcmNotify(androids, notification.payload)
    apnsNotify(ioss, notification.payload)
  }

  @tailrec
  private def gcmNotify[T](regIds: List[String], data: T): Future[HttpResponse] = {
    case class GCMRequest(registration_ids: List[String], data: String)
    implicit val system = ActorSystem()
    import system.dispatcher
    // Place a special SSLContext in scope here to be used by HttpClient.
    // It trusts all server certificates.
    import mogonotify.util.SSLImplicits.trustfulSslContext

    val pipeline: SendReceive = (
      addHeader("Content-Type", "application/json")
        ~> addCredentials(BasicHttpCredentials(s"key=${Settings.GcmApiKey}"))
        ~> sendReceive
      )

    val MaxNotifs = 1000
    val toSendIds = if (regIds.length > MaxNotifs) regIds.take(MaxNotifs) else regIds
    val payload = JacksonConverter.mapper.writeValueAsString(GCMRequest(toSendIds, data.toString))
    val res = pipeline(Post("https://android.googleapis.com/gcm/send", payload))
    if (regIds.length > MaxNotifs)
      gcmNotify(regIds.drop(MaxNotifs), data)
    else
      res
  }

  lazy val appleNotificationServer: AppleNotificationServer = {
    new AppleNotificationServerBasicImpl(
      Settings.ApnsKeystore,
      Settings.ApnsPassword,
      Settings.ApnsKeystoreType,
      Settings.ApnsHost,
      Integer.parseInt(Settings.ApnsPort))
  }

  @tailrec
  private def apnsNotify[T](regIds: List[String], data: T): Future[HttpResponse] = {
    case class APNSContent(badge: Integer, alert: String)
    // content-available :Integer

    case class APNSRequest(aps: APNSContent)

    implicit val system = ActorSystem()
    import system.dispatcher
    // Place a special SSLContext in scope here to be used by HttpClient.
    // It trusts all server certificates.
    import mogonotify.util.SSLImplicits.trustfulSslContext

    val MaxNotifs = 1000
    val res =
      Future {
        val jsonData = JacksonConverter.mapper.writeValueAsString(APNSRequest(APNSContent(1, data.toString)))
        val payload = new PushNotificationPayload(jsonData)
        val pushManager = new PushNotificationManager()
        pushManager.initializeConnection(appleNotificationServer)

        val toSendIds = if (regIds.length > MaxNotifs) regIds.take(MaxNotifs) else regIds
        val devices = toSendIds.map(new BasicDevice(_))
        val notifications = pushManager.sendNotifications(payload, devices: _*)
        val successCount = PushedNotification.findSuccessfulNotifications(notifications).size()
        if (successCount == 0)
          HttpResponse(StatusCodes.InternalServerError)
        else
          HttpResponse(StatusCodes.OK)
      }
    if (regIds.length > MaxNotifs)
      apnsNotify(regIds.drop(MaxNotifs), data)
    else
      res
  }
}


