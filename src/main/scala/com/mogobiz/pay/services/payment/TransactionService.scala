/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.services.payment

import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat

import akka.actor.ActorSystem
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.mogobiz.pay.common.{ CartRate, Cart }
import com.mogobiz.pay.config.{ Environment, Settings, DefaultComplete }
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.exceptions.Exceptions.{ InvalidContextException, MogopayException, UnauthorizedException }
import com.mogobiz.pay.handlers.payment.{ Submit, SubmitParams }
import com.mogobiz.pay.handlers.shipping.ShippingPrice
import com.mogobiz.pay.implicits.Implicits
import com.mogobiz.pay.model.Mogopay.{ TransactionRequest, Account, BOTransaction, TransactionStatus }
import com.mogobiz.pay.model.ParamRequest.{ TransactionInit, SelectShippingPriceParam, ListShippingPriceParam }
import com.mogobiz.pay.services.ServicesUtil
import com.mogobiz.session.{ SessionESDirectives, Session }
import com.mogobiz.session.SessionESDirectives._
import com.mogobiz.system.ActorSystemLocator
import com.mogobiz.utils.CustomSslConfiguration
import spray.can.Http
import spray.client.pipelining._
import spray.http._
import spray.routing._

import scala.concurrent.duration._
import scala.concurrent._
import scala.util.{ Failure, Success }

class TransactionService(implicit executionContext: ExecutionContext) extends Directives with DefaultComplete with CustomSslConfiguration {
  implicit val timeout = Timeout(40 seconds)

  //  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
  //  val responseFuture = pipeline {
  //    Get("http://maps.googleapis.com/maps/api/elevation/json?locations=27.988056,86.925278&sensor=false")
  //  }

  implicit val system = ActorSystemLocator()

  // execution context for futures

  val serviceName = "transaction"

  val route = {
    pathPrefix(serviceName) {
      init ~
        selectShipping ~
        verify ~
        submit ~
        submitWithSession ~
        download ~
        initGroupPayment ~
        refund
    }
  }

