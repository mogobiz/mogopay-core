package com.mogobiz.pay.boot

import java.io.File
import java.sql.Timestamp
import java.util.{Currency, UUID, Date}

import com.mogobiz.es.{EsClient, Settings => esSettings}
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.model.Mogopay.AccountStatus.AccountStatus
import com.mogobiz.pay.model.Mogopay._
import com.mogobiz.pay.model.Mogopay.TelephoneStatus.TelephoneStatus
import com.mogobiz.pay.model.Mogopay.AccountStatus.AccountStatus
import com.mogobiz.pay.model.Mogopay.CBPaymentMethod.CBPaymentMethod
import com.mogobiz.pay.model.Mogopay.CBPaymentProvider.CBPaymentProvider
import com.mogobiz.pay.settings.{Mapping, Settings}
import com.sksamuel.elastic4s.ElasticDsl._
import org.apache.shiro.crypto.hash.Sha256Hash
import org.elasticsearch.indices.IndexAlreadyExistsException
import org.elasticsearch.index.query.TermQueryBuilder
import org.elasticsearch.transport.RemoteTransportException

import scala.util.Random
import scala.util.control.NonFatal
import scala.util.parsing.json.JSONObject

object DBInitializer {
  def apply(fillWithFixtures: Boolean = false) = {
    try {
      //EsClient.client.execute(delete index Settings.Mogopay.EsIndex).await
      EsClient.client.execute(create index Settings.Mogopay.EsIndex).await
      if (Settings.DerbySequence.length > 0) {
        import scalikejdbc._
        DB autoCommit { implicit session =>
          try {
            SQL(Settings.DerbySequence).execute.apply()
          }
          catch {
            case NonFatal(e) =>
              // Ignore if sequence exists
              //e.printStackTrace()
          }
        }
      }
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

    // Création des comptes marchands
    val paypalPaylineExternal = createPaymentConfig(CBPaymentProvider.PAYLINE, PAYPAL, PAYLINE, CBPaymentMethod.EXTERNAL, Some(1), Some( """\d+"""))
    val merchantAccount = createMerchantAccount("ebc23bd9-3abc-4684-849d-e4e15c1a0f82", "mogopay@merchant.com", "Mogopay", "Merchant", paypalPaylineExternal)

    val paypalSips2DSConfig = createPaymentConfig(CBPaymentProvider.SIPS, PAYPAL, SIPS, CBPaymentMethod.THREEDS_NO)
    val merchantAccount2 = createMerchantAccount("c3a4548f-1edf-4a1f-8a9a-4f0b374720cd", "seller2@merchant.com", "Merchant2", "TEST", paypalSips2DSConfig)
    createCertification(merchantAccount2)

    val paypalPayboxExternalConfig = createPaymentConfig(CBPaymentProvider.PAYBOX, PAYPAL, PAYBOX_EXTERNAL, CBPaymentMethod.EXTERNAL)
    val merchantAccount3 = createMerchantAccount("d389ae7-5136-42ef-a6d6-f2f02cee075d", "seller3@merchant.com", "Merchant3", "TEST", paypalPayboxExternalConfig)

    val paypalPaybox2DSConfig = createPaymentConfig(CBPaymentProvider.PAYBOX, PAYPAL, PAYBOX_2DS, CBPaymentMethod.THREEDS_NO)
    val merchantAccount4 = createMerchantAccount("17727558-970e-40f3-9fa1-89319995891c", "seller4@merchant.com", "Merchant4", "TEST", paypalPaybox2DSConfig)

    val paypalPayline2DSConfig = createPaymentConfig(CBPaymentProvider.PAYLINE, PAYPAL, PAYLINE, CBPaymentMethod.THREEDS_NO)
    val merchantAccount5 = createMerchantAccount("5873f47c-afb4-4cb7-bc76-16ca88c389e7", "seller5@merchant.com", "Merchant5", "TEST", paypalPayline2DSConfig)

    val paypalPayline3DSConfig = createPaymentConfig(CBPaymentProvider.PAYLINE, PAYPAL, PAYLINE, CBPaymentMethod.THREEDS_REQUIRED)
    val merchantAccount6 = createMerchantAccount("30958ef7-fad3-4f29-988e-df51376974cd", "seller6@merchant.com", "Merchant6", "TEST", paypalPayline3DSConfig)

    val paypalSystemPayExternalConfig = createPaymentConfig(CBPaymentProvider.SYSTEMPAY, PAYPAL, SYS_PAY, CBPaymentMethod.EXTERNAL)
    val merchantAccount7 = createMerchantAccount("d7b864c8-4567-4603-abd4-5f85e9ff56e6", "seller7@merchant.com", "Merchant7", "TEST", paypalSystemPayExternalConfig)

    val paypalSystemPay2DSConfig = createPaymentConfig(CBPaymentProvider.SYSTEMPAY, PAYPAL, SYS_PAY, CBPaymentMethod.THREEDS_NO)
    val merchantAccount8 = createMerchantAccount("78a2fa03-5498-4f07-b716-c9b9c2b64954", "seller8@merchant.com", "Merchant8", "TEST", paypalSystemPay2DSConfig)

    val paypalSystemPay3DSConfig = createPaymentConfig(CBPaymentProvider.SYSTEMPAY, PAYPAL, SYS_PAY, CBPaymentMethod.THREEDS_REQUIRED)
    val merchantAccount9 = createMerchantAccount("92795318-8760-4a5f-b71a-c7dcf4af2b79", "seller9@merchant.com", "Merchant9", "TEST", paypalSystemPay3DSConfig)

    val paypalSipsExternalConfig = createPaymentConfig(CBPaymentProvider.SIPS, PAYPAL, SIPS_2, CBPaymentMethod.EXTERNAL)
    val merchantAccount10 = createMerchantAccount("f56269e3-7d22-4dcf-9812-ff74a7d7d2c7", "seller10@merchant.com", "Merchant10", "TEST", paypalSipsExternalConfig)
    createParcom(merchantAccount10)

    val paypalPaybox3DSConfig = createPaymentConfig(CBPaymentProvider.PAYBOX, PAYPAL, PAYBOX_3DS, CBPaymentMethod.THREEDS_REQUIRED)
    val merchantAccount11 = createMerchantAccount("e7542826-f6cc-46bd-8f61-550b7fea9ca7", "seller11@merchant.com", "Merchant11", "TEST", paypalPaybox3DSConfig)

    val paypalSips3DSPaymentConfig = createPaymentConfig(CBPaymentProvider.SIPS, PAYPAL, SIPS, CBPaymentMethod.THREEDS_REQUIRED)
    val merchantAccount12 = createMerchantAccount("7264a70e-9960-4492-b466-4377a1fc2025", "seller12@merchant.com", "Merchant12", "TEST", paypalSips3DSPaymentConfig)
    createCertification(merchantAccount12)

    // Création des comptes clients
    val client1Account = createClientAccount("8a53ef3e-34e8-4569-8f68-ac0dfc548a0f", "client@merchant.com", "Client 1", "TEST", merchantAccount7, true)
    createClientAccount("7f441fa3-d382-4838-8255-9fc238cdb958", "client1@merchant.com", "Client 2", "TEST 1", merchantAccount7, true)
    createClientAccount("fd80c7e4-c91d-492a-8b48-214b809105d8", "inactif@merchant.com", "Client 3", "Inactif", merchantAccount7, true, AccountStatus.INACTIVE)
    createClientAccount("15995735-56ca-4d19-806b-a6bc7fedc162", "waiting@merchant.com", "Client", "waiting", merchantAccount7, true, AccountStatus.ACTIVE, TelephoneStatus.WAITING_ENROLLMENT)
    createClientAccount("a8858dd5-e14f-4aa0-9504-3d56bab5229d", "existing.account@test.com", "Existing", "Account", merchantAccount7, true)

    createTransaction("2bd49675-bec8-4fd2-bce0-6c83094ce8e8", 10400, client1Account, merchantAccount7, "{\"boCartUuid\":\"e2e3cc44-6c2f-4736-976b-c54a798bd8f0\",\"cartItemVOs\":[{\"registeredCartItemVOs\":[],\"formatedPrice\":\"100,00 €\",\"skuName\":\"type1\",\"productId\":32715,\"formatedTotalPrice\":\"100,00 €\",\"id\":\"acf84259-8a9c-483c-af08-2cfcb22d61d3\",\"calendarType\":\"NO_DATE\",\"shipping\":{\"amount\":0,\"id\":0,\"free\":false,\"height\":0,\"weight\":0,\"weightUnit\":null,\"width\":0,\"linearUnit\":null,\"depth\":0},\"formatedEndPrice\":\"100,00 €\",\"price\":10000,\"formatedTotalEndPrice\":\"100,00 €\",\"xtype\":\"PRODUCT\",\"saleTotalPrice\":10000,\"skuId\":34591,\"endPrice\":10000,\"quantity\":1,\"salePrice\":10000,\"productName\":\"Mens Electric Blue Gym T-Shirt\",\"totalEndPrice\":10000,\"totalPrice\":10000}],\"count\":1,\"formatedPrice\":\"100,00 €\",\"transactionUuid\":\"\",\"finalPrice\":10400,\"coupons\":[],\"formatedEndPrice\":\"100,00 €\",\"price\":10000,\"endPrice\":10000,\"formatedFinalPrice\":\"100,00 €\",\"reduction\":0,\"formatedReduction\":\"0,00 €\",\"shipping\":400}")
    createTransaction("9aeaadde-c36f-4aac-8d2b-5f506b0cf536", 1194, client1Account, merchantAccount7, "{\"boCartUuid\":\"50eca8f7-3679-4477-9fc7-e3811d0d838c\",\"cartItemVOs\":[{\"registeredCartItemVOs\":[],\"formatedPrice\":\"9,99 €\",\"saleTotalEndPrice\":1194,\"skuName\":\"S\",\"productId\":32568,\"formatedTotalPrice\":\"9,99 €\",\"id\":\"8e7e90d1-d2f1-4067-bae2-8cc2015b0876\",\"calendarType\":\"NO_DATE\",\"shipping\":{\"amount\":0,\"id\":0,\"free\":false,\"height\":0,\"weight\":0,\"weightUnit\":null,\"width\":0,\"linearUnit\":null,\"depth\":0},\"formatedEndPrice\":\"11,94 €\",\"price\":999,\"saleEndPrice\":1194,\"tax\":19.6,\"formatedTotalEndPrice\":\"11,94 €\",\"saleTotalPrice\":999,\"xtype\":\"PRODUCT\",\"skuId\":214560,\"endPrice\":1194,\"salePrice\":999,\"quantity\":1,\"productName\":\"Electric Blue Womens Football Shirt\",\"totalEndPrice\":1194,\"totalPrice\":999}],\"count\":1,\"formatedPrice\":\"9,99 €\",\"transactionUuid\":\"\",\"finalPrice\":1594,\"coupons\":[],\"formatedEndPrice\":\"11,94 €\",\"price\":999,\"endPrice\":1194,\"formatedFinalPrice\":\"11,94 €\",\"reduction\":0,\"formatedReduction\":\"0,00 €\",\"shipping\":400}")
  }

  private def createMerchantAccount(uuid: String, email: String, firstname: String, lastname: String, paymentConfig : PaymentConfig) : Account = {
    val account = Account(uuid = uuid,
                          email = email,
                          company = Some("acmesport"),
                          password = new Sha256Hash("1234").toString,
                          civility = Some(Civility.MR),
                          firstName = Some(firstname),
                          lastName = Some(lastname),
                          address = Some(createAddress(firstname, lastname)),
                          status = AccountStatus.ACTIVE,
                          paymentConfig = Some(paymentConfig),
                          roles = List(RoleName.MERCHANT),
                          secret = uuid)
    accountHandler.save(account)
    account
  }

  private def createClientAccount(uuid: String, email: String, firstname: String, lastname: String, owner: Account, withShippingAddress: Boolean, status: AccountStatus = AccountStatus.ACTIVE, telephoneStatus: TelephoneStatus = TelephoneStatus.ACTIVE) : Account = {
    val account = Account(uuid = uuid,
      email = email,
      password = new Sha256Hash("1234").toString,
      civility = Some(Civility.MR),
      firstName = Some(firstname),
      lastName = Some(lastname),
      birthDate = Some(new Date(2000, 0, 1)),
      address = Some(createAddress(firstname, lastname, telephoneStatus)),
      status = status,
      roles = List(RoleName.CUSTOMER),
      owner = Some(owner.uuid),
      shippingAddresses = if (withShippingAddress) List(createShippingAddress(firstname, lastname, true), createShippingAddress(firstname, lastname, false)) else List(),
      secret = uuid)
    accountHandler.save(account)
    account
  }

  private def createAddress(firstname: String, lastname: String, telephoneStatus: TelephoneStatus = TelephoneStatus.ACTIVE) : AccountAddress = {
    val phone = Telephone("+33123456789", "0123456789", "FR", Some("000"), telephoneStatus)
    AccountAddress(civility = Some(Civility.MR),
      firstName = Some(firstname),
      lastName = Some(lastname),
      telephone = Some(phone),
      road = "road",
      road2 = Some("road2"),
      extra = Some("extra"),
      city = "Paris",
      zipCode = Some("75000"),
      country = Some("FR"),
      admin1 = Some("FR.A8"),
      admin2 = Some("FR.A8.75"))
  }

  private def createShippingAddress(firstname: String, lastname: String, active: Boolean) : ShippingAddress = {
    val prefixe = if (active) "Active " else ""
    ShippingAddress(uuid = UUID.randomUUID().toString,
                    active = active,
                    address = createAddress(prefixe + firstname, prefixe + lastname))
  }

  private def createPaymentConfig(cbProvider: CBPaymentProvider,
                                  paypalConfig: Map[String, String],
                                  cbConfig: Map[String, String],
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

  private def createTransaction(transactionUuid: String, amount: Long, customer: Account, vendor: Account, extra: String) = {
    val transactionDate = randomDate();
    val currency = TransactionCurrency("EUR", Currency.getInstance("EUR").getNumericCode, 0.01, 2)
    val creditCard = BOCreditCard("1234XXXXXXXXXXX9087", None, new Date(), CreditCardType.CB)
    val paymentData = BOPaymentData(PaymentType.CREDIT_CARD,
      CBPaymentProvider.SYSTEMPAY,
      Some(new Random().nextInt().toString),
      Some(transactionDate),
      Some(ResponseCode3DS.APPROVED),
      Some("56745"),
      Some("56745"))

    val transaction = BOTransaction(
      uuid = UUID.randomUUID().toString,
      transactionUUID = transactionUuid,
      authorizationId = Math.abs(new Random().nextLong()).toString,
      transactionDate = Option(transactionDate),
      amount = amount,
      currency = currency,
      status = TransactionStatus.PAYMENT_CONFIRMED,
      creationDate = transactionDate,
      endDate = Some(transactionDate),
      paymentData = paymentData,
      merchantConfirmation = true,
      email = Some(customer.email),
      errorCodeOrigin = Some("000"),
      errorMessageOrigin = Some("OK"),
      extra = Some(extra),
      description = None,
      creditCard = Some(creditCard),
      vendor = Some(vendor),
      customer = Some(customer),
      modifications = Nil
    )
    boTransactionHandler.save(transaction, refresh = true)

    boTransactionLogHandler.save(BOTransactionLog(UUID.randomUUID().toString, "OUT", "m", "SYSTEMPAY", transaction.uuid))
    boTransactionLogHandler.save(BOTransactionLog(UUID.randomUUID().toString, "IN", "o", "SYSTEMPAY", transaction.uuid))
  }

  private def randomDate() : Date = {
    val current = System.currentTimeMillis()
    val diff = new Random().nextInt(30) * 24 * 60 * 60 * 1000
    new Date(current - diff)
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