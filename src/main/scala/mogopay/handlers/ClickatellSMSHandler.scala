package mogopay.handlers

import java.net.{URLConnection, URL, URLEncoder}
import java.io.{InputStreamReader, BufferedReader}
import mogopay.config.Settings

trait ClickatellSMSHandler {
  val sender = Settings.clickatell.sender
  val sessionIdUrl = Settings.clickatell.sessionIdUrl
  val sendFlashUrlPattern = Settings.clickatell.sendFlashUrlPattern

  def sendSms(message: String, receiver: String): Unit = {

    val encodedMessage = URLEncoder.encode(message, "UTF-8")
    val urlSession = new URL(sessionIdUrl)

    val sessionIdConnection: URLConnection = urlSession.openConnection()

    val brSession: BufferedReader = new BufferedReader(
      new InputStreamReader(sessionIdConnection.getInputStream))
    var sessionId: String = brSession.readLine()
    brSession.close()

    sessionId = sessionId.substring(4)
    val sendFlashUrl = sendFlashUrlPattern.format(
      sessionId, receiver, sender, encodedMessage)

    val urlFlash: URL = new URL(sendFlashUrl)
    val sendFlashConnection: URLConnection = urlFlash.openConnection()
    val brSendFlash: BufferedReader = new BufferedReader(
      new InputStreamReader(sendFlashConnection.getInputStream))
    brSendFlash.close()
  }
}
