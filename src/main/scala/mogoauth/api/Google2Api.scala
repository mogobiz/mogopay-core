package mogoauth.api

import org.scribe.builder.api.DefaultApi20
import org.scribe.extractors.JsonTokenExtractor
import org.scribe.model.{Verb, OAuthConfig}
import org.scribe.utils.{OAuthEncoder, Preconditions}


class Google2Api extends DefaultApi20 {
  val AuthorizeUrl = "https://accounts.google.com/o/oauth2/auth?response_type=code&client_id=%s&redirect_uri=%s"
  val ScopedAuthorizeUrl = AuthorizeUrl + "&scope=%s"

  override def getAccessTokenEndpoint() = "https://accounts.google.com/o/oauth2/token"

  override def getAccessTokenVerb() = Verb.POST

  override def getAccessTokenExtractor() = new JsonTokenExtractor()

  override def getAuthorizationUrl(config: OAuthConfig) = {
    Preconditions.checkValidUrl(config.getCallback(), "Must provide a valid url as callback. Google2 does not support OOB");
    // Append scope if present
    if (config.hasScope()) {
      String.format(ScopedAuthorizeUrl, config.getApiKey(), OAuthEncoder.encode(config.getCallback()), OAuthEncoder.encode(config.getScope()));
    }
    else {
      String.format(AuthorizeUrl, config.getApiKey(), OAuthEncoder.encode(config.getCallback()));
    }
  }
}
