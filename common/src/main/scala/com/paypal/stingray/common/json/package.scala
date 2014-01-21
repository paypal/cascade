package com.paypal.stingray.common

import scala.util.Try
import scala.reflect.runtime.universe.TypeTag
import scala.reflect.ClassTag

/**
 * Created by awharris on 1/17/14.
 */
package object json {

  // patterns adapted from https://coderwall.com/p/o--apg
  implicit class Unmarshallable(str: String) {
    def toMap: Try[Map[String, Any]] = JsonUtil.fromJsonToMap(str)
    def toMapOf[T <: AnyRef : TypeTag : ClassTag](): Try[Map[String, T]] = JsonUtil.fromJsonToMap[T](str)
    def fromJson[T <: AnyRef : TypeTag : ClassTag](): Try[T] =  JsonUtil.fromJson[T](str)
  }

  implicit class Marshallable[T](marshallMe: T) {
    def toJson: Try[String] = JsonUtil.toJson(marshallMe)
  }

}
