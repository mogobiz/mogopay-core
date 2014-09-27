package mogopay.session

import javax.crypto._
import javax.crypto.spec.SecretKeySpec
import mogopay.config.Settings


trait Crypto {


  def sign(message: String, key: Array[Byte]): String = {
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(new SecretKeySpec(key, "HmacSHA1"))
    Codecs.toHexString(mac.doFinal(message.getBytes("utf-8")))
  }

  def sign(message: String): String =
    sign(message, Settings.SessionSecret.getBytes("utf-8"))

}


object Crypto extends Crypto
