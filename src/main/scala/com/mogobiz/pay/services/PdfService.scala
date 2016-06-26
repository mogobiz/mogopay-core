/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.services

import java.io.File

import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import spray.http.{ContentType, HttpData, HttpEntity, MediaTypes}
import spray.routing.Directives

class PdfService extends Directives with DefaultComplete {
  val route = pathPrefix("pdf") {
    convertHtml2Pdf
  }

  lazy val convertHtml2Pdf = path("html-to-pdf") {
    post {
      var fields = formFields('page, 'xhtml)
      fields { (page, xhtml) =>
        handleCall(pdfHandler.convertToPdf(page, xhtml), (pdfFile: File) => {
          complete(HttpEntity(ContentType(MediaTypes.`application/pdf`), HttpData(pdfFile)))
        })
      }
    }
  }
}
