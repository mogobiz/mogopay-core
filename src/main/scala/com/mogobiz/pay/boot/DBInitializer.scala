package com.mogobiz.pay.boot

import java.io.File
import java.sql.Timestamp
import java.util.{Calendar, Currency, Date, Random}

import com.mogobiz.es.{EsClient, Settings => esSettings}
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.model.Mogopay.AccountStatus.AccountStatus
import com.mogobiz.pay.model.Mogopay.CBPaymentMethod.CBPaymentMethod
import com.mogobiz.pay.model.Mogopay.CBPaymentProvider.CBPaymentProvider
import com.mogobiz.pay.model.Mogopay.RoleName.RoleName
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.pay.settings.{Mapping, Settings}
import com.mogobiz.utils.GlobalUtil._
import com.sksamuel.elastic4s.ElasticDsl._
import org.apache.shiro.crypto.hash.Sha256Hash
import org.elasticsearch.indices.IndexAlreadyExistsException
import org.elasticsearch.index.query.TermQueryBuilder
import org.elasticsearch.transport.RemoteTransportException

import scala.util.control.NonFatal
import scala.util.parsing.json.JSONObject

object DBInitializer {
  def apply(fillWithFixtures: Boolean = false) = {
    try {
      EsClient.client.sync.execute(create index Settings.Mogopay.EsIndex)
      Mapping.set
      fillDB(fillWithFixtures)
    } catch {
      case e: RemoteTransportException if e.getCause().isInstanceOf[IndexAlreadyExistsException] =>
        println(s"Index ${Settings.Mogopay.EsIndex} was not created because it already exists.")
      case e: Throwable => println("*****" + e.getClass.getName()); e.printStackTrace()
    }
  }

