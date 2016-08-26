/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import java.util.{UUID, Calendar}

import com.mogobiz.es.EsClient
import com.mogobiz.pay.model._
import org.specs2.mutable._

class EsClientSpec extends Specification {
  val Uuid = "a7d60bc3-8bf3-43bc-bd1d-fac7d5c539bd"
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
    Some(AccountAddress("Rue Meriau",
      Some("Tour Panorama"),
      "Paris",
      Some("75015"),
      None,
      Some(Civility.MR),
      Some("Me2"),
      Some("You2"),
      None,
      Some(Telephone(
        "0102030405",
        "3314567890987",
        "987",
        Some("123"),
        TelephoneStatus.ACTIVE)),
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

//  "Esclient" should {
//    "Succeed to create object" in {
//      val uuid = EsClient.index(account)
//      assert(uuid === Uuid)
//    }
//    "Succeed to load object" in {
//      val acc = EsClient.load[Account](Uuid)
//      acc must beSome[Account]
//    }
//    "update object" in {
//      val created = EsClient.update(account.copy(email = "you@you.com"), Uuid, false)
//      assert(false === created)
//      val loaded = EsClient.load[Account](Uuid)
//      loaded must beSome[Account]
//      assert(loaded.get.email === "you@you.com")
//    }
//    "delete account" in {
//      val found = EsClient.delete[Account](Uuid)
//      found must beTrue
//    }
//  }
}
