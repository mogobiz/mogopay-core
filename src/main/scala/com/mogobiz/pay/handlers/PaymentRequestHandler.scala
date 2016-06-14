/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import com.mogobiz.es.EsClient
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.model.Mogopay._

class PaymentRequestHandler {
  def find(uuid: String): Option[PaymentRequest] = {
    EsClient.load[PaymentRequest](Settings.Mogopay.EsIndex, uuid)
  }

  def save(pr: PaymentRequest, refresh: Boolean = false) = {
    EsClient.index[PaymentRequest](Settings.Mogopay.EsIndex, pr, refresh = refresh)
  }
}