  lazy val init = path("init") {
    post {
      formFields('merchant_secret, 'transaction_amount.as[Long],
        'return_url ?,
        'group_payment_exp_date.?.as[Option[Long]],
        'group_payment_refund_percentage.?.as[Option[Int]]).as(TransactionInit) { params =>
          session {
            session =>
              {
                import Implicits._
                val cart: Cart = session.sessionData.cart.getOrElse {
                  if (Settings.Mogopay.Anonymous)
                    Cart(count = 0,
                      rate = CartRate(code = "EUR", numericCode = 978, rate = 0.01, fractionDigits = 2),
                      price = 0,
                      endPrice = 0,
                      taxAmount = 0,
                      reduction = 0,
                      finalPrice = 0,
                      cartItems = Array(),
                      coupons = Array(),
                      customs = Map[String, Any]())
                  else
                    throw InvalidContextException("Cart isn't set.")
                }
                handleCall(transactionHandler.init(params, cart),
                  (id: String) => complete(StatusCodes.OK -> Map('transaction_id -> id))
                )
              }
          }
        }
    }
  }

  lazy val selectShipping = path("select-shipping") {
    post {
      formFields('shipmentId, 'rateId).as(SelectShippingPriceParam) {
        params =>
          session {
            session =>
              import Implicits._
              session.sessionData.accountId.map(_.toString) match {
                case None => complete {
                  StatusCodes.Forbidden -> Map('error -> "Not logged in")
                }
                case Some(id) =>
                  handleCall(transactionHandler.selectShippingPrice(session.sessionData, id, params.shipmentId, params.rateId),
                    (shippingPrice: ShippingPrice) => {
                      setSession(session) {
                        complete(StatusCodes.OK -> shippingPrice)
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
          (result: (BOTransaction, Seq[TransactionRequest])) => {
            val transaction = result._1
            val transactions = result._2
            val shipmentError = "SHIPMENT_ERROR".equals(transaction.errorCodeOrigin.getOrElse(""))
            complete(
              StatusCodes.OK -> Map(
                'result -> (if (transaction.status == TransactionStatus.PAYMENT_CONFIRMED && !shipmentError) "success" else "error"),
                'transaction_id -> URLEncoder.encode(transaction.transactionUUID, "UTF-8"),
                'transaction_amount -> URLEncoder.encode(transaction.amount.toString, "UTF-8"),
                'transaction_email -> Option(transaction.email).getOrElse(""),
                'transaction_sequence -> transaction.paymentData.transactionSequence.getOrElse(""),
                'transaction_status -> URLEncoder.encode(transaction.status.toString, "UTF-8"),
                'transaction_start -> URLEncoder.encode(new SimpleDateFormat("yyyyMMddHHmmss").format(transaction.dateCreated), "UTF-8"),
                'transaction_end -> URLEncoder.encode(new SimpleDateFormat("yyyyMMddHHmmss").format(transaction.transactionDate.get), "UTF-8"),
                'transaction_providerid -> URLEncoder.encode(transaction.uuid, "UTF-8"),
                'transaction_type -> URLEncoder.encode(transaction.paymentData.paymentType.toString, "UTF-8"),
                'group_transactions -> transactions,
                'error_shipment -> (if (shipmentError) transaction.errorMessageOrigin.getOrElse("") else "")
              )
            )
          }
        )
      }
    }
  }

  lazy val initGroupPayment = path("init-group-payment") {
    import com.mogobiz.pay.implicits.Implicits._
    get {
      val params = parameters('token, 'transaction_type, 'card_cvv, 'card_month, 'card_year, 'card_type, 'card_number)
      params { (token, transactionType, ccCVV, ccMonth, ccYear, ccType, ccNumber) =>
        session { session =>
          handleCall(transactionHandler.initGroupPayment(token),
            (result: (Account, TransactionRequest, String, String, String)) => {
              ServicesUtil.authenticateSession(session, account = result._1)

              setSession(session) {
                val form = buildFormForInitGroupPayment(
                  account = result._1,
                  transaction = result._2,
                  groupTxUUID = result._3,
                  transactionType = transactionType,
                  successURL = result._4,
                  failureURL = result._5,
                  ccCVV,
                  ccMonth,
                  ccYear,
                  ccType,
                  ccNumber
                )
                respondWithMediaType(MediaTypes.`text/html`) {
                  complete {
                    new HttpResponse(StatusCodes.OK, HttpEntity(form))
                  }
                }
              }
            }
          )
        }
      }
    }
  }

  lazy val refund = path("refund") {
    get {
      val params = parameters('merchant_secret, 'amount.as[Long], 'bo_transaction_uuid, 'locale.?)
      params { (merchantSecret, amount, boTransactionUUID, locale) =>
        handleCall(transactionHandler.refund(merchantSecret, boTransactionUUID, Option(amount), locale),
          (_: Any) => complete(200 -> "")
        )
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
        'card_type.?, 'card_store.?.as[Option[Boolean]], 'payers.?, 'group_tx_uuid.?, 'locale.?).as(SubmitParams) {
          submitParams: SubmitParams =>
            val payersAmountsSum = submitParams.payers.values.sum
            if (!submitParams.payers.isEmpty && payersAmountsSum != submitParams.amount) {
              complete {
                400 -> "The total amount and the payers amounts don't match."
              }
            } else {
              session { session =>
                import Implicits._

                if (submitParams.payers.nonEmpty && !submitParams.payers.keys.toList.contains(session.sessionData.email.get)) {
                  complete(StatusCodes.BadRequest -> "The payers' list doesn't contain the current user.")
                } else {
                  //clientIP { ip =>
                  session.sessionData.ipAddress = Some("192.168.1.1") //Some(ip.toString)
                  session.sessionData.payers = submitParams.payers.toMap[String, Long]
                  session.sessionData.groupTxUUID = submitParams.groupTxUUID

                  setSession(session) {
                    doSubmit(submitParams, session)
                  }
                  //}
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
        'card_month.?, 'card_year.?, 'card_type.?, 'card_store.?.as[Option[Boolean]], 'payers.?, 'group_tx_uuid.?, 'locale.?).as(SubmitParams) {
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
    def isNewSession: Boolean = {
      val sessionTrans = session.sessionData.transactionUuid.getOrElse("__SESSION_UNDEFINED__")
      val incomingTrans = submitParams.transactionUUID
      sessionTrans != incomingTrans
    }

    if (!Settings.Mogopay.Anonymous && submitParams.merchantId != session.sessionData.merchantId.getOrElse("__MERCHANT_UNDEFINED__")) {
      // The customer comes back with the wrong merchant id
      complete {
        StatusCodes.Unauthorized -> "Invalid Merchant id"
      }
    } else {
      // is he authenticated (mogopay payment) or is it his first attempt
      if (!session.sessionData.authenticated && isNewSession) {
        session.clear()
      }
      handleCall(
        transactionHandler.submit(Submit(session.sessionData, submitParams, None, None)),
        (t: (String, String)) => {
          val (serviceName, methodName) = t
          setSession(session) {
            val sessionId = session.id
            val request = Get(s"${Settings.Mogopay.EndPoint}$serviceName/$methodName/$sessionId")
            def cleanSession(session: Session) {
              val authenticated = session.sessionData.authenticated
              val customerId = session.sessionData.accountId
              session.clear()
              session.sessionData.authenticated = authenticated
              session.sessionData.accountId = customerId
            }
            val response = if (Settings.Mogopay.isHTTPS) sslPipeline(request.uri.authority.host).flatMap(_(request)) else {
              val pipeline: Future[SendReceive] = for (
                Http.HostConnectorInfo(connector, _) <- IO(Http) ? Http.HostConnectorSetup(Settings.Mogopay.Host, Settings.Mogopay.Port)
              ) yield sendReceive(connector)
              pipeline.flatMap(_(request))
            }
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

  def buildFormForInitGroupPayment(account: Account, transaction: TransactionRequest, groupTxUUID: String,
    transactionType: String, successURL: String, failureURL: String,
    ccCVV: String, ccMonth: String, ccYear: String, ccType: String, ccNumber: String): String = {
    val submitParams = Map(
      "callback_success" -> successURL,
      "callback_error" -> failureURL,
      "transaction_id" -> transaction.uuid,
      "transaction_amount" -> transaction.amount.toString,
      "merchant_id" -> account.owner.get,
      "transaction_type" -> transactionType,
      "group_tx_uuid" -> groupTxUUID,
      "card_cvv" -> ccCVV,
      "card_month" -> ccMonth,
      "card_year" -> ccYear,
      "card_type" -> ccType,
      "card_number" -> ccNumber
    )

    val form =
      <form id="form" action={ s"${Settings.Mogopay.EndPoint}transaction/submit" } method="POST">
        {
          submitParams.map {
            case (key, value) =>
              <input type="hidden" name={ key } value={ value }/>
          }
        }
      </form>
      <script>document.getElementById('form').submit();</script>

    if (Settings.Env == Environment.DEV) {
      // Just `open /tmp/mogopay-submit-form.html` to start the payment
      java.nio.file.Files.write(java.nio.file.Paths.get("/tmp/mogopay-submit-form.html"),
        form.mkString.getBytes(StandardCharsets.UTF_8))
    }

    form.mkString.replace("\n", "")
  }
}
