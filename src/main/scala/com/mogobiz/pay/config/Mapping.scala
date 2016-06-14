/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.config

import akka.http.scaladsl.model.{ MediaTypes, _ }
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.mogobiz.es.EsClient
import com.mogobiz.utils.HttpRequestor
import com.sksamuel.elastic4s.ElasticDsl._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

object Mapping extends HttpRequestor {

  def mappingNames = List("Account", "BOTransaction", "BOTransactionLog", "Country", "CountryAdmin", "Rate", "TransactionRequest", "TransactionSequence")

  def clear = EsClient().execute(delete index Settings.Mogopay.EsIndex).await

  def set() {
    def route(url: String) = "http://" + com.mogobiz.es.Settings.ElasticSearch.FullUrl + url
    def mappingFor(name: String) = getClass().getResourceAsStream(s"/es/pay/mappings/$name.json")

    implicit val system = akka.actor.ActorSystem("mogopay-boot")
    implicit val materializer = ActorMaterializer()

    mappingNames foreach { name =>
      val url = s"/${Settings.Mogopay.EsIndex}/_mapping/$name"
      val mapping = scala.io.Source.fromInputStream(mappingFor(name)).mkString
      val updatedMapping = mapping.replaceAllLiterally("{{ttl}}", Settings.TransactionRequestDuration.toString + "m")

      val request = HttpRequest(
        method = HttpMethods.POST,
        uri = Uri(route(url)),
        entity = HttpEntity(MediaTypes.`application/json`, updatedMapping)
      )

      val x: Future[Any] = doRequest(request) map { response: HttpResponse =>
        response.status match {
          case StatusCodes.OK => System.err.println(s"The mapping for `$name` was successfully set.")
          case _ =>
            Unmarshal(response.entity).to[String].map { data =>
              System.err.println(s"Error while setting the mapping for `$name`: ${data}")
            }
        }
      }
      Await.result(x, 10 seconds)
    }

    system.shutdown
  }

}
