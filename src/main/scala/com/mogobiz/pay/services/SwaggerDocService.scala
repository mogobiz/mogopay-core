package com.mogobiz.pay.services

import akka.actor.ActorSystem
import com.gettyimages.spray.swagger.SwaggerHttpService
import com.wordnik.swagger.model.ApiInfo

/**
 * Created by hayssams on 11/02/15.
 */
class SwaggerDocService(implicit val system:ActorSystem) extends SwaggerHttpService {

import scala.reflect.runtime.universe._

  override def apiTypes = Seq(typeOf[PdfService], typeOf[RateService])

  override def apiVersion = "2.0"

  override def baseUrl = "/"

  // let swagger-ui determine the host and port
  override def docsPath = "api-docs"

  override def actorRefFactory = system

  override def apiInfo = Some(new ApiInfo("Mogobiz Info", "The fastest ecommerce engine ever !!!", "TOC Url", "mogobiz.io", "Apache V2", "http://www.apache.org/licenses/LICENSE-2.0"))

  def allRoutes = super.routes ~
    get {
      pathPrefix("api-docs") {
        pathEndOrSingleSlash {
          getFromResource("swagger-ui/index.html")
        }
      } ~
        getFromResourceDirectory("swagger-ui")
    }
  //authorizations, not used
}
