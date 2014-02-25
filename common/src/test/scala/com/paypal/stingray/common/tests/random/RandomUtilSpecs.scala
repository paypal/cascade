package com.paypal.stingray.common.tests.random

import org.specs2._
import org.scalacheck.Gen._
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary._
import com.paypal.stingray.common.random.RandomUtil

/**
 * Tests for [[com.paypal.stingray.common.random]]
 */
class RandomUtilSpecs extends Specification with ScalaCheck { def is = s2"""

  RandomUtil provides convenience for choosing random values from a list.

  pickRandomValue should return a value in the provided list                  ${RandomValue().pickInt}
  pickRandomValue(map, convert) should return a converted value from the map  ${RandomValueFromMap().pickValueAndConvert}

"""

  case class RandomValue() {

    def pickInt = forAll(nonEmptyListOf(arbitrary[Int])) { list =>
      val randomPick = RandomUtil.pickRandomValue(list)
      list must contain(randomPick.get)
    }
  }

  case class RandomValueFromMap() {

    def pickValueAndConvert = forAll(nonEmptyMap(arbitrary[(String, String)])) { genMap =>
      val allValues = genMap.map(_._2.toUpperCase())
      val convert = { value: String =>
        Option(value.toUpperCase())
      }
      val randomPick = RandomUtil.pickRandomValue(genMap, convert)

      allValues must contain(randomPick.get)
    }
  }

}
