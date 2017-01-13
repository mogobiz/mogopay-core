/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.boot

import java.io.File
import java.util.{Calendar, Currency, Date, UUID}

import com.mogobiz.es.EsClient
import com.mogobiz.pay.codes.MogopayConstant
import com.mogobiz.pay.common.CartRate
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.config.{Mapping, Settings}
import com.mogobiz.pay.model.AccountStatus.AccountStatus
import com.mogobiz.pay.model.CBPaymentMethod.CBPaymentMethod
import com.mogobiz.pay.model.CBPaymentProvider.CBPaymentProvider
import com.mogobiz.pay.model.TelephoneStatus.TelephoneStatus
import com.mogobiz.pay.model.{PaymentConfig, _}
import com.sksamuel.elastic4s.ElasticDsl._
import org.apache.shiro.crypto.hash.Sha256Hash
import org.elasticsearch.index.query.TermQueryBuilder
import org.elasticsearch.indices.IndexAlreadyExistsException
import org.elasticsearch.transport.RemoteTransportException

import scala.util.Random
import scala.util.control.NonFatal
import scala.util.parsing.json.JSONObject

object DBInitializer {
  def apply(fillWithFixtures: Boolean) = {
    try {
      import EsClient.secureRequest
      //EsClient().execute(secureRequest(delete index Settings.Mogopay.EsIndex)).await
      if (Settings.DerbySequence.length > 0) {
        import scalikejdbc._
        DB autoCommit { implicit session =>
          try {
            SQL(Settings.DerbySequence).execute.apply()
          } catch {
            case NonFatal(e) =>
            // Ignore if sequence exists
            //e.printStackTrace()
          }
        }
      }
      EsClient().execute(secureRequest(create index Settings.Mogopay.EsIndex)).await
      Mapping.set
      if (fillWithFixtures) fillDB()
    } catch {
      case e: RemoteTransportException if e.getCause().isInstanceOf[IndexAlreadyExistsException] =>
        println(s"Index ${Settings.Mogopay.EsIndex} was not created because it already exists.")
      case e: Throwable => println("*****" + e.getClass.getName()); e.printStackTrace()
    }
  }

