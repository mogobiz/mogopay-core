/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */
package com.mogobiz.pay.handlers.connector

import com.mogobiz.mirakl.MiraklClient
import com.mogobiz.mirakl.PaymentModel._

import scala.util.{Failure, Success, Try}

class MiraklHandler {

  def debitCustomer(orders: DebitOrderList) : Boolean = {
    val miraklOrders = orders.order.filterNot{ order : DebitOrder =>
      "Testing debit connector. Please ignore.".equals(order.customer_id) ||
        order.amount.isEmpty ||
        order.order_id.isEmpty ||
        order.order_lines.isEmpty
    }.map { order : DebitOrder =>
      val paymentResult = Try(
        //TODO faire l'appel à la méthode débit du paiement
        true
      )
      val paymentStatus = paymentResult match {
        case Success(_) => {
          // TODO Extract the payment result from the debit result
          PaymentStatus.OK
        }
        case Failure(_) => {
          PaymentStatus.REFUSED
        }
      }

      new OrderPayment(order.order_id.get,
        order.customer_id,
        paymentStatus,
        order.amount,
        order.currency_iso_code,
        None, //TODO récupérer le transactionDate du résultat du paiement
        None) //TODO récupérer le transactionNumber du résultat du paiement
    }

    if (miraklOrders.isEmpty) true
    else return MiraklClient.confimDebit(new OrderPaymentsDto(miraklOrders))
  }

  def refundCustomer(orders: RefundOrderList) : Boolean = {
    val miraklRefund = orders.order.filterNot{ order : RefundOrder =>
      "Testing refund connector. Please ignore.".equals(order.customer_id) ||
        order.amount.isEmpty ||
        order.order_id.isEmpty ||
        order.order_lines.isEmpty
    }.map { order : RefundOrder =>
      order.order_lines.map { orderLines =>
        orderLines.order_line.map { orderLigne =>
          orderLigne.refunds.map { refunds =>
            refunds.refund.map { refund =>
              val paymentResult = Try(
                //TODO faire l'appel à la méthode refund du paiement
                true
              )
              val paymentStatus = paymentResult match {
                case Success(_) => {
                  // TODO Extract the payment result from the refund result
                  PaymentStatus.OK
                }
                case Failure(_) => {
                  PaymentStatus.REFUSED
                }
              }

              new Refund(Some(refund.amount),
                order.currency_iso_code,
                paymentStatus,
                refund.id,
                None, //TODO récupérer le transactionDate du résultat du paiement
                None) //TODO récupérer le transactionNumber du résultat du paiement
            }
          }.getOrElse(Nil)
        }
      }.getOrElse(Nil).flatten
    }.flatten

    if (miraklRefund.isEmpty) true
    else return MiraklClient.confirmRefund(new RefundedOrderLinesBean(miraklRefund))
  }

}