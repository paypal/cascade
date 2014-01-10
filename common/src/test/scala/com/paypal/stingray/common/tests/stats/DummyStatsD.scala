package com.stackmob.tests.common.stats

import com.paypal.stingray.common.stats.{StatsD, StatsdClient}
import com.paypal.stingray.common.values.StaticValues
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.mutable.SynchronizedMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Andrew Harris
 * On 8/19/13 at 8:13 PM
 *
 * This exists because I decided it would be easier to just override all these methods than to try to wrestle Mockito
 * into working with both type parameters and secondary parameters.
 *
 * I realized after the fact that I could also have just mocked the StatsdClient itself. Eh.
 *
 * You can pass it any StaticValues, including a StaticValues.defaultValues, because it only ever makes one call
 * to StaticValues. It's just to meet the constructor requirement of StatsD.
 *
 */
class DummyStatsD extends StatsD {

  // we can use this in Specs to verify that keys were incremented/decremented
  private val keysCalled = new scala.collection.mutable.HashMap[String, AtomicInteger]
    with SynchronizedMap[String, AtomicInteger]

  private def incrementKeyBy(key: String, i: Int) = keysCalled.get(key) match {
    case Some(count) => count.addAndGet(i)
    case None => keysCalled.put(key, new AtomicInteger(i))
  }
  private def incrementKey(key: String) = incrementKeyBy(key, 1)

  private def decrementKeyBy(key: String, i: Int) = keysCalled.get(key) match {
    case Some(count) => count.addAndGet(-(i))
    case None => keysCalled.put(key, new AtomicInteger(-(i)))
  }
  private def decrementKey(key: String) = decrementKeyBy(key, 1)

  private def setTiming(key: String, msec: Int) = keysCalled.get(key) match {
    case Some(timing) => timing.set(msec)
    case None => keysCalled.put(key, new AtomicInteger(msec))
  }

  /**
   * Use this method in Specs to verify that a certain amount of calls were made.
   * @param key the StatsD key to retrieve
   * @return for a counter, the count; for a timer, the last timing set
   */
  def getCalls(key: String): Int = keysCalled.getOrElse(key, new AtomicInteger(0)).get()

  /**
   * Use this method in Specs to clear a StatsD history. Also probably not thread-safe.
   */
  def resetCalls {
    keysCalled.clear()
  }

  override def timeMethod[T](key: String)(f: => T): T = timeMethods(key)(f)

  override def timeMethods[T](keys: String*)(f: => T): T = {
    keys.foreach(incrementKey(_))
    f
  }

  override def timeFutureMethod[A](key: String)(f: => Future[A])(implicit ec: ExecutionContext): Future[A] =
    timeFutureMethods(key)(f)

  override def timeFutureMethods[A](keys: String*)(f: => Future[A])(implicit ec: ExecutionContext): Future[A] = {
    keys.foreach(incrementKey(_))
    f
  }

  override def increment(key: String, i: Int = 1) {
    incrementKeyBy(key, i)
  }

  override def decrement(key: String, i: Int = 1) {
    decrementKeyBy(key, i)
  }

  override def timing(key: String, msec: Int) {
    setTiming(key, msec)
  }
}
