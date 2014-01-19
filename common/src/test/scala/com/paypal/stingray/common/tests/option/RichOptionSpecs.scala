package com.paypal.stingray.common.tests.option

import org.specs2.Specification
import org.specs2.execute.{Result => SpecsResult}
import com.paypal.stingray.common.option._
import java.util.concurrent.atomic.AtomicInteger
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.common.tests.option
 *
 * User: aaron
 * Date: 5/18/12
 * Time: 7:58 PM
 */

class RichOptionSpecs extends Specification { def is =
  "RichOptionSpecs".title                                                                                               ^
  """
  RichOption is a wrapper for Option[T] types
  """                                                                                                                   ^
  "sideEffectNone should"                                                                                               ^
    "execute the given function only if the contained option is None"                                                   ! SideEffectNone().executesOnlyIfNone ^
    "execute the given function only if the contained option is Some"                                                   ! SideEffectSome().executesOnlyIfSome ^
                                                                                                                        end ^
  "orThrow should"                                                                                                      ^
    "throw only if the contained option is None"                                                                        ! OrThrow().throwsOnlyIfNone ^
                                                                                                                        end

  trait Context extends CommonImmutableSpecificationContext {
    protected val someValue = 20
    protected def makeRichOption[T](opt: Option[T]): RichOption[T] = new RichOption[T](opt)

    protected def makeRichOption[T]: RichOption[T] = makeRichOption(Option.empty[T])
    protected def makeRichOption[T](t: T): RichOption[T] = makeRichOption(t.some)
  }

  case class SideEffectNone() extends Context {
    def executesOnlyIfNone: SpecsResult = {
      val int = new AtomicInteger(0)
      val someValue = 20
      val wrappedNone = makeRichOption[Int]
      val wrappedSome = makeRichOption(someValue)
      wrappedNone.sideEffectNone { int.set(1) }
      wrappedSome.sideEffectNone { int.set(5) }
      int.get must beEqualTo(1)
    }
  }

  case class SideEffectSome() extends Context {
    def executesOnlyIfSome: SpecsResult = {
      val int = new AtomicInteger(0)
      val someValue = 20
      val wrappedNone = makeRichOption[Int]
      val wrappedSome = makeRichOption(someValue)
      wrappedNone.sideEffectSome { _ => int.set(-1) }
      wrappedSome.sideEffectSome { x => int.set(x) }
      int.get must beEqualTo(20)
    }
  }

  case class OrThrow() extends Context {
    def throwsOnlyIfNone: SpecsResult = {
      val someValue = 20
      val wrappedNone = makeRichOption[Int]
      val wrappedSome = makeRichOption(someValue)
      val exception = new Exception(getClass.getCanonicalName)
      (wrappedNone orThrow(exception) must throwA(exception)) and
      (wrappedSome orThrow(exception) must not throwA(exception)) and
      (wrappedSome orThrow(exception) must beEqualTo(someValue))
    }
  }

}
