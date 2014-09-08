package mogopay.util

import javax.net.ssl.{SSLSession, HostnameVerifier}

object NaiveHostnameVerifier {
  val JaxwsHostNameVerifier: String = "com.sun.xml.internal.ws.transport.https.client.hostname.verifier"
}

class NaiveHostnameVerifier extends HostnameVerifier {
  def verify(hostName: String, session: SSLSession): Boolean = true
}


