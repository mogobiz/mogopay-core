package mogopay.handlers.shipping

import mogopay.model.Mogopay.ShippingAddress
import org.json4s.JValue

class KialaShippingHandler extends ShippingService {
  override def calculatePrice(shippingAddress: ShippingAddress, currencyCode: String,
                              cart: JValue): Seq[ShippingPrice] = ???
}
