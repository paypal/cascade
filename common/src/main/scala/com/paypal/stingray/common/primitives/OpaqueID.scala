package com.paypal.stingray.common.translatable.primitives

import java.io.Serializable
import com.fasterxml.jackson.annotation.JsonIgnore

trait OpaqueID extends Serializable {
  @JsonIgnore def getUnderlyingPrimitive: AnyRef
}
