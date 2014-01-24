package com.paypal.stingray.examples.common.enumeration

import com.paypal.stingray.common.enumeration._
import com.paypal.stingray.common.json._
import com.fasterxml.jackson.core._
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.annotation._

/**
 * An example implementation of [[com.paypal.stingray.common.enumeration.Enumeration]].
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

  /** Several convenience methods for EnumReaders are available in [[com.paypal.stingray.common.enumeration]].
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
  override def serialize(value: MyEnum, jgen: JsonGenerator, provider: SerializerProvider) {
    jgen.writeString(value.stringVal)
  }
}

/**
 * Similarly, more complex deserialization can be performed inside the implicit EnumReader implementation,
 * but in most cases it is sufficient to build out a simple pattern match for each Enumeration type.
 */
private[this] class MyEnumDeserializer extends JsonDeserializer[MyEnum] {
  override def deserialize(jp: JsonParser, ctxt: DeserializationContext): MyEnum = {
    jp.getText.toEnum[MyEnum]
  }
}

/**
 * Normally, case classes making use of an Enumeration would live outside of the file for the Enumeration.
 * If case classes do live inside the same file, they need to be at the top-most level in the file.
 * Otherwise, the serializer won't find the case class type, and won't know how to work with it.
 * @param v1 an example parameter
 */
private case class CS(v1: MyEnum)

/**
 * A simple example runner, demonstrating how to serialize and deserialize objects with Enumerations.
 * The `toJson` and `fromJson` calls are from implicits in [[com.paypal.stingray.common.json]].
 */
object MyEnumRunner {
  def main(args: Array[String]) {

    println("custom Enumeration example")

    val cs = CS(MyEnum.CaseOne)
    println(cs.toString)

    val json = cs.toJson.get
    println(json)

    val cs2 = json.fromJson[CS].get
    println(cs2)
  }
}
