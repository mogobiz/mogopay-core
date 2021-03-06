/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.payment

object BankErrorCodes {
  def getErrorMessage(code: String): String = errorMessages.getOrElse(code, "")

  private val errorMessages: Map[String, String] = Map(
      "0"   -> "Transaction Approved",
      "1"   -> "Refer to Issuer",
      "2"   -> "Refer to Issuer, special",
      "3"   -> "No Merchant",
      "4"   -> "Pick Up Card",
      "5"   -> "Do Not Honour",
      "6"   -> "Error",
      "7"   -> "Pick Up Card, Special",
      "8"   -> "Honour With Identification",
      "9"   -> "Request In Progress",
      "10"  -> "Approved For Partial Amount",
      "11"  -> "Approved, VIP",
      "12"  -> "Invalid Transaction",
      "13"  -> "Invalid Amount",
      "14"  -> "Invalid Card Number",
      "15"  -> "No Issuer",
      "16"  -> "Approved, Update Track 3",
      "19"  -> "Re-enter Last Transaction",
      "21"  -> "No Action Taken",
      "22"  -> "Suspected Malfunction",
      "23"  -> "Unacceptable Transaction Fee",
      "25"  -> "Unable to Locate Record On File",
      "30"  -> "Format Error",
      "31"  -> "Bank Not Supported By Switch",
      "33"  -> "Expired Card, Capture",
      "34"  -> "Suspected Fraud, Retain Card",
      "35"  -> "Card Acceptor, Contact Acquirer, Retain Card",
      "36"  -> "Restricted Card, Retain Card",
      "37"  -> "Contact Acquirer Security Department, Retain Card",
      "38"  -> "PIN Tries Exceeded, Capture",
      "39"  -> "No Credit Account",
      "40"  -> "Function Not Supported",
      "41"  -> "Lost Card",
      "42"  -> "No Universal Account",
      "43"  -> "Stolen Card",
      "44"  -> "No Investment Account",
      "51"  -> "Insufficient Funds",
      "52"  -> "No Cheque Account",
      "53"  -> "No Savings Account",
      "54"  -> "Expired Card",
      "55"  -> "Incorrect PIN",
      "56"  -> "No Card Record",
      "57"  -> "Function Not Permitted to Cardholder",
      "58"  -> "Function Not Permitted to Terminal",
      "59"  -> "Suspected Fraud",
      "60"  -> "Acceptor Contact Acquirer",
      "61"  -> "Exceeds Withdrawal Limit",
      "62"  -> "Restricted Card",
      "63"  -> "Security Violation",
      "64"  -> "Original Amount Incorrect",
      "66"  -> "Acceptor Contact Acquirer, Security",
      "67"  -> "Capture Card",
      "75"  -> "PIN Tries Exceeded",
      "82"  -> "CVV Validation Error",
      "90"  -> "Cutoff In Progress",
      "91"  -> "Card Issuer Unavailable",
      "92"  -> "Unable To Route Transaction",
      "93"  -> "Cannot Complete, Violation Of The Law",
      "94"  -> "Duplicate Transaction",
      "96"  -> "System Error",
      "100" -> "No Shop Cart",
      "101" -> "Sales using multi Shop is not authorized",
      "102" -> "Error during payment redirection"
  )
}
