package com.paypal.stingray.http.client

import com.stackmob.newman.dsl.Protocol
import com.stackmob.newman.{SprayHttpClient, HttpClient}
import java.nio.charset.Charset
import com.stackmob.newman.Constants._
import scalaz.{Functor, Validation, EitherT}
import language.implicitConversions
import scala.concurrent.Future
import com.paypal.stingray.common.logging.LoggingSugar
import com.paypal.stingray.concurrent.future._
import com.paypal.stingray.common.stats.StatsD
import akka.actor.ActorSystem

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.http
 *
 * User: aaron
 * Date: 6/27/13
 * Time: 6:46 PM
 *
 * The AsyncBaseClient trait, intended for more complex extension by asynchronous clients
 */
trait AsyncBaseClientLow extends BaseClientCommon with LoggingSugar {

  private lazy val logger = getLogger[AsyncBaseClientLow]

  /**
   * the asynchronous equivalent of {{{BaseClient#Instrument}}}
   */
  protected def instrument[Succ](i: => FutureTry[Succ]): FutureTry[Succ] = {
    instrument[Throwable, Succ](i)(identity[Throwable])
  }

  /**
   * the asynchronous equivalent of {{{BaseClient#Instrument}}}
   */
  protected def instrument[Fail, Succ](i: => FutureValidation[Fail, Succ])
                                      (implicit fn: Throwable => Fail): Future[Validation[Fail, Succ]] = {
    Instrumentation(i,
      targetService,
      currentHost,
      failureExecutionTime,
      warningExecutionTime,
      debugExecutionTime,
      defaultStat)(fn)(statsD)
  }

  protected def instrument[Fail, Succ](i: => EitherT[Future, Fail, Succ])
                                      (implicit fn: Throwable => Fail,
                                       f: Functor[Future]): Future[Validation[Fail, Succ]] = {
    instrument[Fail, Succ](i.validation)
  }
}

/**
 * an abstract class wrapper for the base HTTP client for StackMob. intended to allow for clients to easily
 * extend AsyncBaseClient to force any AsyncBaseClient subclass to take AsyncBaseClient's constructor arguments in their constructors
 */
abstract class AsyncBaseClient(override val targetService: String,
                               override val protocol: Protocol,
                               override val host: String,
                               override val port: Int,
                               override val httpClient: HttpClient = new SprayHttpClient(ActorSystem("base-client")),
                               override val charset: Charset = UTF8Charset)(implicit override val statsD: StatsD) extends AsyncBaseClientLow
