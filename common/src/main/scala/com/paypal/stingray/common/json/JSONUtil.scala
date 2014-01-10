package com.paypal.stingray.common.json

import net.liftweb.{json => liftJson}
import liftJson.{Formats, Serialization, NoTypeHints}
import liftJson.JsonAST._

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 3/12/12
 * Time: 6:43 PM
 */

object JSONUtil {

  implicit class RichJValue(json: JValue) {
    def toWireFormat: String = prepare(json)
    def toPretty: String = pretty(json)
  }

  def prepare(json: JValue): String = liftJson.compactRender(json)

  def pretty(json: JValue): String = liftJson.pretty(liftJson.render(json))

  //serializing & de-serializing case classes

  def deserialize[T <: AnyRef](json: String)(implicit m: Manifest[T]): T = liftJson.parse(json).extract[T](Serialization.formats(NoTypeHints), m)

  def deserialize[T <: AnyRef](json: String, formats: Formats)(implicit m:Manifest[T]): T = liftJson.parse(json).extract[T](formats, m)

  def serialize[T <: AnyRef](caseClass: T): String = serialize(caseClass, Serialization.formats(NoTypeHints))

  def serialize[T <: AnyRef](caseClass: T, formats: Formats): String = Serialization.write(caseClass)(formats)

}
