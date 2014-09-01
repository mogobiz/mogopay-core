package mogoauth.services

import akka.util.Timeout
import mogoauth.api.Google2Api
import mogopay.config.Settings
import mogopay.session.SessionESDirectives._
import mogopay.util.JacksonConverter
import org.scribe.builder.ServiceBuilder
import org.scribe.exceptions.OAuthException
import org.scribe.model._
import spray.http.StatusCodes
import spray.routing.Directives

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import spray.http.StatusCode._

class GoogleService(implicit executionContext: ExecutionContext) extends Directives {
  implicit val timeout = Timeout(10.seconds)

  val route = pathPrefix("oauth") {
    pathPrefix("google") {
      signin ~ callback
    }
  }

  def buildService() = new ServiceBuilder()
    .provider(classOf[Google2Api])
    .apiKey(Settings.GoogleConsumerKey)
    .apiSecret(Settings.GoogleConsumerSecret)
    .callback(Settings.GoogleCallback)
    .scope(Settings.GoogleScope)
    .build()

  val api = new Google2Api()

  def getAccessToken(requestToken: Token, verifier: Verifier): Token = {
    val request = new OAuthRequest(Verb.POST, api.getAccessTokenEndpoint())
    request.addBodyParameter(OAuthConstants.CLIENT_ID, Settings.GoogleConsumerKey)
    request.addBodyParameter(OAuthConstants.CLIENT_SECRET, Settings.GoogleConsumerSecret)
    request.addBodyParameter(OAuthConstants.CODE, verifier.getValue())
    request.addBodyParameter(OAuthConstants.REDIRECT_URI, Settings.GoogleCallback)
    request.addBodyParameter("grant_type", "authorization_code")
    request.addBodyParameter(OAuthConstants.SCOPE, Settings.GoogleScope)
    println(request.getBodyContents)
    val response = request.send()
    val accessData = JacksonConverter.deserialize[Map[String, String]](response.getBody)
    val accessToken = accessData.get("access_token")
    new Token(accessToken.getOrElse(throw new OAuthException("Cannot extract an access token. Response was: " + response.getBody)), "", response.getBody)
  }

  lazy val signin = path("signin") {
    get {
      val service = buildService()
      val authURL = service.getAuthorizationUrl(null)
      redirect(authURL, StatusCodes.TemporaryRedirect)
    }
  }

  lazy val callback = path("callback") {
    get {
      parameters('code.?) { code =>
        if (code.isDefined) {
          val service = buildService()
          val verifier = new Verifier(code.get)
          val accessToken = getAccessToken(null, verifier)
          println(accessToken.getRawResponse)
          val ResourceUrl = Settings.GoogleResourceUrl
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
