package com.paypal.stingray.common.tests

import com.paypal.stingray.common.option._
import org.scalacheck.{Gen, Arbitrary}
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
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

  // an optimization over letting Scalacheck try to create these objects
  private lazy val unicodeChars = (Character.MIN_VALUE to Character.MAX_VALUE).filter(Character.isDefined(_)).toArray

  // all used inside `isAUnicodeControlChar`
  private lazy val unicodeC0C1ControlCodes = 0x007F :: (0x0000 to 0x001F).toList
  private lazy val unicodeNewlines = (0x2028 to 0x2029).toList
  private lazy val unicodeInterlinearNotation = (0xFFF9 to 0xFFFB).toList
  private lazy val unicodeBidirectionalTextControl = List(0x200E, 0x200F) ++ (0x202A to 0x202E).toList
  private lazy val unicodeVariationSelectors = (0xFE00 to 0xFE0F).toList
  private lazy val unicodeVariationSupplement = (0xE0100 to 0xE01EF).toList
  private lazy val unicodeMongolianFree = (0x180B to 0x180E).toList ++ List(0x200C, 0x200D)

  /**
   * Tests whether `i` is an Int value for a Unicode control character.
   * See http://en.wikipedia.org/wiki/Unicode_control_characters
   * @param i the value
   * @return whether it is a control character
   */
  def isAUnicodeControlChar(i: Int): Boolean = {
    unicodeC0C1ControlCodes.contains(i) ||
      unicodeNewlines.contains(i) ||
      unicodeInterlinearNotation.contains(i) ||
      unicodeBidirectionalTextControl.contains(i) ||
      unicodeVariationSelectors.contains(i) ||
      unicodeVariationSupplement.contains(i) ||
      unicodeMongolianFree.contains(i)
  }

  // similarly, an optimization over Scalacheck
  private lazy val jsonChars = unicodeChars.filter(c => !isAUnicodeControlChar(c) && c != '\\' && c != '\"')

  /** Any legal JSON character: any Unicode, non-control, not a double-quote, not a backslash */
  lazy val genJsonChar: Gen[Char] = choose(0, jsonChars.size - 1).map(jsonChars(_))

  /** Legal JSON strings */
  lazy val genJsonString: Gen[String] = nonEmptyListOf(genJsonChar).map(_.mkString)

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
    Gen.alphaStr.map(s =>
      c.runtimeClass.getConstructor(classOf[String], classOf[Throwable]).newInstance(s, null).asInstanceOf[T])
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

