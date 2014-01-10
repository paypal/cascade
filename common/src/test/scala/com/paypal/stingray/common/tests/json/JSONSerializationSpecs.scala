package com.stackmob.tests.common.json

import org.specs2._
import org.scalacheck._
import Prop._
import Arbitrary._
import com.paypal.stingray.common.json.JSONSerialization
import com.paypal.stingray.common.annotate.AnnotationHelpers._
import scala.collection.JavaConverters._
import org.codehaus.jackson.`type`.TypeReference
import scala.reflect.BeanProperty
import java.util.{Map => JMap, List => JList}
import org.codehaus.jackson.annotate.JsonCreator
import com.stackmob.tests.common.util.CommonImmutableSpecificationContext

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 3/28/12
 * Time: 5:49 PM
 */

class JSONSerializationSpecs extends Specification with ScalaCheck { override def is =

  "JSONSerializationSpecs".title                                                                    ^
  """
  The JSON serialization class provides methods for serializing and deserializing JSON via Jackson.
  """                                                                                               ^
  "Serialize and deserialize a map"                                                                 ! mapContext().ok ^
  "Serialize and deserialize a list using Jackson type references"                                  ! typeReferenceContext().ok ^
  "Serialize and deserialize a naked array"                                                         ! nakedArrayContext().ok ^
  "Serialize and deserialize a naked int"                                                           ! nakedIntContext().ok ^
  "Serialize and deserialize and object with Integers"                                              ! objectsWithIntegersContext().ok ^
  "Deserialize an object with extra properties"                                                     ! extraPropertiesContext().ok ^
                                                                                                    end

  case class extraPropertiesContext() extends context {
    def ok = apply {
      forAll(arbitrary[Int], Gen.alphaStr) { (i, s) =>
        val extraPropJson = """{"i": %s, "s": "%s", "extra": "extra"}""".format(i, s)
        val t = JSONSerialization.deserialize(extraPropJson, classOf[ThingWithInteger])
        t must beEqualTo(new ThingWithInteger(i, s))
      }
    }
  }

  case class objectsWithIntegersContext() extends context {
    def ok = apply {
      forAll(arbitrary[Int], Gen.alphaStr) { (i, s) =>
        val t1 = new ThingWithInteger(i, s)
        val serialized = JSONSerialization.serialize(t1)
        val t2 = JSONSerialization.deserialize(serialized, classOf[ThingWithInteger])
        t1 must beEqualTo(t2)
      }
    }
  }

  case class nakedIntContext() extends context {
    def ok = apply {
      forAll(arbitrary[Int]) { num =>
        val node = JSONSerialization.deserializeToJsonTree(num.toString)
        node.isInt must beTrue
      }
    }
  }

  case class nakedArrayContext() extends context {
    def ok = apply {
      forAll(Gen.listOf1(arbitrary[Int]).map(s => "[%s]".format(s.mkString(",")))) { arr =>
        val node = JSONSerialization.deserializeToJsonTree(arr)
        node.isArray must beTrue
      }
    }
  }

  case class typeReferenceContext() extends context {
    def ok = apply {
      forAll(Gen.listOf1(arbitrary[Int])) { lst =>
        val json = JSONSerialization.serialize(lst.asJava)
        val cpy = JSONSerialization.deserialize(json, new TypeReference[JList[Long]]{})
        lst must haveTheSameElementsAs(cpy.asScala)
      }
    }
  }

  case class mapContext() extends context {
    def ok = apply {
      forAll(Gen.alphaStr, Gen.alphaStr) { (email, first) =>
        val stringMap = Map("Email" -> email, "First" -> first, "Date" -> System.currentTimeMillis.toString)
        val json = JSONSerialization.serialize(stringMap.asJava)
        val deserializedMap = JSONSerialization.deserialize(json, classOf[JMap[String, String]])
        stringMap must haveTheSameElementsAs(deserializedMap.asScala)
      }
    }
  }

  trait context extends CommonImmutableSpecificationContext

}

case class ThingWithInteger @JsonCreator() (@ScalaJsonProperty("i") @BeanProperty anInt: java.lang.Integer,
                                            @ScalaJsonProperty("s") @BeanProperty aString: String)
