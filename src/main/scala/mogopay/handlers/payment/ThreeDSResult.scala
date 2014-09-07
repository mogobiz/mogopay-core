package mogopay.handlers.payment

import mogopay.model.Mogopay.ResponseCode3DS.ResponseCode3DS

case class ThreeDSResult(
  code: ResponseCode3DS,
  url: String,
  method: String,
  mdName: String,
  mdValue: String,
  pareqName: String,
  pareqValue: String,
  termUrlName: String,
  termUrlValue: String
)