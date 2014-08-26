/**
 * Copyright 2013-2014 PayPal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paypal.cascade.examples.common.enumeration

import com.paypal.cascade.common.enumeration._
import com.paypal.cascade.json._
import com.fasterxml.jackson.core._
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.annotation._
import com.paypal.cascade.common.logging.LoggingSugar
import java.io.IOException

/**
 * An example implementation of Enumeration.
 *
 * For a given Enumeration, @JsonSerialize and @JsonDeserialize annotations must be set,
 * and JsonSerializer/JsonDeserializer classes need to be provided for those annotations.
 *
 * Luckily, most enumeration serializers and deserializers can be copied verbatim
 * from this example, changing type parameters where needed. Delicious boilerplate.
 */
@JsonSerialize(using = classOf[MyEnumSerializer])
@JsonDeserialize(using = classOf[MyEnumDeserializer])
sealed abstract class MyEnum extends Enumeration

/**
 * Each Enumeration is implemented as a series of case objects extending a base abstract class,
 * that itself extends Enumeration. Each case object must override `stringVal`, and the `stringVal` for each
 * should be unique across all cases of a given Enumeration; no two objects should have the same `stringVal` value,
 * regardless of uppercase/lowercase.
 */
object MyEnum {

  case object CaseOne extends MyEnum {
    override val stringVal = "caseone"
  }

  case object CaseTwo extends MyEnum {
    override val stringVal = "casetwo"
  }

  /** Several convenience methods for EnumReaders are available in {{{ com.paypal.cascade.common.enumeration._ }}}.
    * Generally, this will be implemented as a pattern match across all the case objects of an Enumeration.
    * If a case object is missing from the pattern match, a non-exhaustive match warning will be raised at compile.
    */
  implicit val myEnumReader: EnumReader[MyEnum] = new EnumReader[MyEnum] {
    override def read(s: String): Option[MyEnum] = s match {
      case CaseOne.stringVal => Some(CaseOne)
      case CaseTwo.stringVal => Some(CaseTwo)
      case _ => None
    }
  }

}

/**
 * More complex Enumeration processing can be added to each implemented JsonSerializer,
 * but in most cases it is sufficient to simply return the stringVal of the enum.
 */
private[this] class MyEnumSerializer extends JsonSerializer[MyEnum] {
  override def serialize(value: MyEnum, jgen: JsonGenerator, provider: SerializerProvider): Unit = {
    jgen.writeString(value.stringVal)
  }
}

/**
 * Similarly, more complex deserialization can be performed inside the implicit EnumReader implementation,
 * but in most cases it is sufficient to build out a simple pattern match for each Enumeration type.
 */
private[this] class MyEnumDeserializer extends JsonDeserializer[MyEnum] {
  override def deserialize(jp: JsonParser, ctxt: DeserializationContext): MyEnum = {
    try {
      jp.getText.toEnum[MyEnum]
    } catch {
      // Needs to throw an IOException or the compiler complains with Scala 2.11
      case e: EnumerationException => throw new IOException(e)
    }
  }
}

/**
 * A simple example runner, demonstrating how to serialize and deserialize objects with Enumerations.
 * The `toJson` and `fromJson` calls are from implicits in {{{ com.paypal.cascade.json._ }}}.
 */
object MyEnumRunner extends LoggingSugar {

  /**
   * Normally, case classes making use of an Enumeration would live outside of the file containing the Enumeration,
   * in an object more appropriate for the classes using them.
   *
   * There are some ser/deser caveats regarding where to define case classes that contain Enumerations:
   *  - OK: in a package object
   *  - OK: in an object inside a different file
   *  - OK: in an object inside the same file as the Enumeration (as seen here)
   *  - NOT OK: in a trait that is mixed in to a class or object in which deser takes place
   *  - NOT OK: in a def that performs ser/deser
   *
   *  Note that this is not an exhaustive list of possibilities. Always test!
   *
   * @param v1 an example parameter
   */
  case class CS(v1: MyEnum)

  /** Run it! */
  def main(args: Array[String]): Unit = {
    val logger = getLogger[MyEnumRunner.type]

    logger.debug("custom Enumeration example")

    val cs = CS(MyEnum.CaseOne)
    logger.debug(cs.toString)

    val json = cs.toJson.get
    logger.debug(json)

    val cs2 = json.fromJson[CS].get
    logger.debug(cs2.toString)
  }

}
