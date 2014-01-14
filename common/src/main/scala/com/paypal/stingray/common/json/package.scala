package com.paypal.stingray.common

import net.liftweb.json._
import scala.util.{Failure, Try}

/**
 * The objects contained here are borrowed patterns from net.liftweb.json.JsonScalaz,
 * which was one of our preferred JSON parsing libraries until Dec 2013.
 *
 * Created by awharris on 1/10/14.
 */
package object json {

  sealed trait JSONUtilException extends Exception
  case class UnexpectedJSONError(was: JValue, expected: Class[_ <: JValue]) extends JSONUtilException
  case class NoSuchFieldError(name: String, json: JValue) extends JSONUtilException
  case class UncategorizedError(key: String, desc: String, args: List[Any]) extends JSONUtilException

  trait JSONR[A] {
    def read(json: JValue): Try[A]
  }

  trait JSONW[A] {
    def write(value: A): JValue
  }

  trait JSON[A] extends JSONR[A] with JSONW[A]

  implicit def Try2JSONR[A](f: JValue => Try[A]): JSONR[A] = new JSONR[A] {
    def read(json: JValue) = f(json)
  }

  def fromJSON[A: JSONR](json: JValue): Try[A] = implicitly[JSONR[A]].read(json)
  def toJSON[A: JSONW](value: A): JValue = implicitly[JSONW[A]].write(value)

  def field[A: JSONR](name: String)(json: JValue): Try[A] = json match {
    case JObject(fs) => {
      fs.find(_.name == name)
        .map(f => implicitly[JSONR[A]].read(f.value))
        .getOrElse(implicitly[JSONR[A]].read(JNothing))
        .orElse(Failure(NoSuchFieldError(name, json)))
    }
    case x => Failure(UnexpectedJSONError(x, classOf[JObject]))
  }

}
