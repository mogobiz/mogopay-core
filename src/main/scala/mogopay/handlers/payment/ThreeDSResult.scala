package mogopay.handlers.payment

import mogopay.model.Mogopay.ResponseCode3DS.ResponseCode3DS

case class ThreeDSResult(
                          code: ResponseCode3DS = null,
                          url: String = null,
                          method: String = null,
                          mdName: String = null,
                          mdValue: String = null,
                          pareqName: String = null,
                          pareqValue: String = null,
                          termUrlName: String = null,
                          termUrlValue: String = null
                          )