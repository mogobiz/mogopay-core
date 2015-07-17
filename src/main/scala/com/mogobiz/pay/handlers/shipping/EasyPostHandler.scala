/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.shipping

import com.easypost.EasyPost
import com.easypost.model._
import com.mogobiz.json.JacksonConverter
import com.mogobiz.pay.common.{CompanyAddress, Shipping, Cart}
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.model.Mogopay.{Rate => PayRate, _}
import org.json4s._
import scala.collection.JavaConversions._
import scala.collection.mutable

class EasyPostHandler extends ShippingService {

  val EASYPOST_SHIPPING_PREFIX = "EASYPOST_"

  EasyPost.apiKey = "ueG20zkjZWwNjUszp1Pr2w"

  override def calculatePrice(shippingAddress: ShippingAddress, cart: Cart): Seq[ShippingPrice] = {
    cart.compagnyAddress.map { compagnyAddress =>
      val shippingContent = extractShippingContent(cart)
      val parcelAndFixAmountTail = computeShippingParcelAndFixAmount(shippingContent)

      parcelAndFixAmountTail.parcel.map { shippingParcel =>
        val easyPostRates = rate(
          compagnyAddress,
          shippingAddress.address,
          shippingParcel,
          cart
        )

        easyPostRates.map {easyPostRate =>
          var rate : Option[PayRate] = rateHandler.findByCurrencyCode(cart.rate.code)
          val currencyFractionDigits : Integer = rate.map { _.currencyFractionDigits }.getOrElse(2)
          val price = easyPostRate.getRate * Math.pow(10, currencyFractionDigits.doubleValue())
          val finalPrice = parcelAndFixAmountTail.amount + rateHandler.convert(price.toLong, easyPostRate.getCurrency, cart.rate.code).getOrElse(price.toLong)

          createShippingPrice(EASYPOST_SHIPPING_PREFIX + easyPostRate.getShipmentId, easyPostRate.getId, easyPostRate.getCarrier, easyPostRate.getService, easyPostRate.getServiceCode, finalPrice, cart.rate.code)
        }
      }.getOrElse(Seq())
    }.getOrElse(Seq())
  }

  override def isManageShipmentId(shippingPrice: ShippingPrice): Boolean = shippingPrice.shipmentId.startsWith(EASYPOST_SHIPPING_PREFIX)

  override def confirmShipmentId(shippingPrice: ShippingPrice): Long = {
    val shipment = Shipment.retrieve(shippingPrice.shipmentId.substring(EASYPOST_SHIPPING_PREFIX.length))
    val rate = Rate.retrieve(shippingPrice.rateId)
    val s = shipment.buy(rate)
    shippingPrice.price
  }

  private def computeShippingParcelAndFixAmount(shippingList: List[Shipping]) : ShippingParcelAndFixAmount = {
    if (shippingList.isEmpty) ShippingParcelAndFixAmount(0, None)
    else {
      val parcelAndFixAmountTail = computeShippingParcelAndFixAmount(shippingList.tail)
      val shipping = shippingList.head;
      if (shipping.free) parcelAndFixAmountTail
      else if (shipping.amount > 0) ShippingParcelAndFixAmount(parcelAndFixAmountTail.amount + shipping.amount, parcelAndFixAmountTail.parcel)
      else {
        val parcelTail = parcelAndFixAmountTail.parcel
        val height = Math.max(convertLinear(shipping.height, shipping.linearUnit), parcelTail.map{_.height}.getOrElse(0.0))
        val width = Math.max(convertLinear(shipping.width, shipping.linearUnit), parcelTail.map{_.width}.getOrElse(0.0))
        val length = Math.max(convertLinear(shipping.depth, shipping.linearUnit), parcelTail.map{_.length}.getOrElse(0.0))
        val weight = Math.max(convertWeight(shipping.weight, shipping.weightUnit), parcelTail.map{_.weight}.getOrElse(0.0))
        ShippingParcelAndFixAmount(parcelAndFixAmountTail.amount, Some(ShippingParcel(height, width, length, weight)))
      }
    }
  }

