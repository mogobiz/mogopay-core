package com.mogobiz.pay.handlers

import com.mogobiz.html2pdf.Html2Pdf


/**
 * Created by hayssams on 29/10/14.
 */
class PdfHandler {
  def convertToPdf(page:String, input : String) = Html2Pdf.convert(page,input)
}
