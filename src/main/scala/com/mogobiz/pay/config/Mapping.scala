/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.config

import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.mogobiz.es.EsClient
import com.mogobiz.utils.HttpRequestor
import com.sksamuel.elastic4s.http.ElasticDsl._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Mapping {

  def mappingNames =
    List("Account",
         "BOTransaction",
         "BOTransactionLog",
         "Country",
         "CountryAdmin",
         "Rate",
         "TransactionRequest",
         "TransactionSequence")

  def clear = EsClient().execute(deleteIndex(Settings.Mogopay.EsIndex)).await

  def set() {
    def route(url: String) =
      "http://" + com.mogobiz.es.Settings.ElasticSearch.FullUrl + url

    def mappingFor(name: String) =
      getClass().getResourceAsStream(s"/es/pay/mappings/$name.json")

    implicit val system = akka.actor.ActorSystem("mogopay-boot")

    mappingNames foreach { name =>
      val url = s"/${Settings.Mogopay.EsIndex}/_mapping/$name"
      val mapping = scala.io.Source.fromInputStream(mappingFor(name)).mkString
      // TODO: remove ttl
      val updatedMapping = mapping.replaceAllLiterally(
        "{{ttl}}",
        Settings.TransactionRequestDuration.toString + "m")

      val request = HttpRequest(
        method = HttpMethods.POST,
        uri = Uri(url),
        entity = HttpEntity(MediaTypes.`application/json`, updatedMapping)
      )
      val response = HttpRequestor.doRequest(request)
      response map { response: HttpResponse =>
        response.status match {
          case StatusCodes.OK =>
            System.err.println(s"The mapping for `$name` was successfully set.")
          case _ =>
            Unmarshal(response.entity).to[String].map { data =>
              System.err.println(
                s"Error while setting the mapping for `$name`: $data")
            }
        }
      }
      Await.result(response, 10.seconds)
    }
    system.terminate()
  }

}
