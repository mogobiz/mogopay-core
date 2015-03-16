package com.mogobiz.pay.model

/**
 * Created by yoannbaudy on 01/12/2014.
 */
object RequestParameters {

  case class TransactionInit(merchant_secret: String, transaction_amount: Long, currency_code: String, currency_rate: Double, extra: Option[String])

  case class ListShippingPriceRequest(currency_code: String, transaction_extra: String)

  case class SelectShippingPriceRequest(currency_code: String, transaction_extra: String, provider: String, service: String, rate_type: String)

  case class LoginRequest(email: String, password: String, merchant_id: Option[String], is_customer: Boolean)

  case class SignupRequest(email: String, password: String, password2: String,
                           lphone: String, civility: String, firstname: String,
                           lastname: String, birthday: String, road: String, road2: Option[String], extra: Option[String],
                           city: String, zip_code: String, admin1: String, admin2: String, country: String,
                           is_merchant: Boolean, merchant_id: Option[String], company: Option[String],
                           website: Option[String], validation_url: String, from_name: String, from_email: String)


  case class UpdateProfileRequest(password: Option[String],
                                  password2: Option[String],
                                  company: String,
                                  website: String,
                                  lphone: String,
                                  civility: String,
                                  firstname: String,
                                  lastname: String,
                                  birthday: String,
                                  road: String,
                                  road2: Option[String],
                                  city: String,
                                  zip_code: String,
                                  country: String,
                                  admin1: String,
                                  admin2: String,
                                  vendor: Option[String],
                                  payment_method: String,
                                  cb_provider: String,
                                  payline_account: Option[String],
                                  payline_key: Option[String],
                                  payline_contract: Option[String],
                                  payline_custom_payment_page_code: Option[String],
                                  payline_custom_payment_template_url: Option[String],
                                  paybox_site: Option[String],
                                  paybox_key: Option[String],
                                  paybox_rank: Option[String],
                                  paybox_merchant_id: Option[String],
                                  sips_merchant_id: Option[String],
                                  sips_merchant_country: Option[String],
                                  sips_merchant_certificate_file_name: Option[String],
                                  sips_merchant_certificate_file_content: Option[String],
                                  sips_merchant_parcom_file_name: Option[String],
                                  sips_merchant_parcom_file_content: Option[String],
                                  sips_merchant_logo_path: Option[String],
                                  systempay_shop_id: Option[String],
                                  systempay_contract_number: Option[String],
                                  systempay_certificate: Option[String],
                                  password_subject: Option[String],
                                  password_content: Option[String],
                                  password_pattern: Option[String],
                                  callback_prefix: Option[String],
                                  paypal_user: Option[String],
                                  paypal_password: Option[String],
                                  paypal_signature: Option[String],
                                  kwixo_params: Option[String],
                                  email_field: String,
                                  password_field: String)

  case class UpdateProfileLightRequest(password: String, password2: String, civility: String, firstname: String, lastname: String, birthday: String)

  case class Html2PdfRequest(page:String, xhtml:String)

  case class TransactionInitRequest(merchant_secret:String, transaction_amount:Long, currency_code:String, currency_rate:Double, extra:Option[String])
}

