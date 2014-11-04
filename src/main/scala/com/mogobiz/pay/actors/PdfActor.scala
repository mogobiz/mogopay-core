package com.mogobiz.pay.actors

import java.io.File

import akka.actor.Actor
import com.mogobiz.pay.config.MogopayHandlers._

import scala.util.Try


object PdfActor {
  case class HtmlPdfRequest(page:String, input:String)
}
class PdfActor extends Actor {

  import PdfActor._

  def receive: Receive = {
    case HtmlPdfRequest(page, input) => sender ! Try(pdfHandler.convertToPdf(page, input))
  }
}
