package mogopay.util

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.math.BigInteger
import java.security.Key
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher

import mogobiz.util.Base64


object RSA {
  def genKeyPair(folder: File) {
    val kpg: KeyPairGenerator = KeyPairGenerator.getInstance("RSA")
    kpg.initialize(2048)
    val kp: KeyPair = kpg.genKeyPair
    val fact: KeyFactory = KeyFactory.getInstance("RSA")
    val pub: RSAPublicKeySpec = fact.getKeySpec(kp.getPublic, classOf[RSAPublicKeySpec])
    val priv: RSAPrivateKeySpec = fact.getKeySpec(kp.getPrivate, classOf[RSAPrivateKeySpec])
    saveToFile(new File(folder, "public.key"), pub.getModulus, pub.getPublicExponent)
    saveToFile(new File(folder, "private.key"), priv.getModulus, priv.getPrivateExponent)
  }

  private def saveToFile(file: File, mod: BigInteger, exp: BigInteger) {
    val oout: ObjectOutputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))
    try {
      oout.writeObject(mod)
      oout.writeObject(exp)
    }
    catch {
      case e: Exception => {
        throw new IOException("Unexpected error", e)
      }
    }
    finally {
      oout.close
    }
  }

  private def readKeyFromFile(keyFile: InputStream, pub: Boolean): Key = {
    val oin: ObjectInputStream = new ObjectInputStream(new BufferedInputStream(keyFile))
    try {
      val m: BigInteger = oin.readObject.asInstanceOf[BigInteger]
      val e: BigInteger = oin.readObject.asInstanceOf[BigInteger]
      val fact: KeyFactory = KeyFactory.getInstance("RSA")
      if (pub) {
        val keySpec: RSAPublicKeySpec = new RSAPublicKeySpec(m, e)
        return fact.generatePublic(keySpec)
      }
      else {
        val keySpec: RSAPrivateKeySpec = new RSAPrivateKeySpec(m, e)
        return fact.generatePrivate(keySpec)
      }
    }
    catch {
      case e: Exception => {
        e.printStackTrace
        throw new RuntimeException("Spurious serialisation error", e)
      }
    }
    finally {
      oin.close
    }
  }

  def encrypt(data: Array[Byte], publicKey: InputStream): Array[Byte] = {
    val pubKey: Key = readKeyFromFile(publicKey, true)
    val cipher: Cipher = Cipher.getInstance("RSA")
    cipher.init(Cipher.ENCRYPT_MODE, pubKey)
    val cipherData: Array[Byte] = cipher.doFinal(data)
    return cipherData
  }

  def encrypt(data: String, publicKey: InputStream): String = {
    val pubKey: Key = readKeyFromFile(publicKey, true)
    val cipher: Cipher = Cipher.getInstance("RSA")
    cipher.init(Cipher.ENCRYPT_MODE, pubKey)
    val cipherData: Array[Byte] = cipher.doFinal(data.getBytes)
    return Base64.encodeBytes(cipherData, 0, cipherData.length, Base64.URL_SAFE)
  }

  def decrypt(cipherData: Array[Byte], privateKey: InputStream): Array[Byte] = {
    val prvKey: Key = readKeyFromFile(privateKey, false)
    val cipher: Cipher = Cipher.getInstance("RSA")
    cipher.init(Cipher.DECRYPT_MODE, prvKey)
    val data: Array[Byte] = cipher.doFinal(cipherData)
    return data
  }

  def decrypt(cipherData: String, privateKey: InputStream): String = {
    val prvKey: Key = readKeyFromFile(privateKey, false)
    val cipher: Cipher = Cipher.getInstance("RSA")
    cipher.init(Cipher.DECRYPT_MODE, prvKey)
    val data: Array[Byte] = cipher.doFinal(Base64.decode(cipherData, Base64.URL_SAFE))
    return new String(data)
  }

  def main(args: Array[String]) {
    var dataEncoded64: String = RSA.encrypt("hayssam@saleh.fr;customer_email;1234567890123456;AZERAZERAZERAZERAZERAZER", new FileInputStream("/Users/hayssams/git/mogopay-core/mogopay/web-app/WEB-INF/secretkeys/public.key"))
    System.out.println(dataEncoded64)
    System.out.println("^^^^^^^^^^^^")
    var data: String = RSA.decrypt("D3cHhsyst5__sz1Nh75Xk45EGYKFIn94EJb3xH585b0zsmZYjkDlXHo-UhVgkGqpw-aMldc5TDYbv0V54Tvmwbh9zBluhzkN9YN0ZAMaZ38DAKlVpee8bY-HQeB2Kgm9wxvDa_62XddvMIDM8ef4DrqWWhXCoO6lj-NiAha_oSzxTb42uAunBFM4Msl8pe0ctDUtyU5sIjTO4gjvjwQeJVKpRPtuGj4TpNANJaFGNbyiEgU-8ue6iD2oTdTTHwy4XOIlxDShgPkuKrzkjYMoHAr9SSCwV_HaxPJj9zL2PKu_GsV9D9isukI1F1jjdL9jt9-HF8PQGHMg2CfHka_oMA==", new FileInputStream("/Users/hayssams/git/mogopay-core/mogopay/web-app/WEB-INF/secretkeys/private.key"))
    System.out.println(dataEncoded64 + "/" + data)
    val data2: Array[Byte] = RSA.encrypt("1245673241657342671437432714632".getBytes, new FileInputStream("/Users/hayssams/git/mogopay-core/mogopay/web-app/WEB-INF/secretkeys/public.key"))
    val data3: Array[Byte] = RSA.decrypt(data2, new FileInputStream("/Users/hayssams/git/mogopay-core/mogopay/web-app/WEB-INF/secretkeys/private.key"))
    System.out.println(new String(data3))
    dataEncoded64 = RSA.encrypt("demande;1379067702831;039142286;yoann.baudy@ebiznext.com", new FileInputStream("/Users/hayssams/git/mogopay-core/mogopay/web-app/WEB-INF/secretkeys/public.key"))
    System.out.println("******")
    System.out.println(dataEncoded64)
    data = RSA.decrypt("g_U1K1bLqGRuz1v66Al7pcCa_TgwO48kdhh7hAcBGrOFCKw-08f-00okShMoBrvAo26zEFl2sDicr_f_AqRZ7LtM7ngzCpYU7Z-HRk7o75-IunVvtuo4hyY_bN3Q-wdnRPgsO_0-L6_q9-INL5CLcbwQ0pbP2NS4gcJrQ-Gn18vyvLWJVHNHffbiVW3mkzsJngAe8IMSsXhvWaMg0tR9PDnR_C78720HlGS5BMgabUTIsx5orGoUNBEjYVVMWwZtrdhTphm1XRgQExN8YrYqieoCuTdjUlQvqA80jjM0df73qOke163f1W1Jpzbvk4FuUW8a8DXVtJ0MdbUladhj2A==", new FileInputStream("/Users/hayssams/git/mogopay-core/mogopay/web-app/WEB-INF/secretkeys/private.key"))
    System.out.println(dataEncoded64 + "/" + data)
    System.out.println(data)
  }
}
