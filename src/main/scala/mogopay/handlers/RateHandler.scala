package mogopay.handlers

import java.text.NumberFormat
import java.util.{Currency, Locale}

import com.sksamuel.elastic4s.ElasticDsl._
import mogopay.config.Settings
import mogopay.es.EsClient
import mogopay.model.Mogopay.Rate

class RateHandler {
  def list = EsClient.searchAll[Rate](search in Settings.DB.INDEX -> "Rate")

  def format(amount: Long, currency: String, country: String): Option[String] = {
    findByCurrencyCode(currency) map { rate =>
      val locale = new Locale(country)

      val numberFormat = NumberFormat.getCurrencyInstance(locale)
      numberFormat.setCurrency(Currency.getInstance(currency))

      numberFormat.format(amount * rate.currencyRate)
    }
  }

  def findByCurrencyCode(currency: String): Option[Rate] = {
    import java.util.Calendar
    import org.elasticsearch.search.sort.SortOrder._

    val req = search in Settings.DB.INDEX -> "Rate" filter {
      termFilter("currencyCode", currency)
    } query {
      range("activationDate") from 0 to Calendar.getInstance.getTime.getTime
    } sort {
      by.field("activationDate").order(DESC)
    }

    EsClient.search[Rate](req)
  }

  def convert(amount: Long, originCurrency: String, destinationCurrency: String): Option[Long] = {
    if (originCurrency == destinationCurrency) {
      Option(amount)
    } else {
      (findByCurrencyCode(originCurrency), findByCurrencyCode(destinationCurrency)) match {
        case (Some(src), Some(dst)) => Option(amount /
          (src.currencyRate * Math.pow(10, src.currencyFractionDigits.toDouble)) *
          (dst.currencyRate * Math.pow(10, dst.currencyFractionDigits.toDouble))) map (_.toLong)
        case _ => None
      }
    }
  }
}
