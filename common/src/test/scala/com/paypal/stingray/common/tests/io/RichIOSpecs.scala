package com.paypal.stingray.common.tests.io

import scalaz._
import Scalaz._
import scalaz.effect._
import org.specs2.Specification
import org.specs2.execute.{Failure => SpecsFailure, Result => SpecsResult}
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext
import com.paypal.stingray.common.io._
import com.paypal.stingray.common.logging.LoggingSugar

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.common.tests.io
 *
 * User: aaron
 * Date: 6/5/12
 * Time: 9:56 AM
 */

class RichIOSpecs extends Specification { def is =
  "RichIOSpecs".title                                                                                                   ^
  """
  RichIO is StackMob's pimp for the IO construct
  """                                                                                                                   ^
  "RichIO#validation should"                                                                                            ^
    "return a Failure(t) when the IO throws"                                                                            ! ThrowingIO().validationReturnsFailure ^
    "return a Success(s) when the IO doesn't throw"                                                                     ! SuccessfulIO().validationReturnsSuccess ^
                                                                                                                        end

  trait Context extends CommonImmutableSpecificationContext with LoggingSugar

  case class ThrowingIO() extends Context {
    private val ex = new Exception("thrower")
    private val thrower: IO[Int] = ((throw ex): Int).pure[IO]
    def validationReturnsFailure: SpecsResult = thrower.toValidation.unsafePerformIO().map { _ =>
      SpecsFailure("validation succeeded when it shoudl have failed")
    } valueOr { t: Throwable =>
      t must beEqualTo(ex)
    }
  }

  case class SuccessfulIO() extends Context {
    private val logger = getLogger[SuccessfulIO]
    private val v = 22
    def validationReturnsSuccess: SpecsResult = v.pure[IO].toValidation.unsafePerformIO().map { i: Int =>
      (i must beEqualTo(v)): SpecsResult
    } valueOr { t: Throwable =>
      logger.warn(t.getMessage, t)
      SpecsFailure("validation failed with exception %s".format(t.getMessage))
    }
  }

}
