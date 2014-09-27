package mogopay.util

import java.io.ByteArrayOutputStream
import javax.crypto.Cipher
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}

import mogobiz.util.Base64

object SymmetricCrypt {
  def encrypt(clearText: String, cryptoSecret: String, cryptoAlgorithm: String): String = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream
    stream.write(clearText.getBytes)
    var bytes: Array[Byte] = stream.toByteArray
    val cipher: Cipher = Cipher.getInstance(cryptoAlgorithm)
    val cryptoKey: SecretKeySpec = new SecretKeySpec(cryptoSecret.getBytes, cryptoAlgorithm.split("/")(0))
    cipher.init(Cipher.ENCRYPT_MODE, cryptoKey)
    bytes = cipher.doFinal(bytes)
    val useInitializationVector: Boolean = if (cryptoAlgorithm.indexOf('/') < 0) false else cryptoAlgorithm.split("/")(1).toUpperCase ne "ECB"
    if (useInitializationVector) {
      val iv: Array[Byte] = cipher.getIV
      val out2: Array[Byte] = new Array[Byte](iv.length + 1 + bytes.length)
      out2(0) = iv.length.asInstanceOf[Byte]
      System.arraycopy(iv, 0, out2, 1, iv.length)
      System.arraycopy(bytes, 0, out2, 1 + iv.length, bytes.length)
      bytes = out2
    }
    val cryptedData: String = Base64.encodeBytes(bytes, 0, bytes.length, Base64.URL_SAFE)
    return cryptedData
  }

  def decrypt(cryptedData: String, cryptoSecret: String, cryptoAlgorithm: String): String = {
    val cipher: Cipher = Cipher.getInstance(cryptoAlgorithm)
    val cryptoKey: SecretKeySpec = new SecretKeySpec(cryptoSecret.getBytes, cryptoAlgorithm.split("/")(0))
    val useInitializationVector: Boolean = if (cryptoAlgorithm.indexOf('/') < 0) false else cryptoAlgorithm.split("/")(1).toUpperCase ne "ECB"
    var cryptedBytes: Array[Byte] = Base64.decode(cryptedData, Base64.URL_SAFE)
    if (useInitializationVector) {
      val ivLen: Int = cryptedBytes(0)
      val ivSpec: IvParameterSpec = new IvParameterSpec(cryptedBytes, 1, ivLen)
      cipher.init(Cipher.DECRYPT_MODE, cryptoKey, ivSpec)
      cryptedBytes = cipher.doFinal(cryptedBytes, 1 + ivLen, cryptedBytes.length - 1 - ivLen)
    }
    else {
      cipher.init(Cipher.DECRYPT_MODE, cryptoKey)
      cryptedBytes = cipher.doFinal(cryptedBytes)
    }
    new String(cryptedBytes)
  }

  def main(args: Array[String]) {
    val input: String = "{\"storeName\":\"eCommerce\",\"storeCode\":\"ecommerce\",\"owneremail\":\"root@mogobiz.com\",\"ownerfirstname\":\"root\",\"ownerlastname\":\"root\"}"
    val data: String = encrypt(input, "1234567890123456", "AES")
    val res: String = decrypt(data, "1234567890123456", "AES")
    System.out.println(res + "/" + data)
  }
}