  private def fillDB() {
    val PAYPAL = Map("paypalUser" -> "hayssams-facilitator_api1.yahoo.com",
                     "paypalPassword"  -> "1365940711",
                     "paypalSignature" -> "An5ns1Kso7MWUdW4ErQKJJJ4qi4-AIvKXMZ8RRQl6BBiVO5ISM9ECdEG")
    val PAYLINE =
      Map("paylineAccount" -> "26399702760590", "paylineKey" -> "SH0gPsNhvHmePmlZz3Mj", "paylineContract" -> "1234567")
    val SIPS   = Map("sipsMerchantId" -> "011223344553333", "sipsMerchantCountry" -> "fr")
    val SIPS_2 = Map("sipsMerchantId" -> "011223344551112", "sipsMerchantCountry" -> "fr")
    val PAYBOX_EXTERNAL = Map(
        "payboxSite"       -> "1999888",
        "payboxKey"        -> "110647233",
        "payboxRank"       -> "32",
        "payboxMerchantId" -> "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF",
        "payboxContract"   -> "PAYBOX_SYSTEM")
    val PAYBOX_2DS = Map(
        "payboxSite"       -> "1999888",
        "payboxKey"        -> "1999888I",
        "payboxRank"       -> "85",
        "payboxMerchantId" -> "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF",
        "payboxContract"   -> "PAYBOX_DIRECT")
    val PAYBOX_3DS = Map("payboxSite" -> "1999888",
                         "payboxKey"        -> "1999888I",
                         "payboxRank"       -> "69",
                         "payboxMerchantId" -> "109518543",
                         "payboxContract"   -> "PAYBOX_DIRECT")
    val SYS_PAY = Map("systempayShopId" -> "34889127",
                      "systempayContractNumber" -> "5028717",
                      "systempayCertificate"    -> "7736291283331938")
    val AUTHORIZENET = Map("apiLoginID" -> "5zLq4S76A", "transactionKey" -> "4tMKn44B9a8Cn9C5", "md5Key" -> "wilson")

    // Création des comptes marchands
    val paypalPaylineExternal = createPaymentConfig(CBPaymentProvider.PAYLINE,
                                                    PAYPAL,
                                                    Map(),
                                                    PAYLINE,
                                                    CBPaymentMethod.EXTERNAL,
                                                    Some(1),
                                                    Some("""\d+"""))
    val merchantAccount = createMerchantAccount("ebc23bd9-3abc-4684-849d-e4e15c1a0f82",
                                                "mogopay@merchant.com",
                                                "Mogopay",
                                                "Merchant",
                                                paypalPaylineExternal)

    val paypalSips2DSConfig =
      createPaymentConfig(CBPaymentProvider.SIPS, PAYPAL, Map(), SIPS, CBPaymentMethod.THREEDS_NO)
    val merchantAccount2 = createMerchantAccount("c3a4548f-1edf-4a1f-8a9a-4f0b374720cd",
                                                 "seller2@merchant.com",
                                                 "Merchant2",
                                                 "TEST",
                                                 paypalSips2DSConfig)
    createCertification(merchantAccount2)

    val paypalPayboxExternalConfig =
      createPaymentConfig(CBPaymentProvider.PAYBOX, PAYPAL, Map(), PAYBOX_EXTERNAL, CBPaymentMethod.EXTERNAL)
    val merchantAccount3 = createMerchantAccount("d389ae7-5136-42ef-a6d6-f2f02cee075d",
                                                 "seller3@merchant.com",
                                                 "Merchant3",
                                                 "TEST",
                                                 paypalPayboxExternalConfig)

    val paypalPaybox2DSConfig =
      createPaymentConfig(CBPaymentProvider.PAYBOX, PAYPAL, Map(), PAYBOX_2DS, CBPaymentMethod.THREEDS_NO)
    val merchantAccount4 = createMerchantAccount("17727558-970e-40f3-9fa1-89319995891c",
                                                 "seller4@merchant.com",
                                                 "Merchant4",
                                                 "TEST",
                                                 paypalPaybox2DSConfig)

    val paypalPayline2DSConfig =
      createPaymentConfig(CBPaymentProvider.PAYLINE, PAYPAL, Map(), PAYLINE, CBPaymentMethod.THREEDS_NO)
    val merchantAccount5 = createMerchantAccount("5873f47c-afb4-4cb7-bc76-16ca88c389e7",
                                                 "seller5@merchant.com",
                                                 "Merchant5",
                                                 "TEST",
                                                 paypalPayline2DSConfig)

    val paypalPayline3DSConfig =
      createPaymentConfig(CBPaymentProvider.PAYLINE, PAYPAL, Map(), PAYLINE, CBPaymentMethod.THREEDS_REQUIRED)
    val merchantAccount6 = createMerchantAccount("30958ef7-fad3-4f29-988e-df51376974cd",
                                                 "seller6@merchant.com",
                                                 "Merchant6",
                                                 "TEST",
                                                 paypalPayline3DSConfig)

    val paypalSystemPayExternalConfig =
      createPaymentConfig(CBPaymentProvider.SYSTEMPAY, PAYPAL, Map(), SYS_PAY, CBPaymentMethod.EXTERNAL)
    val merchantAccount7 = createMerchantAccount("d7b864c8-4567-4603-abd4-5f85e9ff56e6",
                                                 "seller7@merchant.com",
                                                 "Merchant7",
                                                 "TEST",
                                                 paypalSystemPayExternalConfig)

    val paypalSystemPay2DSConfig =
      createPaymentConfig(CBPaymentProvider.SYSTEMPAY, PAYPAL, Map(), SYS_PAY, CBPaymentMethod.THREEDS_NO)
    val merchantAccount8 = createMerchantAccount("78a2fa03-5498-4f07-b716-c9b9c2b64954",
                                                 "seller8@merchant.com",
                                                 "Merchant8",
                                                 "TEST",
                                                 paypalSystemPay2DSConfig)

    val paypalSystemPay3DSConfig =
      createPaymentConfig(CBPaymentProvider.SYSTEMPAY, PAYPAL, Map(), SYS_PAY, CBPaymentMethod.THREEDS_REQUIRED)
    val merchantAccount9 = createMerchantAccount("92795318-8760-4a5f-b71a-c7dcf4af2b79",
                                                 "seller9@merchant.com",
                                                 "Merchant9",
                                                 "TEST",
                                                 paypalSystemPay3DSConfig)

    val paypalSipsExternalConfig =
      createPaymentConfig(CBPaymentProvider.SIPS, PAYPAL, Map(), SIPS_2, CBPaymentMethod.EXTERNAL)
    val merchantAccount10 = createMerchantAccount("f56269e3-7d22-4dcf-9812-ff74a7d7d2c7",
                                                  "seller10@merchant.com",
                                                  "Merchant10",
                                                  "TEST",
                                                  paypalSipsExternalConfig)
    createParcom(merchantAccount10)

    val paypalPaybox3DSConfig =
      createPaymentConfig(CBPaymentProvider.PAYBOX, PAYPAL, Map(), PAYBOX_3DS, CBPaymentMethod.THREEDS_REQUIRED)
    val merchantAccount11 = createMerchantAccount("e7542826-f6cc-46bd-8f61-550b7fea9ca7",
                                                  "seller11@merchant.com",
                                                  "Merchant11",
                                                  "TEST",
                                                  paypalPaybox3DSConfig)

    val paypalSips3DSPaymentConfig =
      createPaymentConfig(CBPaymentProvider.SIPS, PAYPAL, Map(), SIPS, CBPaymentMethod.THREEDS_REQUIRED)
    val merchantAccount12 = createMerchantAccount("7264a70e-9960-4492-b466-4377a1fc2025",
                                                  "seller12@merchant.com",
                                                  "Merchant12",
                                                  "TEST",
                                                  paypalSips3DSPaymentConfig)
    createCertification(merchantAccount12)

    //    val paymentConfig12 = createPaymentConfig(CBPaymentProvider.AUTHORIZENET, PAYPAL, AUTHORIZENET, Map(), CBPaymentMethod.EXTERNAL)
    //    var merchantAccountInfoANet = createMerchantAccount("cccccccc-cccc-cccc-cccc-cccccccccccc", "mogopay-anet@merchant.com",
    //      "ANet", "Merchant", paymentConfig12)

    val systemPayCustomConfig = createPaymentConfig(cbProvider = CBPaymentProvider.SYSTEMPAY,
                                                    paypalConfig = PAYPAL,
                                                    applePayConfig = Map(),
                                                    cbConfig = SYS_PAY,
                                                    cbMethod = CBPaymentMethod.THREEDS_NO,
                                                    senderEmail = Some("mogopay-systempay-custom@merchant.com"),
                                                    senderName = Some("Mogopay Systempay Custom"))
    val merchantAccountInfoSystempayCustom = createMerchantAccount("cccccccc-4567-4602-abd5-4f85e9ff56e6",
                                                                   "mogopay-systempay-custom@merchant.com",
                                                                   "Mogopay Systempay Custom",
                                                                   "TEST",
                                                                   systemPayCustomConfig)

    val payboxCustomConfig = createPaymentConfig(cbProvider = CBPaymentProvider.PAYBOX,
                                                 paypalConfig = PAYPAL,
                                                 applePayConfig = Map(),
                                                 cbConfig = PAYBOX_2DS,
                                                 cbMethod = CBPaymentMethod.THREEDS_NO,
                                                 senderEmail = Some("mogopay-paybox-custom@merchant.com"),
                                                 senderName = Some("Mogopay Paybox Custom"))
    val merchantAccountInfoPayboxCustom = createMerchantAccount("dddddddd-4567-4602-abd5-4f85e9ff56e6",
                                                                "mogopay-paybox-custom@merchant.com",
                                                                "Mogopay Paybox Custom",
                                                                "TEST",
                                                                payboxCustomConfig)

    val paylineCustomConfig = createPaymentConfig(cbProvider = CBPaymentProvider.PAYLINE,
                                                  paypalConfig = PAYPAL,
                                                  applePayConfig = Map(),
                                                  cbConfig = PAYLINE,
                                                  cbMethod = CBPaymentMethod.THREEDS_NO,
                                                  senderEmail = Some("mogopay-payline-custom@merchant.com"),
                                                  senderName = Some("Mogopay Payline Custom"))
    val merchantAccountInfoPaylineCustom = createMerchantAccount("eeeeeeee-4567-4602-abd5-4f85e9ff56e6",
                                                                 "mogopay-payline-custom@merchant.com",
                                                                 "Mogopay Payline Custom",
                                                                 "TEST",
                                                                 paylineCustomConfig)

    val sipsCustomConfig = createPaymentConfig(cbProvider = CBPaymentProvider.SIPS,
                                               paypalConfig = PAYPAL,
                                               applePayConfig = Map(),
                                               cbConfig = SIPS,
                                               cbMethod = CBPaymentMethod.THREEDS_NO,
                                               senderEmail = Some("mogopay-sips-custom@merchant.com"),
                                               senderName = Some("Mogopay SIPS Custom"))
    val merchantAccountInfoSIPSCustom = createMerchantAccount("ffffffff-4567-4602-abd5-4f85e9ff56e6",
                                                              "mogopay-sips-custom@merchant.com",
                                                              "Mogopay SIPS Custom",
                                                              "TEST",
                                                              sipsCustomConfig)
    createCertification(merchantAccountInfoSIPSCustom)

    val paymentConfig12External = createPaymentConfig(CBPaymentProvider.AUTHORIZENET,
                                                      PAYPAL,
                                                      Map(),
                                                      AUTHORIZENET,
                                                      CBPaymentMethod.EXTERNAL,
                                                      None,
                                                      Some(""),
                                                      Some("anet-merchant-external"),
                                                      Some("anet-merchant-external@mogopay.com"))
    val paymentConfig12Custom = createPaymentConfig(CBPaymentProvider.AUTHORIZENET,
                                                    PAYPAL,
                                                    Map(),
                                                    AUTHORIZENET,
                                                    CBPaymentMethod.THREEDS_NO,
                                                    None,
                                                    Some(""),
                                                    Some("anet-merchant-custom"),
                                                    Some("anet-merchant-custome@mogopay.com"))
    var merchantAccountInfoANetExternal = createMerchantAccount("f802a048-e8ec-4619-abf0-d3a0e0eecc2e",
                                                                "mogopay-anet-external@merchant.com",
                                                                "ANET External",
                                                                "Merchant",
                                                                paymentConfig12External)
    var merchantAccountInfoANetCustom = createMerchantAccount("f5c4a907-6f73-4ecf-ba34-3c7d97d3d6ba",
                                                              "mogopay-anet-custom@merchant.com",
                                                              "ANet Custom",
                                                              "Merchant",
                                                              paymentConfig12Custom)

    // Création des comptes clients
    val client1Account = createClientAccount("8a53ef3e-34e8-4569-8f68-ac0dfc548a0f",
                                             "client@merchant.com",
                                             "Client 1",
                                             "TEST",
                                             merchantAccount7,
                                             true)
    createClientAccount("7f441fa3-d382-4838-8255-9fc238cdb958",
                        "client1@merchant.com",
                        "Client 2",
                        "TEST 1",
                        merchantAccount7,
                        true)
    createClientAccount("fd80c7e4-c91d-492a-8b48-214b809105d8",
                        "inactif@merchant.com",
                        "Client 3",
                        "Inactif",
                        merchantAccount7,
                        true,
                        AccountStatus.INACTIVE)
    createClientAccount("15995735-56ca-4d19-806b-a6bc7fedc162",
                        "waiting@merchant.com",
                        "Client",
                        "waiting",
                        merchantAccount7,
                        true,
                        AccountStatus.ACTIVE,
                        TelephoneStatus.WAITING_ENROLLMENT)
    createClientAccount("a8858dd5-e14f-4aa0-9504-3d56bab5229d",
                        "existing.account@test.com",
                        "Existing",
                        "Account",
                        merchantAccount7,
                        true)
    //    createClientAccount("e6ca3ff5-ee4f-4184-b3e0-0adde58c77c6", "client-anet@merchant.com", "ANet", "Client", merchantAccountInfoANet, false)
    createClientAccount("36992642-8bd3-41e0-aaaf-92956c0f78a1",
                        "client-anet-external@merchant.com",
                        "ANet External",
                        "Client",
                        merchantAccountInfoANetExternal,
                        false)
    createClientAccount("d31bbce4-29b4-465a-aedd-1784ee6e3929",
                        "client-anet-custom@merchant.com",
                        "ANet Custom",
                        "Client",
                        merchantAccountInfoANetCustom,
                        false)
    createClientAccount("cccccccc-29b4-465a-aedd-1784ee6e3929",
                        "customer-systempay-custom@merchant.com",
                        "Systempay Custom",
                        "Customer",
                        merchantAccountInfoSystempayCustom,
                        false)
    createClientAccount("dddddddd-29b4-465a-aedd-1784ee6e3929",
                        "customer-paybox-custom@merchant.com",
                        "Paybox Custom",
                        "Customer",
                        merchantAccountInfoPayboxCustom,
                        false)
    createClientAccount("eeeeeeee-29b4-465a-aedd-1784ee6e3929",
                        "customer-payline-custom@merchant.com",
                        "Payline Custom",
                        "Customer",
                        merchantAccountInfoPaylineCustom,
                        false,
                        geoCoords = Option("49.849524,3.287492"))
    createClientAccount("ffffffff-29b4-465a-aedd-1784ee6e3929",
                        "customer-sips-custom@merchant.com",
                        "SIPS Custom",
                        "Customer",
                        merchantAccountInfoSIPSCustom,
                        false,
                        geoCoords = Option("48.892011,2.379271"))

    createTransaction(
        "4c7a5788-0079-4781-b823-047cbef84198",
        "4c7a5788-0079-4781-b823-047cbef84198",
        2560,
        client1Account,
        merchantAccount7,
        "{\"uuid\":\"4c7a5788-0079-4781-b823-047cbef84198\",\"transactionUUID\":\"4c7a5788-0079-4781-b823-047cbef84198\",\"authorizationId\":\"\",\"transactionDate\":1424351055148,\"amount\":2560,\"currency\":{\"code\":\"EUR\",\"numericCode\":978,\"rate\":0.01,\"fractionDigits\":2},\"status\":\"PAYMENT_CONFIRMED\",\"endDate\":1424351055164,\"paymentData\":{\"paymentType\":\"CREDIT_CARD\",\"cbProvider\":\"SYSTEMPAY\",\"transactionSequence\":\"140258\",\"orderDate\":1424351046248,\"status3DS\":null,\"transactionId\":null,\"authorizationId\":null},\"merchantConfirmation\":true,\"email\":\"client@merchant.com\",\"errorCodeOrigin\":\"00\",\"errorMessageOrigin\":\"\",\"extra\":\"{\\\"boCartUuid\\\":\\\"6faede3a-744e-426f-b7a3-e79ef4228293\\\",\\\"cartItemVOs\\\":[{\\\"registeredCartItemVOs\\\":[],\\\"formatedPrice\\\":\\\"18,00 €\\\",\\\"saleTotalEndPrice\\\":2160,\\\"skuName\\\":\\\"XL\\\",\\\"productId\\\":32689,\\\"formatedTotalPrice\\\":\\\"18,00 €\\\",\\\"id\\\":\\\"ca3ee296-e79a-4c31-a9c1-8c196693103b\\\",\\\"calendarType\\\":\\\"NO_DATE\\\",\\\"shipping\\\":{\\\"amount\\\":0,\\\"id\\\":0,\\\"free\\\":false,\\\"height\\\":0,\\\"weight\\\":0,\\\"weightUnit\\\":null,\\\"width\\\":0,\\\"linearUnit\\\":null,\\\"depth\\\":0},\\\"formatedEndPrice\\\":\\\"21,60 €\\\",\\\"price\\\":1800,\\\"saleEndPrice\\\":2160,\\\"tax\\\":20,\\\"formatedTotalEndPrice\\\":\\\"21,60 €\\\",\\\"saleTotalPrice\\\":1800,\\\"xtype\\\":\\\"PRODUCT\\\",\\\"skuId\\\":214957,\\\"endPrice\\\":2160,\\\"salePrice\\\":1800,\\\"quantity\\\":1,\\\"productName\\\":\\\"Womens Pink Gym T-Shirt\\\",\\\"totalEndPrice\\\":2160,\\\"totalPrice\\\":1800}],\\\"count\\\":1,\\\"formatedPrice\\\":\\\"18,00 €\\\",\\\"transactionUuid\\\":\\\"\\\",\\\"finalPrice\\\":2560,\\\"coupons\\\":[],\\\"formatedEndPrice\\\":\\\"21,60 €\\\",\\\"price\\\":1800,\\\"endPrice\\\":2160,\\\"formatedFinalPrice\\\":\\\"21,60 €\\\",\\\"reduction\\\":0,\\\"formatedReduction\\\":\\\"0,00 €\\\",\\\"shipping\\\":400}\",\"description\":\"\",\"creditCard\":{\"number\":\"497010XXXXXX0000\",\"holder\":null,\"expiryDate\":1464732000000,\"cardType\":\"CB\"},\"vendor\":{\"uuid\":\"d7b864c8-4567-4603-abd4-5f85e9ff56e6\",\"email\":\"seller7@merchant.com\",\"company\":\"acmesport\",\"website\":null,\"password\":\"03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4\",\"civility\":\"MR\",\"firstName\":\"Merchant7\",\"lastName\":\"TEST\",\"birthDate\":null,\"address\":{\"road\":\"road\",\"road2\":\"road2\",\"city\":\"Paris\",\"zipCode\":\"75000\",\"extra\":\"extra\",\"civility\":\"MR\",\"firstName\":\"Merchant7\",\"lastName\":\"TEST\",\"telephone\":{\"phone\":\"+33123456789\",\"lphone\":\"0123456789\",\"isoCode\":\"FR\",\"pinCode3\":\"000\",\"status\":\"ACTIVE\"},\"country\":\"FR\",\"admin1\":\"FR.A8\",\"admin2\":\"FR.A8.75\"},\"status\":\"ACTIVE\",\"loginFailedCount\":0,\"waitingPhoneSince\":-1,\"waitingEmailSince\":-1,\"extra\":null,\"lastLogin\":null,\"paymentConfig\":{\"kwixoParam\":null,\"paypalParam\":\"{\\\"paypalUser\\\" : \\\"hayssams-facilitator_api1.yahoo.com\\\", \\\"paypalPassword\\\" : \\\"1365940711\\\", \\\"paypalSignature\\\" : \\\"An5ns1Kso7MWUdW4ErQKJJJ4qi4-AIvKXMZ8RRQl6BBiVO5ISM9ECdEG\\\"}\",\"cbParam\":\"{\\\"systempayShopId\\\" : \\\"34889127\\\", \\\"systempayContractNumber\\\" : \\\"5028717\\\", \\\"systempayCertificate\\\" : \\\"7736291283331938\\\"}\",\"cbProvider\":\"SYSTEMPAY\",\"paymentMethod\":\"EXTERNAL\",\"emailField\":\"user_email\",\"passwordField\":\"user_password\",\"pwdEmailContent\":null,\"pwdEmailSubject\":null,\"callbackPrefix\":null,\"passwordPattern\":\"\",\"dateCreated\":1424350358357,\"lastUpdated\":1424350358357},\"country\":null,\"roles\":[\"MERCHANT\"],\"owner\":null,\"emailingToken\":null,\"shippingAddresses\":[],\"secret\":\"d7b864c8-4567-4603-abd4-5f85e9ff56e6\",\"creditCards\":[],\"walletId\":null,\"dateCreated\":1424350358373,\"lastUpdated\":1424350358373},\"customer\":null,\"modifications\":[{\"uuid\":\"f092b37a-583c-4c32-9aee-61c3029294e1\",\"xdate\":1424351046406,\"ipAddr\":null,\"oldStatus\":\"INITIATED\",\"newStatus\":\"PAYMENT_REQUESTED\",\"comment\":null,\"dateCreated\":1424351046406,\"lastUpdated\":1424351046406},{\"uuid\":\"56c2ea83-a49a-4883-86bb-8dcea9fa4e4a\",\"xdate\":1424351055164,\"ipAddr\":null,\"oldStatus\":\"PAYMENT_REQUESTED\",\"newStatus\":\"PAYMENT_CONFIRMED\",\"comment\":\"00\",\"dateCreated\":1424351055164,\"lastUpdated\":1424351055164}],\"dateCreated\":1424351046352,\"lastUpdated\":1424351055176}",
      paylineCustomConfig)
    createTransaction(
        "f9f71371-17f3-4dcd-bf8f-5d313470ccdf",
        "f9f71371-17f3-4dcd-bf8f-5d313470ccdf",
        2080,
        client1Account,
        merchantAccount7,
        "{\"uuid\":\"f9f71371-17f3-4dcd-bf8f-5d313470ccdf\",\"transactionUUID\":\"f9f71371-17f3-4dcd-bf8f-5d313470ccdf\",\"authorizationId\":\"\",\"transactionDate\":1424351129173,\"amount\":2080,\"currency\":{\"code\":\"EUR\",\"numericCode\":978,\"rate\":0.01,\"fractionDigits\":2},\"status\":\"PAYMENT_CONFIRMED\",\"endDate\":1424351129185,\"paymentData\":{\"paymentType\":\"CREDIT_CARD\",\"cbProvider\":\"SYSTEMPAY\",\"transactionSequence\":\"140259\",\"orderDate\":1424351121747,\"status3DS\":null,\"transactionId\":null,\"authorizationId\":null},\"merchantConfirmation\":true,\"email\":\"client@merchant.com\",\"errorCodeOrigin\":\"00\",\"errorMessageOrigin\":\"\",\"extra\":\"{\\\"boCartUuid\\\":\\\"3429ca0e-0e8e-4749-99c2-0822d91b4b3a\\\",\\\"cartItemVOs\\\":[{\\\"registeredCartItemVOs\\\":[],\\\"formatedPrice\\\":\\\"14,00 €\\\",\\\"saleTotalEndPrice\\\":1680,\\\"skuName\\\":\\\"L\\\",\\\"productId\\\":32709,\\\"formatedTotalPrice\\\":\\\"14,00 €\\\",\\\"id\\\":\\\"30aced5e-5688-424f-9f65-50c1367cc65c\\\",\\\"calendarType\\\":\\\"NO_DATE\\\",\\\"shipping\\\":{\\\"amount\\\":0,\\\"id\\\":0,\\\"free\\\":false,\\\"height\\\":0,\\\"weight\\\":0,\\\"weightUnit\\\":null,\\\"width\\\":0,\\\"linearUnit\\\":null,\\\"depth\\\":0},\\\"formatedEndPrice\\\":\\\"16,80 €\\\",\\\"price\\\":1400,\\\"saleEndPrice\\\":1680,\\\"tax\\\":20,\\\"formatedTotalEndPrice\\\":\\\"16,80 €\\\",\\\"saleTotalPrice\\\":1400,\\\"xtype\\\":\\\"PRODUCT\\\",\\\"skuId\\\":214942,\\\"endPrice\\\":1680,\\\"salePrice\\\":1400,\\\"quantity\\\":1,\\\"productName\\\":\\\"Mens Olive Gym T-Shirt\\\",\\\"totalEndPrice\\\":1680,\\\"totalPrice\\\":1400}],\\\"count\\\":1,\\\"formatedPrice\\\":\\\"14,00 €\\\",\\\"transactionUuid\\\":\\\"\\\",\\\"finalPrice\\\":2080,\\\"coupons\\\":[],\\\"formatedEndPrice\\\":\\\"16,80 €\\\",\\\"price\\\":1400,\\\"endPrice\\\":1680,\\\"formatedFinalPrice\\\":\\\"16,80 €\\\",\\\"reduction\\\":0,\\\"formatedReduction\\\":\\\"0,00 €\\\",\\\"shipping\\\":400}\",\"description\":\"\",\"creditCard\":{\"number\":\"497010XXXXXX0000\",\"holder\":null,\"expiryDate\":1464732000000,\"cardType\":\"CB\"},\"vendor\":{\"uuid\":\"d7b864c8-4567-4603-abd4-5f85e9ff56e6\",\"email\":\"seller7@merchant.com\",\"company\":\"acmesport\",\"website\":null,\"password\":\"03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4\",\"civility\":\"MR\",\"firstName\":\"Merchant7\",\"lastName\":\"TEST\",\"birthDate\":null,\"address\":{\"road\":\"road\",\"road2\":\"road2\",\"city\":\"Paris\",\"zipCode\":\"75000\",\"extra\":\"extra\",\"civility\":\"MR\",\"firstName\":\"Merchant7\",\"lastName\":\"TEST\",\"telephone\":{\"phone\":\"+33123456789\",\"lphone\":\"0123456789\",\"isoCode\":\"FR\",\"pinCode3\":\"000\",\"status\":\"ACTIVE\"},\"country\":\"FR\",\"admin1\":\"FR.A8\",\"admin2\":\"FR.A8.75\"},\"status\":\"ACTIVE\",\"loginFailedCount\":0,\"waitingPhoneSince\":-1,\"waitingEmailSince\":-1,\"extra\":null,\"lastLogin\":null,\"paymentConfig\":{\"kwixoParam\":null,\"paypalParam\":\"{\\\"paypalUser\\\" : \\\"hayssams-facilitator_api1.yahoo.com\\\", \\\"paypalPassword\\\" : \\\"1365940711\\\", \\\"paypalSignature\\\" : \\\"An5ns1Kso7MWUdW4ErQKJJJ4qi4-AIvKXMZ8RRQl6BBiVO5ISM9ECdEG\\\"}\",\"cbParam\":\"{\\\"systempayShopId\\\" : \\\"34889127\\\", \\\"systempayContractNumber\\\" : \\\"5028717\\\", \\\"systempayCertificate\\\" : \\\"7736291283331938\\\"}\",\"cbProvider\":\"SYSTEMPAY\",\"paymentMethod\":\"EXTERNAL\",\"emailField\":\"user_email\",\"passwordField\":\"user_password\",\"pwdEmailContent\":null,\"pwdEmailSubject\":null,\"callbackPrefix\":null,\"passwordPattern\":\"\",\"dateCreated\":1424350358357,\"lastUpdated\":1424350358357},\"country\":null,\"roles\":[\"MERCHANT\"],\"owner\":null,\"emailingToken\":null,\"shippingAddresses\":[],\"secret\":\"d7b864c8-4567-4603-abd4-5f85e9ff56e6\",\"creditCards\":[],\"walletId\":null,\"dateCreated\":1424350358373,\"lastUpdated\":1424350358373},\"customer\":null,\"modifications\":[{\"uuid\":\"76398031-755b-43c0-b0be-070efbb63bde\",\"xdate\":1424351121767,\"ipAddr\":null,\"oldStatus\":\"INITIATED\",\"newStatus\":\"PAYMENT_REQUESTED\",\"comment\":null,\"dateCreated\":1424351121767,\"lastUpdated\":1424351121767},{\"uuid\":\"ec7fde59-fd5a-4e13-b0bc-3fc896dfbc7e\",\"xdate\":1424351129185,\"ipAddr\":null,\"oldStatus\":\"PAYMENT_REQUESTED\",\"newStatus\":\"PAYMENT_CONFIRMED\",\"comment\":\"00\",\"dateCreated\":1424351129185,\"lastUpdated\":1424351129185}],\"dateCreated\":1424351121764,\"lastUpdated\":1424351129194}",
      paylineCustomConfig)
    createTransaction(
        "931eedc2-a4cd-431f-ba9c-aba4ed68806c",
        "931eedc2-a4cd-431f-ba9c-aba4ed68806c",
        1188000,
        client1Account,
        merchantAccount7,
        "{\"uuid\":\"931eedc2-a4cd-431f-ba9c-aba4ed68806c\",\"transactionUUID\":\"931eedc2-a4cd-431f-ba9c-aba4ed68806c\",\"authorizationId\":\"\",\"transactionDate\":1424351179994,\"amount\":1188000,\"currency\":{\"code\":\"EUR\",\"numericCode\":978,\"rate\":0.01,\"fractionDigits\":2},\"status\":\"PAYMENT_CONFIRMED\",\"endDate\":1424351180006,\"paymentData\":{\"paymentType\":\"CREDIT_CARD\",\"cbProvider\":\"SYSTEMPAY\",\"transactionSequence\":\"140260\",\"orderDate\":1424351173247,\"status3DS\":null,\"transactionId\":null,\"authorizationId\":null},\"merchantConfirmation\":true,\"email\":\"client@merchant.com\",\"errorCodeOrigin\":\"00\",\"errorMessageOrigin\":\"\",\"extra\":\"{\\\"boCartUuid\\\":\\\"c133b2a8-682d-4a4e-b349-13e3ef7c6f57\\\",\\\"cartItemVOs\\\":[{\\\"registeredCartItemVOs\\\":[{\\\"id\\\":\\\"4c406b47-2007-4326-bac8-c88e4f91533c\\\",\\\"phone\\\":\\\"0123456789\\\",\\\"cartItemId\\\":\\\"6461c756-2dc5-4641-a9b4-79f90cd7ad8d\\\",\\\"email\\\":\\\"client@merchant.com\\\",\\\"birthdate\\\":\\\"2000-01-01T00:00:00Z\\\",\\\"lastname\\\":\\\"Client 1\\\",\\\"firstname\\\":\\\"TEST\\\"}],\\\"formatedPrice\\\":\\\"9 900,00 €\\\",\\\"saleTotalEndPrice\\\":1188000,\\\"skuName\\\":\\\"VIP Seat\\\",\\\"productId\\\":31938,\\\"formatedTotalPrice\\\":\\\"9 900,00 €\\\",\\\"id\\\":\\\"6461c756-2dc5-4641-a9b4-79f90cd7ad8d\\\",\\\"calendarType\\\":\\\"NO_DATE\\\",\\\"formatedEndPrice\\\":\\\"11 880,00 €\\\",\\\"price\\\":990000,\\\"saleEndPrice\\\":1188000,\\\"tax\\\":20,\\\"formatedTotalEndPrice\\\":\\\"11 880,00 €\\\",\\\"saleTotalPrice\\\":990000,\\\"xtype\\\":\\\"SERVICE\\\",\\\"skuId\\\":214707,\\\"endPrice\\\":1188000,\\\"salePrice\\\":990000,\\\"quantity\\\":1,\\\"productName\\\":\\\"Play Golf on the Moon\\\",\\\"totalEndPrice\\\":1188000,\\\"totalPrice\\\":990000}],\\\"count\\\":1,\\\"formatedPrice\\\":\\\"9 900,00 €\\\",\\\"transactionUuid\\\":\\\"\\\",\\\"finalPrice\\\":1188000,\\\"coupons\\\":[],\\\"formatedEndPrice\\\":\\\"11 880,00 €\\\",\\\"price\\\":990000,\\\"endPrice\\\":1188000,\\\"formatedFinalPrice\\\":\\\"11 880,00 €\\\",\\\"reduction\\\":0,\\\"formatedReduction\\\":\\\"0,00 €\\\",\\\"shipping\\\":0}\",\"description\":\"\",\"creditCard\":{\"number\":\"497010XXXXXX0000\",\"holder\":null,\"expiryDate\":1464732000000,\"cardType\":\"CB\"},\"vendor\":{\"uuid\":\"d7b864c8-4567-4603-abd4-5f85e9ff56e6\",\"email\":\"seller7@merchant.com\",\"company\":\"acmesport\",\"website\":null,\"password\":\"03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4\",\"civility\":\"MR\",\"firstName\":\"Merchant7\",\"lastName\":\"TEST\",\"birthDate\":null,\"address\":{\"road\":\"road\",\"road2\":\"road2\",\"city\":\"Paris\",\"zipCode\":\"75000\",\"extra\":\"extra\",\"civility\":\"MR\",\"firstName\":\"Merchant7\",\"lastName\":\"TEST\",\"telephone\":{\"phone\":\"+33123456789\",\"lphone\":\"0123456789\",\"isoCode\":\"FR\",\"pinCode3\":\"000\",\"status\":\"ACTIVE\"},\"country\":\"FR\",\"admin1\":\"FR.A8\",\"admin2\":\"FR.A8.75\"},\"status\":\"ACTIVE\",\"loginFailedCount\":0,\"waitingPhoneSince\":-1,\"waitingEmailSince\":-1,\"extra\":null,\"lastLogin\":null,\"paymentConfig\":{\"kwixoParam\":null,\"paypalParam\":\"{\\\"paypalUser\\\" : \\\"hayssams-facilitator_api1.yahoo.com\\\", \\\"paypalPassword\\\" : \\\"1365940711\\\", \\\"paypalSignature\\\" : \\\"An5ns1Kso7MWUdW4ErQKJJJ4qi4-AIvKXMZ8RRQl6BBiVO5ISM9ECdEG\\\"}\",\"cbParam\":\"{\\\"systempayShopId\\\" : \\\"34889127\\\", \\\"systempayContractNumber\\\" : \\\"5028717\\\", \\\"systempayCertificate\\\" : \\\"7736291283331938\\\"}\",\"cbProvider\":\"SYSTEMPAY\",\"paymentMethod\":\"EXTERNAL\",\"emailField\":\"user_email\",\"passwordField\":\"user_password\",\"pwdEmailContent\":null,\"pwdEmailSubject\":null,\"callbackPrefix\":null,\"passwordPattern\":\"\",\"dateCreated\":1424350358357,\"lastUpdated\":1424350358357},\"country\":null,\"roles\":[\"MERCHANT\"],\"owner\":null,\"emailingToken\":null,\"shippingAddresses\":[],\"secret\":\"d7b864c8-4567-4603-abd4-5f85e9ff56e6\",\"creditCards\":[],\"walletId\":null,\"dateCreated\":1424350358373,\"lastUpdated\":1424350358373},\"customer\":null,\"modifications\":[{\"uuid\":\"31854cb1-fec1-4142-a74b-527796c9abb9\",\"xdate\":1424351173264,\"ipAddr\":null,\"oldStatus\":\"INITIATED\",\"newStatus\":\"PAYMENT_REQUESTED\",\"comment\":null,\"dateCreated\":1424351173264,\"lastUpdated\":1424351173264},{\"uuid\":\"39d7ba26-35a7-447b-b857-773bc29ad10a\",\"xdate\":1424351180006,\"ipAddr\":null,\"oldStatus\":\"PAYMENT_REQUESTED\",\"newStatus\":\"PAYMENT_CONFIRMED\",\"comment\":\"00\",\"dateCreated\":1424351180006,\"lastUpdated\":1424351180006}],\"dateCreated\":1424351173261,\"lastUpdated\":1424351180015}",
      paylineCustomConfig)

    val APPLEPAY: Map[String, String] = Map()
    val applePayConfig = createPaymentConfig(CBPaymentProvider.NONE,
                                             PAYPAL,
                                             APPLEPAY,
                                             PAYLINE,
                                             CBPaymentMethod.EXTERNAL,
                                             Some(42),
                                             Some("""\d+"""))
    var applePayMerchant = createMerchantAccount("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                                                 "mogopay-apay@merchant.com",
                                                 "Merchant",
                                                 "Mogopay",
                                                 applePayConfig)
    val applePayClient = createClientAccount("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                                             "client-apay@merchant.com",
                                             "Apple Pay",
                                             "Client",
                                             applePayMerchant,
                                             false,
                                             geoCoords = Option("48.871806,2.297987"))
  }

