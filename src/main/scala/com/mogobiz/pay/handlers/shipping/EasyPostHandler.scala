/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.shipping

import com.easypost.EasyPost
import com.easypost.model._
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.mogobiz.pay.codes.MogopayConstant
import com.mogobiz.pay.common.{Cart, CompanyAddress, ShippingWithQuantity, ShopCart}
import com.mogobiz.pay.config.MogopayHandlers.handlers._
import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.exceptions.Exceptions.ShippingException
import com.mogobiz.pay.model.{Rate => PayRate, _}
import com.typesafe.scalalogging.StrictLogging
import com.typesafe.scalalogging.Logger
import org.apache.commons.lang.StringUtils
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal

object EasyPostHandler {

  val EASYPOST_SHIPPING_PREFIX = "EASYPOST_"

}

class EasyPostHandler extends ShippingHandler {

  import EasyPostHandler._

  EasyPost.apiKey = Settings.Shipping.EasyPost.ApiKey

  override def computePrice(shippingAddress: ShippingAddress, cart: Cart): ShippingDataList = {
    cart.compagnyAddress.map { compagnyAddress =>
      cart.shopCarts.find(_.shopId == MogopayConstant.SHOP_MOGOBIZ).map { mogobizShop =>
        val shippingContent = extractShippingContent(mogobizShop)
        computeShippingParcelAndFixAmount(cart, shippingContent).map { parcelPrice =>
          val fixPrice = cart.shippingRulePrice.map { price =>
            Some(convertStorePrice(price, cart))
          }.getOrElse(if (parcelPrice.parcel.isEmpty) Some(parcelPrice.amount) else None)
          val amount = if (parcelPrice.parcel.isEmpty) 0 else parcelPrice.amount
          val parcel = parcelPrice.parcel.getOrElse(parcelPrice.fixParcel.get)

          Try(computeRate(compagnyAddress, shippingAddress, cart, mogobizShop, parcel, fixPrice, amount)) match {
            case Success(r) => r
            case Failure(ex) => {
              logger.info(ex.getMessage, ex)
              ShippingDataList(Some(ShippingPriceError.UNKNOWN), Nil)
            }
          }
        }.getOrElse(ShippingDataList(None, Nil))
      }.getOrElse(ShippingDataList(None, Nil))
    }.getOrElse(ShippingDataList(Some(ShippingPriceError.NO_COMPAGNY_ADDRESS), Nil))
  }

  protected def computeRate(compagnyAddress: CompanyAddress,
                            shippingAddress: ShippingAddress,
                            cart: Cart,
                            mogobizShop: ShopCart,
                            parcel: ShippingParcel,
                            fixPrice: Option[Long],
                            amount: Long): ShippingDataList = {
    val shipment = rate(
        compagnyAddress,
        shippingAddress.address,
        parcel,
        cart,
        mogobizShop
    )

    val easyPostRates = shipment.getRates.toList

    if (easyPostRates.size == 0) {
      val message = StringUtils.join(shipment.getMessages.toList.map { m =>
        m.getCarrier + " " + m.getType + " => " + m.getMessage
      }, ", ")
      logger.info("EasyPost Messages : " + message)
      ShippingDataList(Some(ShippingPriceError.SHIPPING_TYPE_NOT_ALLOWED), Nil)
    }
    else {
      ShippingDataList(None, easyPostRates.map { easyPostRate =>
        val rate: Option[PayRate] = rateHandler.findByCurrencyCode(cart.rate.code)
        val currencyFractionDigits: Integer = rate.map {
          _.currencyFractionDigits
        }.getOrElse(2)
        val price = easyPostRate.getRate * Math.pow(10, currencyFractionDigits.doubleValue())
        val finalPrice = fixPrice.getOrElse(
          amount + rateHandler.convert(price.toLong, easyPostRate.getCurrency, cart.rate.code).getOrElse(price.toLong))

        createShippingData(shippingAddress.address,
          EASYPOST_SHIPPING_PREFIX + easyPostRate.getShipmentId,
          easyPostRate.getId,
          easyPostRate.getCarrier,
          easyPostRate.getService,
          easyPostRate.getServiceCode,
          finalPrice,
          cart.rate.code)
      })
    }
  }

