package com.paypal.stingray.common.stats

import com.paypal.stingray.common.values.StaticValues
import com.paypal.stingray.common.constants.ValueConstants
import concurrent.{ExecutionContext, Future}

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 9/18/13
 * Time: 2:09 PM
 */
class StatsDCommon(client: StatsdClient, svs: StaticValues) extends StatsD {

  def this(host: String, port: Int, svs: StaticValues) = {
    this(new StatsdClient(host, port), svs)
  }

  private val clusterName = svs.get(ValueConstants.ClusterName).getOrElse("dev")

  private def qualifiedKey(key: String): String = {
    val prefix = s"$clusterName.$getFullHostname"
    // Just in case this function gets abused again
    if(key.startsWith(prefix)) {
      key
    } else {
      s"$prefix.$key"
    }
  }

  private[stats] def timeSince(start: Long): Int = {
    (System.currentTimeMillis() - start).toInt
  }

  override def timeMethod[T](key: String)(f: => T): T = {
    timeMethods(key)(f)
  }

  override def timeMethods[T](keys: String*)(f: => T): T = {
    val st = System.currentTimeMillis()
    try {
      f
    }
    finally {
      val time = timeSince(st)
      for { key <- keys } {
        timing(key, time)
      }
    }
  }

  override def timeFutureMethod[A](key: String)(f: => Future[A])(implicit ec: ExecutionContext): Future[A] = {
    timeFutureMethods(key)(f)
  }

  override def timeFutureMethods[A](keys: String*)(f: => Future[A])(implicit ec: ExecutionContext): Future[A] = {
    val st = System.currentTimeMillis()
    val ff = f
    ff onComplete { _ =>
      val time = timeSince(st)
      for { key <- keys } {
        timing(key, time)
      }
    }
    ff
  }

  override def increment(key:String, i:Int = 1) {
    client.increment(qualifiedKey(key), i)
  }

  override def decrement(key:String, i:Int = 1) {
    client.decrement(qualifiedKey(key), i)
  }

  override def timing(key: String, msec: Int) {
    client.timing(qualifiedKey(key), msec)
  }
}
