package com.mogobiz.pay.handlers.shipping

import com.easypost.EasyPost
import com.easypost.model.{Shipment, Parcel, Address, Rate}
import com.mogobiz.json.JacksonConverter
import com.mogobiz.pay.model.Mogopay.{AccountAddress, ShippingParcel}
import scala.collection.JavaConversions._
import scala.collection.mutable

class EasyPostHandler {
  EasyPost.apiKey = "ueG20zkjZWwNjUszp1Pr2w"

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

  def rate(from: AccountAddress, to: AccountAddress, parcel: ShippingParcel): Seq[Rate] = {
    val fromAddress = accountAddressToMap(from)
    val toAddress = accountAddressToMap(to)

    val parcelMap = mutable.HashMap[String, AnyRef](
      "height" -> parcel.height.asInstanceOf[AnyRef],
      "width" -> parcel.width.asInstanceOf[AnyRef],
      "length" -> parcel.length.asInstanceOf[AnyRef],
      "weight" -> parcel.weight.asInstanceOf[AnyRef])

    val parc = Parcel.create(parcelMap)

    val shipmentMap = mutable.HashMap[String, AnyRef](
      "from_address" -> fromAddress,
      "to_address" -> toAddress,
      "parcel" -> parc)

    val shipment = Shipment.create(shipmentMap)
    val buyCarriers = List("USPS")
    shipment.getRates
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

  }
}
