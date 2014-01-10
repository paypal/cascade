package com.paypal.stingray.common.stats

import concurrent.{ExecutionContext, Future}

/**
 * create a new StatsD statistic
 * usage:
 * implicit val statsd = new StatsD(host, port)
 * object MyStat extends StatsDStat("MyStat")
 * MyStat.incr()
 * MyStat.decrBy(22)
 * MyStat time {
 *  ...
 * }
 * ...
 * @param key the key that this stat represents
 */
abstract class StatsDStat(val key:String) {

  def incr(implicit c:StatsD) {
    c.increment(key)
  }

  def incrBy(i:Int)(implicit c:StatsD) {
    c.increment(key, i)
  }

  def decr(implicit c:StatsD) {
    c.increment(key)
  }
  def decrBy(i:Int)(implicit c:StatsD) {
    c.decrement(key, i)
  }

  def time[T](f: => T)(implicit c:StatsD): T = {
    c.timeMethod(key)(f)
  }

  // important to note: in order to time a future, the future must be declared within the function's argument clause
  def timeFuture[T](future: => Future[T])(implicit c:StatsD, ec: ExecutionContext): Future[T] = {
    c.timeFutureMethod(key)(future)
  }
}

