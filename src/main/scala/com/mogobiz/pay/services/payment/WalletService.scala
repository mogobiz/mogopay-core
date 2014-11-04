package com.mogobiz.pay.services.payment

import com.mogobiz.pay.model.Mogopay.{Account, CreditCard}

trait WalletService {
  def getCards(walletId:String) : List[CreditCard]
  def setCards(walletId:String, cards: List[CreditCard]) : Unit
  def createWallet(account : Account) : Account
}
