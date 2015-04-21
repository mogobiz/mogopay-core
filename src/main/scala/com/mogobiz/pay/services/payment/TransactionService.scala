package com.mogobiz.pay.services.payment

import java.io.File
import java.net.URLEncoder
import java.text.SimpleDateFormat

import akka.actor.ActorSystem
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.mogobiz.pay.config.{Settings, DefaultComplete}
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.exceptions.Exceptions.{MogopayException, UnauthorizedException}
import com.mogobiz.pay.handlers.payment.{Submit, SubmitParams}
import com.mogobiz.pay.handlers.shipping.ShippingPrice
import com.mogobiz.pay.implicits.Implicits
import com.mogobiz.pay.model.Mogopay.{Account, BOTransaction, TransactionStatus}
import com.mogobiz.pay.model.ParamRequest.{TransactionInit, SelectShippingPriceParam, ListShippingPriceParam}
import com.mogobiz.pay.services.ServicesUtil
import com.mogobiz.session.{SessionESDirectives, Session}
import com.mogobiz.session.SessionESDirectives._
import spray.can.Http
import spray.client.pipelining._
import spray.http._
import spray.routing._
import spray.routing.directives.RouteDirectives

import scala.concurrent.duration._
import scala.concurrent._
import scala.util.{Failure, Success}


