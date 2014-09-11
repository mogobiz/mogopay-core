package mogopay.services.payment

import mogopay.model.Mogopay.{Account, CreditCard}

trait WalletService {
  def getCards(walletId:String) : List[CreditCard]
  def setCards(walletId:String, cards: List[CreditCard]) : Unit
  def createWallet(account : Account) : Account
}
