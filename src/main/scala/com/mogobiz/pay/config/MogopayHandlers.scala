/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.config

import com.ebiznext.mogopay.payment.SipsHandler
import com.mogobiz.pay.handlers._
import com.mogobiz.pay.handlers.payment._
import com.mogobiz.pay.handlers.shipping._

object MogopayHandlers {
  val handlers: MogopayCake =
    if (Settings.CakeClass != null && Settings.CakeClass.trim.length > 0)
      Class.forName(Settings.CakeClass.trim).asInstanceOf[MogopayCake]
    else
      new DefaultMogopayCake()
}
