package com.mogobiz.pay.model

import com.mogobiz.pay.model.Mogopay.Account

/**
 * Created by yoannbaudy on 21/06/16.
 */
case class AccountWithChanges(account: Account, changes: List[AccountChange] = Nil)

case class AccountChange(newAccount: Option[Account] = None, updateAccount: Option[Account] = None)
