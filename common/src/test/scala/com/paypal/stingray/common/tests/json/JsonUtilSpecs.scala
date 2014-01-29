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
    a case class containing an optional AnyVal type                          ${CaseClasses.OptionalAnyValMember().ok}
    a case class containing an optional AnyRef type                          ${CaseClasses.OptionalAnyRefMember().ok}
    a case class containing another case class                               ${CaseClasses.NestedClasses().ok}

  JsonUtil should
    not deserialize malformed json                                           ${Badness.MalformedJson().fails}
    not deserialize json that is type mismatched                             ${Badness.MismatchedTypes().fails}
    deserialize json that is missing an AnyVal, with a default value         ${Badness.MissingAnyVal().ok}
    deserialize json that is missing an AnyRef, with a null value            ${Badness.MissingAnyRef().ok}
  """

  object BasicTypes {

    case class Strings() {
      def ok = forAll(genNonEmptyAlphaStr) { str =>  // TODO: write exhaustive json chars generator
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

    case class OptionalAnyValMember() {
      def ok = forAll(genNonEmptyAlphaStr, option(arbitrary[Int])) { (s, mbInt) =>
        val to = toJson(OptionalAnyValData(s, mbInt)).get
        val from = fromJson[OptionalAnyValData](to).get

        // Note: Jackson serializes None as null, so `mbTwo` will always be present as a key,
        // but possibly with a null value
        val mbIntToJson = mbInt match {
          case None => "null"
          case Some(i) => s"$i"
        }

        (to must beEqualTo("""{"one":"%s","mbTwo":%s}""".format(s, mbIntToJson))) and
          (from must beEqualTo(OptionalAnyValData(s, mbInt)))
      }
    }

    case class OptionalAnyRefMember() {
      def ok = forAll(option(genNonEmptyAlphaStr), arbitrary[Int]) { (mbStr, i) =>
        val to = toJson(OptionalAnyRefData(mbStr, i)).get
        val from = fromJson[OptionalAnyRefData](to).get

        // Note: Jackson serializes None as null, so `mbOne` will always be present as a key,
        // but possibly with a null value
        val mbStrToJson = mbStr match {
          case None => "null"
          case Some(str) => """"%s"""".format(str)
        }

        (to must beEqualTo("""{"mbOne":%s,"two":%d}""".format(mbStrToJson, i))) and
          (from must beEqualTo(OptionalAnyRefData(mbStr, i)))
      }
    }

    case class NestedClasses() {
      def ok = forAll(genNonEmptyAlphaStr) { s =>
        val to = toJson(NestedOuter(NestedInner(s))).get
        val from = fromJson[NestedOuter](to).get

        (to must beEqualTo("""{"inner":{"one":"%s"}}""".format(s))) and
          (from must beEqualTo(NestedOuter(NestedInner(s))))
      }
    }
  }

  object Badness {

    case class MalformedJson() {
      def fails = forAll(genNonEmptyAlphaStr, genNonEmptyAlphaStr) { (k, v) =>
        val unquotedKey = """{%s:"%s"}""".format(k, v)
        val partiallyQuotedKey1 = """{"%s:"%s"}""".format(k, v)
        val partiallyQuotedKey2 = """{%s":"%s"}""".format(k, v)
        val unquotedStringValue = """{"%s":%s}""".format(k, v)
        val partiallyQuotedStringValue1 = """{"%s":"%s}""".format(k, v)
        val partiallyQuotedStringValue2 = """{"%s":%s"}""".format(k, v)
        val noOpeningBrace = """"%s":"%s"}""".format(k, v)
        val noClosingBrace = """{"%s":"%s"""".format(k, v)
        val unfinishedInnerList1 = """{"%s":[%s}""".format(k, v)
        val unfinishedInnerList2 = """{"%s":%s]""".format(k, v)
        val unfinishedInnerMap1 = """{"%s":{"%s":}}""".format(k, v)
        val unfinishedInnerMap2 = """{"%s":{"%s":"%s"}""".format(k, v, v)
        val unfinishedInnerMap3 = """{"%s":"%s":"%s"}}""".format(k, v, v)

        (fromJson[Map[String, String]](unquotedKey) must beFailedTry) and
          (fromJson[Map[String, String]](partiallyQuotedKey1) must beFailedTry) and
          (fromJson[Map[String, String]](partiallyQuotedKey2) must beFailedTry) and
          (fromJson[Map[String, String]](unquotedStringValue) must beFailedTry) and
          (fromJson[Map[String, String]](partiallyQuotedStringValue1) must beFailedTry) and
          (fromJson[Map[String, String]](partiallyQuotedStringValue2) must beFailedTry) and
          (fromJson[Map[String, String]](noOpeningBrace) must beFailedTry) and
          (fromJson[Map[String, String]](noClosingBrace) must beFailedTry) and
          (fromJson[Map[String, List[String]]](unfinishedInnerList1) must beFailedTry) and
          (fromJson[Map[String, List[String]]](unfinishedInnerList2) must beFailedTry) and
          (fromJson[Map[String, Map[String, String]]](unfinishedInnerMap1) must beFailedTry) and
          (fromJson[Map[String, Map[String, String]]](unfinishedInnerMap2) must beFailedTry) and
          (fromJson[Map[String, Map[String, String]]](unfinishedInnerMap3) must beFailedTry)
      }
    }

    case class MismatchedTypes() {
      def fails = forAll(genNonEmptyAlphaStr, genNonEmptyAlphaStr) { (k, v) =>
        val to = toJson(Map(k -> v)).get

        // Note: if instead `v` were an Int, and `fromJson[Map[String, String]]` were used,
        // Jackson would attempt to convert the value to a String, which would be a Success
        val from = fromJson[Map[String, Int]](to)

        from must beFailedTry
      }
    }

    case class MissingAnyVal() {
      import JsonUtilSpecs._
      def ok = forAll(genNonEmptyAlphaNumStr) { k =>
        val from = fromJson[TwoMemberMixedBasicData]("""{"one":"%s"}""".format(k)).get

        // this is a known Jackson quirk: missing AnyVals in case classes get instantiated at default values, e.g. 0
        from must beEqualTo(TwoMemberMixedBasicData(k, 0))
      }
    }

    case class MissingAnyRef() {
      import JsonUtilSpecs._
      def ok = forAll(arbitrary[Int]) { k =>
        val from = fromJson[TwoMemberMixedBasicData]("""{"two":%d}""".format(k)).get

        // this is another known Jackson quirk: missing AnyRefs in case classes come in as `null`
        from must beEqualTo(TwoMemberMixedBasicData(null, k))
      }
    }
  }
}

object JsonUtilSpecs {
  case class OneMemberData(value: String)
  case class TwoMemberMixedBasicData(one: String, two: Int)
  case class TwoMemberMixedComplexData(one: List[String], two: Map[String, Int])
  case class OptionalAnyValData(one: String, mbTwo: Option[Int])
  case class OptionalAnyRefData(mbOne: Option[String], two: Int)
  case class NestedInner(one: String)
  case class NestedOuter(inner: NestedInner)
}