  override def isValidShipmentId(shippingPrice: ShippingData): Boolean =
    shippingPrice.shipmentId.startsWith(EASYPOST_SHIPPING_PREFIX)

  override def confirmShipmentId(shippingData: ShippingData): ShippingData = {
    if (shippingData.confirm) shippingData
    else {
      val shipment = Shipment.retrieve(shippingData.shipmentId.substring(EASYPOST_SHIPPING_PREFIX.length))
      val rate     = Rate.retrieve(shippingData.rateId)
      val s        = shipment.buy(rate)
      shippingData.copy(confirm = true, trackingCode = Some(s.getTrackingCode))
    }
  }

  protected def computeShippingParcelAndFixAmount(
      cart: Cart,
      shippingList: List[ShippingWithQuantity]): Option[ShippingParcelAndFixAmount] = {
    if (shippingList.isEmpty) None
    else {
      val parcelPriceTail =
        computeShippingParcelAndFixAmount(cart, shippingList.tail).getOrElse(ShippingParcelAndFixAmount(0, None, None))
      val quantity = shippingList.head.quantity
      val shipping = shippingList.head.shipping

      val parcelTail = parcelPriceTail.parcel
      val height = convertLinear(shipping.height, shipping.linearUnit) * quantity + parcelTail.map {
        _.height
      }.getOrElse(0.0)
      val width = Math.max(convertLinear(shipping.width, shipping.linearUnit), parcelTail.map {
        _.width
      }.getOrElse(0.0))
      val length = Math.max(convertLinear(shipping.depth, shipping.linearUnit), parcelTail.map {
        _.length
      }.getOrElse(0.0))
      val weight = convertWeight(shipping.weight, shipping.weightUnit) * quantity + parcelTail.map {
        _.weight
      }.getOrElse(0.0)
      val parcel = ShippingParcel(height, width, length, weight)

      if (shipping.free)
        Some(
            ShippingParcelAndFixAmount(parcelPriceTail.amount,
                                       Some(parcelPriceTail.fixParcel.getOrElse(parcel)),
                                       parcelPriceTail.parcel))
      else if (shipping.amount > 0)
        Some(
            ShippingParcelAndFixAmount(parcelPriceTail.amount + convertStorePrice(shipping.amount, cart) * quantity,
                                       Some(parcelPriceTail.fixParcel.getOrElse(parcel)),
                                       parcelPriceTail.parcel))
      else Some(ShippingParcelAndFixAmount(parcelPriceTail.amount, parcelPriceTail.fixParcel, Some(parcel)))
    }
  }

  /**
    * convert the linear value into IN
    * 1 in = 2.54 cm
    *
    * @param linear
    * @param unit
    * @return
    */
  protected def convertLinear(linear: Long, unit: String): Double = {
    unit match {
      case "CM" => linear / 2.54d
      case _    => linear
    }
  }

  /**
    * convert te weight into OZ
    * 1 OZ = 28.3495231 g
    * 1 OZ = 28.3495231/1000 kg
    * 1 OZ = 16.000 lb
    *
    * @param weight
    * @param unit
    * @return
    */
  protected def convertWeight(weight: Long, unit: String): Double = {
    unit match {
      case "KG" => weight * 1000 / 28.3495231d
      case "G"  => weight / 28.3495231d
      case "LB" => weight / 16.0d
      case _    => weight
    }
  }

  case class ShippingParcelAndFixAmount(amount: Long,
                                        fixParcel: Option[ShippingParcel],
                                        parcel: Option[ShippingParcel])

