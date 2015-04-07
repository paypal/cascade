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
package com.paypal.cascade.common.tests.util.casts

import org.specs2._
import com.paypal.cascade.common.option._
import com.paypal.cascade.common.util.casts._
import org.scalacheck._
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary.arbitrary
import com.paypal.cascade.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests for implicit casts in [[com.paypal.cascade.common.util.casts]]
 */

class CastSpecs extends Specification with ScalaCheck { override def is = s2"""

  The cast utilities provide a type safe way to perform casts without throwing exceptions.

  Cast an Any                                                                          ${cast().any}
  Cast any primitive                                                                   ${cast().primitive}
  Cast an Option[_]                                                                    ${cast().option}
  Cast an Array[_]                                                                     ${cast().array}
  Cast a Traversable[_]                                                                ${cast().traversable}
  Cast a List[_]                                                                       ${cast().list}
  CastIf an Any                                                                        ${castIf().any}
  CastIf any primitive                                                                 ${castIf().primitive}
  CastIf an Option[_]                                                                  ${castIf().option}
  CastIf an Array[_]                                                                   ${castIf().array}
  CastIf a Traversable[_]                                                              ${castIf().traversable}
  CastIf a List[_]                                                                     ${castIf().list}

"""

  case class castIf() extends CommonImmutableSpecificationContext {

    def any = apply {
      forAll(genFoo, genBar, arbitrary[String], arbitrary[Boolean], arbitrary[Boolean]) { (foo: Foo, bar: Bar, s: String, isNull: Boolean, bool: Boolean) =>
        val f = if (isNull) null else foo
        val b = if (isNull) null else bar
        if (isNull) {
          (f.castIf[Bar](c => bool) must beNone) and
            (f.castIf[Foo](c => bool) must beNone) and
            (b.castIf[Bar](c => bool) must beNone) and
            (b.castIf[Foo](c => bool) must beNone)
        } else {
          (f.castIf[Bar](c => bool) must beNone) and
            (f.castIf[Foo](c => bool) must beLike { case Some(c) if bool => ok; case None if !bool => ok }) and
            (b.castIf[Bar](c => bool) must beLike { case Some(c) if bool => ok; case None if !bool => ok }) and
            (b.castIf[Foo](c => bool) must beNone)
        }
      }
    }

    def primitive = apply {
      forAll(arbitrary[Boolean], arbitrary[Byte], arbitrary[Char], arbitrary[Short], arbitrary[Int], arbitrary[Long],
        arbitrary[Float], arbitrary[Double]) { (bool: Boolean, b: Byte, c: Char, s: Short, i: Int, l: Long, f: Float, d: Double) =>
        bool.castIf[Boolean](bool => bool) must beLike { case Some(r) if bool => ok; case None if !bool => ok }
        b.castIf[Byte](b => bool) must beLike { case Some(r) if bool => ok; case None if !bool => ok }
        c.castIf[Char](c => bool) must beLike { case Some(r) if bool => ok; case None if !bool => ok }
        s.castIf[Short](s => bool) must beLike { case Some(r) if bool => ok; case None if !bool => ok }
        i.castIf[Int](i => bool) must beLike { case Some(r) if bool => ok; case None if !bool => ok }
        l.castIf[Long](l => bool) must beLike { case Some(r) if bool => ok; case None if !bool => ok }
        f.castIf[Float](f => bool) must beLike { case Some(r) if bool => ok; case None if !bool => ok }
        d.castIf[Double](d => bool) must beLike { case Some(r) if bool => ok; case None if !bool => ok }
      }
    }

    def option = apply {
      forAll(genOptionFoo, genOptionBar, arbitrary[String], arbitrary[Boolean]) { (f: Option[Foo], b: Option[Bar], s: String, bool: Boolean) =>
        (f, b) match {
          case (Some(_), Some(_)) => {
            (f.castIf[Bar](c => bool) must beNone) and
              (f.castIf[Foo](c => bool) must beLike { case Some(c) if bool => ok; case None if !bool => ok }) and
              (b.castIf[Foo](c => bool) must beNone) and
              (b.castIf[Bar](c => bool) must beLike { case Some(c) if bool => ok; case None if !bool => ok })
          }
          case (Some(_), None) => {
            (f.castIf[Bar](c => bool) must beNone) and
              (f.castIf[Foo](c => bool) must beLike { case Some(c) if bool => ok; case None if !bool => ok }) and
              (b.castIf[Foo](c => bool) must beNone) and
              (b.castIf[Bar](c => bool) must beNone)
          }
          case (None, Some(_)) => {
            (f.castIf[Bar](c => bool) must beNone) and
              (f.castIf[Foo](c => bool) must beNone) and
              (b.castIf[Foo](c => bool) must beNone) and
              (b.castIf[Bar](c => bool) must beLike { case Some(c) if bool => ok; case None if !bool => ok })
          }
          case (None, None) => {
            (f.castIf[Bar](c => bool) must beNone) and
              (f.castIf[Foo](c => bool) must beNone) and
              (b.castIf[Foo](c => bool) must beNone) and
              (b.castIf[Bar](c => bool) must beNone)
          }
        }
      }
    }

    def array = apply {
      forAll(Gen.containerOf[Array, Int](arbitrary[Int]), Gen.containerOf[Array, String](arbitrary[String]), arbitrary[Int], arbitrary[String]) { (iArray: Array[Int], sArray: Array[String], i: Int, s: String) =>
        (iArray.castIf[Int](c => c > i).forall(_ > i) must beTrue) and
          (sArray.castIf[String](c => c > s).forall(_ > s) must beTrue) and
          (iArray.castIf[String](c => c > s) must beEmpty) and
          (sArray.castIf[Int](c => c > i) must beEmpty)
      }
    }

    def traversable = apply {
      forAll(Gen.containerOf[Set, Foo](genFoo), Gen.containerOf[Set, Bar](genBar), arbitrary[String]) { (fSet: Set[Foo], bSet: Set[Bar], s: String) =>
        (fSet.castIf[Foo](c => c.msg > s).forall(_.msg > s) must beTrue) and
          (bSet.castIf[Bar](c => c.msg > s).forall(_.msg > s) must beTrue) and
          (fSet.castIf[Bar](c => c.msg > s) must beEmpty) and
          (bSet.castIf[Foo](c => c.msg > s) must beEmpty)
      }
    }

    def list = apply {
      forAll(Gen.listOf(genFoo), Gen.listOf(genBar), arbitrary[String]) { (fList: List[Foo], bList: List[Bar], s: String) =>
        (fList.castIf[Foo](c => c.msg > s).forall(_.msg > s) must beTrue) and
          (bList.castIf[Bar](c => c.msg > s).forall(_.msg > s) must beTrue) and
          (fList.castIf[Bar](c => c.msg > s) must beEmpty) and
          (bList.castIf[Foo](c => c.msg > s) must beEmpty)
      }
    }

  }

