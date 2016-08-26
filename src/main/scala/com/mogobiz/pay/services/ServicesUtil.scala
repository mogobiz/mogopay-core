/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.services

import com.mogobiz.pay.model.Account
import com.mogobiz.session.Session

object ServicesUtil {
  def authenticateSession(session: Session, account: Account) = {
    import com.mogobiz.pay.implicits.Implicits._
    session.sessionData.email = Some(account.email)
    session.sessionData.accountId = Some(account.uuid)
    session.sessionData.merchantId = account.owner
    session.sessionData.isMerchant = account.owner.isEmpty
    session.sessionData.authenticated = true
  }
}