  def rate(from: CompanyAddress, to: AccountAddress, parcel: ShippingParcel, cart: Cart, shopCart: ShopCart): Shipment = {
    val parcelMap = mutable.HashMap[String, AnyRef]("height" -> parcel.height.asInstanceOf[AnyRef],
                                                    "width"  -> parcel.width.asInstanceOf[AnyRef],
                                                    "length" -> parcel.length.asInstanceOf[AnyRef],
                                                    "weight" -> parcel.weight.asInstanceOf[AnyRef])

    val parc = Parcel.create(parcelMap)

    val shipmentMap = mutable.HashMap[String, AnyRef]("from_address" -> companyAddressToMap(from),
                                                      "to_address"   -> accountAddressToMap(to),
                                                      "parcel"       -> parc,
                                                      "reference"    -> shopCart.cartItems(0).id,
                                                      "customs_info" -> cartToMap(cart, shopCart))

    if (Settings.Shipping.EasyPost.UpsCostCenter.length > 0)
      shipmentMap
        .put("options", mutable.HashMap[String, AnyRef]("cost_center" -> Settings.Shipping.EasyPost.UpsCostCenter))

    Shipment.create(shipmentMap)
  }

  protected def cartToMap(cart: Cart, shopCart: ShopCart): CustomsInfo = {
    val originCountry = cart.compagnyAddress.map {
      _.country
    }.getOrElse("US")
    val rate: Option[PayRate] = rateHandler.findByCurrencyCode(cart.rate.code)
    val currencyFractionDigits: Integer = rate.map {
      _.currencyFractionDigits
    }.getOrElse(2)

    val customsItemsList: java.util.List[AnyRef] = shopCart.cartItems.flatMap { cartItem =>
      cartItem.shipping.map { shipping =>
        val price     = cartItem.saleTotalEndPrice
        val p: Double = Math.pow(10, currencyFractionDigits.doubleValue());
        val priceUSD  = rateHandler.convert(price, cart.rate.code, "USD").getOrElse(price) / p

        val map = mutable.HashMap[String, AnyRef](
            "description"    -> cartItem.name,
            "quantity"       -> cartItem.quantity.asInstanceOf[AnyRef],
            "value"          -> priceUSD.asInstanceOf[AnyRef],
            "weight"         -> convertWeight(shipping.weight, shipping.weightUnit).asInstanceOf[AnyRef],
            "origin_country" -> originCountry
        )
        CustomsItem.create(map)
      }
    }.toList

    //eel_pfc 	string 	EEL or PFC
    //value less than $2500: "NOEEI 30.37(a)"; value greater than $2500: see Customs Guide
    //customs_items 	array 	CustomsItems included

    val map: java.util.Map[String, AnyRef] = mutable.HashMap[String, AnyRef](
        "customs_certify" -> false.asInstanceOf[AnyRef],
        "contents_type"   -> "merchandise",
        "eel_pfc"         -> "NOEEI 30.37(a)",
        "customs_items"   -> customsItemsList
    )

    CustomsInfo.create(map)

  }

  protected def accountAddressToMap(addr: AccountAddress): Address = {
    val country = addr.country.getOrElse("US")
    val state   = addr.admin1.getOrElse("US.CA")
    val stateName = countryAdminHandler
      .getAdmin1ByCode(country, state)
      .map { admin1: CountryAdmin =>
        admin1.name.getOrElse(state)
      }
      .getOrElse(state)
    val fromAddressMap: java.util.Map[String, AnyRef] = mutable
      .HashMap[String, String]("name" -> (addr.civility.map {
                                     _.toString + " "
                                   }.getOrElse("") + addr.lastName.getOrElse("") + " " + addr.firstName.getOrElse("")),
                               "company" -> addr.company.getOrElse(""),
                               "street1" -> addr.road,
                               "street2" -> addr.road2.getOrElse(""),
                               "city"    -> addr.city,
                               "state"   -> stateName,
                               "country" -> country,
                               "zip"     -> addr.zipCode.getOrElse(""),
                               "phone"   -> formatPhone(addr.telephone.map(_.lphone).getOrElse("")))
      .filter(_._2.length > 0)

    Address.create(fromAddressMap)
  }

  lazy val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

  protected def formatPhone(phone: String): String = {
    try {
      phoneUtil.format(phoneUtil.parse(phone, null), PhoneNumberFormat.NATIONAL) //.replaceAll("\\s","")
    } catch {
      case NonFatal(e) =>
        phone
    }
  }

