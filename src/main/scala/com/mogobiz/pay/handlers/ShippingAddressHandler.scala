/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import com.sksamuel.elastic4s.ElasticDsl._
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.model._

class ShippingAddressHandler {
  //  def find(id: Long): Option[ShippingAddress] = dbTransaction {
  //    implicit session =>
  //    shippingAddresses.where(_.id === id).firstOption
  //  }

  private def flatten[T](o: Option[Seq[T]]): Seq[T] = o.map(c => c).getOrElse(Nil)

  def findByAccount(accountId: String): Seq[ShippingAddress] = {
    flatten(accountHandler.load(accountId).map(_.shippingAddresses)) //.find(_.active)
  }

  //  def updateActiveValue(id: Long, active: Boolean) = dbTransaction {
  //    implicit session =>
  //    shippingAddresses.where(_.id === id).map(_.active).update(active)
  //  }
  //
  //  def save(address: ShippingAddress): ShippingAddress =  dbTransaction {
  //    implicit session =>
  //    shippingAddresses returning shippingAddresses += address
  //  }
}
