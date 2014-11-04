package com.mogobiz.pay

import com.mogobiz.pay.actors.BootedMogopaySystem
import com.mogobiz.pay.config.MogopayActors

/**
 * Created by hayssams on 04/11/14.
 */
object Cli extends App with MogopayActors with BootedMogopaySystem
