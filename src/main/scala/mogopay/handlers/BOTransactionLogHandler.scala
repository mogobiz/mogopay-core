package mogopay.handlers

import mogopay.es.EsClient
import mogopay.model.Mogopay._

class BOTransactionLogHandler {
  def save(transactionLog: BOTransactionLog) = EsClient.index(transactionLog, false)
}
