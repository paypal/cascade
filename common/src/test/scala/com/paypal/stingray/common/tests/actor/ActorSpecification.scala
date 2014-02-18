package com.paypal.stingray.common.tests.actor

import scala.concurrent.{Await, ExecutionContext, Future}
import org.specs2.specification.{SpecificationStructure, Step, Fragments}
import akka.testkit.TestKit
import scala.concurrent.duration._
import scala.util.Try
import com.paypal.stingray.common.trys._

/**
 * Test harness trait for Specs involving Akka Actors
 */
trait ActorSpecification extends SpecificationStructure {
  this: TestKit =>

  /**
   * Forces execution to occur synchronously, on one thread
   */
  lazy val synchronousExecution = new ExecutionContext {
    override def reportFailure(t: Throwable) {
    }
    override def execute(runnable: Runnable) {
      runnable.run()
    }
  }

  override def map(fs: => Fragments): Fragments = super.map(fs).add(Step(system.shutdown()))

  /** Used inside of [[RichFuture]] to control Await timing. By default, blocks infinitely; override if needed. */
  lazy val actorSpecAwaitDuration: Duration = Duration.Inf

  /**
   * Wrapper for Futures
   *
   * {{{
   *   val f = Future { ... }
   *   f.toTry
   * }}}
   *
   * @param f the Future to wrap
   * @tparam T the type of the Future
   */
  implicit class RichFuture[T](f: Future[T]) {

    /**
     * Blocks for a result on `f`, wrapping failures or a timeout in a Try
     * @param awaitFor Duration to await for the Future; by default, waits infinitely
     * @return a Try of the value, or a failure/timeout
     */
    def toTry(awaitFor: Duration = actorSpecAwaitDuration): Try[T] = Try { Await.result(f, awaitFor) }
    def toTry: Try[T] = this.toTry()

    /**
     * Blocks for a result on `f`, yielding Some if successful or None if not
     * @param awaitFor Duration to await for the Future; by default, waits infinitely
     * @return Some value if successful, None if not
     */
    def toOption(awaitFor: Duration = actorSpecAwaitDuration): Option[T] = toTry(awaitFor).toOption
    def toOption: Option[T] = this.toOption()

    /**
     * Blocks for a result on `f`, wrapping failures or a timeout in a right-biased Either
     * @param awaitFor Duration to await for the Future; by default, waits infinitely
     * @return a right-biased Either of the value, or a failure/timeout
     */
    def toEither(awaitFor: Duration = actorSpecAwaitDuration): Either[Throwable, T] = toTry(awaitFor).toEither
    def toEither: Either[Throwable, T] = this.toEither()
  }

  /**
   * Wrapper for Futures containing Try objects
   * @param f the Future to wrap
   * @tparam T the type of the Try inside the Future
   */
  implicit class RichFutureThrowable[T](f: Future[Try[T]]) {

    /**
     * Blocks for a result on `f`, wrapping a timeout in a Try, and flattens
     * @param awaitFor Duration to await for the Future; by default, waits infinitely
     * @return a Try of the value, or a failure/timeout
     */
    def toTry(awaitFor: Duration = actorSpecAwaitDuration): Try[T] = Try { Await.result(f, awaitFor) }.flatten
    def toTry: Try[T] = this.toTry()

    /**
     * Blocks for a result on `f`, yielding Some if successful or None if not
     * @param awaitFor Duration to await for the Future; by default, waits infinitely
     * @return Some value if successful, None if not
     */
    def toOption(awaitFor: Duration = actorSpecAwaitDuration): Option[T] = toTry(awaitFor).toOption
    def toOption: Option[T] = this.toOption()

    /**
     * Blocks for a result on `f`, wrapping failures or a timeout in a right-biased Either
     * @param awaitFor Duration to await for the Future; by default, waits infinitely
     * @return a right-biased Either of the value, or a failure/timeout
     */
    def toEither(awaitFor: Duration = actorSpecAwaitDuration): Either[Throwable, T] = toTry(awaitFor).toEither
    def toEither: Either[Throwable, T] = this.toEither()
  }

}

