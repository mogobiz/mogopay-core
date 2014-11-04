package com.mogobiz.pay.services


import java.io.File

import com.mogobiz.pay.actors.PdfActor.HtmlPdfRequest
import com.mogobiz.pay.config.DefaultComplete
import spray.http.{ContentType, MediaTypes, HttpData, HttpEntity}
import spray.routing.Directives

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Try

class PdfService(pdfActor: ActorRef)(implicit executionContext: ExecutionContext) extends Directives with DefaultComplete {
  implicit val timeout = Timeout(10.seconds)
  val route = pathPrefix("pdf") {
    convertHtml2Pdf
  }

  lazy val convertHtml2Pdf = path("html-to-pdf") {
    post {
      var fields = formFields('page, 'xhtml)
      fields { (page, xhtml) =>
        onComplete((pdfActor ? HtmlPdfRequest(page, xhtml)).mapTo[Try[File]]) { call =>
          handleComplete(call, (pdfFile: File) => {
            complete(HttpEntity(ContentType(MediaTypes.`application/pdf`), HttpData(pdfFile)))
          })
        }
      }
    }
  }
}
