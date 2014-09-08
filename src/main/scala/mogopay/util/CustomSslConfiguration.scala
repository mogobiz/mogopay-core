package mogopay.util

import javax.net.ssl.{KeyManager, X509TrustManager, SSLContext}
import java.security.cert.X509Certificate

import spray.io.ClientSSLEngineProvider

trait CustomSslConfiguration {
  implicit val trustfulSslContext: SSLContext = {
    object BlindFaithX509TrustManager extends X509TrustManager {
      def checkClientTrusted(chain: Array[X509Certificate], authType: String) = ()

      def checkServerTrusted(chain: Array[X509Certificate], authType: String) = ()

      def getAcceptedIssuers = Array[X509Certificate]()
    }

    val context = SSLContext.getInstance("TLS")
    context.init(Array[KeyManager](), Array(BlindFaithX509TrustManager), null)
    context
  }

  implicit val sslEngineProvider: ClientSSLEngineProvider = {
    ClientSSLEngineProvider { engine =>
      //engine.setEnabledCipherSuites(Array("TLS_RSA_WITH_AES_256_CBC_SHA"))
      engine.setEnabledProtocols(Array("SSLv3", "TLSv1"))
      engine
    }
  }
}