  protected def companyAddressToMap(addr: CompanyAddress): Address = {
    val fromAddressMap: java.util.Map[String, AnyRef] = mutable
      .HashMap[String, String]("company" -> addr.company,
                               "name"    -> addr.company,
                               "street1" -> addr.road,
                               "street2" -> addr.road2,
                               "city"    -> addr.city,
                               "state"   -> addr.state.getOrElse("CA"),
                               "country" -> addr.country,
                               "zip"     -> addr.zipCode,
                               "phone"   -> formatPhone(addr.phone.getOrElse("")))
      .filter(_._2.length > 0)

    Address.create(fromAddressMap)
  }
}

object EasyPostHandlerTest extends App {
  val logger = LoggerFactory.getLogger("com.mogobiz.pay.handlers.shipping.EasyPostHandler")
  EasyPost.apiKey = "ueG20zkjZWwNjUszp1Pr2w"

  val customsItemMap: java.util.Map[String, AnyRef] = mutable.HashMap[String, AnyRef](
      "description"      -> "T-shirt",
      "quantity"         -> 1.asInstanceOf[AnyRef],
      "value"            -> 11.asInstanceOf[AnyRef],
      "weight"           -> 6.asInstanceOf[AnyRef],
      "origin_country"   -> "US",
      "hs_tariff_number" -> "610910")

  val customsItem1 = CustomsItem.create(customsItemMap);

  var customsItemsList: java.util.List[AnyRef] = Array(customsItem1).toList;

  val customsInfoMap: java.util.Map[String, AnyRef] = mutable.HashMap[String, AnyRef](
      "customs_certify" -> false.asInstanceOf[AnyRef],
      //"customs_signer" -> "Jarrett Streebin",
      "contents_type" -> "gift",
      "eel_pfc"       -> "NOEEI 30.37(a)",
      "customs_items" -> customsItemsList)

  val customsInfo = CustomsInfo.create(customsInfoMap);

  val fromAddressMap: java.util.Map[String, AnyRef] = mutable.HashMap[String, AnyRef]("name" -> "acmesports",
                                                                                      "company" -> "acmesports",
                                                                                      "street1" -> "179 N Harbor Dr",
                                                                                      "city"    -> "Redondo Beach",
                                                                                      "country" -> "US",
                                                                                      "state"   -> "CA",
                                                                                      "zip"     -> "90277",
                                                                                      "phone"   -> "(248) 123-7654");
  val fromAddress = Address.create(fromAddressMap)

  val toAddressMap: java.util.Map[String, AnyRef] = mutable.HashMap[String, AnyRef](
      "name"    -> "Yoann Baudy",
      "company" -> "acmesports",
      "street1" -> "14 rue de la récré",
      "city"    -> "Saint Christophe des Bois",
      "country" -> "FR",
      "zip"     -> "35210")
  val toAddress = Address.create(toAddressMap)

  val parcelMap: java.util.Map[String, AnyRef] = mutable.HashMap[String, AnyRef]("height" -> 3.asInstanceOf[AnyRef],
                                                                                 "width"  -> 6.asInstanceOf[AnyRef],
                                                                                 "length" -> 9.asInstanceOf[AnyRef],
                                                                                 "weight" -> 20.asInstanceOf[AnyRef])

  val shipmentMap: java.util.Map[String, AnyRef] = mutable.HashMap[String, AnyRef](
      "to_address"   -> toAddress,
      "from_address" -> fromAddress,
      "parcel"       -> Parcel.create(parcelMap),
      "customs_info" -> customsInfo)

  val shipment = Shipment.create(shipmentMap)

  val rate: java.util.Map[String, AnyRef] = mutable.HashMap[String, AnyRef]("id" -> shipment.getRates.get(0).getId())
  val newShipment                         = Shipment.retrieve(shipment.getId).buy(Rate.retrieve(shipment.getRates.get(0).getId()))
  logger.debug(newShipment.getStatus)
}
