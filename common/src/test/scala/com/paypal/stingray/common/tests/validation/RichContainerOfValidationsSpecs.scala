package com.stackmob.tests.common.validation

import org.specs2.Specification

/**
 * Created by IntelliJ IDEA.
 * 
 * com.paypal.stingray.common.tests.validation
 * 
 * User: aaron
 * Date: 12/5/12
 * Time: 2:46 PM
 */
class RichContainerOfValidationsSpecs extends Specification { def is =
  "RichContainerOfValidationsSpecs".title                                                                               ^
  "RichContainerOfValidations is a pimp on a M[Validation[E, S]]"                                                       ^
  "the ValidationNel function should work correctly"                                                                    ! ValidationNelWorks ^ end

  private def ValidationNelWorks = {
    import scalaz._
    import Scalaz._
    import com.paypal.stingray.common.validation._

    type V = List[Validation[Int, String]]
    val baseIntList = 1 :: 2 :: Nil
    val baseStringList = baseIntList.map(_.toString)

    val listOfAllSuccesses: V = baseStringList.map(_.success[Int])
    val listOfAllFailures: V = baseIntList.map(_.fail[String])
    val listOfSomeFailures: V = baseIntList.map(_.fail[String]) ++ baseStringList.map(_.success[Int])

    (listOfAllSuccesses.ValidationNel.toEither must beRight.like {
      case successList => successList must beEqualTo(baseStringList)
    }) and
    (listOfAllFailures.ValidationNel.toEither must beLeft.like {
      case failNonEmptyList => failNonEmptyList.list must beEqualTo(baseIntList)
    }) and
    (listOfSomeFailures.ValidationNel.toEither must beLeft.like {
      case failNonEmptyList => failNonEmptyList.list must beEqualTo(baseIntList)
    })
  }
}
