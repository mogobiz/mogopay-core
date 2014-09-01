package mogopay.services

import java.net.URLEncoder
import java.text.SimpleDateFormat

import akka.actor.{ActorSystem, ActorRef}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import mogopay.actors.TransactionActor._
import mogopay.config.Settings
import mogopay.handlers.shipping.ShippingPrice
import mogopay.model.Mogopay.{TransactionStatus, BOTransaction}
import mogopay.services.Util._
import mogopay.session.Session
import mogopay.session.SessionESDirectives._
import mogopay.util.GlobalUtil._
import spray.can.Http
import spray.can.client.{ClientConnectionSettings, HostConnectorSettings}
import spray.http.HttpHeaders.{`Content-Type`, Cookie}
import spray.http._
import spray.routing.Directives

import scala.concurrent.{Future, Await, ExecutionContext}
import scala.concurrent.duration._
import scala.util.{Success, Failure, Try}
import spray.client.pipelining._


class TransactionService(actor: ActorRef)(implicit executionContext: ExecutionContext) extends Directives {
  implicit val timeout = Timeout(10 seconds)

  //  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
  //  val responseFuture = pipeline {
  //    Get("http://maps.googleapis.com/maps/api/elevation/json?locations=27.988056,86.925278&sensor=false")
  //  }

  implicit val system = ActorSystem()

  import system.dispatcher

  // execution context for futures


  val serviceName = "transaction"

  val route = {
    pathPrefix(serviceName) {
      searchByCustomer ~
        init ~
        path("test") {
          get {
            session { session =>
              setSession(session) {
                complete {
                  "hello"
                }
              }
            }
          }
        } ~
        listShipping ~
        selectShipping ~
        verify ~
        submit
    }
  }

  lazy val searchByCustomer = path("searchByCustomer" / JavaUUID) { uuid =>
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
        val message: Init = Init(secret, amount, code, rate, extra)
        onComplete((actor ? message).mapTo[Try[String]]) { res =>
          res match {
            case Failure(t) => complete(toHTTPResponse(t) -> Map('error -> t.toString))
            case Success(id) => complete(StatusCodes.OK -> Map('transaction_id -> id, 'url -> "/pay/transaction/submit"))

          }
        }
      }
    }
  }

  lazy val listShipping = path("list-shipping") {
    import mogopay.config.Implicits._
    get {
      session { session =>
        parameters('currency_code, 'transaction_extra) { (currencyCode, transactionExtra) =>
          session.sessionData.accountId.map(_.toString) match {
            case None => complete {
              StatusCodes.Forbidden -> Map('error -> "Not logged in")
            }
            case Some(id) =>
              val message = GetShippingPrices(currencyCode, transactionExtra, id)
              val result = (actor ? message).mapTo[Try[Seq[ShippingPrice]]]
              val tryShippingPrices = Await.result(result, Duration.Inf)
              tryShippingPrices match {
                case Success(shippingPrices) =>
                  session.sessionData.shippingPrices = Option(shippingPrices.toList)
                  setSession(session) {
                    complete(StatusCodes.OK -> shippingPrices)
                  }
                case Failure(t) => complete(toHTTPResponse(t) -> Map('error -> t.toString))
              }
          }
        }
      }
    }
  }

  lazy val selectShipping = path("select-shipping") {
    import mogopay.config.Implicits._
    get {
      session { session =>
        val params = parameters('currency_code, 'transaction_extra, 'provider, 'service, 'rate_type)
        params { (currencyCode, transactionExtra, provider, service, rateType) =>
          session.sessionData.accountId.map(_.toString) match {
            case None => complete {
              StatusCodes.Forbidden -> Map('error -> "Not logged in")
            }
            case Some(id) =>
              val message = GetShippingPrices(currencyCode, transactionExtra, id)

              val result: Try[Seq[ShippingPrice]] = session.sessionData.shippingPrices
                .map(_.asInstanceOf[Seq[ShippingPrice]])
                .map(Success(_))
                .getOrElse(Await.result((actor ? message).mapTo[Try[Seq[ShippingPrice]]], Duration.Inf))

              result match {
                case Success(shippingPrices) =>
                  val message = GetShippingPrice(shippingPrices, provider, service, rateType)
                  val shippingPrice = Await.result((actor ? message).mapTo[Option[ShippingPrice]], Duration.Inf)
                  setSession(session += ("selectedShippingPrice" -> shippingPrice)) {
                    complete {
                      StatusCodes.OK -> shippingPrice
                    }
                  }
                case Failure(t) => complete(toHTTPResponse(t) -> Map('error -> t.toString))
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
      params { (secret, amount, transactionUUID) =>
        val message = Verify(secret, amount, transactionUUID)
        val res = (actor ? message).mapTo[Try[BOTransaction]]
        onComplete(res) { transaction =>
          transaction match {
            case Failure(t) => complete(StatusCodes.InternalServerError)
            case Success(transaction) =>
              transaction match {
                case Success(transaction) =>
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
                case Failure(t) => complete(toHTTPResponse(t), t.toString)
              }
          }
        }
      }
    }
  }

  lazy val submit = path("submit") {
    post {
      formFields('callback_success.?, 'callback_error.?, 'url_cardinfo.?, 'transaction_id,
        'transaction_amount.as[Long], 'merchant_id.?, 'transaction_type, 'save_card.?.as[Option[Boolean]],
        'card_cvv.?, 'card_number.?, 'customer_email.?, 'customer_password.?, 'transaction_desc.?,
        'card_month.?, 'card_year.?, 'card_type.?).as(SubmitParams) {
        submitParams =>
          session {
            session =>
              import mogopay.config.Implicits._
              val message = Submit(session, submitParams, None, None)
              val r = (actor ? message).mapTo[Try[(String, String)]]
              onComplete(r) {
                case Failure(e) => complete(StatusCodes.InternalServerError)
                case Success(ta) => {
                  ta match {
                    case Failure(t) =>
                      complete {
                        toHTTPResponse(t) -> Map('error -> t.toString)
                      }
                    case Success((serviceName, methodName)) =>
                      val csrfToken = session.sessionData.csrfToken
                      val sessionId = session.id
                      killSession(session) {
                        val pipeline: Future[SendReceive] =
                          for (
                            Http.HostConnectorInfo(connector, _) <-
                            IO(Http) ? Http.HostConnectorSetup(Settings.ServerListen, Settings.ServerPort)

                          ) yield sendReceive(connector)
                        //                    redirect(s"/pay/$serviceName/$methodName?csrf_token=${session.sessionData.csrfToken}", StatusCodes.TemporaryRedirect)
                        val request = Get(s"/pay/$serviceName/$methodName?csrf_token=${csrfToken}") ~> addHeader(Cookie(HttpCookie(Settings.SessionCookieName, sessionId)))
                        val response = pipeline.flatMap(_(request))
                        onComplete(response) {
                          case Failure(t) => complete(toHTTPResponse(t), t.toString)
                          case Success(response) =>
                            complete {
                              response.withEntity(HttpEntity(ContentType(MediaTypes.`text/html`), response.entity.data))
                            }
                        }
                      }
                  }
                }
              }
          }
      }
    }
  }
}