  private def fillDB(fillWithFixtures: Boolean) {
    val PAYPAL = Map("paypalUser" -> "hayssams-facilitator_api1.yahoo.com", "paypalPassword" -> "1365940711", "paypalSignature" -> "An5ns1Kso7MWUdW4ErQKJJJ4qi4-AIvKXMZ8RRQl6BBiVO5ISM9ECdEG")
    val PAYLINE = Map("paylineAccount" -> "26399702760590", "paylineKey" -> "SH0gPsNhvHmePmlZz3Mj", "paylineContract" -> "1234567")
    val SIPS = Map("sipsMerchantId" -> "011223344553333", "sipsMerchantCountry" -> "fr")
    val SIPS_2 = Map("sipsMerchantId" -> "011223344551112", "sipsMerchantCountry" -> "fr")
    val PAYBOX_EXTERNAL = Map("payboxSite" -> "1999888", "payboxKey" -> "110647233", "payboxRank" -> "32", "payboxMerchantId" -> "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF", "payboxContract" -> "PAYBOX_SYSTEM")

    val PAYBOX_2DS = Map("payboxSite" -> "1999888", "payboxKey" -> "1999888I", "payboxRank" -> "85", "payboxMerchantId" -> "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF", "payboxContract" -> "PAYBOX_SYSTEM")

    val PAYBOX_3DS = Map("payboxSite" -> "1999888", "payboxKey" -> "1999888I", "payboxRank" -> "69", "payboxMerchantId" -> "109518543", "payboxContract" -> "PAYBOX_DIRECT")

    val SYS_PAY = Map("systempayShopId" -> "34889127", "systempayContractNumber" -> "5028717", "systempayCertificate" -> "7736291283331938")

    val customer = RoleName.CUSTOMER
    val merchant = RoleName.MERCHANT

    val clientTelephone = Telephone("33672308706", "0672308706", "FR", None, TelephoneStatus.ACTIVE)
    val clientTelephone1 = Telephone("33685711396", "0685711396", "FR", None, TelephoneStatus.ACTIVE)
    val clientTelephoneInactive = Telephone("33698765432", "0698765432", "FR", None, TelephoneStatus.ACTIVE)
    val clientTelephoneWaitingEnroll = Telephone("33623456789", "0623456789", "FR", None, TelephoneStatus.WAITING_ENROLLMENT)
    val merchantTelephone = Telephone("33644104178", "0644104178", "FR", None, TelephoneStatus.ACTIVE)
    val merchantTelephone2 = Telephone("33644104179", "0644104179", "FR", None, TelephoneStatus.ACTIVE)
    val merchantTelephone3 = Telephone("33644104119", "0644104119", "FR", None, TelephoneStatus.ACTIVE)
    val merchantTelephone4 = Telephone("33644104129", "0644104129", "FR", None, TelephoneStatus.ACTIVE)
    val merchantTelephone5 = Telephone("33644104139", "0644104139", "FR", None, TelephoneStatus.ACTIVE)
    val merchantTelephone6 = Telephone("33644104149", "0644104149", "FR", None, TelephoneStatus.ACTIVE)
    val merchantTelephone7 = Telephone("33644104159", "0644104159", "FR", None, TelephoneStatus.ACTIVE)
    val merchantTelephone8 = Telephone("33644104169", "0644104169", "FR", None, TelephoneStatus.ACTIVE)
    val merchantTelephone9 = Telephone("33644104179", "0644104179", "FR", None, TelephoneStatus.ACTIVE)
    val merchantTelephone10 = Telephone("33690104179", "0690104179", "FR", None, TelephoneStatus.ACTIVE)
    val merchantTelephone11 = Telephone("33691104179", "0691104179", "FR", None, TelephoneStatus.ACTIVE)
    val merchantTelephone12 = Telephone("33611104179", "0611104179", "FR", None, TelephoneStatus.ACTIVE)

    val clientAccountAddress: AccountAddress = createAddress("Rue victor HUGO", "Paris", "75005", "France")
    val clientAccountAddress1 = createAddress("road 1", "city 1", "zip 1", "country 1")
    val clientInactifAddress = createAddress("road 2", "city 2", "zip 2", "France")
    val clientWaitingEnrollAddress = createAddress("road 3", "city 3", "zip 3", "France")
    val merchantAccountAddress = createAddress("4 Place de la Defense", "Puteaux", "92400", "France")
    val merchantAccountAddress2 = createAddress("Rue Saint Michel2", "Paris", "75007", "France")
    val merchantAccountAddress3 = createAddress("Rue Saint Michel3", "Paris", "75007", "France")
    val merchantAccountAddress4 = createAddress("Rue Saint Michel4", "Paris", "75007", "France")
    val merchantAccountAddress5 = createAddress("Rue Saint Michel5", "Paris", "75007", "France")
    val merchantAccountAddress6 = createAddress("Rue Saint Michel6", "Paris", "75007", "France")
    val merchantAccountAddress7 = createAddress("Rue Saint Michel7", "Paris", "75007", "France")
    val merchantAccountAddress8 = createAddress("Rue Saint Michel8", "Paris", "75007", "France")
    val merchantAccountAddress9 = createAddress("Rue Saint Michel9", "Paris", "75007", "France")
    val merchantAccountAddress10 = createAddress("Rue Saint Michel10", "Paris", "75007", "France")
    val merchantAccountAddress11 = createAddress("Rue Saint Michel11", "Paris", "75007", "France")
    val merchantAccountAddress12 = createAddress("Rue Saint Michel12", "Paris", "75007", "France")

    val paymentConfig1 = createPaymentConfig(CBPaymentProvider.PAYLINE,
      PAYPAL, PAYLINE, CBPaymentMethod.EXTERNAL, Some(1), Some( """\d+"""))
    var merchantAccountInfo = createAccount("Mogopay", "Merchant",
      "mogopay@merchant.com", merchantTelephone, merchantAccountAddress,
      merchant, Some(paymentConfig1), None, uuid = "mogopay")

    val clientAccountInfo = createAccount("Client 1", "TEST", "client@merchant.com", clientTelephone, clientAccountAddress, customer, None, Some(merchantAccountInfo))
    val clientAccountInfo1 = createAccount("Client 2", "TEST 1", "client1@merchant.com", clientTelephone1, clientAccountAddress1, customer, None, Some(merchantAccountInfo))
    val clientInactifAccountInfo = createAccount("Client 3", "Inactif", "inactif@merchant.com", clientTelephoneInactive, clientInactifAddress, customer, None, Some(merchantAccountInfo), AccountStatus.INACTIVE)
    val clientWaitingEnrollAccountInfo = createAccount("Client", "waiting", "waiting@merchant.com", clientTelephoneWaitingEnroll, clientWaitingEnrollAddress, customer, None, Some(merchantAccountInfo))

    createBOTransaction(merchantAccountInfo, Some("1"))
    createBOTransaction(merchantAccountInfo, Some("2"))
    createBOTransaction(merchantAccountInfo, None, Some(clientAccountInfo1))
    createBOTransaction(merchantAccountInfo, None, Some(clientAccountInfo1))
    createBOTransaction(merchantAccountInfo)
    createBOTransaction(merchantAccountInfo)

    val sipsPaymentConfig = createPaymentConfig(CBPaymentProvider.SIPS, PAYPAL, SIPS, CBPaymentMethod.THREEDS_NO)
    val merchantAccountInfo2 = createAccount("Merchant2", "TEST", "seller2@merchant.com", merchantTelephone2, merchantAccountAddress2, merchant, Some(sipsPaymentConfig), None)
    createCertification(merchantAccountInfo2)

    val sipsPaymentConfig12 = createPaymentConfig(CBPaymentProvider.SIPS, PAYPAL, SIPS, CBPaymentMethod.THREEDS_REQUIRED)
    val merchantAccountInfo12 = createAccount("Merchant12", "TEST", "seller12@merchant.com", merchantTelephone12, merchantAccountAddress12, merchant, Some(sipsPaymentConfig12), None)
    createCertification(merchantAccountInfo12)

    val paymentConfig2 = createPaymentConfig(CBPaymentProvider.SIPS, PAYPAL, SIPS_2, CBPaymentMethod.EXTERNAL)
    merchantAccountInfo = createAccount("Merchant10", "TEST", "seller10@merchant.com", merchantTelephone10, merchantAccountAddress10, merchant, Some(paymentConfig2), None)
    createParcom(merchantAccountInfo)

    var payboxPaymentConfig = createPaymentConfig(CBPaymentProvider.PAYBOX, PAYPAL, PAYBOX_EXTERNAL, CBPaymentMethod.EXTERNAL)
    createAccount("Merchant3", "TEST", "seller3@merchant.com", merchantTelephone3, merchantAccountAddress3, merchant, Some(payboxPaymentConfig), None)

    payboxPaymentConfig = createPaymentConfig(CBPaymentProvider.PAYBOX, PAYPAL, PAYBOX_2DS, CBPaymentMethod.THREEDS_NO)
    createAccount("Merchant4", "TEST", "seller4@merchant.com", merchantTelephone4, merchantAccountAddress4, merchant, Some(payboxPaymentConfig), None)

    payboxPaymentConfig = createPaymentConfig(CBPaymentProvider.PAYBOX, PAYPAL, PAYBOX_3DS, CBPaymentMethod.THREEDS_REQUIRED)
    createAccount("Merchant11", "TEST", "seller11@merchant.com", merchantTelephone11, merchantAccountAddress11, merchant, Some(payboxPaymentConfig), None)

    val paymentConfig3 = createPaymentConfig(CBPaymentProvider.PAYLINE, PAYPAL, PAYLINE, CBPaymentMethod.THREEDS_NO)
    createAccount("Merchant5", "TEST", "seller5@merchant.com", merchantTelephone5, merchantAccountAddress5, merchant, Some(paymentConfig3), None)

    val paymentConfig4 = createPaymentConfig(CBPaymentProvider.PAYLINE, PAYPAL, PAYLINE, CBPaymentMethod.THREEDS_REQUIRED)
    createAccount("Merchant6", "TEST", "seller6@merchant.com", merchantTelephone6, merchantAccountAddress6, merchant, Some(paymentConfig4), None)

    val paymentConfig5 = createPaymentConfig(CBPaymentProvider.SYSTEMPAY, PAYPAL, SYS_PAY, CBPaymentMethod.EXTERNAL)
    createAccount("Merchant7", "TEST", "seller7@merchant.com", merchantTelephone7, merchantAccountAddress7, merchant, Some(paymentConfig5), None, AccountStatus.ACTIVE, "seller7@merchant.com", "seller7")

    val paymentConfig6 = createPaymentConfig(CBPaymentProvider.SYSTEMPAY, PAYPAL, SYS_PAY, CBPaymentMethod.THREEDS_NO)
    createAccount("Merchant8", "TEST", "seller8@merchant.com", merchantTelephone8, merchantAccountAddress8, merchant, Some(paymentConfig6), None, AccountStatus.ACTIVE, "seller8@merchant.com", "seller8")

    val paymentConfig7 = createPaymentConfig(CBPaymentProvider.SYSTEMPAY, PAYPAL, SYS_PAY, CBPaymentMethod.THREEDS_REQUIRED)
    createAccount("Merchant9", "TEST", "seller9@merchant.com", merchantTelephone9, merchantAccountAddress9, merchant, Some(paymentConfig7), None, AccountStatus.ACTIVE, "seller9@merchant.com", "seller9")

//    val rateEUR = Rate(newUUID, "EUR", Calendar.getInstance.getTime, 0.01, 2)
//    EsClient.index(Settings.Mogopay.EsIndex, rateEUR, true)
//    val rateGBP = new Rate(newUUID, "GBP", Calendar.getInstance.getTime, 0.00829348, 2)
//    EsClient.index(Settings.Mogopay.EsIndex, rateGBP, true)
  }

