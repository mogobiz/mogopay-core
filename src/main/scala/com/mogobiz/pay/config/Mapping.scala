/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.config

import com.mogobiz.es.EsClient
import com.sksamuel.elastic4s.ElasticDsl._
import spray.client.pipelining._
import spray.http._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

object Mapping {

  def mappingNames = List("Account", "BOTransaction", "BOTransactionLog", "Country", "CountryAdmin", "Rate", "TransactionRequest", "TransactionSequence")

  def clear = EsClient().execute(delete index Settings.Mogopay.EsIndex).await

  def set() {
    def route(url: String) = "http://" + com.mogobiz.es.Settings.ElasticSearch.FullUrl + url
    def mappingFor(name: String) = getClass().getResourceAsStream(s"/es/pay/mappings/$name.json")

    implicit val system = akka.actor.ActorSystem("mogopay-boot")
    val pipeline: HttpRequest => scala.concurrent.Future[HttpResponse] = sendReceive

    mappingNames foreach { name =>
      val url = s"/${Settings.Mogopay.EsIndex}/$name/_mapping"
      val mapping = scala.io.Source.fromInputStream(mappingFor(name)).mkString
      val updatedMapping = mapping.replaceAllLiterally("{{ttl}}", Settings.TransactionRequestDuration.toString + "m")
      val x: Future[Any] = pipeline(Post(route(url), updatedMapping)) map { response: HttpResponse =>
        response.status match {
          case StatusCodes.OK => System.err.println(s"The mapping for `$name` was successfully set.")
          case _ => System.err.println(s"Error while setting the mapping for `$name`: ${response.entity.asString}")
        }
      }
      Await.result(x, 10 seconds)
    }

    system.shutdown
  }

}
