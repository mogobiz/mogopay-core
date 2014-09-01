package mogoauth.services

import akka.util.Timeout
import mogopay.config.Settings
import mogopay.session.SessionESDirectives._
import org.scribe.builder.ServiceBuilder
import org.scribe.builder.api.FacebookApi
import org.scribe.model.{OAuthRequest, Verb, Verifier}
import spray.http.StatusCode._
import spray.http.StatusCodes
import spray.routing.Directives

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class FacebookService(implicit executionContext: ExecutionContext) extends Directives {
  implicit val timeout = Timeout(10.seconds)

  val route = pathPrefix("oauth") {
    pathPrefix("facebook") {
      signin ~ callback
    }
  }

  def buildService() = new ServiceBuilder()
    .provider(classOf[FacebookApi])
    .apiKey(Settings.FacebookConsumerKey)
    .apiSecret(Settings.FacebookConsumerSecret)
    .callback(Settings.FacebookCallback)
    .build()

  lazy val signin = path("signin") {
    get {
      val service = buildService()
      val authURL = service.getAuthorizationUrl(null)
      redirect(authURL, StatusCodes.TemporaryRedirect)
    }
  }

  lazy val callback = path("callback") {
    get {
      session {
        session =>
          parameters('code.?) { code =>
            if (code.isDefined) {
              val service = buildService()
              val verifier = new Verifier(code.get)
              val accessToken = service.getAccessToken(null, verifier)
              val ResourceUrl = Settings.FacebookResourceUrl
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
