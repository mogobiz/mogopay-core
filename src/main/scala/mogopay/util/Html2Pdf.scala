package mogopay.util

import java.io._
import java.nio.charset.Charset

import com.itextpdf.text.{Rectangle, PageSize, Document}
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.tool.xml.XMLWorkerHelper

object Html2Pdf {
  def pageSize(page: String): Rectangle = {
    page match {
      case "A4" => PageSize.A4
      case "Letter" => PageSize.LETTER
      case "Legal" => PageSize.LEGAL
      case _ => PageSize.A4
    }
  }

  def convert(page: String, input: String): File = {
    val inputStream = new ByteArrayInputStream(input.getBytes(Charset.forName("UTF-8")))
    val outProcessFile = File.createTempFile("htmlToPdf", ".pdf")
    val document = new Document(pageSize(page))
    val writer = PdfWriter.getInstance(document, new FileOutputStream(outProcessFile))
    document.open()
    XMLWorkerHelper.getInstance().parseXHtml(writer, document, inputStream)
    document.close()
    outProcessFile
  }
}
