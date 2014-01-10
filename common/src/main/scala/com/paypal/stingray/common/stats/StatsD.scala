package com.paypal.stingray.common.stats

import com.paypal.stingray.common.values.StaticValues
import com.paypal.stingray.common.constants.ValueConstants
import com.paypal.stingray.common.env.EnvironmentCommon
import concurrent.{ExecutionContext, Future}

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.common.stats
 *
 * User: aaron
 * Date: 2/13/12
 * Time: 5:50 PM
 *
 * Stolen & adapted from Ty's code in the github.com/Stackmob/Stackmob project
 */
trait StatsD extends EnvironmentCommon {

  def timeMethod[T](key: String)(f: => T): T

  def timeMethods[T](keys: String*)(f: => T): T

  def timeFutureMethod[A](key: String)(f: => Future[A])(implicit ec: ExecutionContext): Future[A]

  def timeFutureMethods[A](keys: String*)(f: => Future[A])(implicit ec: ExecutionContext): Future[A]

  def increment(key:String, i:Int = 1)

  def decrement(key:String, i:Int = 1)

  def timing(key: String, msec: Int)
}
