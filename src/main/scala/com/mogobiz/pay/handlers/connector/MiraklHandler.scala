/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */
package com.mogobiz.pay.handlers.connector

import com.mogobiz.mirakl.MiraklClient
import com.mogobiz.mirakl.PaymentModel._
import com.mogobiz.pay.config.MogopayHandlers.handlers.transactionHandler
import com.mogobiz.pay.config.MogopayHandlers.handlers.boShopTransactionHandler
import com.mogobiz.pay.config.MogopayHandlers.handlers.rateHandler
import com.mogobiz.pay.model
import com.typesafe.scalalogging.StrictLogging

class MiraklHandler extends StrictLogging {

  def debitCustomer(orders: DebitOrderList) : Boolean = {
    val miraklOrders = orders.order.filterNot{ order : DebitOrder =>
      "Testing debit connector. Please ignore.".equals(order.customer_id) ||
        order.amount.isEmpty ||
        order.order_id.isEmpty ||
        order.order_lines.isEmpty
    }.map { order : DebitOrder =>
      val paymentResult = order.shop_id.map { shopId =>
        order.order_commercial_id.map { transactionId =>
          boShopTransactionHandler.findByShopIdAndTransactionUuid(shopId, transactionId).map { shopTransaction =>
            order.amount.map { amount =>
              order.currency_iso_code.map { currencyCode =>
                rateHandler.findByCurrencyCode(currencyCode).map { currency =>
                  Some(transactionHandler.validatePayment(shopTransaction))
                }.getOrElse {
                  logger.error(s"Currency with code $currencyCode is not found")
                  None
                }
              }.getOrElse {
                logger.error(s"Currency code not provided")
                None
              }
            }.getOrElse {
              logger.error(s"Amount not provided")
              None
            }
          }.getOrElse {
            logger.error(s"Transaction $transactionId not found")
            None
          }
        }.getOrElse {
          logger.error(s"Order Commercial Id not provided")
          None
        }
      }.getOrElse {
        logger.error(s"Shop Id not provided")
        None
      }
      val paymentStatus = paymentResult.map { pr =>
        if (pr.status == model.PaymentStatus.COMPLETE) PaymentStatus.OK
        else PaymentStatus.REFUSED
      }.getOrElse(PaymentStatus.REFUSED)

      new OrderPayment(order.order_id.get,
        order.customer_id,
        paymentStatus,
        order.amount,
        order.currency_iso_code,
        paymentResult.flatMap {_.transactionDate},
        paymentResult.flatMap {_.transactionId})
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
    }.flatMap { order : RefundOrder =>
      order.order_lines.map { orderLines =>
        orderLines.order_line.map { orderLigne =>
          orderLigne.refunds.map { refunds =>
            refunds.refund.map { refund =>
              val paymentResult = order.shop_id.map { shopId =>
                order.order_commercial_id.map { transactionId =>
                  boShopTransactionHandler.findByShopIdAndTransactionUuid(shopId, transactionId).map { shopTransaction =>
                    order.currency_iso_code.map { currencyCode =>
                      rateHandler.findByCurrencyCode(currencyCode).map { currency =>
                        Some(transactionHandler.refundPayment(shopTransaction))
                      }.getOrElse {
                        logger.error(s"Currency with code $currencyCode is not found")
                        None
                      }
                    }.getOrElse {
                      logger.error(s"Currency code not provided")
                      None
                    }
                  }.getOrElse {
                    logger.error(s"Transaction $transactionId not found")
                    None
                  }
                }.getOrElse {
                  logger.error(s"Order Commercial Id not provided")
                  None
                }
              }.getOrElse {
                logger.error(s"Shop Id not provided")
                None
              }
              val paymentStatus = paymentResult.map { pr =>
                if (pr.status == model.PaymentStatus.COMPLETE) PaymentStatus.OK
                else PaymentStatus.REFUSED
              }.getOrElse(PaymentStatus.REFUSED)

              new Refund(Some(refund.amount),
                order.currency_iso_code,
                paymentStatus,
                refund.id,
                paymentResult.flatMap {_.transactionDate},
                paymentResult.flatMap {_.transactionId})
            }
          }.getOrElse(Nil)
        }
      }.getOrElse(Nil).flatten
    }

    if (miraklRefund.isEmpty) true
    else return MiraklClient.confirmRefund(new RefundedOrderLinesBean(miraklRefund))
  }

}