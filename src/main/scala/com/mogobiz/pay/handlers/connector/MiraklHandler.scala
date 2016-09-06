package com.mogobiz.pay.handlers.connector

import akka.actor.Actor
import com.mogobiz.mirakl.MiraklClient
import com.mogobiz.mirakl.PaymentModel._

import scala.util.{Failure, Success, Try}

/**
 * Created by yoannbaudy on 01/09/16.
 */
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
        Some(order.amount.get),
        None,
        None,
        None)
    }

    if (miraklOrders.isEmpty) true
    else return MiraklClient.confimDebit(new OrderPaymentsDto(miraklOrders))
  }

}