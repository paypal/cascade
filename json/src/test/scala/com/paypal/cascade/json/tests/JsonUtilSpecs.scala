/**
 * Copyright 2013-2015 PayPal
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
package com.paypal.cascade.json.tests

import scala.util.Try

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalacheck.Gen._
import org.scalacheck.Prop._
import org.specs2._

import com.paypal.cascade.common.tests.scalacheck._
import com.paypal.cascade.json._

/**
 * Tests that exercise methods in [[com.paypal.cascade.json.JsonUtil]] by way of
 * implicit wrapper methods in the [[com.paypal.cascade.json]] package object.
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
    a case class containing a single polymorphic member                      ${CaseClasses.OneMemberAny().ok}
    a case class containing multiple members of mixed basic types            ${CaseClasses.TwoMemberMixedBasic().ok}
    a case class containing mutliple members of mixed complex types          ${CaseClasses.TwoMemberMixedComplex().ok}
    a case class containing an optional AnyVal type                          ${CaseClasses.OptionalAnyValMember().ok}
    a case class containing an optional AnyRef type                          ${CaseClasses.OptionalAnyRefMember().ok}
    a case class containing another case class                               ${CaseClasses.NestedClasses().ok}
    a case class containing an optional case class                           ${CaseClasses.OptionalNested().ok}
    a case class containing a list of options                                ${CaseClasses.ListOptionMember().ok}

  JsonUtil should
    not deserialize malformed json                                           ${Badness.MalformedJson().fails}
    not deserialize json that is type mismatched                             ${Badness.MismatchedTypes().fails}
    not convert between incompatible types                                   ${Badness.BadValueConversions().fails}
    deserialize null from Any with None, and deserialize Some(_) otherwise   ${Badness.AnyToNullConversion().ok}
    deserialize json that is missing an AnyVal, with a default value         ${Badness.MissingAnyVal().ok}
    deserialize json that is missing an AnyRef, with a null value            ${Badness.MissingAnyRef().ok}

  """

  object BasicTypes {

    private def basicMatcher[T : Manifest](in: T, expectedJson: String) = {
      val to = in.toJson.get
      val from = to.fromJson[T].get
      val toConvert: Any = from.asInstanceOf[Any]
      val converted = toConvert.convertValue[T].get
      (to must beEqualTo(expectedJson)) and
        (from must beEqualTo(in)) and
        (converted must beEqualTo(from))
    }

    case class Strings() {
      def ok = forAll(genJsonString) { str =>
        basicMatcher(str, "\"%s\"".format(str)) }
    }

    case class Ints() {
      def ok = forAll(arbitrary[Int]) { i =>
        basicMatcher(i, i.toString) }
    }

    case class Longs() {
      def ok = forAll(arbitrary[Long]) { i =>
        basicMatcher(i, i.toString) }
    }

    case class Floats() {
      def ok = forAll(arbitrary[Float]) { i =>
        basicMatcher(i, i.toString) }
    }

    case class Doubles() {
      def ok = forAll(arbitrary[Double]) { i =>
        basicMatcher(i, i.toString) }
    }

    case class Dates() {
      def ok = forAll(arbitrary[DateTime]) { dt =>
        basicMatcher(dt, "\"%s\"".format(ISODateTimeFormat.dateTime().print(dt))) }
    }
  }

  object Maps {

    private def basicMapsMatcher[T : Manifest, U : Manifest](obj: Map[T, U], expectedJson: String) = {
      val to = obj.toJson.get
      val from = to.fromJson[Map[T, U]].get
      val toConvert: Any = from.asInstanceOf[Any]
      val converted = toConvert.convertValue[Map[T, U]].get

      (to must beEqualTo(expectedJson)) and
        (from.toSeq must containTheSameElementsAs(obj.toSeq)) and
        (converted.toSeq must containTheSameElementsAs(from.toSeq))
    }

    private def listJson(lst: List[String]): String = {
      val inner = (for {
        li <- lst
      } yield """"%s"""".format(li)).mkString(",")
      s"[$inner]"
    }

    case class StringToString() {
      def ok = forAll(genJsonString, genJsonString) { (k, v) =>
        basicMapsMatcher(Map(k -> v), """{"%s":"%s"}""".format(k, v))
      }
    }

    case class StringToInt() {
      def ok = forAll(genJsonString, arbitrary[Int]) { (k, v) =>
        basicMapsMatcher(Map(k -> v), """{"%s":%d}""".format(k, v))
      }
    }

    case class StringToListString() {
      def ok = forAll(genJsonString, nonEmptyListOf(genJsonString)) { (k, l) =>
        val to = Map(k -> l).toJson.get
        val from = to.fromJson[Map[String, List[String]]].get
        val toConvert: Any = from.asInstanceOf[Any]
        val converted = toConvert.convertValue[Map[String, List[String]]].get

        (to must beEqualTo("""{"%s":%s}""".format(k, listJson(l)))) and
          (from.get(k) must beSome.like { case lst =>
            lst.toSeq must containTheSameElementsAs(l.toSeq)
          }) and
          (converted must beEqualTo(from))
      }
    }

    case class StringToListListString() {
      def ok = forAll(genJsonString, nonEmptyListOf(nonEmptyListOf(genJsonString))) { (k, l) =>
        val to = Map(k -> l).toJson.get
        val from = to.fromJson[Map[String, List[List[String]]]].get
        val toConvert: Any = from.asInstanceOf[Any]
        val converted = toConvert.convertValue[Map[String, List[List[String]]]].get

        val innerListString = (for {
          innerList <- l
        } yield listJson(innerList)).mkString(",")

        (to must beEqualTo("""{"%s":[%s]}""".format(k, innerListString))) and
          (from.get(k) must beSome.like { case lst =>
            lst.toSeq must containTheSameElementsAs(l.toSeq)
          }) and
          (converted must beEqualTo(from))
      }
    }

    case class StringToListInt() {
      def ok = forAll(genJsonString, nonEmptyListOf(arbitrary[Int])) { (k, l) =>
        val to = Map(k -> l).toJson.get
        val from = to.fromJson[Map[String, List[Int]]].get
        val toConvert: Any = from.asInstanceOf[Any]
        val converted = toConvert.convertValue[Map[String, List[Int]]].get

        val listJson = (for {
          li <- l
        } yield s"$li").mkString(",")

        (to must beEqualTo("""{"%s":[%s]}""".format(k, listJson))) and
          (from.get(k) must beSome.like { case lst =>
            lst.toSeq must containTheSameElementsAs(l.toSeq)
          }) and
          (converted must beEqualTo(from))
      }
    }

    case class StringToMapStringString() {
      def ok = forAll(genJsonString, genJsonString, genJsonString) { (k, k1, v1) =>
        val to = Map(k -> Map(k1 -> v1)).toJson.get
        val from = to.fromJson[Map[String, Map[String, String]]].get
        val toConvert: Any = from.asInstanceOf[Any]
        val converted = toConvert.convertValue[Map[String, Map[String, String]]].get

        (to must beEqualTo("""{"%s":{"%s":"%s"}}""".format(k, k1, v1))) and
          (from.get(k) must beSome.like { case m =>
            m must havePair(k1 -> v1)
          }) and
          (converted must beEqualTo(from))
      }
    }
  }

  object Lists {

    case class ListOption() {
      def ok = forAll(nonEmptyListOf(genOption(genJsonString))) { l =>
        val to = l.toJson.get
        val from = to.fromJson[List[Option[String]]].get
        val toConvert: Any = from.asInstanceOf[Any]
        val converted = toConvert.convertValue[List[Option[String]]].get

        (from.toSeq must containTheSameElementsAs(l.toSeq)) and
          (converted must beEqualTo(from))
      }
    }

  }

  object CaseClasses {
    import com.paypal.cascade.json.tests.JsonUtilSpecs._

    private def caseClassMatcher[T : Manifest](obj: T, expectedJson: String) = {
      val to = obj.toJson.get
      val from = to.fromJson[T].get
      val toConvert: Any = from.asInstanceOf[Any]
      val converted = toConvert.convertValue[T].get

      (to must beEqualTo(expectedJson)) and
        (from must beEqualTo(obj)) and
        (converted must beEqualTo(from))
    }

    case class OneMember() {
      def ok = forAll(genJsonString) { v =>
        caseClassMatcher(OneMemberData(v), """{"value":"%s"}""".format(v))
      }
    }

    case class OneMemberAny() {
      def ok = forAll(genJsonString, arbitrary[Int], option(genJsonString)) { (v, i, mbS) =>
        val stringData = OneMemberAnyData(v)//, """{"value":"%s"}""".format(v)) and
        val intData = OneMemberAnyData(i)//, """{"value":%d}""".format(v))
        val nestedData = OneMemberAnyData(stringData)
        val optionalAnyData = OneMemberAnyData(OneMemberOptionalAnyData(mbS))

        // these force a JSON conversion ping-pong, going to and from a JSON String,
        // to make sure that we lose the original input data type along the way,
        // and are forced to convert it out with `convertValue`
        val sdJson = stringData.toJson.get.fromJson[OneMemberAnyData].get.value.convertValue[String].get
        val idJson = intData.toJson.get.fromJson[OneMemberAnyData].get.value.convertValue[Int].get
        val ndJson = nestedData.toJson.get.fromJson[OneMemberAnyData].get.value.convertValue[OneMemberAnyData].get
        val ndJsonInner = ndJson.value.convertValue[String].get

        // force a two-step conversion
        val optionalAnyDataJson = optionalAnyData.toJson.get.fromJson[OneMemberAnyData].get
        val optionalAnyDataInnerJson = optionalAnyDataJson.value.convertValue[OneMemberOptionalAnyData].get
        val optionalAnyDataJsonValue = optionalAnyDataInnerJson.value.convertValue[Option[String]].get

        (sdJson must beEqualTo(v)) and
          (idJson must beEqualTo(i)) and
          (ndJson must beEqualTo(stringData)) and
          (ndJsonInner must beEqualTo(v)) and
          (optionalAnyDataJsonValue must beEqualTo(mbS))
      }
    }

    case class TwoMemberMixedBasic() {
      def ok = forAll(genJsonString, arbitrary[Int]) { (s, i) =>
        caseClassMatcher(TwoMemberMixedBasicData(s, i), """{"one":"%s","two":%d}""".format(s, i))
      }
    }

    case class TwoMemberMixedComplex() {
      def ok = forAll(genJsonString, genJsonString, genJsonString, arbitrary[Int]) { (li1, li2, k, v) =>
        caseClassMatcher(
          TwoMemberMixedComplexData(List(li1, li2), Map(k -> v)),
          """{"one":["%s","%s"],"two":{"%s":%d}}""".format(li1, li2, k, v))
      }
    }

    case class OptionalAnyValMember() {
      def ok = forAll(genJsonString, option(arbitrary[Int])) { (s, mbInt) =>

        //None's should not be serialized
        val mbIntToJson: String = mbInt match {
          case None => ""
          case Some(i) => ",\"mbTwo\":" + i
        }

        caseClassMatcher(OptionalAnyValData(s, mbInt), """{"one":"%s"%s}""".format(s, mbIntToJson))
      }
    }

    case class OptionalAnyRefMember() {
      def ok = forAll(option(genJsonString), arbitrary[Int]) { (mbStr, i) =>

        //None's should not be serialized
        val mbStrToJson = mbStr match {
          case None => ""
          case Some(str) => """"mbOne":"%s",""".format(str)
        }

        caseClassMatcher(OptionalAnyRefData(mbStr, i), """{%s"two":%d}""".format(mbStrToJson, i))
      }
    }

    case class NestedClasses() {
      def ok = forAll(genJsonString) { s =>
        caseClassMatcher(NestedOuter(NestedInner(s)), """{"inner":{"one":"%s"}}""".format(s))
      }
    }

    private lazy val genNestedInner: Gen[NestedInner] = for {
      s <- genJsonString
    } yield NestedInner(s)

    case class OptionalNested() {
      def ok = forAll(option(genNestedInner)) { mbN =>
        val to = OptionalInner(mbN).toJson.get
        val from = to.fromJson[OptionalInner].get
        val toConvert: Any = from.asInstanceOf[Any]
        val converted = toConvert.convertValue[OptionalInner].get

        (from must beEqualTo(OptionalInner(mbN))) and
          (converted must beEqualTo(from))
      }
    }

    case class ListOptionMember() {
      def ok = forAll(nonEmptyListOf(genOption(genJsonString))) { l =>
        val to = ListOptionData(l).toJson.get
        val from = to.fromJson[ListOptionData].get
        val toConvert: Any = from.asInstanceOf[Any]
        val converted = toConvert.convertValue[ListOptionData].get

        (from.l.toSeq must containTheSameElementsAs(l.toSeq)) and
          (converted must beEqualTo(from))
      }
    }
  }

  object Badness {
    import com.paypal.cascade.json.tests.JsonUtilSpecs._

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

        (unquotedKey.fromJson[Map[String, String]] must beFailedTry) and
          (partiallyQuotedKey1.fromJson[Map[String, String]] must beFailedTry) and
          (partiallyQuotedKey2.fromJson[Map[String, String]] must beFailedTry) and
          (unquotedStringValue.fromJson[Map[String, String]] must beFailedTry) and
          (partiallyQuotedStringValue1.fromJson[Map[String, String]] must beFailedTry) and
          (partiallyQuotedStringValue2.fromJson[Map[String, String]] must beFailedTry) and
          (noOpeningBrace.fromJson[Map[String, String]] must beFailedTry) and
          (noClosingBrace.fromJson[Map[String, String]] must beFailedTry) and
          (unfinishedInnerList1.fromJson[Map[String, List[String]]] must beFailedTry) and
          (unfinishedInnerList2.fromJson[Map[String, List[String]]] must beFailedTry) and
          (unfinishedInnerMap1.fromJson[Map[String, Map[String, String]]] must beFailedTry) and
          (unfinishedInnerMap2.fromJson[Map[String, Map[String, String]]] must beFailedTry) and
          (unfinishedInnerMap3.fromJson[Map[String, Map[String, String]]] must beFailedTry)
      }
    }

    // TODO this test needs to get updated
    // Comes from finding that Jackson serializes the empty string from String -> Int as 0.
    // Also need to address that Jackson provides defaults for missing parameter matches when deserializing.
    // changing genJsonString from using nonEmptyListOf to listOf causes failure every time because it tests the empty string.
    case class MismatchedTypes() {
      def fails = {
        forAll(
          genJsonString,
          genJsonString.suchThat(s => Try {Integer.decode(s)}.isFailure)) { (k, v) =>
          val to = Map(k -> v).toJson.get

          // Note: if instead `v` were an Int, and `fromJson[Map[String, String]]` were used,
          // Jackson would attempt to convert the value to a String, which would be a Success
          val from = to.fromJson[Map[String, Int]]

          val toConvert: Any = from.asInstanceOf[Any]
          val converted = toConvert.convertValue[Map[String, Int]]

          (from must beFailedTry) and (converted must beFailedTry)
        }
      }
    }

    // There are probably more variations that could be tested here.
    case class BadValueConversions() {
      def fails = {
        forAll(
          genJsonString,
          nonEmptyListOf(arbitrary[Int]),
          nonEmptyListOf(genJsonString.suchThat(s => Try {Integer.decode(s)}.isFailure))) { (s, il, sl) =>
          val omd = OneMemberData(s)

          /*
           We can't test List[Int] => List[String] because of
           the aforementioned quirk regarding converting Int to String.

           What happens instead is that the List[Int] gets successfully converted
           to a List[String] because Jackson knows that Ints have a String
           representation. This is a known quirk, and we test for it elsewhere.
           */

          (omd.asInstanceOf[Any].convertValue[String] must beFailedTry) and
            (omd.asInstanceOf[Any].convertValue[List[Int]] must beFailedTry) and
            (omd.asInstanceOf[Any].convertValue[List[String]] must beFailedTry) and
            (s.asInstanceOf[Any].convertValue[OneMemberData] must beFailedTry) and
            (s.asInstanceOf[Any].convertValue[List[Int]] must beFailedTry) and
            (s.asInstanceOf[Any].convertValue[List[String]] must beFailedTry) and
            (il.asInstanceOf[Any].convertValue[OneMemberData] must beFailedTry) and
            (il.asInstanceOf[Any].convertValue[String] must beFailedTry) and
            (sl.asInstanceOf[Any].convertValue[OneMemberData] must beFailedTry) and
            (sl.asInstanceOf[Any].convertValue[String] must beFailedTry) and
            (sl.asInstanceOf[Any].convertValue[List[Int]] must beFailedTry)
        }
      }
    }

    case class AnyToNullConversion() {
      def ok = {
        forAll(option(genJsonString)) { mbS =>
          val data = OneMemberAnyData(mbS)
          val json = data.toJson.get.fromJson[OneMemberAnyData].get
          val value = json.value.convertValue[Option[String]].get

          /*
           This was discovered during testing: if you serialize a None, which with the settings in
           this module are simply not serialized, and then deserialize it to an Any, Jackson doesn't know
           what to do and will deserialize a null instead.

           If that behavior is fixed in the future, this test will begin failing. :)
           */
          mbS match {
            case Some(s) => value must beSome.like { case v: String =>
              v must beEqualTo(s)
            }
            case None => value must beNull
          }
        }
      }
    }

    case class MissingAnyVal() {
      def ok = forAll(genJsonString) { k =>
        val from = """{"one":"%s"}""".format(k).fromJson[TwoMemberMixedBasicData].get

        // this is a known Jackson quirk: missing AnyVals in case classes get instantiated at default values, e.g. 0
        from must beEqualTo(TwoMemberMixedBasicData(k, 0))
      }
    }

    case class MissingAnyRef() {
      def ok = forAll(arbitrary[Int]) { k =>
        val from = """{"two":%d}""".format(k).fromJson[TwoMemberMixedBasicData].get

        // this is another known Jackson quirk: missing AnyRefs in case classes come in as `null`
        from must beEqualTo(TwoMemberMixedBasicData(null, k))
      }
    }
  }
}

object JsonUtilSpecs {
  case class OneMemberData(value: String)
  case class OneMemberAnyData(value: Any)
  case class OneMemberOptionalAnyData(value: Option[Any])
  case class TwoMemberMixedBasicData(one: String, two: Int)
  case class TwoMemberMixedComplexData(one: List[String], two: Map[String, Int])
  case class OptionalAnyValData(one: String, mbTwo: Option[Int])
  case class OptionalAnyRefData(mbOne: Option[String], two: Int)
  case class OptionalInner(mbInner: Option[NestedInner])
  case class ListOptionData(l: List[Option[String]])
  case class NestedInner(one: String)
  case class NestedOuter(inner: NestedInner)
}