  private def createAddress(road: String, city: String, zip: String, country: String) = {
    val tel = Some(Telephone("+33123456789", "0123456789", "", None, TelephoneStatus.ACTIVE))
    AccountAddress(road, None, city, Some(zip), None, None, None, None, tel, Some(country), None, None)
    //    AccountAddress(road, None, city, Some(zip), None, None, None, None, None, Some(country), None, None)
  }

  type M = Map[String, String]

  private def createPaymentConfig(cbProvider: CBPaymentProvider,
                                  paypalConfig: M,
                                  cbConfig: M,
                                  cbMethod: CBPaymentMethod,
                                  id: Option[Long] = None,
                                  passwordPattern: Option[String] = Some("")) = {
    PaymentConfig(
      None,
      Some(JSONObject(paypalConfig).toString()),
      Some(JSONObject(cbConfig).toString()),
      cbProvider,
      cbMethod,
      "user_email", "user_password",
      None, None, None, passwordPattern)
  }

  private def createAccount(firstName: String, lastName: String, email: String,
                            telephone: Telephone,
                            accountAddress: AccountAddress, role: RoleName,
                            paymentConfig: Option[PaymentConfig],
                            owner: Option[Account],
                            status: AccountStatus = AccountStatus.ACTIVE,
                            secret: String = newUUID,
                            uuid: String = newUUID): Account = {
    val account = Account(uuid,
      email,
      Some("COMPA"),
      Some("http://www.merchant.com"),
      new Sha256Hash("1234").toString,
      Some(Civility.MRS),
      Some(firstName),
      Some(lastName),
      Some(new Timestamp(0)),
      Some(accountAddress),
      status,
      0,
      0L,
      0L,
      None,
      None,
      paymentConfig,
      countryHandler.findByCode("FR"),
      List(role),
      owner.map(_.uuid),
      None,
      List(ShippingAddress("addr1", active = false, null)),
      secret,
      Nil)
    accountHandler.save(account)
    account
  }

