package mogopay.handlers.payment

import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.ActorSystem
import mogopay.config.HandlersConfig._
import mogopay.config.{Environment, Settings}
import mogopay.es.EsClient
import mogopay.handlers.UtilHandler
import mogopay.model.Mogopay._
import mogopay.util.{Sha512, GlobalUtil}
import org.joda.time.format.ISODateTimeFormat
import org.json4s.jackson.JsonMethods._
import mogopay.util.GlobalUtil._
import spray.http.{HttpResponse, Uri}
import spray.http.Uri.Query

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import spray.http._
import spray.client.pipelining._

import scala.util._

class PayboxHandler extends PaymentHandler {
  def done(data: SessionData, stringToString: Map[String, String]): Any = ???

  def callbackPayment(stringToString: Map[String, String]): Any = ???

  def done3DSecureCheck(data: SessionData, stringToString: Map[String, String]): Any = ???

  def callback3DSecureCheck(sessionData: SessionData, params: Map[String, String]): Try[Unit] = {
    val statusPBX = params("StatusPBX")
    val vendorAndUuid = params("IdSession")
    val vendorAndUuidArray = vendorAndUuid.split("--")
    val vendorId = vendorAndUuidArray(0)
    val transactionUuid = vendorAndUuidArray(1)
    val transaction = EsClient.load[BOTransaction](transactionUuid).get
    val id3d = params("ID3D")

    if (params("3DSTATUS") == "Y" && params("3DSIGNVAL") == "Y" && params("3DENROLLED") == "Y" && params("3DERROR") == "0") {
      transaction.paymentData.copy(status3DS = Some(ResponseCode3DS.APPROVED))
    } else {
      transaction.paymentData.copy(status3DS = Some(ResponseCode3DS.REFUSED))
    }
    EsClient.index(transaction, false)
    Success()
  }

  implicit val system = ActorSystem()

  import system.dispatcher

  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
  implicit val formats = new org.json4s.DefaultFormats {
  }

