/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import com.mogobiz.html2pdf.Html2Pdf

class PdfHandler {
  def convertToPdf(page: String, input: String) = Html2Pdf.convert(page, input)
}