  private def now(): Timestamp = new Timestamp(new java.util.Date().getTime)

  var i = 0

  private def createBOTransaction(vendor: Account,
                                  transactionUuid: Option[String] = None,
                                  customer: Option[Account] = None) = {
    val creditCard = BOCreditCard("1234XXXXXXXXXXX9087",
      Some("CLIENT SYMPA"), new Timestamp(new java.util.Date().getTime),
      CreditCardType.CB)
    val currency = TransactionCurrency("EUR", Currency.getInstance("EUR").getNumericCode, 0.001, 2)

    val rn: Random = new Random()

    val boPaymentData = BOPaymentData(PaymentType.CREDIT_CARD,
      CBPaymentProvider.SIPS,
      Some(rn.nextInt().toString),
      Some(now()),
      Some(ResponseCode3DS.APPROVED),
      Some("56745"),
      Some("56745"))

    val trans = BOTransaction(
      newUUID,
      transactionUUID = transactionUuid.getOrElse(newUUID),
      authorizationId = Math.abs(rn.nextLong).toString,
      transactionDate = Option(new Date((2014 - 1900) - i, 1, 1)),
      amount = (i + 1) * 100,
      currency = currency,
      status = TransactionStatus.PAYMENT_CONFIRMED,
      creationDate = now(),
      endDate = Some(now()),
      paymentData = boPaymentData,
      merchantConfirmation = true,
      email = Some(s"client${i += 1; i}@merchant.com"),
      errorCodeOrigin = Some("000"),
      errorMessageOrigin = Some("OK IMPEC"),
      extra = Some( """{"price": 100, "count": 1}"""),
      description = None,
      creditCard = Some(creditCard),
      vendor = Some(vendor),
      customer = customer,
      modifications = Nil
    )

    val tlog1 = BOTransactionLog(newUUID,
      "OUT",
      "m",
      "SIPS",
      trans.uuid)
    boTransactionLogHandler.save(tlog1)

    val tlog2 = BOTransactionLog(newUUID,
      "IN",
      "o",
      "SIPS",
      trans.uuid)
    boTransactionLogHandler.save(tlog2)

    val modificationStatus1 = ModificationStatus(newUUID,
      now(),
      Some("127.0.0.1"),
      None,
      Some(TransactionStatus.PAYMENT_REQUESTED),
      Some("Payment requested"))
    //      Some("Payment requested"),
    //      trans.uuid)

    val modificationStatus2 = ModificationStatus(newUUID,
      now(),
      Some("127.0.0.1"),
      None,
      Some(TransactionStatus.THREEDS_TESTED),
      Some("3DS check"))
    //      Some("3DS check"),
    //      trans.uuid)

    val modificationStatus3 = ModificationStatus(newUUID,
      now(),
      Some("127.0.0.1"),
      None,
      Some(TransactionStatus.PAYMENT_CONFIRMED),
      Some("Payment confirmed"))
    //      Some("Payment confirmed"),
    //      trans.uuid)

    EsClient.index(Settings.Mogopay.EsIndex, trans, true)
  }

