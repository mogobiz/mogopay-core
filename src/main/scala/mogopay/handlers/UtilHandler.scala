package mogopay.handlers

import java.security.SecureRandom
import org.json4s._
import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.JdbcBackend
import scala.util._
import java.sql.Timestamp
import java.text.SimpleDateFormat

object UtilHandler {
  /**
   * Used, among other things, to generate CSRF tokens
   */
  def generateNonce(): String = {
    val random: Array[Byte] = Array.fill[Byte](16)(0)
    val buffer: StringBuilder = new StringBuilder()
    val randomSource: SecureRandom  = new SecureRandom()

    val zero: Int = '0'
    val A: Int = 'A'

    randomSource.nextBytes(random)
    for (j <- 0 to random.length - 1) {
      val b1 = ((random(j) & 0xf0) >> 4).toByte
      val b2 = (random(j) & 0x0f).toByte
      if (b1 < 10) buffer.append((zero + b1).toChar)
      else         buffer.append((A + (b1 - 10)).toChar)
      if (b2 < 10) buffer.append((zero + b2).toChar)
      else         buffer.append( (A + (b2 - 10)).toChar)
    }

    buffer.toString()
  }

  def generatePincode3(): String = {
    val generator = new java.util.Random()
    generator.nextInt(10) + "" + generator.nextInt(10) + "" + generator.nextInt(10)
  }

  def checkLuhn(ccNumber: String): Boolean = {
    var sum = 0
    var alternate = false
    for (i <- ccNumber.length() - 1 to 0) {
      var n = Integer.parseInt(ccNumber.substring(i, i + 1))
      if (alternate) {
        n = n * 2
        if (n > 9) n = (n % 10) + 1
      }
      sum += n
      alternate = !alternate
    }
    sum % 10 == 0
  }

  def hideCardNumber(number: String, replacement: String): String = {
    replacement * (number.length - 4) + number.substring(number.length() - 4)
  }

  // From https://stackoverflow.com/a/4608061
  def writeToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f, "UTF-8")
    try { op(p) } finally { p.close() }
  }

  def buildDate(date: String, format: String): java.sql.Timestamp = {
    new Timestamp(new SimpleDateFormat(format).parse(date).getTime)
  }
}
