package mogopay.util

/**
 * Created by hayssams on 08/09/14.
 */
class TrustedSSLFactory {

}

import javax.net.ssl._
import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.security.cert.X509Certificate

object TrustedSSLFactory {
  def getTrustingSSLSocketFactory: SSLSocketFactory = {
    TrustedSSLFactory.createSSLSocketFactory
  }

  private def createSSLSocketFactory: SSLSocketFactory = {
    val trustManagers: Array[TrustManager] = Array[TrustManager](new TrustedSSLFactory.NaiveTrustManager)
    var sslContext: SSLContext = null
    try {
      sslContext = SSLContext.getInstance("SSL")
      sslContext.init(new Array[KeyManager](0), trustManagers, new SecureRandom)
      sslContext.getSocketFactory
    }
    catch {
      case e: GeneralSecurityException => {
        null
      }
    }
  }

  val JaxwsSslSockeetFactory: String = "com.sun.xml.internal.ws.transport.https.client.SSLSocketFactory"


  private class NaiveTrustManager extends X509TrustManager {
    def checkClientTrusted(certs: Array[X509Certificate], authType: String) {
    }

    def checkServerTrusted(certs: Array[X509Certificate], authType: String) {
    }

    def getAcceptedIssuers: Array[X509Certificate] = {
      new Array[X509Certificate](0)
    }
  }

}


