/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import java.net.{URLConnection, URL, URLEncoder}
import java.io.{InputStreamReader, BufferedReader}

import com.mogobiz.pay.config.Settings

trait ClickatellSMSHandler {
  val sender              = Settings.Clickatell.sender
  val sessionIdUrl        = Settings.Clickatell.sessionIdUrl
  val sendFlashUrlPattern = Settings.Clickatell.sendFlashUrlPattern

  def sendSms(message: String, receiver: String): Unit = {

    val encodedMessage = URLEncoder.encode(message, "UTF-8")
    val urlSession     = new URL(sessionIdUrl)

    val sessionIdConnection: URLConnection = urlSession.openConnection()

    val brSession: BufferedReader = new BufferedReader(new InputStreamReader(sessionIdConnection.getInputStream))
    var sessionId: String = brSession.readLine()
    brSession.close()

    sessionId = sessionId.substring(4)
    val sendFlashUrl = sendFlashUrlPattern.format(sessionId, receiver, sender, encodedMessage)

    val urlFlash: URL                      = new URL(sendFlashUrl)
    val sendFlashConnection: URLConnection = urlFlash.openConnection()
    val brSendFlash: BufferedReader        = new BufferedReader(new InputStreamReader(sendFlashConnection.getInputStream))
    brSendFlash.close()
  }
}
