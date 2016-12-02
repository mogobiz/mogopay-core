/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.payment

import java.util
import javax.xml.namespace.QName
import javax.xml.soap.SOAPEnvelope
import javax.xml.soap.SOAPHeader
import javax.xml.soap.SOAPMessage
import javax.xml.ws.handler.MessageContext
import javax.xml.ws.handler.soap.SOAPHandler
import javax.xml.ws.handler.soap.SOAPMessageContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URLEncoder
import java.util.Iterator
import java.util.LinkedHashMap
import java.util.Set

import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.model._
import com.mogobiz.utils.GlobalUtil._

class TraceHandler extends SOAPHandler[SOAPMessageContext] {
  def this(transactionUuid: String, transactionShopUuid: String, provider: String, step: TransactionShopStep.TransactionShopStep) {
    this()
    this.transactionUuid = transactionUuid
    this.transactionShopUuid = transactionShopUuid
    this.provider = provider
    this.step = step
  }

  def handleFault(smc: SOAPMessageContext): Boolean = {
    val message: SOAPMessage           = smc.getMessage
    val soapReq: ByteArrayOutputStream = new ByteArrayOutputStream
    try {
      message.writeTo(soapReq)
    } catch {
      case e: Exception => {
        //        getProperty("log").invokeMethod("error", Array[AnyRef]("write soap stream", e))
      }
    } finally {
      try {
        soapReq.close()
      } catch {
        case e: IOException => {
          //          getProperty("log").invokeMethod("info", Array[AnyRef]("close soap stream", e))
        }
      }
      val log: BOTransactionLog = BOTransactionLog(
          uuid = newUUID,
          direction = "IN",
          log = "soapbody=" + URLEncoder.encode(new String(soapReq.toByteArray), "UTF-8"),
          provider = provider,
          transactionUuid = transactionUuid,
          transactionShopUuid = transactionShopUuid,
          step = step
      )
      boTransactionLogHandler.save(log)
    }
    true
  }

  def close(messageContext: MessageContext) {}

  def handleMessage(smc: SOAPMessageContext): Boolean = {
    val outboundProperty: Boolean      = smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY).asInstanceOf[Boolean]
    val message: SOAPMessage           = smc.getMessage
    val soapReq: ByteArrayOutputStream = new ByteArrayOutputStream
    val direction: String              = if (outboundProperty) "Outgoing" else "Incoming"
    try {
      if (!outboundProperty) {
        val soapMsg: SOAPMessage   = smc.getMessage
        val soapEnv: SOAPEnvelope  = soapMsg.getSOAPPart.getEnvelope
        val soapHeader: SOAPHeader = soapEnv.getHeader
        val ite: Iterator[_]       = soapHeader.extractAllHeaderElements
        while (ite.hasNext) {
          val header: SOAPHeader = ite.next.asInstanceOf[SOAPHeader]
          //          getProperty("log").invokeMethod("info", Array[AnyRef](header.getLocalName + "=" + header.getValue))
        }
      }
      message.writeTo(soapReq)
    } catch {
      case e: Exception => {
        //        getProperty("log").invokeMethod("error", Array[AnyRef]("write soap stream", e))
      }
    } finally {
      try {
        soapReq.close()
      } catch {
        case e: IOException => {
          //          getProperty("log").invokeMethod("info", Array[AnyRef]("close soap stream", e))
        }
      }
      //      getProperty("log").invokeMethod("info", Array[AnyRef]("soapbody=" + new String(soapReq.toByteArray)))
      try {
        val log: BOTransactionLog = BOTransactionLog(
            uuid = newUUID,
            direction = if (outboundProperty) "OUT" else "IN",
            log = "soapbody=" + URLEncoder.encode(new String(soapReq.toByteArray)),
            provider = provider,
            transactionUuid = transactionUuid,
            transactionShopUuid = transactionShopUuid,
            step = step
        )
        boTransactionLogHandler.save(log)
      } catch {
        case e: Exception => {
          e.printStackTrace
        }
      }
    }
    outboundProperty
  }

  def getProvider: String = {
    return provider
  }

  def setProvider(provider: String) {
    this.provider = provider
  }

  final def getHeaders: util.Set[QName] = {
    headers
  }

  private var transactionUuid: String = null
  private var transactionShopUuid: String = null
  private var provider: String           = null
  private var step: TransactionShopStep.TransactionShopStep = null
  private final val headers: Set[QName] = null
}
