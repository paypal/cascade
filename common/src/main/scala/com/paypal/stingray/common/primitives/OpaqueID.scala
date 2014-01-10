package com.paypal.stingray.common.translatable.primitives

import org.codehaus.jackson.annotate.JsonIgnore
import java.io.Serializable

trait OpaqueID extends Serializable {
  @JsonIgnore def getUnderlyingPrimitive: AnyRef
}
