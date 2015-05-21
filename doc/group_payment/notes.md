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
