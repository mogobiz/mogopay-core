package mogopay.handlers

import java.text.SimpleDateFormat
import java.util.Date

import com.sksamuel.elastic4s.ElasticDsl._
import mogopay.config.Settings
import mogopay.es.EsClient
import mogopay.exceptions.Exceptions.InvalidContextException
import mogopay.model.Mogopay._
import org.elasticsearch.search.sort.SortOrder._

class BackofficeHandler {
  def listCustomers(sessionData: SessionData, page: Int, max: Int): Seq[Account] = {
    if (!sessionData.isMerchant)
      throw InvalidContextException("User not a merchant")
    val merchantId = sessionData.accountId.getOrElse(throw InvalidContextException("No logged merchant found"))
    val req = search in Settings.ElasticSearch.Index -> "Account" filter {
      termFilter("owner", merchantId)
    } start page * max limit max
    EsClient.searchAll[Account](req)
  }

  def listTransactionLogs(transactionId: String): Seq[BOTransactionLog] = {
    val req = search in Settings.ElasticSearch.Index -> "BOTransactionLog" filter {
      termFilter("transaction", transactionId)
    } sort {
      by field "dateCreated" order DESC
    } start 0 limit Settings.MaxQueryResults
    EsClient.searchAll[BOTransactionLog](req)
  }

  /**
   * @param term Either a Left(merchantId) if the user's a vendor or Right(email) if the user's a customer
   */
  def listTransactions(sessionData: SessionData, startDate: Option[Long], endDate: Option[Long],
                       amount: Option[Int], transactionUuid: Option[String]): Seq[BOTransaction] = {
    def timestampToDate(timestamp: Long) = new SimpleDateFormat(Settings.ElasticSearch.DateFormat).format(new Date(timestamp))

    val accountFilter =
      if (sessionData.isMerchant) {
        val accountId = sessionData.accountId.getOrElse(throw InvalidContextException("No logged user found"))
        termFilter("vendor.uuid", accountId)
      }
      else {
        val accountEmail = sessionData.email.getOrElse(throw InvalidContextException("No logged user found"))
        termFilter("email", accountEmail)
      }

    val filters = List(accountFilter) ++
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
