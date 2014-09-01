package mogoauth.services

import akka.util.Timeout
import mogoauth.api.Github2Api
import mogopay.config.Settings
import mogopay.session.SessionESDirectives._
import org.scribe.builder.ServiceBuilder
import org.scribe.model.{OAuthRequest, Verb, Verifier}
import spray.http.StatusCode._
import spray.http.StatusCodes
import spray.routing.Directives

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class GithubService(implicit executionContext: ExecutionContext) extends Directives {
  implicit val timeout = Timeout(10.seconds)

  val route = pathPrefix("oauth") {
    pathPrefix("github") {
      signin ~ callback
    }
  }


  def buildService() = new ServiceBuilder()
    .provider(classOf[Github2Api])
    .apiKey(Settings.GithubConsumerKey)
    .apiSecret(Settings.GithubConsumerSecret)
    .callback(Settings.GithubCallback)
    .scope(Settings.GithubScope)
    .build()

  lazy val signin = path("signin") {

    get {
      val service = buildService()
      session { session =>
        val authURL = service.getAuthorizationUrl(null)
        redirect(authURL, StatusCodes.TemporaryRedirect)
      }
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
              val ResourceUrl = Settings.GithubResourceUrl
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

