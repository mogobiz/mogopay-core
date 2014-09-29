package mogopay.services.payment

import java.net.URLEncoder
import java.text.SimpleDateFormat

import akka.actor.{ActorRef, ActorSystem}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import mogopay.actors.TransactionActor._
import mogopay.config.Settings
import mogopay.es.EsClient
import mogopay.handlers.shipping.ShippingPrice
import mogopay.model.Mogopay.{TransactionRequest, BOTransaction, TransactionStatus}
import mogopay.services.DefaultComplete
import mogopay.services.Util._
import mogopay.session.Session
import mogopay.session.SessionESDirectives._
import spray.can.Http
import spray.client.pipelining._
import spray.http._
import spray.routing._
import spray.routing.directives.CookieDirectives._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


class TransactionService(actor: ActorRef)(implicit executionContext: ExecutionContext) extends Directives with DefaultComplete {
  implicit val timeout = Timeout(10 seconds)

  //  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
  //  val responseFuture = pipeline {
  //    Get("http://maps.googleapis.com/maps/api/elevation/json?locations=27.988056,86.925278&sensor=false")
  //  }

  implicit val system = ActorSystem()

  // execution context for futures


  val serviceName = "transaction"

  val route = {
    pathPrefix(serviceName) {
      searchByCustomer ~
        init ~
        listShipping ~
        selectShipping ~
        verify ~
        submit
    }
  }

  lazy val searchByCustomer = path("customer" / JavaUUID) { uuid =>
    import mogopay.config.Implicits._
    get {
      complete {
        (actor ? SearchByCustomer(uuid.toString)).mapTo[Seq[BOTransaction]]
      }
    }
  }

