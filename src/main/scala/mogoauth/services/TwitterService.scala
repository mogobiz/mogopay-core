package mogoauth.services

import akka.util.Timeout
import mogopay.config.Settings
import mogopay.session.SessionESDirectives._
import org.scribe.builder.ServiceBuilder
import org.scribe.builder.api.TwitterApi
import org.scribe.model.{OAuthRequest, Token, Verb, Verifier}
import spray.http.StatusCode._
import spray.http.StatusCodes
import spray.routing.Directives

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class TwitterService(implicit executionContext: ExecutionContext) extends Directives {
  implicit val timeout = Timeout(10.seconds)

  val route = pathPrefix("oauth") {
    pathPrefix("twitter") {
      signin ~ callback
    }
  }

  def buildService() = new ServiceBuilder()
    .provider(classOf[TwitterApi.Authenticate])
    .apiKey(Settings.TwitterConsumerKey)
    .apiSecret(Settings.TwitterConsumerSecret)
    .callback(Settings.TwitterCallback)
    .build()

  lazy val signin = get {
    path("signin") {
      session { session =>
        val service = buildService()
        val requestToken = service.getRequestToken()
        val authURL = service.getAuthorizationUrl(requestToken)
        setSession(session += "oauthToken" -> requestToken.getToken += "oauthSecret" -> requestToken.getSecret) {
          redirect(authURL, StatusCodes.TemporaryRedirect)
        }
      }
    }
  }

  lazy val callback = get {
    path("callback") {
      session {
        session =>
          val token = session("oauthToken").toString
          val secret = session("oauthSecret").toString
          parameters('oauth_verifier.?, 'oauth_token.?) { (oauth_verifier, oauth_token) =>
            if (oauth_verifier.isDefined) {
              val service = buildService()
              val verifier = new Verifier(oauth_verifier.get)
              val requestToken = new Token(token, secret)
              val accessToken = service.getAccessToken(requestToken, verifier)
              val params = accessToken.getRawResponse.split('&').map { kv =>
                val kvArray = kv.split('=')
                (kvArray(0), kvArray(1))
              } toMap
              val ResourceUrl = Settings.TwitterResourceUrl + s"screen_name=${params("screen_name")}"
              val request = new OAuthRequest(Verb.GET, ResourceUrl)
              service.signRequest(accessToken, request)
              val response = request.send()
              if (response.getCode == StatusCodes.OK.intValue) {
                complete {
                  response.getBody
                }
              }
              else {
                complete(int2StatusCode(response.getCode))
              }
            }
            else {
              complete(StatusCodes.Unauthorized)
            }
          }
      }
    }
  }
}
