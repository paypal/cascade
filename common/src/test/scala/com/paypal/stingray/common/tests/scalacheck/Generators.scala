package com.paypal.stingray.common.tests.scalacheck

import com.paypal.stingray.common.enumeration._
import com.paypal.stingray.common.env.StingrayEnvironmentType
import com.paypal.stingray.common.option._
import org.scalacheck.{Gen, Arbitrary}
import org.scalacheck.Arbitrary._
//import org.scalacheck.Gen._
import java.util.UUID
import scala.util.Try

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 6/26/12
 * Time: 5:25 PM
 */

trait Generators {

  implicit lazy val arbSymbol: Arbitrary[Symbol] = Arbitrary(arbitrary[String].map(Symbol(_)))

  implicit lazy val arbUUID: Arbitrary[UUID] = Arbitrary(Gen.wrap(UUID.randomUUID))

  lazy val genInvalidUUID: Gen[String] = arbitrary[String].suchThat(s => Try(UUID.fromString(s)).isFailure)

  lazy val genEnvType: Gen[StingrayEnvironmentType] = Gen.oneOf(
    StingrayEnvironmentType.DEVELOPMENT,
    StingrayEnvironmentType.PRODUCTION,
    StingrayEnvironmentType.STAGING
  )

  lazy val genInvalidEnvType: Gen[String] = arbitrary[String].suchThat(_.readEnum[StingrayEnvironmentType].isEmpty)

  lazy val genAlphaLowerNumChar: Gen[Char] = Gen.frequency((9, Gen.alphaLowerChar), (1, Gen.numChar))

  lazy val genNonEmptyAlphaStr: Gen[String] = Gen.nonEmptyListOf(Gen.alphaChar).map(_.mkString)

  def genListWithSizeInRange[T](min: Int, max: Int, gen: Gen[T]): Gen[List[T]] = {
    for {
      n <- Gen.choose(min, max)
      lst <- Gen.listOfN(n, gen)
    } yield lst
  }

  def genStringWithSizeInRange(min: Int, max: Int, gen: Gen[Char]): Gen[String] = {
    genListWithSizeInRange(min, max, gen).map(_.mkString)
  }

  def genError[T <: Throwable](implicit m: Manifest[T]): Gen[T] = {
    Gen.alphaStr.map(s => m.runtimeClass.getConstructor(classOf[String], classOf[Throwable]).newInstance(s, null).asInstanceOf[T])
  }

  def genErrors[T <: Throwable : Manifest]: Gen[List[T]] = Gen.nonEmptyListOf(genError)

  def genOption[T](gen: Gen[T]): Gen[Option[T]] = gen.flatMap(g => Gen.oneOf(g.some, none[T]))

}