  case class cast() extends CommonImmutableSpecificationContext {

    def any = apply {
      forAll(genFoo, genBar, arbitrary[Boolean]) { (foo: Foo, bar: Bar, isNull: Boolean) =>
        val f = if (isNull) null else foo
        val b = if (isNull) null else bar
        if (isNull) {
          (f.cast[Bar] must beNone) and
            (f.cast[Foo] must beNone) and
            (b.cast[Bar] must beNone) and
            (b.cast[Foo] must beNone)
        } else {
          (f.cast[Bar] must beNone) and
            (f.cast[Foo] must beSome) and
            (b.cast[Bar] must beSome) and
            (b.cast[Foo] must beNone)
        }
      }
    }

    def primitive = apply {
      forAll(arbitrary[Boolean], arbitrary[Byte], arbitrary[Char], arbitrary[Short], arbitrary[Int], arbitrary[Long],
        arbitrary[Float], arbitrary[Double]) { (bool: Boolean, b: Byte, c: Char, s: Short, i: Int, l: Long, f: Float, d: Double) =>
        bool.cast[Boolean] must beSome
        b.cast[Byte] must beSome
        c.cast[Char] must beSome
        s.cast[Short] must beSome
        i.cast[Int] must beSome
        l.cast[Long] must beSome
        f.cast[Float] must beSome
        d.cast[Double] must beSome
      }
    }

    def option = apply {
      forAll(genOptionFoo, genOptionBar) { (f: Option[Foo], b: Option[Bar]) =>
        (f, b) match {
          case (Some(_), Some(_)) => {
            (f.cast[Bar] must beNone) and
              (f.cast[Foo] must beSome) and
              (b.cast[Foo] must beNone) and
              (b.cast[Bar] must beSome)
          }
          case (Some(_), None) => {
            (f.cast[Bar] must beNone) and
              (f.cast[Foo] must beSome) and
              (b.cast[Foo] must beNone) and
              (b.cast[Bar] must beNone)
          }
          case (None, Some(_)) => {
            (f.cast[Bar] must beNone) and
              (f.cast[Foo] must beNone) and
              (b.cast[Foo] must beNone) and
              (b.cast[Bar] must beSome)
          }
          case (None, None) => {
            (f.cast[Bar] must beNone) and
              (f.cast[Foo] must beNone) and
              (b.cast[Foo] must beNone) and
              (b.cast[Bar] must beNone)
          }
        }
      }
    }

    def array = apply {
      forAll(Gen.containerOf[Array, Int](arbitrary[Int]), Gen.containerOf[Array, String](arbitrary[String])) { (iArray: Array[Int], sArray: Array[String]) =>
        (iArray.cast[Int] must beEqualTo(iArray)) and
          (sArray.cast[String] must beEqualTo(sArray)) and
          (iArray.cast[String] must beEmpty) and
          (sArray.cast[Int] must beEmpty)
      }
    }

    def traversable = apply {
      forAll(Gen.containerOf[Set, Foo](genFoo), Gen.containerOf[Set, Bar](genBar)) { (fSet: Set[Foo], bSet: Set[Bar]) =>
        (fSet.cast[Foo] must containTheSameElementsAs(fSet.toSeq)) and
          (bSet.cast[Bar] must containTheSameElementsAs(bSet.toSeq)) and
          (fSet.cast[Bar] must beEmpty) and
          (bSet.cast[Foo] must beEmpty)
      }
    }

    def list = apply {
      forAll(Gen.listOf(genFoo), Gen.listOf(genBar)) { (fList: List[Foo], bList: List[Bar]) =>
        (fList.cast[Foo] must containTheSameElementsAs(fList)) and
          (bList.cast[Bar] must containTheSameElementsAs(bList)) and
          (fList.cast[Bar] must beEmpty) and
          (bList.cast[Foo] must beEmpty)
      }
    }

  }

  lazy val genFoo: Gen[Foo] = for(msg <- arbitrary[String]) yield Foo(msg)
  lazy val genBar: Gen[Bar] = for(msg <- arbitrary[String]) yield Bar(msg)

  lazy val genOptionFoo: Gen[Option[Foo]] = Gen.oneOf(for(msg <- arbitrary[String]) yield Foo(msg).some, Gen.const(None))
  lazy val genOptionBar: Gen[Option[Bar]] = Gen.oneOf(for(msg <- arbitrary[String]) yield Bar(msg).some, Gen.const(None))

  case class Foo(msg: String = "foo")
  case class Bar(msg: String = "bar")

}
