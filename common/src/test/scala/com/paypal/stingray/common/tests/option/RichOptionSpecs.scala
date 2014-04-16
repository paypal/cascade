package com.paypal.stingray.common.tests.option

import org.specs2.Specification
import com.paypal.stingray.common.option._
import java.util.concurrent.atomic.AtomicInteger
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests for implicit [[com.paypal.stingray.common.option.RichOption]]
 */

class RichOptionSpecs extends Specification { override def is = s2"""
  RichOption is a wrapper for Option[T] types

  executeIfNone should
    exeute the given function only if the contained option is None    ${ExecuteIfNone().executesOnlyIfNone}

  sideEffectNone should
    execute the given function only if the contained option is None   ${SideEffectNone().executesOnlyIfNone}
    execute the given function only if the contained option is Some   ${SideEffectSome().executesOnlyIfSome}

  orThrow should
    throw only if the contained option is None                        ${OrThrow().throwsOnlyIfNone}

  toFuture should
    yield a future with the options value if some                     ${ToFuture().returnsValue}
    yield a future with Throwable as its failure state if none        ${ToFuture().returnsThrowable}

  Convenience method none[T] on option works                          ${None().works}
  """

  trait Context extends CommonImmutableSpecificationContext {
    protected val someValue = 20
  }

  case class ExecuteIfNone() extends Context {
    def executesOnlyIfNone = apply {
      val int = new AtomicInteger(0)
      val wrappedNone = Option.empty[Int]
      val wrappedSome = Option(someValue)
      wrappedNone.executeIfNone { int.set(1) }
      wrappedSome.executeIfNone { int.set(5) }
      int.get must beEqualTo(1)
    }
  }

  case class SideEffectNone() extends Context {
    def executesOnlyIfNone = apply {
      val int = new AtomicInteger(0)
      val wrappedNone = Option.empty[Int]
      val wrappedSome = Option(someValue)
      wrappedNone.sideEffectNone { int.set(1) }
      wrappedSome.sideEffectNone { int.set(5) }
      int.get must beEqualTo(1)
    }
  }

  case class SideEffectSome() extends Context {
    def executesOnlyIfSome = apply {
      val int = new AtomicInteger(0)
      val wrappedNone = Option.empty[Int]
      val wrappedSome = Option(someValue)
      wrappedNone.sideEffectSome { _ => int.set(-1) }
      wrappedSome.sideEffectSome { x => int.set(x) }
      int.get must beEqualTo(someValue)
    }
  }

  case class OrThrow() extends Context {
    def throwsOnlyIfNone = apply {
      val wrappedNone = Option.empty[Int]
      val wrappedSome = Option(someValue)
      val exception = new Exception(getClass.getCanonicalName)
      (wrappedNone.orThrow(exception) must throwA(exception)) and
      (wrappedSome.orThrow(exception) must not throwA(exception)) and
      (wrappedSome.orThrow(exception) must beEqualTo(someValue))
    }
  }

  case class ToFuture() extends Context {
    def returnsValue = apply {
      val wrappedSome = Option(someValue)
      val successState = wrappedSome.toFuture(new Exception("test exception"))
      successState must beEqualTo(someValue).await
    }

    def returnsThrowable = apply {
      val wrappedNone = Option.empty[Int]
      val ex = new Exception("test exception")
      val failureState = wrappedNone.toFuture(ex)
      val result = failureState.value.get
      result must beFailedTry[Int].withThrowable[Exception](ex.getMessage)
    }
  }

  case class None() extends Context {
    def works = apply {
      import com.paypal.stingray.common.option.{none => StingrayNone}
      val stringNone = StingrayNone[String]
      stringNone must beEqualTo(Option.empty[String])
    }
  }

}