  private def createCertification(merchant: Account) = {
    val content1 =
      """d6FDdyqW1EGZGNyXVK0VwiqsuxPhU1Q0yIPlXUaOqQIlCoLGOWqVrjL6e2neIIfJ31
15BJT9zCHzojfuy5hMTH0x28U3syZAFIGvlAnIu8BJjmoUroJi0YqIoQ68D1PWTaKA
d7x1oCBoje01qPv1w0lmTXm2qZfGAvRZvBwG0fbokGvtdMzsx2hEDWJt9Rq67VIOIE
ifX7VNEI1ASqhWKwSd5QXHwUbWLWYHBNAlKqjj54jtSiKpKHkBOmswkdyzFCgwggp5
qfp6VQq3VCDUpuYR5NtsdeAGoorE3PEkb2PqPfbwuEYu1qjRFPQ38K3hOOhdTuAREr
FgDgAoawJh6er8zAzvMevVoNyS61A1AJVyPtkywwc9D9ds4hkwGrmw7eX4GdXDtuwo
jW464NHLkuELvx3EDbOc6dIn376hrxD1z5dxlq1USFqK4cnLNg2PI9LueaXfQ5glcE
7SaMgtc6CjTmAwIs0fSyMQqx7JjRdgaXAWYwysucstFKHfp909wEX69Fn2gCYixfWt
hSIx6QvfCWOJaO1JSnaE2YGNm1lWup6h0BVMI9f5r4E155qto4VGAtez1eEM74GU59
QPCELUz1PWA0LZ2FSGJs8xBMQHrbMQdV6ADk5heBLmdZ8PfvJj1SEYOSGyEBfL2Ti5
CE6AYaVo2JrNP3QcRFkVFjEPRUOG5SnHEhHPyJmsWSgNYiRbRYCeyXxX737WwMo9A0
nNFTQwgssl65RA6bTEb0ppFBoqAZDqUhTu6vi7n4fU3l77FsaPD3JquenHcGADEq8C
3Wx8ErnSeYNq7IknZgx2p53l8kWpye9FFP295tAOaR1MOCPr6Nanb6XdNqPMLbMie5
rDwBmHkyYAaBAOQgeUGWesdDyBFqk7RaaTxEe0832XHNPKy2zeVmyFNSjwUWgbLrl0
rsReILx7ax42GlWh3o7kXtKI6suRq4z6WVW2sIdiXeOELnNyK1bjaB0Mj3QZyb7juy
vQmm1GZrgHrLYCiasSnqYxeMW4LWMWZ3cGi1hm1vqQGlWX0xvOgPNIUm16yGyxG6KO
E37eGotStOixl2cPARRNdsuplNscBwdweN1LuzcsqAjssAUTVnuKSsJbPzpZgDts6E
wIv7qESup7N9OCmGy9nhL0I1lUzimOe2HSso76OfdPHqjdloHGifpdcYf6KB6IlgiR
G2CAsyqGqWQIBCEYyCrtldAK90LOHcSGiUNx7S6QSntJZA6HpHzKe4v8PtBc8d0ANo
YTh8PMYTeYO9hd1Tmjee0tRojwu82CyWuObw1KXgTH2XfT5FMl9AIB8XyiNVz1YbSK
st3QD28MdHTiPdPiBzsbBhi3Ayo0NCnMUEXqsKl4QuOKDqfbDCNlbctleYZz8wtPqF
OtIxgBfD2vh1o8kvgONA3bNkRN90y9KqNlAoBtdAQEqhrEA5XiI3maNdTHrQ8Ie6xY
SJn60xTPzrzag21RdMilolZCvBkRGKWUohha8cLcxJWpNkr9Z9OLWBUHIS7Szz9uXa
GpGbpCH2NLEpPvtj5QX0Ma9YWGDwpdkhwqVAout2G7veY2iJzfm4sTWUPtVImXXT3k
Qg0tQKfF2AwhP7ewHmES5NQSlHYnRvIQIyC7k4OsDgXLJoCjr429kwC8Oa3Swchn7u
1125DDC1CB098B541A5F8EBD8D831A8539D5D3B5126AA9B8C323B76C26F7B41972
DB0AFD49B83A93B71AF769CA9E9DF07E4735353BD8CA2857B1eFcB1ZxNnOK4rRiC
YhZYW3QiL6OW9eXqQr1zR6dLlJOiMDbG6OGmKSYrbRLjNxgYF6O0LOphKqjcTtEjC4
qRYApJkYmWXLLANZn46w0I65L63PlBVrpYPSvFAu25aUMaSwcELNUKcpgFq5tsI1wG
128DB1E65B18A9F9DE89346E1B5CBD0F5D6B648DAD986207B9E65D209644493711
E9CBFC9D2A08D292E2DAC97A66542566118B217ACA9D3BC030CC46BD49CFF49EFC
YhZYW3QiL6OW9eXqQr1zR6dLlJOiMDbG6OGmKSYrbRLjNxgYF6O0LOphKqjcTtEjC4
qRYApJkYmWXLLANZn46w0I65L63PlBVrpYPSvFAu25aUMaSwcELNUKcpgFq5tsI1wG
1125DDC1CB098B541A5F8EBD8D831A8539D5D3B5126AA9B8C323B76C26F7B41972
DB0AFD49B83A93B71AF769CA9E9DF07E4735353BD8CA2857B1eFcB1ZxNnOK4rRiC
YhZYW3QiL6OW9eXqQr1zR6dLlJOiMDbG6OGmKSYrbRLjNxgYF6O0LOphKqjcTtEjC4
qRYApJkYmWXLLANZn46w0I65L63PlBVrpYPSvFAu25aUMaSwcELNUKcpgFq5tsI1wG
112BB312085367C359CCBE5492FADC5E3F637DD5557C92BEB13C2A477FD6F3AE48
2660F3AF33D0A33BDAAA23338980680FCBE29301B50718F611eFcB1ZxNnOK4rRiC
YhZYW3QiL6OW9eXqQr1zR6dLlJOiMDbG6OGmKSYrbRLjNxgYF6O0LOphKqjcTtEjC4
qRYApJkYmWXLLANZn46w0I65L63PlBVrpYPSvFAu25aUMaSwcELNUKcpgFq5tsI1wG
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
BOUTIQUE DE TEST REXT,23/02/2006,V4,SIPS,RCPR+++++++++++++++++++++
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++END
      """


    val certifDir = getCertifDir(merchant)
    certifDir.mkdirs()

    val certifTargetFile = new File(certifDir, "certif.fr.011223344553333")
    certifTargetFile.delete()
    scala.tools.nsc.io.File(certifTargetFile.getAbsolutePath).writeAll(content1)

    val content2 = "F_CERTIFICATE!" +
      new File(certifDir, "certif").getAbsolutePath + "!\n"
    val targetFile = new File(certifDir, "pathfile")
    targetFile.delete()
    scala.tools.nsc.io.File(targetFile.getAbsolutePath).writeAll(content2)
  }

