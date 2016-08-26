/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.payment

import com.mogobiz.pay.model.ResponseCode3DS.ResponseCode3DS

case class ThreeDSResult(code: ResponseCode3DS = null,
                         url: String = null,
                         method: String = null,
                         mdName: String = null,
                         mdValue: String = null,
                         pareqName: String = null,
                         pareqValue: String = null,
                         termUrlName: String = null,
                         termUrlValue: String = null)
