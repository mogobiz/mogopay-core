package mogoauth.api

import org.scribe.builder.api.DefaultApi20
import org.scribe.model.OAuthConfig
import org.scribe.utils.{OAuthEncoder, Preconditions}


class Github2Api extends DefaultApi20 {
  val AUTHORIZE_URL = "https://github.com/login/oauth/authorize?client_id=%s&redirect_uri=%s"
  val SCOPED_AUTHORIZE_URL = AUTHORIZE_URL + "&scope=%s"

  override def getAccessTokenEndpoint() = "https://github.com/login/oauth/access_token"

  override def getAuthorizationUrl(config: OAuthConfig) = {
    Preconditions.checkValidUrl(config.getCallback(), "Must provide a valid url as callback. Github does not support OOB");
    // Append scope if present
    if (config.hasScope()) {
      String.format(SCOPED_AUTHORIZE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()), OAuthEncoder.encode(config.getScope()));
    }
    else {
      String.format(AUTHORIZE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()));
    }
  }
}
