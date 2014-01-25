package com.paypal.stingray.common.tests.option

import org.specs2.Specification
import com.paypal.stingray.common.option._
import java.util.concurrent.atomic.AtomicInteger
import org.specs2.execute.{Result => SpecsResult}
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests for implicit [[com.paypal.stingray.common.option.RichOptionTuple]]
 */

class RichOptionTupleSpecs extends Specification { def is =
  "RichOptionTupleSpecs".title                                                                                          ^
  """
  RichOptionTuple is a wrapper for (Option[T], Option[U]) types (ie: 2-tuples of options)
  """                                                                                                                   ^
  "fold should"                                                                                                         ^
    "execute exactly one function according to the defined permutation of Options"                                      ! Fold().accordingToCorrectPermutation
                                                                                                                        end
  trait Context extends CommonImmutableSpecificationContext {
    protected def makeRichOptionTuple[T, U](opt1: Option[T], opt2: Option[U]) = new RichOptionTuple[T, U](opt1 -> opt2)

    protected val bothRes = 111
    protected def bothFn(i: Int, j: Int) = {
      int.incrementAndGet()
      bothRes
    }

    protected val leftRes = 222
    protected def leftFn(i: Int) = {
      int.incrementAndGet()
      leftRes
    }

    protected val rightRes = 333
    protected def rightFn(j: Int) = {
      int.incrementAndGet()
      rightRes
    }

    protected val noneRes = 444
    protected def noneFn = {
      int.incrementAndGet()
      noneRes
    }

    protected val int = new AtomicInteger(0)
    protected val leftVal = 1
    protected val rightVal = 2
    protected val bothW = makeRichOptionTuple(leftVal.some, rightVal.some)
    protected val leftW = makeRichOptionTuple(leftVal.some, Option.empty[Int])
    protected val rightW = makeRichOptionTuple(Option.empty[Int], rightVal.some)
    protected val noneW = makeRichOptionTuple(Option.empty[Int], Option.empty[Int])

    protected def doFold(optTuple: RichOptionTuple[Int, Int]): Int = {
      optTuple.fold(bothFn, leftFn, rightFn, noneFn)
    }
  }


  case class Fold() extends Context {
    def accordingToCorrectPermutation: SpecsResult = {
      int.get.must(beEqualTo(0)) and
      doFold(bothW).must(beEqualTo(bothRes)) and
      int.get.must(beEqualTo(1)) and
      doFold(leftW).must(beEqualTo(leftRes)) and
      int.get.must(beEqualTo(2)) and
      doFold(rightW).must(beEqualTo(rightRes)) and
      int.get.must(beEqualTo(3)) and
      doFold(noneW).must(beEqualTo(noneRes)) and
      int.get.must(beEqualTo(4))
    }
  }
}