  /**
   * convert the linear value into IN
   * 1 in = 2.54 cm
   * @param linear
   * @param unit
   * @return
   */
  private def convertLinear(linear: Long, unit: String) : Double = {
    unit match {
      case "CM" => linear / 2.54d
      case _ => linear
    }
  }

  /**
   * convert te weight into OZ
   * 1 OZ = 28.3495231 g
   * 1 OZ = 28.3495231/1000 kg
   * 1 OZ = 16.000 lb
   * @param weight
   * @param unit
   * @return
   */
  private def convertWeight(weight: Long, unit: String) : Double = {
    unit match {
      case "KG" => weight * 1000 / 28.3495231d
      case "G" => weight / 28.3495231d
      case "LB" => weight / 16.0d
      case _ => weight
    }
  }

  case class ShippingParcelAndFixAmount(amount: Long, parcel: Option[ShippingParcel])

  def rate(from: CompanyAddress, to: AccountAddress, parcel: ShippingParcel, cart: Cart): Seq[Rate] = {
    val parcelMap = mutable.HashMap[String, AnyRef](
      "height" -> parcel.height.asInstanceOf[AnyRef],
      "width" -> parcel.width.asInstanceOf[AnyRef],
      "length" -> parcel.length.asInstanceOf[AnyRef],
      "weight" -> parcel.weight.asInstanceOf[AnyRef])

    val parc = Parcel.create(parcelMap)

    val shipmentMap = mutable.HashMap[String, AnyRef](
      "from_address" -> companyAddressToMap(from),
      "to_address" -> accountAddressToMap(to),
      "parcel" -> parc,
      "customs_info" -> cartToMap(cart))

    val shipment = Shipment.create(shipmentMap)
    shipment.getRates
  }

  private def cartToMap(cart: Cart): CustomsInfo = {
    val originCountry = cart.compagnyAddress.map { _.country }.getOrElse("US")
    val rate : Option[PayRate] = rateHandler.findByCurrencyCode(cart.rate.code)
    val currencyFractionDigits : Integer = rate.map { _.currencyFractionDigits }.getOrElse(2)

    val customsItemsList : java.util.List[AnyRef] = (cart.cartItems.map { cartItem =>
      cartItem.shipping.map { shipping =>
        val price = cartItem.saleTotalEndPrice
        val p : Double = Math.pow(10, currencyFractionDigits.doubleValue());
        val priceUSD = rateHandler.convert(price, cart.rate.code, "USD").getOrElse(price) / p

        val map = mutable.HashMap[String, AnyRef](
          "description" -> cartItem.name,
          "quantity" -> cartItem.quantity.asInstanceOf[AnyRef],
          "value" -> priceUSD.asInstanceOf[AnyRef],
          "weight" -> convertWeight(shipping.weight, shipping.weightUnit).asInstanceOf[AnyRef],
          "origin_country" -> originCountry
        )
        CustomsItem.create(map)
      }
    }.flatten).toList

    //eel_pfc 	string 	EEL or PFC
//value less than $2500: "NOEEI 30.37(a)"; value greater than $2500: see Customs Guide
//customs_items 	array 	CustomsItems included

    val map: java.util.Map[String, AnyRef] = mutable.HashMap[String, AnyRef](
      "customs_certify" -> false.asInstanceOf[AnyRef],
      "contents_type" -> "merchandise",
      "eel_pfc" -> "NOEEI 30.37(a)",
      "customs_items" -> customsItemsList
    )

    CustomsInfo.create(map)

  }