  val franceCountry = Country(UUID.randomUUID.toString, "FR", "FRA", "250", "France", false, false, None, None, None, None, None)

  private def createMerchantAccount(uuid: String,
                                    email: String,
                                    firstname: String,
                                    lastname: String,
                                    paymentConfig: PaymentConfig): Account = {
    val account = Account(uuid = uuid,
                          email = email,
                          company = Some(email),
                          password = new Sha256Hash("1234").toString,
                          civility = Some(Civility.MR),
                          firstName = Some(firstname),
                          lastName = Some(lastname),
                          address = Some(createAddress(firstname, lastname)),
                          status = AccountStatus.ACTIVE,
                          paymentConfig = Some(paymentConfig),
                          roles = List(RoleName.MERCHANT),
                          secret = uuid,
                          country = Some(franceCountry))
    accountHandler.save(account)
    account
  }

  private def createClientAccount(uuid: String,
                                  email: String,
                                  firstname: String,
                                  lastname: String,
                                  owner: Account,
                                  withShippingAddress: Boolean,
                                  status: AccountStatus = AccountStatus.ACTIVE,
                                  telephoneStatus: TelephoneStatus = TelephoneStatus.ACTIVE,
                                  geoCoords: Option[String] = None): Account = {
    val birthDate = Calendar.getInstance()
    birthDate.set(2000, 0, 1)
    val account = Account(
        uuid = uuid,
        email = email,
        password = new Sha256Hash("1234").toString,
        civility = Some(Civility.MR),
        firstName = Some(firstname),
        lastName = Some(lastname),
        birthDate = Some(birthDate.getTime),
        address = Some(
            createAddress(firstname = firstname,
                          lastname = lastname,
                          telephoneStatus = telephoneStatus,
                          geoCoords = geoCoords)),
        status = status,
        roles = List(RoleName.CUSTOMER),
        owner = Some(owner.uuid),
        shippingAddresses =
          if (withShippingAddress)
            List(createShippingAddress(firstname, lastname, true), createShippingAddress(firstname, lastname, false))
          else List(),
        secret = uuid,
        country = Some(franceCountry))
    accountHandler.save(account)
    account
  }

