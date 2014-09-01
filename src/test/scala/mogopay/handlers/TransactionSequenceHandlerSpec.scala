package mogopay.handlers


import mogopay.config.HandlersConfig._
import mogopay.es.{EsClient, Mapping}
import mogopay.model.Mogopay._
import mogopay.util.GlobalUtil._
import org.specs2.mutable._
import org.specs2.specification.BeforeExample

class TransactionSequenceHandlerSpec extends Specification with BeforeExample {
  sequential

  def before = {
    Mapping.clear
    Mapping.set
  }

  "getNextTransactionId" should {
    "generate a new ID if there is none" in {
      val id = transactionSequenceHandler.nextTransactionId("123123")
      id must_== 1
    }

    "increment the ID" in {
      val vendorId = "789"

      val txReq = TransactionSequence(newUUID, vendorId, 1)
      EsClient.index(txReq)

      val id = transactionSequenceHandler.nextTransactionId(vendorId)
      id must_== 2
    }
  }
}