  private def accountAddressToMap(addr: AccountAddress): Address = {
    val fromAddressMap: java.util.Map[String, AnyRef] = mutable.HashMap[String, String](
      "name" -> (addr.civility.map {_.toString + " "}.getOrElse("") + addr.lastName + " " + addr.firstName),
      "company" -> addr.company.getOrElse(""),
      "street1" -> addr.road,
      "street2" -> addr.road2.getOrElse(""),
      "city" -> addr.city,
      "state" -> addr.admin1.getOrElse("US"),
      "country" -> addr.country.getOrElse("US"),
      "zip" -> addr.zipCode.getOrElse(""),
      "phone" -> addr.telephone.map(_.lphone).getOrElse("")).filter(_._2.length > 0)

    Address.create(fromAddressMap)
  }


  private def companyAddressToMap(addr: CompanyAddress): Address = {
    val fromAddressMap: java.util.Map[String, AnyRef] = mutable.HashMap[String, String](
      "company" -> addr.company,
      "name" -> addr.company,
      "street1" -> addr.road,
      "street2" -> addr.road2,
      "city" -> addr.city,
      "state" -> addr.state.getOrElse("CA"),
      "country" -> addr.country,
      "zip" -> addr.zipCode,
      "phone" -> addr.phone.getOrElse("")).filter(_._2.length > 0)

    Address.create(fromAddressMap)
  }
}

object EasyPostHandler extends App {

    EasyPost.apiKey = "ueG20zkjZWwNjUszp1Pr2w"

    val customsItemMap: java.util.Map[String, AnyRef] = mutable.HashMap[String, AnyRef](
    "description" -> "T-shirt",
    "quantity" -> 1.asInstanceOf[AnyRef],
    "value" -> 11.asInstanceOf[AnyRef],
    "weight" -> 6.asInstanceOf[AnyRef],
    "origin_country" -> "US",
    "hs_tariff_number" -> "610910")

    val customsItem1 = CustomsItem.create(customsItemMap);

    var customsItemsList : java.util.List[AnyRef] = Array(customsItem1).toList;

    val customsInfoMap: java.util.Map[String, AnyRef] = mutable.HashMap[String, AnyRef](
    "customs_certify" -> false.asInstanceOf[AnyRef],
    //"customs_signer" -> "Jarrett Streebin",
    "contents_type" -> "gift",
    "eel_pfc" -> "NOEEI 30.37(a)",
    "customs_items" -> customsItemsList)

    val customsInfo = CustomsInfo.create(customsInfoMap);

    val fromAddressMap: java.util.Map[String, AnyRef] = mutable.HashMap[String, AnyRef](
    "name" -> "acmesports",
    "company" -> "acmesports",
      "street1" -> "179 N Harbor Dr",
      "city" -> "Redondo Beach",
      "country" -> "US",
      "state" -> "CA",
      "zip" -> "90277",
      "phone" -> "(248) 123-7654");
  val fromAddress = Address.create(fromAddressMap)

  val toAddressMap: java.util.Map[String, AnyRef] = mutable.HashMap[String, AnyRef](
    "name" -> "Yoann Baudy",
    "company" -> "acmesports",
    "street1" -> "14 rue de la récré",
    "city" -> "Saint Christophe des Bois",
    "country" -> "FR",
    "zip" -> "35210");
  val toAddress = Address.create(toAddressMap)

  val parcelMap: java.util.Map[String, AnyRef] = mutable.HashMap[String, AnyRef](
    "height" -> 3.asInstanceOf[AnyRef],
    "width" -> 6.asInstanceOf[AnyRef],
    "length" -> 9.asInstanceOf[AnyRef],
    "weight" -> 20.asInstanceOf[AnyRef]);

    val shipmentMap: java.util.Map[String, AnyRef] = mutable.HashMap[String, AnyRef](
    "to_address" -> toAddress,
    "from_address" -> fromAddress,
    "parcel" -> Parcel.create(parcelMap),
    "customs_info" -> customsInfo);

    val shipment = Shipment.create(shipmentMap);

    val rate: java.util.Map[String, AnyRef] = mutable.HashMap[String, AnyRef](
    "id" -> shipment.getRates.get(0).getId())
    val newShipment = Shipment.retrieve(shipment.getId).buy(Rate.retrieve(shipment.getRates.get(0).getId()));
  println(newShipment.getStatus)
}