  def startPayment(sessionData: SessionData): Try[Either[String, Uri]] = {
    val transactionUUID = sessionData.transactionUuid.get
    val vendorId = sessionData.vendorId.get
    val paymentConfig = sessionData.paymentConfig.get
    val paymentRequest = sessionData.paymentRequest.get
    val id3d = sessionData.id3d.getOrElse(null)
    val parametres = paymentConfig.cbParam.map(parse(_).extract[Map[String, String]]).getOrElse(Map())
    val transaction = EsClient.load[BOTransaction](transactionUUID).get
    val vendor = EsClient.load[Account](vendorId).get

    val context = if (Settings.environment == Environment.DEV) "TEST" else "PRODUCTION"
    val ctxMode = context
    transactionHandler.updateStatus(vendorId, transactionUUID, null, TransactionStatus.PAYMENT_REQUESTED, null)
    var paymentResult: PaymentResult = PaymentResult(
      id = paymentRequest.id,
      orderDate = paymentRequest.orderDate,
      amount = paymentRequest.amount,
      ccNumber = "",
      cardType = null,
      expirationDate = null,
      cvv = "",
      transactionId = "",
      transactionDate = null,
      transactionCertificate = null,
      authorizationId = null,
      status = null,
      errorCodeOrigin = "",
      errorMessageOrigin = Some(""),
      data = "",
      bankErrorCode = "",
      bankErrorMessage = Some(""),
      token = ""
    )
    val currency: Int = paymentRequest.currency.numericCode
    val site: String = parametres("payboxSite")
    val key: String = parametres("payboxKey")
    val rank: String = parametres("payboxRank")
    val idMerchant: String = parametres("payboxMerchantId")
    val transDate: String = new SimpleDateFormat("yyyyMMddHHmmss").format(paymentRequest.orderDate)
    //      val xx: String = URLEncoder.encode(Settings.MogopayEndPoint + "systempay/done/" + paymentRequest.csrfToken, "UTF-8")


    val action = Settings.PayboxMPIEndPoint
    val IdMerchant = idMerchant
    val IdSession = vendorId + "--" + transactionUUID
    val Amount = String.format("%010d", paymentRequest.amount.asInstanceOf[AnyRef])
    val XCurrency = currency.toString
    val CCNumber = paymentRequest.ccNumber
    val CCExpDate = new SimpleDateFormat("MMyy").format(paymentRequest.expirationDate)
    val CVVCode = paymentRequest.cvv
    val URLRetour = Settings.MogopayEndPoint + "paybox/done3DSecureCheck"
    val URLHttpDirect = Settings.MogopayEndPoint + "paybox/callback3DSecureCheck"
    if (parametres("payboxContract") == "PAYBOX_DIRECT" || parametres("payboxContract") == "PAYBOX_DIRECT_PLUS") {
      if (id3d == null && (paymentConfig.paymentMethod == CBPaymentMethod.THREEDS_REQUIRED || paymentConfig.paymentMethod == CBPaymentMethod.THREEDS_IF_AVAILABLE)) {
        val query = Map(
          "IdMerchant=" -> idMerchant,
          "IdSession" -> IdSession,
          "Amount" -> Amount,
          "Currency" -> XCurrency,
          "CCNumber" -> CCNumber,
          "CCExpDate" -> CCExpDate,
          "CVVCode" -> CVVCode,
          "URLRetour" -> URLRetour,
          "URLHttpDirect" -> URLHttpDirect)
        val form =
          s"""
              <html>
                  <head>
                  </head>
                  <body>
                <FORM id="formpaybox" ACTION = "${action}" METHOD="POST">
                  <INPUT TYPE="hidden" NAME ="IdMerchant" VALUE = "${IdMerchant}"><br>
                  <INPUT TYPE="hidden" NAME ="IdSession" VALUE = "${IdSession}"><br>
                  <INPUT TYPE="hidden" NAME ="Amount" VALUE = "${Amount}"><br>
                  <INPUT TYPE="hidden" NAME ="Currency" VALUE = "${XCurrency}"><br>
                  <INPUT TYPE="hidden" NAME ="CCNumber" VALUE = "${CCNumber}"><br>
                  <INPUT TYPE="hidden" NAME ="CCExpDate" VALUE = "${CCExpDate}"><br>
                  <INPUT TYPE="hidden" NAME ="CVVCode" VALUE = "${CVVCode}"><br>
                  <INPUT TYPE="hidden" NAME ="URLRetour" VALUE = "${URLRetour}"><br>
                  <INPUT TYPE="hidden" NAME ="URLHttpDirect" VALUE = "${URLHttpDirect}"><br>
                </FORM>
                    <script>document.getElementById("formpaybox").submit();</script>
                  </body>
              </html>
              """
        Success(Left(form))
      }
      else {
        val query = scala.collection.mutable.Map(
          "NUMQUESTION" -> "idTransaction",
          "REFERENCE=" -> IdSession,
          "DATEQ" -> new SimpleDateFormat("ddMMyyyyhhmmss").format(new Date()),
          "DEVISE" -> currency.toString,
          "MONTANT" -> Amount,
          "TYPE" -> "00003",
          "SITE" -> site,
          "RANG" -> rank,
          "CLE" -> key,
          "ACTIVITE" -> "024",
          "PORTEUR" -> CCNumber,
          "DATEVAL" -> CCExpDate,
          "CVV" -> CVVCode,
          "ARCHIVAGE" -> "",
          "DIFFERE" -> "000",
          "NUMAPPEL" -> "",
          "NUMTRANS" -> "",
          "AUTORISATION" -> "",
          "PAYS=" -> "",
          "VERSION" -> (if (parametres("payboxContract") == "PAYBOX_DIRECT") "00103" else "00104"))


        if (paymentConfig.paymentMethod == CBPaymentMethod.THREEDS_REQUIRED)
          query += "ID3D" -> id3d
        else if (id3d != "" && paymentConfig.paymentMethod == CBPaymentMethod.THREEDS_IF_AVAILABLE)
          query += "ID3D" -> id3d

        val botlog = new BOTransactionLog(uuid = newUUID, provider = "PAYBOX", direction = "OUT", transaction = transactionUUID, log = GlobalUtil.mapToQueryString(query.toMap))
        EsClient.index(botlog, false)
        val response: Future[HttpResponse] = pipeline(Post(Uri(Settings.PayboxWebEndPoint).withQuery(Query(query.toSeq: _*))))
        val responseHttp = response
        val tuples = Await.result(GlobalUtil.fromHttResponse(response), Duration.Inf)

        val bolog = new BOTransactionLog(uuid = newUUID, provider = "PAYBOX", direction = "IN", transaction = transactionUUID, log = GlobalUtil.mapToQueryString(tuples))
        EsClient.index(bolog, false)
        val errorCode = tuples.getOrElse("CODEREPONSE", "")
        val errorMessage = tuples.get("COMMENTAIRE")
        paymentResult = paymentResult.copy(
          ccNumber = UtilHandler.hideCardNumber(paymentResult.ccNumber, "X"),
          status = PaymentStatus.FAILED,
          errorCodeOrigin = errorCode,
          errorMessageOrigin = errorMessage,
          bankErrorCode = errorCode,
          bankErrorMessage = errorMessage)
        if (errorCode.equals("00000")) {
          val transaction = tuples("NUMQUESTION")
          val authorisation = tuples("AUTORISATION")
          paymentResult = paymentResult.copy(
            status = PaymentStatus.COMPLETE,
            transactionDate = new Date(),
            transactionId = transaction,
            authorizationId = authorisation,
            transactionCertificate = null)
        }
        Success(Right(finishPayment(sessionData, paymentResult)))
      }
    }
    else if (parametres("payboxContract") == "PAYBOX_SYSTEM") {
      val hmackey = idMerchant
      val pbxtime = ISODateTimeFormat.dateTimeNoMillis().print(org.joda.time.DateTime.now())
      val query = scala.collection.mutable.Map(
        "PBX_SITE" -> site,
        "PBX_RANG" -> rank,
        "PBX_IDENTIFIANT" -> key,
        "PBX_TOTAL" -> String.format("%010d", paymentRequest.amount),
        "PBX_DEVISE" -> paymentRequest.currency.numericCode,
        "PBX_CMD" -> s"${
          vendorId
        }--${
          transactionUUID
        }",
        "PBX_PORTEUR" -> transaction.email.getOrElse(vendor.email),
        "PBX_RETOUR" -> "AMOUNT:M;REFERENCE:R;AUTO:A;NUMTRANS:T;TYPEPAIE:P;CARTE:C;CARTEDEBUT:N;STATUSTHREEDS:F;THREEDS:G;CARTEFIN:J;DATEFIN:D;CODEREPONSE:E;EMPREINTE:H;SIGNATURE:K",
        "PBX_HASH" -> "SHA512",
        "PBX_TIME" -> pbxtime,
        "PBX_EFFECTUE" -> s"${
          Settings.MogopayEndPoint
        }paybox/done",
        "PBX_REFUSE" -> s"${
          Settings.MogopayEndPoint
        }paybox/done",
        "PBX_ANNULE" -> s"${
          Settings.MogopayEndPoint
        }paybox/done",
        "PBX_REPONDRE_A" -> s"${
          Settings.MogopayEndPoint
        }paybox/callback")
      val queryString = mapToQueryString(query.toMap)
      val hmac = Sha512.hmacDigest(queryString, hmackey)
      val botlog = BOTransactionLog(uuid = newUUID, provider = "PAYBOX", direction = "OUT", transaction = transactionUUID, log = queryString)
      EsClient.index(botlog, false)
      val action = Settings.PayboxSystemEndPoint
      val amountString = String.format("%010d", paymentRequest.amount)


      //NUMTRANS=0003149638&NUMAPPEL=0005000280&NUMQUESTION=0000645802&SITE=5983130&RANG=01&AUTO=XXXXXX&CODEREPONSE=00000&COMMENTAIRE=Demande traitee avec succes&REFABONNE=&PORTEUR=&PAYS=???
      //NUMTRANS=0003149638&NUMAPPEL=0005000280&NUMQUESTION=0000645802&SITE=5983130&RANG=01&AUTO=XXXXXX&CODEREPONSE=00000&COMMENTAIRE=Demande traitee avec succes&REFABONNE=&PORTEUR=&PAYS=???
      val form =
        s"""
<html>
    <head>
    </head>
    <body>
    	Veuillez patienter...
	<FORM id="formpaybox" ACTION = "${action}" METHOD="POST">
	  <INPUT TYPE="hidden" NAME ="PBX_SITE" VALUE = "${site}"><br>
		<INPUT TYPE="hidden" NAME ="PBX_RANG" VALUE = "${rank}"><br>
		<INPUT TYPE="hidden" NAME ="PBX_IDENTIFIANT" VALUE = "${key}"><br>
		<INPUT TYPE="hidden" NAME ="PBX_TOTAL" VALUE = "${amountString}"><br>
		<INPUT TYPE="hidden" NAME ="PBX_DEVISE" VALUE = "${paymentRequest.currency.numericCode}"><br>
		<INPUT TYPE="hidden" NAME ="PBX_CMD" VALUE = "${vendorId + "--" + transactionUUID}"><br>
		<INPUT TYPE="hidden" NAME ="PBX_PORTEUR" VALUE = "${transaction.email.getOrElse(vendor.email)}"><br>
		<INPUT TYPE="hidden" NAME ="PBX_RETOUR" VALUE = "AMOUNT:M;REFERENCE:R;AUTO:A;NUMTRANS:T;TYPEPAIE:P;CARTE:C;CARTEDEBUT:N;THREEDS:G;CARTEFIN:J;DATEFIN:D;DATE_PAYBOX:W;HEURE_PAYBOX:Q;CODEREPONSE:E;EMPREINTE:H;SIGNATURE:K"><br>
		<INPUT TYPE="hidden" NAME ="PBX_HASH" VALUE = "SHA512"><br>
		<INPUT TYPE="hidden" NAME ="PBX_TIME" VALUE = "${pbxtime}"><br>
		<INPUT TYPE="hidden" NAME ="PBX_EFFECTUE" VALUE = "${Settings.MogopayEndPoint}paybox/done"><br>
		<INPUT TYPE="hidden" NAME ="PBX_REFUSE" VALUE = "${Settings.MogopayEndPoint}paybox/refuse"><br>
		<INPUT TYPE="hidden" NAME ="PBX_ANNULE" VALUE = "${Settings.MogopayEndPoint}paybox/cancel"><br>
		<INPUT TYPE="hidden" NAME ="PBX_REPONDRE_A" VALUE = "${Settings.MogopayEndPoint}paybox/callback"><br>
		<INPUT TYPE="hidden" NAME ="PBX_HMAC" VALUE = "${hmac}"><br>
	</FORM>
    	<script>document.getElementById("formpaybox").submit();</script>
    </body>
</html>
"""
      Success(Left(form))
    }
    else {
      Failure(throw new Exception("Invalid Paybox payment mode"))
    }
  }
}