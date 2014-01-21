package com.paypal.stingray.common

import scala.util.Try

/**
 * Created by awharris on 1/17/14.
 */
package object json {

  // patterns adapted from https://coderwall.com/p/o--apg
  // TODO: convert Manifest patterns to use TypeTag, ClassTag when Jackson implements that
  implicit class Unmarshallable(str: String) {
    def toMap: Try[Map[String, Any]] = JsonUtil.fromJsonToMap(str)
    def toMapOf[T : Manifest](): Try[Map[String, T]] = JsonUtil.fromJsonToMap[T](str)
    def fromJson[T : Manifest](): Try[T] =  JsonUtil.fromJson[T](str)
  }

  implicit class Marshallable[T](marshallMe: T) {
    def toJson: Try[String] = JsonUtil.toJson(marshallMe)
  }

}