  private def createAddress(firstname: String,
                            lastname: String,
                            telephoneStatus: TelephoneStatus = TelephoneStatus.ACTIVE,
                            geoCoords: Option[String] = None): AccountAddress = {
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
                   admin2 = Some("FR.A8.75"),
                   geoCoordinates = geoCoords)
  }

  private def createShippingAddress(firstname: String, lastname: String, active: Boolean): ShippingAddress = {
    val prefixe = if (active) "Active " else ""
    ShippingAddress(uuid = UUID.randomUUID().toString,
                    active = active,
                    address = createAddress(prefixe + firstname, prefixe + lastname))
  }

  private def createPaymentConfig(cbProvider: CBPaymentProvider,
                                  paypalConfig: Map[String, String],
                                  applePayConfig: Map[String, String],
                                  cbConfig: Map[String, String],
                                  cbMethod: CBPaymentMethod,
                                  id: Option[Long] = None,
                                  passwordPattern: Option[String] = Some(""),
                                  senderName: Option[String] = None,
                                  senderEmail: Option[String] = None) = {
    PaymentConfig(
        None,
        Some(JSONObject(paypalConfig).toString()),
        Some(JSONObject(applePayConfig).toString()),
        Some(JSONObject(cbConfig).toString()),
        cbProvider,
        cbMethod,
        "user_email",
        "user_password",
        senderName,
        senderEmail,
        None,
        passwordPattern,
        groupPaymentInfo = Some(
            GroupPaymentInfo(
                returnURLforNextPayers = "http://returnurl/",
                successURL = "http://successURL",
                failureURL = "http://failureURL"
            ))
    )
  }

  private def createTransaction(uuid: String,
                                transactionUuid: String,
                                amount: Long,
                                customer: Account,
                                vendor: Account,
                                extra: String,
                                paymentConfig: PaymentConfig) = {
    val transactionDate = randomDate()
    val currency        = CartRate("EUR", Currency.getInstance("EUR").getNumericCode, 0.01, 2)
    val creditCard      = BOCreditCard("1234XXXXXXXXXXX9087", None, new Date(), CreditCardType.CB)
    val paymentData = BOPaymentData(PaymentType.CREDIT_CARD,
                                    CBPaymentProvider.SYSTEMPAY,
                                    Some(new Random().nextInt().toString),
                                    Some(transactionDate),
                                    Some(ResponseCode3DS.APPROVED),
                                    Some("56745"),
                                    Some("56745"))

    val transaction = BOTransaction(
        uuid = uuid,
        transactionUUID = transactionUuid,
        Calendar.getInstance().getTime,
        None,
        vendor = vendor,
        customer = Some(customer),
        email = customer.email,
        amount = amount,
        currency = currency,
        status = TransactionStatus.PAYMENT_AUTHORIZED,
        None,
        None,
        "",
        None,
        paymentConfig,
        PaymentType.CREDIT_CARD,
        None,
        None,
        merchantConfirmation = true
    )
    boTransactionHandler.create(transaction)

    val shopTransaction = BOShopTransaction(UUID.randomUUID().toString,
      MogopayConstant.SHOP_MOGOBIZ,
      transaction.uuid,
      amount,
      currency,
      ShopTransactionStatus.AUTHORIZED,
      None,
      paymentConfig,
      extra,
      "",
      Nil,
      None)
    boShopTransactionHandler.create(shopTransaction)

    boTransactionLogHandler.save(
        BOTransactionLog(UUID.randomUUID().toString,
                         "OUT",
                         "m",
                         "SYSTEMPAY",
                         transaction.uuid,
                          shopTransaction.uuid,
                         TransactionShopStep.START_PAYMENT))
    boTransactionLogHandler.save(
        BOTransactionLog(UUID.randomUUID().toString,
                         "IN",
                         "o",
                         "SYSTEMPAY",
                         transaction.uuid,
          shopTransaction.uuid,
          TransactionShopStep.START_PAYMENT))
  }

  private def randomDate(): Date = {
    val current = System.currentTimeMillis()
    val diff    = new Random().nextInt(30) * 24 * 60 * 60 * 1000
    new Date(current - diff)
  }

  private def createCertification(merchant: Account) = {
    val content1 = """d6FDdyqW1EGZGNyXVK0VwiqsuxPhU1Q0yIPlXUaOqQIlCoLGOWqVrjL6e2neIIfJ31
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
    val parcomContent = """CANCEL_URL!http://!
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

    val parcomDefaultContent = """BGCOLOR!FFFFFF!
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
    val targetContent = s"""D_LOGO!${Settings.ImagesPath + "sips/logo/"}!
                                                        |F_DEFAULT!${new File(certifDir, "parcom.default").getAbsolutePath}!
                                                                                                                             |F_PARAM!${new File(
                               certifDir,
                               "parcom").getAbsolutePath}!
                                                                                                                                                                                        |F_CERTIFICATE!${new File(
                               certifDir,
                               "certif").getAbsolutePath}!
                                                                                                                                                                                                                                                         |F_CTYPE!jsp!
                                                                                                                                                                                                                                                         |""".stripMargin
    scala.tools.nsc.io.File(targetFile.getAbsolutePath).writeAll(targetContent)
  }

  private def getCertifDir(merchant: Account): File = {
    new File(Settings.Sips.CertifDir + "/" + merchant.uuid.toString)
  }
}

object DbInitMain extends App {
  try {
    import EsClient.secureActionRequest
    secureActionRequest(
        EsClient().client
          .prepareDeleteByQuery(Settings.Mogopay.EsIndex)
          .setQuery(new TermQueryBuilder("_type", "Account"))).execute.actionGet
    secureActionRequest(
        EsClient().client
          .prepareDeleteByQuery(Settings.Mogopay.EsIndex)
          .setQuery(new TermQueryBuilder("_type", "BOTransaction"))).execute.actionGet
    secureActionRequest(
        EsClient().client
          .prepareDeleteByQuery(Settings.Mogopay.EsIndex)
          .setQuery(new TermQueryBuilder("_type", "BOTransactionLog"))).execute.actionGet
    secureActionRequest(
        EsClient().client
          .prepareDeleteByQuery(Settings.Mogopay.EsIndex)
          .setQuery(new TermQueryBuilder("_type", "ESSession"))).execute.actionGet
    secureActionRequest(
        EsClient().client
          .prepareDeleteByQuery(Settings.Mogopay.EsIndex)
          .setQuery(new TermQueryBuilder("_type", "TransactionSequence"))).execute.actionGet
    secureActionRequest(
        EsClient().client
          .prepareDeleteByQuery(Settings.Mogopay.EsIndex)
          .setQuery(new TermQueryBuilder("_type", "TransactionRequest"))).execute.actionGet
  } catch {
    case NonFatal(_) => println()
  }
  DBInitializer(false)
}
