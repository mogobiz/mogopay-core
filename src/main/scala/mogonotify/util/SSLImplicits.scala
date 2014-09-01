package mogonotify.util

import java.security.cert.X509Certificate
import javax.net.ssl.{KeyManager, X509TrustManager, SSLContext}

object SSLImplicits {
  implicit def trustfulSslContext: SSLContext = {
    object BlindFaithX509TrustManager extends X509TrustManager {
      def checkClientTrusted(chain: Array[X509Certificate], authType: String) = ()

      def checkServerTrusted(chain: Array[X509Certificate], authType: String) = ()

      def getAcceptedIssuers = Array[X509Certificate]()
    }
    val context = SSLContext.getInstance("TLS")
    context.init(Array[KeyManager](), Array(BlindFaithX509TrustManager), null)
    context
  }
}
