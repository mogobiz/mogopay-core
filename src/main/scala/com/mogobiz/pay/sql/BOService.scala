package com.mogobiz.pay.sql

import com.mogobiz.pay.settings.Settings
import scalikejdbc._

trait BOService {
  def newId()(implicit session: DBSession): Int = {
    val res = session.connection.createStatement().executeQuery(Settings.NextVal)
    res.next()
    res.getInt(1)
  }
}
