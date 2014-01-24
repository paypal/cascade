package com.paypal.stingray.examples.common.enumeration

import com.paypal.stingray.common.enumeration._
import com.fasterxml.jackson.core._
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.annotation._

@JsonSerialize(using = classOf[MyEnumSerializer])
@JsonDeserialize(using = classOf[MyEnumDeserializer])
sealed abstract class MyEnum extends Enumeration

object MyEnum {
  case object CaseOne extends MyEnum {
    override val stringVal = "caseone"
  }
  case object CaseTwo extends MyEnum {
    override val stringVal = "casetwo"
  }

  implicit val myEnumReader: EnumReader[MyEnum] = new EnumReader[MyEnum] {
    override def read(s: String): Option[MyEnum] = s match {
      case CaseOne.stringVal => Some(CaseOne)
      case CaseTwo.stringVal => Some(CaseTwo)
      case _ => None
    }
  }
}

object MyEnumRunner {
  def main(args: Array[String]) {
    import com.paypal.stingray.common.json._

    println("custom Enumeration example")

    case class CS(v1: MyEnum)
    val cs = CS(MyEnum.CaseOne)
    println(cs.toString)

    val json = cs.toJson.get
    println(json)

    val cs2 = json.fromJson[CS].get
    println(cs2)
  }
}

private[this] class MyEnumSerializer extends JsonSerializer[MyEnum] {
  override def serialize(value: MyEnum, jgen: JsonGenerator, provider: SerializerProvider) {
    jgen.writeString(value.stringVal)
  }
}

private[this] class MyEnumDeserializer extends JsonDeserializer[MyEnum] {
  override def deserialize(jp: JsonParser, ctxt: DeserializationContext): MyEnum = {
    jp.getText.toEnum[MyEnum]
  }
}


