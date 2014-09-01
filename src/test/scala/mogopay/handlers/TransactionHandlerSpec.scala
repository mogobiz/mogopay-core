package mogopay.handlers

import mogopay.config.HandlersConfig._
import mogopay.es.{EsClient, Mapping}
import mogopay.model.Mogopay.BOTransaction
import org.specs2.mutable._

class TransactionHandlerSpec extends Specification with Before {
  def before = {
    Mapping.clear
    Mapping.set


    val tx = BOTransaction("123", null, null, None, 0L, null, null, null, null, null, false, null, null, null,
      null, null, null, null, null, null, null)
    EsClient.index(tx, false)
  }

  "searchByCustomer" should {
    "find an existent result" in {
      transactionHandler.searchByCustomer("123") must beSome
    }

    "not find a non-existent result" in {
      transactionHandler.searchByCustomer("0") must beNone
    }
  }
}