  lazy val init = path("init") {
    import mogopay.config.Implicits._
    get {
      val params = parameters('merchant_secret, 'transaction_amount.as[Long], 'currency_code, 'currency_rate.as[Double], 'extra ?)
      params { (secret, amount, code, rate, extra) =>
        onComplete((actor ? Init(secret, amount, code, rate, extra)).mapTo[Try[String]]) { call =>
          handleComplete(call, (id: String) =>
            complete(StatusCodes.OK -> Map('transaction_id -> id, 'url -> "/pay/transaction/submit"))
          )
        }
      }
    }
  }

  lazy val listShipping = path("list-shipping") {
    import mogopay.config.Implicits._
    get {
      session {
        session =>
          parameters('currency_code, 'transaction_extra) {
            (currencyCode, transactionExtra) =>
              session.sessionData.accountId.map(_.toString) match {
                case None => complete {
                  StatusCodes.Forbidden -> Map('error -> "Not logged in")
                }
                case Some(id) =>
                  onComplete((actor ? GetShippingPrices(currencyCode, transactionExtra, id)).mapTo[Try[Seq[ShippingPrice]]]) { call =>
                    handleComplete(call,
                      (shippinggPrices: Seq[ShippingPrice]) => {
                        session.sessionData.shippingPrices = Option(shippinggPrices.toList)
                        setSession(session) {
                          complete(StatusCodes.OK -> shippinggPrices)
                        }
                      }
                    )
                  }
              }
          }
      }
    }
  }

  lazy val selectShipping = path("select-shipping") {
    import mogopay.config.Implicits._
    get {
      session {
        session =>
          val params = parameters('currency_code, 'transaction_extra, 'provider, 'service, 'rate_type)
          params {
            (currencyCode, transactionExtra, provider, service, rateType) =>
              session.sessionData.accountId.map(_.toString) match {
                case None => complete {
                  StatusCodes.Forbidden -> Map('error -> "Not logged in")
                }
                case Some(id) =>
                  val message = GetShippingPrices(currencyCode, transactionExtra, id)

                  onComplete((actor ? GetShippingPrices(currencyCode, transactionExtra, id)).mapTo[Try[Seq[ShippingPrice]]]) { call =>
                    handleComplete(call,
                      (shippingPrices: Seq[ShippingPrice]) => {
                        val message = GetShippingPrice(shippingPrices, provider, service, rateType)
                        val shippingPrice = Await.result((actor ? message).mapTo[Option[ShippingPrice]], Duration.Inf)
                        setSession(session += ("selectedShippingPrice" -> shippingPrice)) {
                          complete {
                            StatusCodes.OK -> shippingPrice
                          }
                        }
                      }
                    )
                  }
              }
          }
      }
    }
  }

  lazy val verify = path("verify") {
    import mogopay.config.Implicits._
    get {
      val params = parameters('merchant_secret, 'transaction_amount.?.as[Option[Long]], 'transaction_id)
      params {
        (secret, amount, transactionUUID) =>
          onComplete((actor ? Verify(secret, amount, transactionUUID)).mapTo[Try[BOTransaction]]) { call =>
            handleComplete(call,
              (transaction: BOTransaction) => {
                complete(
                  StatusCodes.OK ->
                    Map(
                      'result -> (if (transaction.status == TransactionStatus.PAYMENT_CONFIRMED) "success" else "error"),
                      'transaction_id -> URLEncoder.encode(transaction.transactionUUID, "UTF-8"),
                      'transaction_amount -> URLEncoder.encode(transaction.amount.toString, "UTF-8"),
                      'transaction_email -> Option(transaction.email).getOrElse(""),
                      'transaction_status -> URLEncoder.encode(transaction.status.toString, "UTF-8"),
                      'transaction_start -> URLEncoder.encode(new SimpleDateFormat("yyyyMMddHHmmss").format(transaction.creationDate), "UTF-8"),
                      'transaction_end -> URLEncoder.encode(new SimpleDateFormat("yyyyMMddHHmmss").format(transaction.transactionDate.get), "UTF-8"),
                      'transaction_providerid -> URLEncoder.encode(transaction.uuid, "UTF-8"),
                      'transaction_type -> URLEncoder.encode(transaction.paymentData.paymentType.toString, "UTF-8")
                    ))
              }
            )
          }
      }
    }
  }


  /**
   * 1. External Payment
   * callback_success, callback_error, merchant_id, transaction_id, transaction_type, transaction_amount
   * 2. Custom Payment
   * callback_success, callback_error, merchant_id, transaction_id, transaction_type, transaction_amount,
   * card_type, card_month,card_year,card_cvv
   * 3. Mogpay Payment
   * 3.1 First URL (amount only)
   * callback_success, callback_error, merchant_id, transaction_id, transaction_type, transaction_amount
   * callback_cardinfo, callback_cvv, callback_auth
   * 3.2 second URL (come back from auth screen) - sent here when user was not authenticated
   * callback_success, callback_error, merchant_id, transaction_id, transaction_type, transaction_amount
   * callback_cardinfo, callback_cvv, callback_auth
   * user_email, user_password
   * 3.3 third URL (come back from cvv screen) - sent here once user is authenticated
   * callback_success, callback_error, merchant_id, transaction_id, transaction_type, transaction_amount
   * callback_cardinfo, callback_cvv, callback_auth
   * card_cvv
   */
  lazy val submit = path("submit") {
    post {
      formFields('callback_success.?, 'callback_error.?, 'callback_cardinfo.?, 'callback_auth.?, 'callback_cvv.?, 'transaction_id.?,
        'transaction_amount.?.as[Option[Long]], 'merchant_id.?, 'transaction_type.?,
        'card_cvv.?, 'card_number.?, 'user_email.?, 'user_password.?, 'transaction_desc.?,
        'card_month.?, 'card_year.?, 'card_type.?).as(SubmitParams) {
        submitParams =>
          session {
            session =>
              import mogopay.config.Implicits._
              def isNewSession(): Boolean = {
                val sessionTrans = session.sessionData.transactionUuid.getOrElse("__SESSION_UNDEFINED__")
                val incomingTrans = submitParams.transactionUUID.getOrElse("__INCOMING_UNDEFINED__")
                sessionTrans != incomingTrans
              }
              if (!session.sessionData.authenticated || isNewSession())
                session.clear()

              onComplete((actor ? Submit(session.sessionData, submitParams, None, None)).mapTo[Try[(String, String)]]) { call =>
                handleComplete(call,
                  (t: (String, String)) => {
                    val (serviceName, methodName) = t
                    setSession(session) {
                      val sessionId = session.id
                      val pipeline: Future[SendReceive] =
                        for (
                          Http.HostConnectorInfo(connector, _) <-
                          IO(Http) ? Http.HostConnectorSetup(Settings.ServerListen, Settings.ServerPort)

                        ) yield sendReceive(connector)
                      println(s"request ->${Settings.MogopayEndPoint}$serviceName/$methodName/$sessionId")
                      val request = Get(s"${
                        Settings.MogopayEndPoint
                      }$serviceName/$methodName/$sessionId")
                      def cleanSession(session: Session) {
                        val authenticated = session.sessionData.authenticated
                        val customerId = session.sessionData.accountId
                        session.clear()
                        session.sessionData.authenticated = authenticated
                        session.sessionData.accountId = customerId
                      }
                      val response = pipeline.flatMap(_(request))
                      onComplete(response) {
                        case Failure(t) =>
                          t.printStackTrace()
                          cleanSession(session)
                          setSession(session) {
                            complete(toHTTPResponse(t), t.toString)

                          }
                        case Success(response) =>
                          println("success->" + response.entity.data.asString)
                          complete {
                            response.withEntity(HttpEntity(ContentType(MediaTypes.`text/html`), response.entity.data))
                          }
                      }
                    }
                  }
                )
              }
          }
      }
    }
  }
}
