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
 package com.paypal.stingray.json.tests

import com.paypal.stingray.json.JsonUtil._
import com.paypal.stingray.common.tests.scalacheck._
import org.specs2._
import org.scalacheck.Gen._
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

/**
 * Tests that exercise methods in [[com.paypal.stingray.json.JsonUtil]]
 */
class JsonUtilSpecs
  extends Specification
  with ScalaCheck { override def is = s2"""

  JsonUtil is an interface to Jackson that allows for simple serialization/deserialization of case classes, etc

  JsonUtil should serialize and deserialize basic types, such as
    an arbitrary String without modifying it                                 ${BasicTypes.Strings().ok}
    an Int as a String representation of that Int                            ${BasicTypes.Ints().ok}
    a Long as a String representation of that Long                           ${BasicTypes.Longs().ok}
    a Float as a String representation of that Float                         ${BasicTypes.Floats().ok}
    a Double as a String representation of that Double                       ${BasicTypes.Doubles().ok}
    a Date as an ISO8601 String representation of that Date                  ${BasicTypes.Dates().ok}

  JsonUtil should serialize and deserialize more complex types, such as
    a Map[String, String]                                                    ${Maps.StringToString().ok}
    a Map[String, Int]                                                       ${Maps.StringToInt().ok}
    a Map[String, List[String]]                                              ${Maps.StringToListString().ok}
    a Map[String, List[Int]]                                                 ${Maps.StringToListInt().ok}
    a Map[String, List[List[String]]]                                        ${Maps.StringToListListString().ok}
    a Map[String, Map[String, String]]                                       ${Maps.StringToMapStringString().ok}
    a List[Option[String]]                                                   ${Lists.ListOption().ok}

  JsonUtil should serialize and deserialize case classes, such as
    a case class containing a single data member                             ${CaseClasses.OneMember().ok}
    a case class containing multiple members of mixed basic types            ${CaseClasses.TwoMemberMixedBasic().ok}
    a case class containing mutliple members of mixed complex types          ${CaseClasses.TwoMemberMixedComplex().ok}
    a case class containing an optional AnyVal type                          ${CaseClasses.OptionalAnyValMember().ok}
    a case class containing an optional AnyRef type                          ${CaseClasses.OptionalAnyRefMember().ok}
    a case class containing another case class                               ${CaseClasses.NestedClasses().ok}
    a case class containing an optional case class                           ${CaseClasses.OptionalNested().ok}
    a case class containing a list of options                                ${CaseClasses.ListOptionMember().ok}

  JsonUtil should
    not deserialize malformed json                                           ${Badness.MalformedJson().fails}
    not deserialize json that is type mismatched                             ${skipped} // Badness.MismatchedTypes().fails
    deserialize json that is missing an AnyVal, with a default value         ${Badness.MissingAnyVal().ok}
    deserialize json that is missing an AnyRef, with a null value            ${Badness.MissingAnyRef().ok}

  Implicit class tests

  toJson and fromJson methods work directly on strings                       ${ImplicitClasses().success}

  """

  object BasicTypes {

    case class Strings() {
      def ok = forAll(genJsonString) { str =>
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

    case class Dates() {
      def ok = forAll(arbitrary[DateTime]) { dt =>
        val to = toJson(dt).get
        val from = fromJson[DateTime](to).get
        //currently Jackson-Joda serializer doesn't preserve timezone, which is what we want anyways
        val expected = "\"%s\"".format(ISODateTimeFormat.dateTime().print(dt))
        (to must beEqualTo(expected)) and (from must beEqualTo(dt))
      }
    }
  }

  object Maps {

    case class StringToString() {
      def ok = forAll(genJsonString, genJsonString) { (k, v) =>
        val to = toJson(Map(k -> v)).get
        val from = fromJson[Map[String, String]](to).get

        // string interpolation highlighting in triple-quoted strings doesn't render well in IntelliJ
        (to must beEqualTo("""{"%s":"%s"}""".format(k, v))) and
          (from must havePair(k -> v))
      }
    }

    case class StringToInt() {
      def ok = forAll(genJsonString, arbitrary[Int]) { (k, v) =>
        val to = toJson(Map(k -> v)).get
        val from = fromJson[Map[String, Int]](to).get

        (to must beEqualTo("""{"%s":%d}""".format(k, v))) and
          (from must havePair(k -> v))
      }
    }

    case class StringToListString() {
      def ok = forAll(genJsonString, nonEmptyListOf(genJsonString)) { (k, l) =>
        val to = toJson(Map(k -> l)).get
        val from = fromJson[Map[String, List[String]]](to).get

        val listJson = (for {
          li <- l
        } yield """"%s"""".format(li)).mkString(",")

        (to must beEqualTo("""{"%s":[%s]}""".format(k, listJson))) and
          (from.get(k) must beSome.like { case lst =>
            lst.toSeq must containTheSameElementsAs(l.toSeq)
          })
      }
    }

    case class StringToListListString() {
      def ok = forAll(genJsonString, nonEmptyListOf(nonEmptyListOf(genJsonString))) { (k, l) =>
        val to = toJson(Map(k -> l)).get
        val from = fromJson[Map[String, List[List[String]]]](to).get

        from.get(k) must beSome.like { case lst =>
          lst.toSeq must containTheSameElementsAs(l.toSeq)
        }
      }
    }

    case class StringToListInt() {
      def ok = forAll(genJsonString, nonEmptyListOf(arbitrary[Int])) { (k, l) =>
        val to = toJson(Map(k -> l)).get
        val from = fromJson[Map[String, List[Int]]](to).get

        val listJson = (for {
          li <- l
        } yield s"$li").mkString(",")

        (to must beEqualTo("""{"%s":[%s]}""".format(k, listJson))) and
          (from.get(k) must beSome.like { case lst =>
            lst.toSeq must containTheSameElementsAs(l.toSeq)
          })
      }
    }

    case class StringToMapStringString() {
      def ok = forAll(genJsonString, genJsonString, genJsonString) { (k, k1, v1) =>
        val to = toJson(Map(k -> Map(k1 -> v1))).get
        val from = fromJson[Map[String, Map[String, String]]](to).get

        (to must beEqualTo("""{"%s":{"%s":"%s"}}""".format(k, k1, v1))) and
          (from.get(k) must beSome.like { case m =>
            m must havePair(k1 -> v1)
          })
      }
    }
  }

  object Lists {

    case class ListOption() {
      def ok = forAll(nonEmptyListOf(genOption(genJsonString))) { l =>
        val to = toJson(l).get
        val from = fromJson[List[Option[String]]](to).get

        from.toSeq must containTheSameElementsAs(l.toSeq)
      }
    }

  }

  object CaseClasses {
    import JsonUtilSpecs._

    case class OneMember() {
      def ok = forAll(genJsonString) { v =>
        val to = toJson(OneMemberData(v)).get
        val from = fromJson[OneMemberData](to).get

        (to must beEqualTo("""{"value":"%s"}""".format(v))) and
          (from must beEqualTo(OneMemberData(v)))
      }
    }

    case class TwoMemberMixedBasic() {
      def ok = forAll(genJsonString, arbitrary[Int]) { (s, i) =>
        val to = toJson(TwoMemberMixedBasicData(s, i)).get
        val from = fromJson[TwoMemberMixedBasicData](to).get

        (to must beEqualTo("""{"one":"%s","two":%d}""".format(s, i))) and
          (from must beEqualTo(TwoMemberMixedBasicData(s, i)))
      }
    }

    case class TwoMemberMixedComplex() {
      def ok = forAll(genJsonString, genJsonString, genJsonString, arbitrary[Int]) { (li1, li2, k, v) =>
        val to = toJson(TwoMemberMixedComplexData(List(li1, li2), Map(k -> v))).get
        val from = fromJson[TwoMemberMixedComplexData](to).get

        (to must beEqualTo("""{"one":["%s","%s"],"two":{"%s":%d}}""".format(li1, li2, k, v))) and
          (from must beEqualTo(TwoMemberMixedComplexData(List(li1, li2), Map(k -> v))))
      }
    }

    case class OptionalAnyValMember() {
      def ok = forAll(genJsonString, option(arbitrary[Int])) { (s, mbInt) =>
        val to = toJson(OptionalAnyValData(s, mbInt)).get
        val from = fromJson[OptionalAnyValData](to).get

        //None's should not be serialized
        val mbIntToJson: String = mbInt match {
          case None => ""
          case Some(i) => ",\"mbTwo\":" + i
        }

        (to must beEqualTo("""{"one":"%s"%s}""".format(s, mbIntToJson))) and
          (from must beEqualTo(OptionalAnyValData(s, mbInt)))
      }
    }

    case class OptionalAnyRefMember() {
      def ok = forAll(option(genJsonString), arbitrary[Int]) { (mbStr, i) =>
        val to = toJson(OptionalAnyRefData(mbStr, i)).get
        val from = fromJson[OptionalAnyRefData](to).get

        //None's should not be serialized
        val mbStrToJson = mbStr match {
          case None => ""
          case Some(str) => """"mbOne":"%s",""".format(str)
        }

        (to must beEqualTo("""{%s"two":%d}""".format(mbStrToJson, i))) and
          (from must beEqualTo(OptionalAnyRefData(mbStr, i)))
      }
    }

    case class NestedClasses() {
      def ok = forAll(genJsonString) { s =>
        val to = toJson(NestedOuter(NestedInner(s))).get
        val from = fromJson[NestedOuter](to).get

        (to must beEqualTo("""{"inner":{"one":"%s"}}""".format(s))) and
          (from must beEqualTo(NestedOuter(NestedInner(s))))
      }
    }

    private lazy val genNestedInner: Gen[NestedInner] = for {
      s <- genJsonString
    } yield NestedInner(s)

    case class OptionalNested() {
      def ok = forAll(option(genNestedInner)) { mbN =>
        val to = toJson(OptionalInner(mbN)).get
        val from = fromJson[OptionalInner](to).get

        from must beEqualTo(OptionalInner(mbN))
      }
    }

    case class ListOptionMember() {
      def ok = forAll(nonEmptyListOf(genOption(genJsonString))) { l =>
        val to = toJson(ListOptionData(l)).get
        val from = fromJson[ListOptionData](to).get

        from.l.toSeq must containTheSameElementsAs(l.toSeq)
      }
    }
  }

  object Badness {
    import JsonUtilSpecs._

    case class MalformedJson() {
      def fails = forAll(genJsonString, genJsonString) { (k, v) =>
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

    // TODO this test needs to get updated
    // Comes from finding that Jackson serializes the empty string from String -> Int as 0.
    // Also need to address that Jackson provides defaults for missing parameter matches when deserializing.
    // changing genJsonString from using nonEmptyListOf to listOf causes failure every time because it tests the empty string.
    case class MismatchedTypes() {
      def fails = {
        forAll(genJsonString, genJsonString) { (k, v) =>
          val to = toJson(Map(k -> v)).get

          // Note: if instead `v` were an Int, and `fromJson[Map[String, String]]` were used,
          // Jackson would attempt to convert the value to a String, which would be a Success
          val from = fromJson[Map[String, Int]](to)

          from must beFailedTry
        }
      }
    }

    case class MissingAnyVal() {
      def ok = forAll(genJsonString) { k =>
        val from = fromJson[TwoMemberMixedBasicData]("""{"one":"%s"}""".format(k)).get

        // this is a known Jackson quirk: missing AnyVals in case classes get instantiated at default values, e.g. 0
        from must beEqualTo(TwoMemberMixedBasicData(k, 0))
      }
    }

    case class MissingAnyRef() {
      def ok = forAll(arbitrary[Int]) { k =>
        val from = fromJson[TwoMemberMixedBasicData]("""{"two":%d}""".format(k)).get

        // this is another known Jackson quirk: missing AnyRefs in case classes come in as `null`
        from must beEqualTo(TwoMemberMixedBasicData(null, k))
      }
    }

  }

  case class ImplicitClasses() {
    import com.paypal.stingray.json._
    def success = forAll(genJsonString) { str =>
      val to = str.toJson.get
      val from = to.fromJson[String].get
      // for whatever reason, the compiler balks on string interpolation here
      // e.g. "\"$str\"" is not recognized as a valid string
      (to must beEqualTo("\"%s\"".format(str))) and (from must beEqualTo(str))
    }
  }
}

object JsonUtilSpecs {
  case class OneMemberData(value: String)
  case class TwoMemberMixedBasicData(one: String, two: Int)
  case class TwoMemberMixedComplexData(one: List[String], two: Map[String, Int])
  case class OptionalAnyValData(one: String, mbTwo: Option[Int])
  case class OptionalAnyRefData(mbOne: Option[String], two: Int)
  case class OptionalInner(mbInner: Option[NestedInner])
  case class ListOptionData(l: List[Option[String]])
  case class NestedInner(one: String)
  case class NestedOuter(inner: NestedInner)
}