class TransactionService(implicit executionContext: ExecutionContext) extends Directives with DefaultComplete {
  implicit val timeout = Timeout(40 seconds)

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
        submit ~
        submitWithSession ~
        download ~
        initGroupPayment
    }
  }

  lazy val searchByCustomer = path("customer" / JavaUUID) { uuid =>
    import Implicits._
    get {
      handleCall(transactionHandler.searchByCustomer(uuid.toString),
        (res: Seq[BOTransaction]) => complete(res))
    }
  }

  lazy val init = path("init") {
    post {
      formFields('merchant_secret, 'transaction_amount.as[Long],
        'currency_code, 'currency_rate.as[Double],
        'extra ?, 'return_url ?).as(TransactionInit) { params =>
        import Implicits._
          handleCall(transactionHandler.init(params),
            (id: String) => complete(StatusCodes.OK -> Map('transaction_id -> id))
          )
      }
    }
  }

  lazy val listShipping = path("list-shipping") {
    post {
      formFields('currency_code, 'transaction_extra).as(ListShippingPriceParam) {
        params =>
          session {
            session =>
              import Implicits._
              session.sessionData.accountId.map(_.toString) match {
                case None => complete {
                  StatusCodes.Forbidden -> Map('error -> "Not logged in")
                }
                case Some(id) =>
                  handleCall(transactionHandler.shippingPrices(params.currency_code, params.transaction_extra, id),
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

  lazy val selectShipping = path("select-shipping") {
    post {
      formFields('currency_code, 'transaction_extra, 'provider, 'service, 'rate_type).as(SelectShippingPriceParam) {
        params =>
          session {
            session =>
              import Implicits._
              session.sessionData.accountId.map(_.toString) match {
                case None => complete {
                  StatusCodes.Forbidden -> Map('error -> "Not logged in")
                }
                case Some(id) =>
                  handleCall(transactionHandler.shippingPrices(params.currency_code, params.transaction_extra, id),
                    (shippingPrices: Seq[ShippingPrice]) => {
                      val shippingPrice = transactionHandler.shippingPrice(shippingPrices, params.provider, params.service, params.rate_type)
                      session.sessionData.selectShippingPrice = shippingPrice
                      setSession(session) {
                        complete {
                          StatusCodes.OK -> shippingPrice.get
                        }
                      }
                    }
                  )
              }
          }
      }
    }
  }

  lazy val verify = path("verify") {
    import Implicits._
    get {
      val params = parameters('merchant_secret, 'transaction_amount.?.as[Option[Long]], 'transaction_id)
      params { (secret, amount, transactionUUID) =>
        handleCall(transactionHandler.verify(secret, amount, transactionUUID),
          (result: (BOTransaction, Seq[BOTransaction])) => {
            val transaction = result._1
            val transactions = result._2
            complete(
              StatusCodes.OK -> Map(
                'result -> (if (transaction.status == TransactionStatus.PAYMENT_CONFIRMED) "success" else "error"),
                'transaction_id -> URLEncoder.encode(transaction.transactionUUID, "UTF-8"),
                'transaction_amount -> URLEncoder.encode(transaction.amount.toString, "UTF-8"),
                'transaction_email -> Option(transaction.email).getOrElse(""),
                'transaction_sequence -> transaction.paymentData.transactionSequence.getOrElse(""),
                'transaction_status -> URLEncoder.encode(transaction.status.toString, "UTF-8"),
                'transaction_start -> URLEncoder.encode(new SimpleDateFormat("yyyyMMddHHmmss").format(transaction.creationDate), "UTF-8"),
                'transaction_end -> URLEncoder.encode(new SimpleDateFormat("yyyyMMddHHmmss").format(transaction.transactionDate.get), "UTF-8"),
                'transaction_providerid -> URLEncoder.encode(transaction.uuid, "UTF-8"),
                'transaction_type -> URLEncoder.encode(transaction.paymentData.paymentType.toString, "UTF-8"),
                'group_transactions -> transactions
              )
            )
          }
        )
      }
    }
  }

  lazy val initGroupPayment = path("init-group-payment") {
    import com.mogobiz.pay.implicits.Implicits._

    def initCall(account: Account, transaction: BOTransaction,
                 transactionType: String, next: (Account, BOTransaction, String) => Route): Route = {
      val initParams = Map(
        "merchant_secret" -> transaction.vendor.get.secret,
        "transaction_amount" -> transaction.amount.toString,
        "currency_code" -> transaction.currency.code,
        "currency_rate" -> transaction.currency.rate.toString // todo: à quoi sert currency rate?
      )
      val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

      try {
        val uri = Uri(s"${Settings.Mogopay.EndPoint}transaction/init")
        val post: HttpRequest = Post(uri, initParams.mkString("&"))
        val result: HttpResponse = Await.result(pipeline(post), Duration.Inf)
        next(account, transaction, transactionType)
      } catch {
        case e: TimeoutException => complete(500 -> "An operation timed out.")
        case _: Throwable        => complete(500 -> "")
      }
    }

    def redirectToSubmit(account: Account, transaction: BOTransaction, transactionType: String) = {
      val submitParams = Map(
        "callback_success"   -> "",
        "callback_error"     -> "",
        "transaction_id"     -> transaction.uuid,
        "transaction_amount" -> transaction.amount.toString,
        "merchant_id"        -> account.owner.get,
        "transaction_type"   -> transactionType,
        "payers"             -> (account.email + ":" + transaction.amount.toString)
      )

      val form =
        <form id="form" action="/pay/transaction/submit" method="POST">
          {submitParams.map { case (key, value) =>
            <input type="hidden" name={key} value={value}/>
        }}
        </form>
          <script>document.getElementById('form').submit();</script>

      respondWithMediaType(MediaTypes.`text/html`) {
        complete {
          new HttpResponse(StatusCodes.OK, HttpEntity(form.mkString.replace("\n", "")))
        }
      }
    }

    get {
      val params = parameters('token, 'transaction_type)
      params { (token, transactionType) =>
        session { session =>
          handleCall(transactionHandler.initGroupPayment(token),
            (result: (Account, BOTransaction)) => {
              ServicesUtil.authenticateSession(session, account = result._1)

              setSession(session) {
                initCall(result._1, result._2, transactionType, redirectToSubmit)
              }
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
      formFields('callback_success, 'callback_error, 'callback_cardinfo.?, 'callback_auth.?, 'callback_cvv.?,
        'transaction_id, 'transaction_amount.as[Long], 'merchant_id, 'transaction_type, 'card_cvv.?, 'card_number.?,
        'user_email.?, 'user_password.?, 'transaction_desc.?, 'gateway_data.?, 'card_month.?, 'card_year.?,
        'card_type.?, 'card_store.?.as[Option[Boolean]], 'payers.?).as(SubmitParams) {
        submitParams: SubmitParams =>
          val payersAmountsSum = submitParams.payers.values.sum
          if (payersAmountsSum != submitParams.amount) {
            complete { 400 -> "The total amount and the payers amounts don't match." }
          } else {
            session { session =>
              import Implicits._

              session.sessionData.payers = submitParams.payers.toMap[String, Long]

              setSession(session) {
                doSubmit(submitParams, session)
              }
            }
          }
      }
    }
  }

  lazy val submitWithSession = path("submit" / Segment) { sessionUuid =>
    post {
      formFields('callback_success, 'callback_error, 'callback_cardinfo.?, 'callback_auth.?, 'callback_cvv.?, 'transaction_id,
        'transaction_amount.as[Long], 'merchant_id, 'transaction_type,
        'card_cvv.?, 'card_number.?, 'user_email.?, 'user_password.?, 'transaction_desc.?, 'gateway_data.?,
        'card_month.?, 'card_year.?, 'card_type.?, 'card_store.?.as[Option[Boolean]], 'payers.?).as(SubmitParams) {
        submitParams =>
          val session = SessionESDirectives.load(sessionUuid).get
          doSubmit(submitParams, session)
      }
    }
  }

  lazy val download = path("download" / Segment) { transactionUuid =>
    get {
      parameters('page ? "A4", 'langCountry) { (pageFormat, langCountry) =>
        session { session =>
          import Implicits._
          session.sessionData.accountId match {
            case Some(accountId: String) =>
              handleCall(transactionHandler.download(accountId, transactionUuid, pageFormat, langCountry), (pdfFile: File) => {
                getFromFile(pdfFile)
              })
            case _ => completeException(new UnauthorizedException("Not logged in"))
          }
        }
      }
    }
  }

  private def doSubmit(submitParams: SubmitParams, session: Session): Route = {
    import Implicits._
    def isNewSession(): Boolean = {
      val sessionTrans = session.sessionData.transactionUuid.getOrElse("__SESSION_UNDEFINED__")
      val incomingTrans = submitParams.transactionUUID
      sessionTrans != incomingTrans
    }

    if (submitParams.merchantId != session.sessionData.merchantId.getOrElse("__MERCHANT_UNDEFINED__")) {
      complete {
        StatusCodes.Unauthorized -> "Invalid Merchant id"
      }
    }
    else {
      if (!session.sessionData.authenticated && isNewSession()) {
        session.clear()
      }
      handleCall(
        transactionHandler.submit(Submit(session.sessionData, submitParams, None, None)),
        (t: (String, String)) => {
          val (serviceName, methodName) = t
          setSession(session) {
            val sessionId = session.id
            val pipeline: Future[SendReceive] =
              for (
                Http.HostConnectorInfo(connector, _) <-
                IO(Http) ? Http.HostConnectorSetup(Settings.Mogopay.Host, Settings.Mogopay.Port)

              ) yield sendReceive(connector)
            val request = Get(s"${Settings.Mogopay.EndPoint}$serviceName/$methodName/$sessionId")
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
                  def toHTTPResponse(t: Throwable): StatusCode = t match {
                    case e: MogopayException => e.code
                    case _ => StatusCodes.InternalServerError
                  }
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
