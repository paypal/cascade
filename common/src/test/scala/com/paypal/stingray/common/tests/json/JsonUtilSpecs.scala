package com.paypal.stingray.common.tests.json

import com.paypal.stingray.common.json._
import com.paypal.stingray.common.json.JsonUtil._
import com.paypal.stingray.common.tests.scalacheck._
import org.specs2._
import org.scalacheck.Gen._
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen

/**
 * Tests that exercise methods in [[com.paypal.stingray.common.json.JsonUtil]]
 */
class JsonUtilSpecs
  extends Specification
  with ScalaCheck { def is = s2"""

  JsonUtil is an interface to Jackson that allows for simple serialization/deserialization of case classes, etc

  JsonUtil should serialize and deserialize basic types, such as
    an arbitrary String without modifying it                                 ${BasicTypes.Strings().ok}
    an Int as a String representation of that Int                            ${BasicTypes.Ints().ok}
    a Long as a String representation of that Long                           ${BasicTypes.Longs().ok}
    a Float as a String representation of that Float                         ${BasicTypes.Floats().ok}
    a Double as a String representation of that Double                       ${BasicTypes.Doubles().ok}

  JsonUtil should serialize and deserialize more complex types, such as
    a Map[String, String]                                                    ${Maps.StringToString().ok}
    a Map[String, Int]                                                       ${Maps.StringToInt().ok}
    a Map[String, List[String]]                                              ${Maps.StringToListString().ok}
    a Map[String, List[Int]]                                                 ${Maps.StringToListInt().ok}
    a Map[String, Map[String, String]]                                       ${Maps.StringToMapStringString().ok}

  JsonUtil should serialize and deserialize case classes, such as
    a case class containing a single data member                             ${CaseClasses.OneMember().ok}
    a case class containing multiple members of mixed basic types            ${CaseClasses.TwoMemberMixedBasic().ok}
    a case class containing mutliple members of mixed complex types          ${CaseClasses.TwoMemberMixedComplex().ok}

  JsonUtil should fail to deserialize
    malformed json
    json that does not correspond to a given specified type
    json that corresponds to only some data members in a specified type
  """

  //TODO: tests using optional types

  object BasicTypes {

    case class Strings() {
      def ok = forAll(genNonEmptyAlphaStr) { str =>  // TODO: write valid json chars generator
        val to = toJson(str).get
        val from = fromJson[String](to).get
        // for whatever reason, the compiler balks on string interpolation here
        // e.g. "\"$str\"" is not recognized as a valid string
        (to must beEqualTo("\"%s\"".format(str))) and (from must beEqualTo(str))
      }
    }

    case class Ints() {
      def ok = forAll(arbitrary[Int]) { i =>
        val to = toJson(i).get
        val from = fromJson[Int](to).get
        (to must beEqualTo(i.toString)) and (from must beEqualTo(i))
      }
    }

    case class Longs() {
      def ok = forAll(arbitrary[Long]) { i =>
        val to = toJson(i).get
        val from = fromJson[Long](to).get
        (to must beEqualTo(i.toString)) and (from must beEqualTo(i))
      }
    }

    case class Floats() {
      def ok = forAll(arbitrary[Float]) { i =>
        val to = toJson(i).get
        val from = fromJson[Float](to).get
        (to must beEqualTo(i.toString)) and (from must beEqualTo(i))
      }
    }

    case class Doubles() {
      def ok = forAll(arbitrary[Double]) { i =>
        val to = toJson(i).get
        val from = fromJson[Double](to).get
        (to must beEqualTo(i.toString)) and (from must beEqualTo(i))
      }
    }
  }

  object Maps {

    case class StringToString() {
      def ok = forAll(genNonEmptyAlphaStr, genNonEmptyAlphaStr) { (k, v) =>
        val to = toJson(Map(k -> v)).get
        val from = fromJson[Map[String, String]](to).get

        // string interpolation highlighting in triple-quoted strings doesn't render well in IntelliJ
        (to must beEqualTo("""{"%s":"%s"}""".format(k, v))) and
          (from must havePair(k -> v))
      }
    }

    case class StringToInt() {
      def ok = forAll(genNonEmptyAlphaStr, arbitrary[Int]) { (k, v) =>
        val to = toJson(Map(k -> v)).get
        val from = fromJson[Map[String, Int]](to).get

        (to must beEqualTo("""{"%s":%d}""".format(k, v))) and
          (from must havePair(k -> v))
      }
    }

    case class StringToListString() {
      def ok = forAll(genNonEmptyAlphaStr, genNonEmptyAlphaStr, genNonEmptyAlphaStr) { (k, v1, v2) =>
        val to = toJson(Map(k -> List(v1, v2))).get
        val from = fromJson[Map[String, List[String]]](to).get

        (to must beEqualTo("""{"%s":["%s","%s"]}""".format(k, v1, v2))) and
          (from must havePair(k -> List(v1, v2)))
      }
    }

    case class StringToListInt() {
      def ok = forAll(genNonEmptyAlphaStr, arbitrary[Int], arbitrary[Int]) { (k, v1, v2) =>
        val to = toJson(Map(k -> List(v1, v2))).get
        val from = fromJson[Map[String, List[Int]]](to).get

        (to must beEqualTo("""{"%s":[%d,%d]}""".format(k, v1, v2))) and
          (from must havePair(k -> List(v1, v2)))
      }
    }

    case class StringToMapStringString() {
      def ok = forAll(genNonEmptyAlphaStr, genNonEmptyAlphaStr, genNonEmptyAlphaStr) { (k, k1, v1) =>
        val to = toJson(Map(k -> Map(k1 -> v1))).get
        val from = fromJson[Map[String, Map[String, String]]](to).get

        (to must beEqualTo("""{"%s":{"%s":"%s"}}""".format(k, k1, v1))) and
          (from.get(k) must beSome.like { case m =>
            m must havePair(k1 -> v1)
          })
      }
    }
  }

  object CaseClasses {
    import JsonUtilSpecs._

    case class OneMember() {
      def ok = forAll(genNonEmptyAlphaStr) { v =>
        val to = toJson(OneMemberData(v)).get
        val from = fromJson[OneMemberData](to).get

        (to must beEqualTo("""{"value":"%s"}""".format(v))) and
          (from must beEqualTo(OneMemberData(v)))
      }
    }

    case class TwoMemberMixedBasic() {
      def ok = forAll(genNonEmptyAlphaNumStr, arbitrary[Int]) { (s, i) =>
        val to = toJson(TwoMemberMixedBasicData(s, i)).get
        val from = fromJson[TwoMemberMixedBasicData](to).get

        (to must beEqualTo("""{"one":"%s","two":%d}""".format(s, i))) and
          (from must beEqualTo(TwoMemberMixedBasicData(s, i)))
      }
    }

    lazy val genStringIntPair: Gen[(String, Int)] = for {
      s <- genNonEmptyAlphaNumStr
      i <- arbitrary[Int]
    } yield (s, i)
    case class TwoMemberMixedComplex() {
      def ok = forAll(genNonEmptyAlphaNumStr, genNonEmptyAlphaNumStr, genNonEmptyAlphaNumStr, arbitrary[Int]) { (li1, li2, k, v) =>
        val to = toJson(TwoMemberMixedComplexData(List(li1, li2), Map(k -> v))).get
        val from = fromJson[TwoMemberMixedComplexData](to).get

        (to must beEqualTo("""{"one":["%s","%s"],"two":{"%s":%d}}""".format(li1, li2, k, v))) and
          (from must beEqualTo(TwoMemberMixedComplexData(List(li1, li2), Map(k -> v))))
      }
    }
  }
}

object JsonUtilSpecs {
  case class OneMemberData(value: String)
  case class TwoMemberMixedBasicData(one: String, two: Int)
  case class TwoMemberMixedComplexData(one: List[String], two: Map[String, Int])
}
