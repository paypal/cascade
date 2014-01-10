package com.stackmob.tests.common.actor

import scala.concurrent.{Await, ExecutionContext, Future}
import org.specs2.specification.{SpecificationStructure, Step, Fragments}
import akka.testkit.TestKit
import scalaz.{Validation, \/}
import scala.concurrent.duration._

/**
 * Created by taylor on 7/16/13.
 */

trait ActorSpecification extends SpecificationStructure {
  this: TestKit =>

  lazy val synchronousExecution = new ExecutionContext {
    override def reportFailure(t: Throwable) {
    }
    override def execute(runnable: Runnable) {
      runnable.run()
    }
  }

  override def map(fs: => Fragments): Fragments = super.map(fs).add(Step(system.shutdown()))

  implicit class RichFuture[T](f: Future[T]) {
    def toScalazEither: Throwable \/ T = \/.fromTryCatch(Await.result(f, 10.seconds))
    def toOption: Option[T] = toScalazEither.toOption
    def toEither: Either[Throwable, T] = toScalazEither.toEither
  }

  implicit class RichFutureThrowable[S <: Throwable, T](f: Future[Validation[S, T]]) {
    def toScalazEither: S \/ T = Await.result(f, 10.seconds).disjunction
    def toOption: Option[T] = toScalazEither.toOption
    def toEither: Either[S, T] = toScalazEither.toEither
  }

  implicit class RichFutureEither[S <: Throwable, T](f: Future[S \/ T]) {
    def toScalazEither: S \/ T = Await.result(f, 10.seconds)
    def toOption: Option[T] = toScalazEither.toOption
    def toEither: Either[S, T] = toScalazEither.toEither
  }

}

