/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.sql

import com.mogobiz.pay.config.Settings
import scalikejdbc._

trait BOService {
  def newId()(implicit session: DBSession): Int = {
    val res = session.connection.createStatement().executeQuery(Settings.NextVal)
    res.next()
    res.getInt(1)
  }
}
