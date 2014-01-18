package com.paypal.stingray.common

/**
 * Created by awharris on 1/17/14.
 */
package object json {

    // patterns adapted from https://coderwall.com/p/o--apg
    implicit class Unmarshallable(str: String) {
      def toMap: Map[String, Any] = JsonUtil.toMap(str)
      def toMapOf[V: Manifest](): Map[String, V] = JsonUtil.toMap[V](str)
      def fromJson[T: Manifest](): T =  JsonUtil.fromJson[T](str)
    }

    implicit class Marshallable[T](marshallMe: T) {
      def toJson: String = JsonUtil.toJson(marshallMe)
    }

}