  private def createParcom(merchant: Account) = {
    val parcomContent =
      """CANCEL_URL!http://!
RETURN_URL!http://!
ADVERT!!
BACKGROUND!!
CANCEL_LOGO!!
CARD_LIST!CB,VISA,MASTERCARD!
CURRENCY!978!
LANGUAGE!fr!
LOGO!!
LOGO2!!
MERCHANT_COUNTRY!fr!
MERCHANT_LANGUAGE!fr!
PAYMENT_MEANS!CB,2,VISA,2,MASTERCARD,2!
RETURN_LOGO!!
SUBMIT_LOGO!!
TEMPLATE!!
      """

    val parcomDefaultContent =
      """BGCOLOR!FFFFFF!
BLOCK_ALIGN!center!
BLOCK_ORDER!1,2,3,4,5,6,7,8,9!
HEADER_FLAG!yes!
TEXTCOLOR!000000!
      """

    val certifContent = """
<%
/*__DEBUT__
merchant_id!011223344551112!
merchant_country!fr!
certificate_expired!20130607!
certificate_version!1.0!
certificate_type!jsp!
certificate_data!
0YleDrvtaCoLd3RgfzpMCCNie8cz6j8AockJQLR1D358TxzlmZWeNbN3WWWGEMXOXI
IEPLAhYrYbCva0eXgwgRivDh9i7tagXmijzcOI1mqFEE3WCJ3t564J6Zk5C2zPNjEK
rb5icHLyzBU3rqn9xKDHSaVOUy6B9FNNEBD0uXfe3dPX59PoMkPDUSiXcvKOPBIsjt
cT707LewOz8goaoq1y785c9v8j8y8422R3hYdeU8PpVimSqiNVB6iemyjuaW2Mi0ZJ
rCZfkd3ezArQ8WO20OpfUO6L2tiqmvfKrElJQs7EKg95ouMpB4q2aFwqooLd9w75Xs
ZvUKZEY5JQDNMXXufMsGz3lx97l2nNl5tCVPEYOoSeSFpVTxM7TsmfuxzuPpfdhoN7
0Jzyn6gcWbBjHbS3bdz1QFqR3dkLozkuNiPTsD3dq37gn6IDLAjpYOmpFeueJu71zz
hGBlEBxj74SS9hSrc1o3c7tE7NhpZyThdjUpVDvWWPzQJ8hUci566ie4p6elPim5yq
NqC9IUjtZSwJOW7NyQE862CnAbLGTcSCZQLZCBrMjLrRjIN1LQDZV2utFVWrqVKh6X
3nDIrV426fngiGWA3RUjmMzQijMwCh76XCgGCqZCCI18DLyhkbQvLuWP2VSCPD6lZL
hUGzSGoiGUisk2EIOKQ5ZWWYKN65tq8CCJg9zgizjs7ZcOT2AylafemW6iheS4CJZu
YsmOszKxZukBX1WrSfIUzr2tYS3JyttFQsy3OZnZ5uNGDqHXvI6FkZAhtaGNoBx4ex
LtasYYidA2LrxpiLICw5NQrcjeOO4CEaivKBA4zeU3zX0sogo1dkuyIm1j9vMq3wRG
O3FMOLtHydU5rEKXpFjE0z61cwXTcAXdBrRPsTcIu9Ed0gzsMzL3k3lAtmeayNpSyy
FxYvrBL5OocGTgLWM8OB84fPO453LqyITJoUoh77a5vzoVU7Wqt6ZrYsKACFAY1E5t
8JXiuBrRdEhYMru6AGYXVp7OBPnyCaII4eeXdqfAJeBnma59navoHzu7mXJpbNcw8F
D1H8cEUoFmYSZqMQchvfDDyAPrOhkzu9GY2A9aB5wPCexaqWmrGumYeX7f0ihvEz8C
hwzTdkzMbCwbPsO20APkLUYWiQjT6OSx7noQrHrcCyhze41cErswSPRNG3U6On0936
YXNo2uMkXVUDuNUtu4zTxkNPlVbMeTQw3VfMKeoQYs2VltLTE2PgoX8MDyZuUcgT76
n7A6xXdFWSGy9tFP8FxAIcOw70MO7RLH2uyzxP4tvpMF6gJb6QJEICBLwsI0MUsnaR
ilqCnARWgSUIWrM8ire7vMvnwSiihotQP4de2ViqUyZO21LRuT53Um2RGtnRvGybdK
r2SPOVWOJPpr5Ky8Bc6mm7YmSWD9BC2hBIQhJK95OThL7XjjOsreIRuXbVPjPrlPzr
ayLqvsCGESq2v8d9QBpRp82BJonWVHSPkgNDYZjBZh2B7gAkSFzBv0SYrs4sW6PRmi
MhpBut7ImIgiK4C2D3z8hEo2hzOKIsTTmVwMXogsotGaG0DiUxBaMFst3Oh4IvxAXy
5ubv9lvc7krx7EGIYT8GZI9ptVtpPLOa9mCza3pXHeubIDQfvfmPrU4XtYVUtBO7Hi
112FB5132AB01F5F9F2EFED651E1419985A36E6AEC9FE87BD673B3609C931CCB15
6BBFB9BBAC6347BFD9622BA80AFD084F8360A5BB22B58A2E11eFcB1ZxNnOK4rRiC
YhZYW3QiL6OW9eXqQr1zR6dLlJOiMDbG6OGmKSYrbRLjNxgYF6O0LOphKqjcTtEjC4
qRYApJkYmWXLLANZn46w0I65L63PlBVrpYPSvFAu25aUMaSwcELNUKcpgFq5tsI1wG
112FE71364819E21D66F29BA2C7A454A9C6B8E125C548EA736E5CFC112A9D2C1F1
31A4305A5EFC45FB08C432E416E16058D245A81EA6D6A877A1eFcB1ZxNnOK4rRiC
YhZYW3QiL6OW9eXqQr1zR6dLlJOiMDbG6OGmKSYrbRLjNxgYF6O0LOphKqjcTtEjC4
qRYApJkYmWXLLANZn46w0I65L63PlBVrpYPSvFAu25aUMaSwcELNUKcpgFq5tsI1wG
112FB5132AB01F5F9F2EFED651E1419985A36E6AEC9FE87BD673B3609C931CCB15
6BBFB9BBAC6347BFD9622BA80AFD084F8360A5BB22B58A2E11eFcB1ZxNnOK4rRiC
YhZYW3QiL6OW9eXqQr1zR6dLlJOiMDbG6OGmKSYrbRLjNxgYF6O0LOphKqjcTtEjC4
qRYApJkYmWXLLANZn46w0I65L63PlBVrpYPSvFAu25aUMaSwcELNUKcpgFq5tsI1wG
1121343CCAF762A709F204F712B0434A2CEAF0C3A1FBCF98FA43B3609C931CCB15
6E0C11251B2AF27EF3B269BF7BDB4B980B0FC63CCCA30AA021eFcB1ZxNnOK4rRiC
YhZYW3QiL6OW9eXqQr1zR6dLlJOiMDbG6OGmKSYrbRLjNxgYF6O0LOphKqjcTtEjC4
qRYApJkYmWXLLANZn46w0I65L63PlBVrpYPSvFAu25aUMaSwcELNUKcpgFq5tsI1wG
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
sips02demo,06/05/1999,V4,SIPS,DEMO++++++++++++++++++++++++++++++++
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++END
__FIN__*/
%>
</html>
                        """

    val certifDir = getCertifDir(merchant)
    certifDir.mkdirs()

    val parcomTargetFile = new File(certifDir, "parcom.011223344551112")
    parcomTargetFile.delete()
    scala.tools.nsc.io.File(parcomTargetFile.getAbsolutePath).writeAll(parcomContent)

    val parcomDefaultTargetFile = new File(certifDir, "parcom.default")
    parcomDefaultTargetFile.delete()
    scala.tools.nsc.io.File(parcomDefaultTargetFile.getAbsolutePath).writeAll(parcomDefaultContent)

    var certifTargetFile = new File(certifDir, "certif.fr.011223344551112.jsp")
    certifTargetFile.delete()
    scala.tools.nsc.io.File(certifTargetFile.getAbsolutePath).writeAll(certifContent)

    val targetFile = new File(certifDir, "pathfile")
    targetFile.delete()
    val targetContent =
      s"""D_LOGO!${Settings.ImagesPath + "sips/logo/"}!
        |F_DEFAULT!${new File(certifDir, "parcom.default").getAbsolutePath}!
        |F_PARAM!${new File(certifDir, "parcom").getAbsolutePath}!
        |F_CERTIFICATE!${new File(certifDir, "certif").getAbsolutePath}!
        |F_CTYPE!jsp!
        |""".stripMargin
    scala.tools.nsc.io.File(targetFile.getAbsolutePath).writeAll(targetContent)
  }

