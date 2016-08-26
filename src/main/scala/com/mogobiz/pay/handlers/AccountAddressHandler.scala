/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

class AccountAddressHandler {
  //  def findByRoadAndCity(road: String, city: String): Option[AccountAddress] = dbTransaction {
  //    implicit session =>
  //    accountAddresses.where(_.road === road)
  //      .where(_.city === city)
  //      .list
  //      .headOption
  //  }
  //
  //  def save(addr: AccountAddress): AccountAddress = dbTransaction { implicit session =>
  //    val id = (accountAddresses returning accountAddresses.map(_.id)) += addr
  //    addr.copy(id = Some(id))
  //  }
  //
  //  def find(id: Long): Option[AccountAddress] = dbTransaction { implicit session =>
  //    accountAddresses.where(_.id === id).firstOption
  //  }
  //
  //  def delete(id: Long): Try[Unit] = dbTransaction { implicit session =>
  //    val r = accountAddresses.where(_.id === id).delete
  //    if (r == 0) Failure(new AccountAddressDoesNotExistException)
  //    else        Success(())
  //  }
}
