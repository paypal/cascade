package com.paypal.stingray.http.client

import scalaz.Validation
import com.stackmob.newman.Constants._
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import java.nio.charset.Charset
import scalaz.Scalaz._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import com.paypal.stingray.common.stats.StatsD
import com.paypal.stingray.common.validation.ThrowableValidation
import akka.actor.ActorSystem

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.http.client
 *
 * User: aaron
 * Date: 6/6/12
 * Time: 9:13 PM
 *
 * the BaseClient trait, intended for more complex extension by clients
 */
trait BaseClientLow extends BaseClientCommon {

  /**
   * wrapper to instrument a client method. use it like this in your service client:
   * {{{
   * def doSomething(arg: Int) = instrument[ReturnType] {
   *   val something: IOThrowableValidation[ReturnType] = io(getSomething(arg))
   *   something
   * }
   * }}}
   * @param i the IOThrowableValidation[Succ] that should be executed and instrumented
   * @tparam Success the type to be returned in the success case
   * @return a new IOThrowableValidation[Succ] which when executed will instrument and execute the call
   */
  protected def instrument[Success](i: => ThrowableValidation[Success])
                                   (implicit statsD: StatsD): ThrowableValidation[Success] = {
    instrument[Throwable, Success](i)(identity[Throwable], statsD)
  }

  /**
   * wrapper to instrument a client method. use it like this in your service client:
   * {{{
   * def doSomething(arg: Int) = instrument[Fail, ReturnType] {
   *   val something: IOValidation[Fail, ReturnType] = io(getSomething(arg))
   *   something
   * }
   * }}}

   * @param i the IOValidation to instrument and execute
   * @param fn a function to convert any encountered thrown exceptions to the {{{Fail}}}
   * @tparam Fail the type to be returned when a failure happens
   * @tparam Succ the type to be returned when a success happens
   * @return a new {{{IOValidation[Fail, Succ]}}} which when executed will instrument and execute the call
   */
  protected def instrument[Fail, Succ](i: => Validation[Fail, Succ])
                                      (implicit fn: Throwable => Fail, statsD: StatsD): Validation[Fail, Succ] = {
    val fut = Instrumentation.apply(Future.successful(i),
      targetService,
      currentHost,
      failureExecutionTime,
      warningExecutionTime,
      debugExecutionTime,
      defaultStat)(fn)(statsD)
    Await.result(fut, Duration.Inf)
  }

  /**
   * wait properly for an asynchronous operation to complete
   * @param i the future to wait on, wrapped inside an IO
   * @param dur the max duration to wait for the future
   * @param exHandler the exception handler. make this handle TimeoutException if you want to
   *                  handle the case where the operation doesn't return in time
   * @tparam Fail the failure type
   * @tparam Succ the success type
   * @return
   */
  protected def asyncWait[Fail, Succ](i: => FutureValidation[Fail, Succ],
                                      dur: Duration)
                                     (implicit exHandler: Throwable => Fail): Validation[Fail, Succ] = {
    try {
      Await.result(i, dur)
    } catch {
      case t: Throwable => exHandler(t).fail[Succ]
    }
  }
}

/**
 * an abstract class wrapper for the base HTTP client for StackMob. intended to allow for clients to easily
 * extend BaseClient to force any BaseClient subclass to take BaseClient's ctor arguments in their ctors
 */
abstract class BaseClient(override val targetService: String,
                          override val protocol: Protocol,
                          override val host: String,
                          override val port: Int,
                          override val httpClient: HttpClient = new SprayHttpClient(ActorSystem("base-client")),
                          override val charset: Charset = UTF8Charset)(implicit override val statsD: StatsD) extends BaseClientLow

