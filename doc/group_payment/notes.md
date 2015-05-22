* the /init call should include a `group_payment_exp_date` parameter that
    contains the timestamp of the payment's expiration date.
* the /init call *can* specify a `group_payment_refund_percentage` parameter.
    If one of the customers doesn't pay before the expiration date, all the
    payers will be refunded of the percentage of amount they've paid.
    Defaults to 100 if not specified in the /init call.
* the /submit call must include a list of payers in this format:
    `email1:amount1,email2:amount2,...,emailN:amountN`.
* /verify returns, among other things, `group_transactions` which is a list of
    `TransactionRequest`s. It contains as many items as customers who
    didn't pay yet: the list is empty if all the customers paid their part.
* When a user visits the merchant's website with a group payment token, the
    merchant should query for their credit card information (e.g. through a
    HTML form), then redirect them to /init-group-payment (after submiting the
    same form e.g.) with the token and transaction_type (both come from the
    user), as well as card_cvv, card_month, card_year, card_type and
    card_number.
