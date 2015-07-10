package com.mogobiz.pay.handlers.shipping

import com.easypost.EasyPost
import com.easypost.model.{Shipment, Parcel, Address, Rate}
import com.mogobiz.json.JacksonConverter
import com.mogobiz.pay.common.{CompanyAddress, Shipping, Cart}
import com.mogobiz.pay.config.MogopayHandlers._
import com.mogobiz.pay.model.Mogopay.{Rate => PayRate, ShippingAddress, AccountAddress, ShippingParcel}
import org.json4s._
import scala.collection.JavaConversions._
import scala.collection.mutable

class EasyPostHandler extends ShippingService {
  EasyPost.apiKey = "ueG20zkjZWwNjUszp1Pr2w"

  override def calculatePrice(shippingAddress: ShippingAddress, cart: Cart): Seq[ShippingPrice] = {
    cart.compagnyAddress.map { compagnyAddress =>
      val shippingContent = extractShippingContent(cart)
      val parcelAndFixAmountTail = computeShippingParcelAndFixAmount(shippingContent)

      parcelAndFixAmountTail.parcel.map { shippingParcel =>
        val easyPostRates = rate(
          compagnyAddress,
          shippingAddress.address,
          shippingParcel
        )

        easyPostRates.map {easyPostRate =>
          var rate : Option[PayRate] = rateHandler.findByCurrencyCode(cart.rate.code)
          val currencyFractionDigits : Integer = rate.map { _.currencyFractionDigits }.getOrElse(2)
          val price = easyPostRate.getRate * Math.pow(10, currencyFractionDigits.doubleValue())
          val finalPrice = parcelAndFixAmountTail.amount + rateHandler.convert(price.toLong, easyPostRate.getCurrency, cart.rate.code).getOrElse(price.toLong)

          createShippingPrice(easyPostRate.getShipmentId, easyPostRate.getId, easyPostRate.getCarrier, easyPostRate.getService, easyPostRate.getServiceCode, finalPrice, cart.rate.code)
        }
      }.getOrElse(Seq())
    }.getOrElse(Seq())
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

  def rate(from: CompanyAddress, to: AccountAddress, parcel: ShippingParcel): Seq[Rate] = {
    val parcelMap = mutable.HashMap[String, AnyRef](
      "height" -> parcel.height.asInstanceOf[AnyRef],
      "width" -> parcel.width.asInstanceOf[AnyRef],
      "length" -> parcel.length.asInstanceOf[AnyRef],
      "weight" -> parcel.weight.asInstanceOf[AnyRef])

    val parc = Parcel.create(parcelMap)

    val shipmentMap = mutable.HashMap[String, AnyRef](
      "from_address" -> companyAddressToMap(from),
      "to_address" -> accountAddressToMap(to),
      "parcel" -> parc)

    val shipment = Shipment.create(shipmentMap)
    shipment.getRates
  }


  private def accountAddressToMap(addr: AccountAddress): Address = {
    val fromAddressMap: java.util.Map[String, AnyRef] = mutable.HashMap[String, String](
      "company" -> addr.company.getOrElse(""),
      "street1" -> addr.road,
      "street2" -> addr.road2.getOrElse(""),
      "city" -> addr.city,
      "state" -> addr.admin1.getOrElse("US"),
      "country" -> addr.country.getOrElse("US"),
      "zip" -> addr.zipCode.getOrElse(""),
      "phone" -> addr.telephone.map(_.phone).getOrElse("")).filter(_._2.length > 0)

    Address.create(fromAddressMap)

  }


  private def companyAddressToMap(addr: CompanyAddress): Address = {
    val fromAddressMap: java.util.Map[String, AnyRef] = mutable.HashMap[String, String](
      "company" -> addr.company,
      "street1" -> addr.road,
      "street2" -> addr.road2.getOrElse(""),
      "city" -> addr.city,
      "state" -> addr.state.getOrElse("US"),
      "country" -> addr.country,
      "zip" -> addr.zipCode).filter(_._2.length > 0)

    Address.create(fromAddressMap)

  }

  def buy(shipmentId: String, rateId: String): Shipment = {
    val shipment = new Shipment()
    shipment.setId(shipmentId)
    //val rate = mutable.HashMap[String, AnyRef]("id" -> rateId)
    val rate = new Rate(rateId, "dum", "my", null, null, null, null, null, null, null, null, null, null)
    shipment.buy(rate)
  }
}

object EasyPostHandler {
  def main(params: Array[String]) = {
    /*
    val handler = new EasyPostHandler()
    val rates = handler.rate(
//      AccountAddress(firstName =Some("Hayssam"), lastName = Some("Saleh"), road = "60 rue Emeriau", city = "Paris", country = Some("FR"), zipCode = Some("75015")),
//      AccountAddress(firstName =Some("Hayssam"), lastName = Some("Saleh"), road = "23 rue Vernet", city = "Paris", country = Some("FR"), zipCode = Some("75008")),
      AccountAddress(road = "164 Townsend Street", company= Some("Mogobiz"), city = "San Francisco", country = Some("US"), zipCode = Some("94107")),
      AccountAddress(road = "Vandelay Industries", city = "Bronx", country = Some("US"), zipCode = Some("10451")),
      ShippingParcel(9.0, 6.0, 2.0, 10.0)
    )

    rates.foreach(rate => println(rate.getService + "=" + rate.getRate + " " + rate.getCurrency + ""))

    val res : Shipment = handler.buy(rates(0).getShipmentId, rates(0).getId)
    println(JacksonConverter.serialize(res))

    println(res.getTrackingCode)
*/
  }
}
