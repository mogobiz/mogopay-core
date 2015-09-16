# Group Account
## Check password pattern validity [/is-pattern-valid{?pattern}]

+ Parameters

    + pattern: - PERL Pattern


GET customer-token
GET merchant-token
GET already-exist-email	email	merchant_id.?	account_type
GET check-token-validity	token
GET is-valid-account-id id
GET update-password	password
GET generate-new-phone-code	
GET enroll	lphone	pin_code
GET generate-new-secret	
GET add-credit-card	card_id.?	holder	number.?	expiry_date	type
GET delete-credit-card	card_id
GET logout
GET billing-address	
GET shipping-addresses
GET shipping-address
GET profile-info	email
GET assign-billing-address	road city road2.? zip_code	extra.?	civility	firstname	lastname	company.?	country	admin1	admin2	lphone
GET delete-shipping-address	address_id
GET add-shipping-address	road city road2.? zip_code	extra.?	civility	firstname	lastname	company.?	country	admin1	admin2	lphone
GET update-shipping-address road city road2.? zip_code	extra.?	civility	firstname	lastname	company.?	country	admin1	admin2	lphone
GET active-country-state-shipping
GET list-compagnies
GET list-merchants
POST send-new-password	merchant_id	email locale.?
GET select-shipping-address	address_id
POST login email password merchant_id.? is_customer
GET confirm-signup	token
POST signup email password password2 lphone civility firstname lastname birthday road road2.? extra.? city zip_code admin1 admin2 country is_merchant merchant_id.? company.? website.? validation_url withShippingAddress locale.?
POST update-customer-profile password.? password2.? lphone civility firstname lastname birthday road road2.? city zip_code country admin1 admin2 vendor.? payline_account.? payline_key.? payline_contract.? payline_custom_payment_page_code.? payline_custom_payment_template_url.? paybox_site.? paybox_key.? paybox_rank.? paybox_merchant_id.? sips_merchant_id.? sips_merchant_country.? sips_merchant_certificate_file_name.? sips_merchant_certificate_file_content.? sips_merchant_parcom_file_name.? sips_merchant_parcom_file_content.? sips_merchant_logo_path.? systempay_shop_id.? systempay_contract_number.? systempay_certificate.? anet_api_login_id.? anet_transaction_key.? sender_name.? sender_email.? password_pattern.? callback_prefix.? paypal_user.? paypal_password.? paypal_signature.? apple_pay_anet_api_login_id.? apple_pay_anet_transaction_key.? kwixo_params.? group_payment_return_url_for_next_payers.? group_payment_success_url.? group_payment_failure_url.?
POST update-profile-light password password2 civility firstname lastname birthday



-backoffice
GET customers page max
GET transactions/$transactionId/logs
GET transactions email.? start_date.? start_time.? end_date.? end_time.? amount.? transaction_uuid.? transaction_status.? delivery_status.?
GET transaction/$uuid


-country
GET countries-for-shipping
GET country/$code
GET admins1/$countryCode
GET cities country.? parent_admin1_code.? parent_admin2_code.? name.?
GET admins2/$admin1
GET $countryCode/check-phone-number/$phone


-pdf
POST html-to-pdf page xhtml


-rate
GET list
GET format	amount	currency	country


-user
GET register callback_success callback_error merchant_id email password



-transaction
GET customer/$uuid
POST init merchant_secret transaction_amount return_url.? group_payment_exp_date.? group_payment_refund_percentage.?
POST list-shipping cartProvider cartKeys
POST select-shipping shipmentId rateId
GET verify merchant_secret transaction_amount.? transaction_id
GET init-group-payment token transaction_type card_cvv card_month card_year card_type card_number
GET refund merchant_secret amount bo_transaction_uuid
POST submit callback_success callback_error callback_cardinfo.? callback_auth.? callback_cvv.? transaction_id transaction_amount merchant_id transaction_type card_cvv.? card_number.? user_email.? user_password.? transaction_desc.? gateway_data.? card_month.? card_year.? card_type.? card_store.? payers.? group_tx_uuid.? locale.?
POST submit/$sessionUuid callback_success callback_error callback_cardinfo.? callback_auth.? callback_cvv.? transaction_id transaction_amount merchant_id transaction_type card_cvv.? card_number.? user_email.? user_password.? transaction_desc.? gateway_data.? card_month.? card_year.? card_type.? card_store.? payers.? group_tx_uuid.? locale.?
GET download/$transactionUuid page.? langCountry






