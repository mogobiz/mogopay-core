/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.tests

import java.util.{Calendar, UUID}

import com.mogobiz.es.{EsClient}
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.model._
import com.mogobiz.session.{ESBackend, Session}
import com.sksamuel.elastic4s.ElasticDsl._

// Small Unit Test
case class XXX(a: Int = 10, b: String = "Helmlo")

object TestApp extends App {
  println("start Test App")
  val be = new ESBackend {}
  //val session = be.load("8cba4b06-e09e-4450-920a-670669187b6a").get
  val session = Session()
  session += ("hello", "coucou")
  session += ("hello2", XXX())
  be.store(session)
  //println(be.load("8cba4b06-e09e-4450-920a-670669187b6a").get)

  val Uuid = java.util.UUID.randomUUID().toString
  val account = Account(
      Uuid,
      "me@you.com",
      Some("ebiznext"),
      Some("http://www.ebiznext.com"),
      "changeit",
      Some(Civility.MR),
      Some("Me"),
      Some("You"),
      Some(Calendar.getInstance().getTime),
      Some(
          AccountAddress("Rue Meriau",
                         Some("Tour Panorama"),
                         "Paris",
                         Some("75015"),
                         None,
                         Some(Civility.MR),
                         Some("Me2"),
                         Some("You2"),
                         None,
                         Some(Telephone("0102030405", "3314567890987", "987", Some("123"), TelephoneStatus.ACTIVE)),
                         Some("FRANCE"),
                         Some("Ile De France"),
                         Some("Paris"))),
      AccountStatus.ACTIVE,
      0,
      1000L,
      10000L,
      None,
      None,
      None,
      None,
      List(RoleName.ADMINISTRATOR, RoleName.CUSTOMER),
      None,
      None,
      Nil,
      UUID.randomUUID().toString,
      Nil)
  EsClient.index(Settings.Mogopay.EsIndex, account, false)
  val obj     = EsClient.load[Account](Settings.Mogopay.EsIndex, account.uuid)
  val created = EsClient.update(Settings.Mogopay.EsIndex, account.copy(email = "you@you.com"), false, false)
  val deleted = EsClient.delete[Account](Settings.Mogopay.EsIndex, Uuid, false)
  println(obj)
  println(created)
  println(deleted)

  //  curl -XGET http://localhost:9200/mogopay/ESSession/_search -d '
  //  {
  //    "query" : {
  //      "filtered" : {
  //      "query" : {
  //      "match_all" : { }
  //    },
  //      "filter" : {
  //      "term" : {
  //      "maxAge" : 3700
  //    }
  //    }
  //    }
  //    }
  //  }'
  val req = search in "mogopay" types "Account" postFilter {
    termFilter("lastName", "xsxs")
  }

  //  val req = search in "mogopay" types "Account" query {
  //    filteredQuery query {
  //      matchall
  //    } filter {
  //      termFilter("civility", "mr")
  //    }
  //  }
  val res = EsClient.searchAll[Account](req)
  res.foreach(println)
}
