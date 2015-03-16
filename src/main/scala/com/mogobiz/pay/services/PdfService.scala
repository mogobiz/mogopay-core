package com.mogobiz.pay.services


import java.io.File
import com.mogobiz.pay.implicits.Implicits

import com.mogobiz.pay.config.DefaultComplete
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.model.RequestParameters.{Html2PdfRequest, UpdateProfileRequest}
import com.wordnik.swagger.annotations._
import spray.http.{ContentType, HttpData, HttpEntity, MediaTypes}
import spray.routing.Directives

@Api(value = "/api/pay/pdf", description = "Operations about PDF conversion.", produces = "application/pdf", position = 1)
class PdfService extends Directives with DefaultComplete {
  val route = pathPrefix("pdf") {
    convertHtml2Pdf
  }

  @ApiOperation(nickname = "convertToPdf", httpMethod = "POST",
    value =
      """
        Convert HTML Page to PDF
      """, notes = "", produces = "application/pdf")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "page",
      value = "Page Format, one of :  A4 / Letter / Legal", required = true, dataType = "string", paramType = "form"),
    new ApiImplicitParam(name = "xhtml",
      value = "Input document to convert.", required = true, dataType = "string", paramType = "form")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "PDF document.")
  ))
  def convertHtml2Pdf = path("html-to-pdf") {
    post {
      import Implicits._
      entity(as[Html2PdfRequest]) { req =>
        handleCall(pdfHandler.convertToPdf(req.page, req.xhtml),
          (pdfFile: File) => {
            complete(HttpEntity(ContentType(MediaTypes.`application/pdf`), HttpData(pdfFile)))
          })
      }
    }
  }
}