  private def getCertifDir(merchant: Account): File = {
    new File(Settings.Sips.CertifDir + merchant.uuid.toString)
  }
}

object DbInitMain extends App {
  try {
    EsClient.client.client.prepareDeleteByQuery(Settings.Mogopay.EsIndex).setQuery(new TermQueryBuilder("_type", "Account")).execute.actionGet
    EsClient.client.client.prepareDeleteByQuery(Settings.Mogopay.EsIndex).setQuery(new TermQueryBuilder("_type", "BOTransaction")).execute.actionGet
    EsClient.client.client.prepareDeleteByQuery(Settings.Mogopay.EsIndex).setQuery(new TermQueryBuilder("_type", "BOTransactionLog")).execute.actionGet
    EsClient.client.client.prepareDeleteByQuery(Settings.Mogopay.EsIndex).setQuery(new TermQueryBuilder("_type", "ESSession")).execute.actionGet
    EsClient.client.client.prepareDeleteByQuery(Settings.Mogopay.EsIndex).setQuery(new TermQueryBuilder("_type", "TransactionSequence")).execute.actionGet
    EsClient.client.client.prepareDeleteByQuery(Settings.Mogopay.EsIndex).setQuery(new TermQueryBuilder("_type", "TransactionRequest")).execute.actionGet
  }
  catch {
    case NonFatal(_) => println()
  }
  DBInitializer()
}