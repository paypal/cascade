package com.paypal.stingray.common.annotate

import annotation.target._
import org.codehaus.jackson.annotate.{JsonIgnore, JsonProperty}

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 11/8/11
 * Time: 11:24 PM
 */

object AnnotationHelpers {
  type ScalaIndexedField = IndexedField @beanGetter
  type ScalaJsonProperty = JsonProperty @beanGetter @beanSetter @param
  type ScalaJsonIgnore = JsonIgnore @beanGetter @beanSetter @param
}
