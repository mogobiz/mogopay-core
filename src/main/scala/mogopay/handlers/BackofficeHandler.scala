package mogopay.handlers

import java.text.SimpleDateFormat
import java.util.Date

import com.sksamuel.elastic4s.ElasticDsl._
import mogopay.config.Settings
import mogopay.es.EsClient
import mogopay.model.Mogopay._
import org.elasticsearch.search.sort.SortOrder._

class BackofficeHandler {
  def listCustomers(merchantId: String, page: Int, max: Int): Seq[Account] = {
    val req = search in Settings.ElasticSearch.Index -> "Account" filter {
      termFilter("owner", merchantId)
    } start page * max limit max
    EsClient.searchAll[Account](req)
  }

  def listTransactionLogs(transactionId: String): Seq[BOTransactionLog] = {
    val req = search in Settings.ElasticSearch.Index -> "BOTransactionLog" filter {
      termFilter("transaction", transactionId)
    } sort {
      by field "uuid" order ASC
    } start 0 limit Settings.MaxQueryResults
    EsClient.searchAll[BOTransactionLog](req)
  }

  /**
   * @param term Either a Left(merchantId) if the user's a vendor or Right(email) if the user's a customer
   */
  def listTransactions(term: Either[String, String], startDate: Option[Long], endDate: Option[Long],
                       amount: Option[Int], transactionUuid: Option[String]): Seq[BOTransaction] = {
    def timestampToDate(timestamp: Long) =
      new SimpleDateFormat(Settings.ElasticSearch.DateFormat).format(new Date(timestamp))

    val filters = List(term.fold(termFilter("vendor.uuid", _), termFilter("email", _))) ++
      transactionUuid.map(uuid => termFilter("transactionUuid", uuid)) ++
      amount.map(x => termFilter("amount", x))

    val req = search in Settings.ElasticSearch.Index -> "BOTransaction" filter {
      and(filters: _*)
    } query {
      range("creationDate") from startDate.map(timestampToDate).orNull to endDate.map(timestampToDate).orNull
    } sort {
      by field "dateCreated" order DESC
    } start 0 limit Settings.MaxQueryResults

    EsClient.searchAll[BOTransaction](req)
  }

  def getTransaction(uuid: String): Option[BOTransaction] =
    EsClient.search[BOTransaction](search in Settings.ElasticSearch.Index -> "BOTransaction" filter {
      termFilter("uuid", uuid)
    })
}
