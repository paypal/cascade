package com.paypal.stingray.common.tests

import com.paypal.stingray.common.option._
import org.scalacheck.{Gen, Arbitrary}
import org.scalacheck.Arbitrary._
import java.util.UUID
import scala.util.Try
import scala.reflect.ClassTag

/**
 * Custom generators for use with ScalaCheck
 */

package object scalacheck {

  /** Generates an arbitrary [[scala.Symbol]] */
  implicit lazy val arbSymbol: Arbitrary[Symbol] = Arbitrary(arbitrary[String].map(Symbol(_)))

  /** Generates an arbitrary [[java.util.UUID]] */
  implicit lazy val arbUUID: Arbitrary[UUID] = Arbitrary(Gen.wrap(UUID.randomUUID))

  /** Generates a broken UUID */
  lazy val genInvalidUUID: Gen[String] = arbitrary[String].suchThat(s => Try(UUID.fromString(s)).isFailure)

  /** Generates a character that is either a lowercase alphabetic, or a digit */
  lazy val genAlphaLowerNumChar: Gen[Char] = Gen.frequency((9, Gen.alphaLowerChar), (1, Gen.numChar))

  /** Generates a non-empty String comprised of alphabetics */
  lazy val genNonEmptyAlphaStr: Gen[String] = Gen.nonEmptyListOf(Gen.alphaChar).map(_.mkString)
  
  /** Generates a non-empty String comprised of alphabetics and digits */
  lazy val genNonEmptyAlphaNumStr: Gen[String] =
    Gen.nonEmptyListOf(Gen.frequency((52, Gen.alphaChar), (10, Gen.numChar))).map(_.mkString)

  /**
   * Generates a List[T] of size between `min` and `max`
   * @param min fewest to generate
   * @param max most to generate
   * @param gen the generator to use for List objects
   * @tparam T the type of the generator and List
   * @return a List[T] of size between `min` and `max`
   */
  def genListWithSizeInRange[T](min: Int, max: Int, gen: Gen[T]): Gen[List[T]] = {
    for {
      n <- Gen.choose(min, max)
      lst <- Gen.listOfN(n, gen)
    } yield lst
  }

  /**
   * Generates a String of length between `min` and `max`
   * @param min fewest to generate
   * @param max most to generate
   * @param gen the char generator to use for this String
   * @return a String of length between `min` and `max
   */
  def genStringWithSizeInRange(min: Int, max: Int, gen: Gen[Char]): Gen[String] = {
    genListWithSizeInRange(min, max, gen).map(_.mkString)
  }

  /**
   * Generates a new Throwable with an arbitrary message
   * @param c implicitly, the ClassTag of the Throwable generated
   * @tparam T the type of Throwable generated
   * @return an arbitrary Throwable
   */
  def genError[T <: Throwable](implicit c: ClassTag[T]): Gen[T] = {
    Gen.alphaStr.map(s => c.runtimeClass.getConstructor(classOf[String], classOf[Throwable]).newInstance(s, null).asInstanceOf[T])
  }

  /**
   * Generates a list of arbitrary Throwables
   * @tparam T the type of Throwables generated
   * @return a List of Throwables
   */
  def genErrors[T <: Throwable : ClassTag]: Gen[List[T]] = Gen.nonEmptyListOf(genError)

  /**
   * Generates an arbitrary Option[T]
   * @param gen the generator to use for values inside options
   * @tparam T the type of Option generated
   * @return an Option[T]
   */
  def genOption[T](gen: Gen[T]): Gen[Option[T]] = gen.flatMap(g => Gen.oneOf(g.some, none[T]))

}

