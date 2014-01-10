package com.paypal.stingray.services.cache

import scala.collection.JavaConverters._
import java.net.InetSocketAddress
import net.rubyeye.xmemcached.XMemcachedClientBuilder
import net.rubyeye.xmemcached.command.BinaryCommandFactory
import net.rubyeye.xmemcached.impl.ArrayMemcachedSessionLocator
import scalaz._
import com.paypal.stingray.common.validation._
import com.paypal.stingray.common.logging.LoggingSugar

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.services.cache
 *
 * User: aaron
 * Date: 3/12/13
 * Time: 2:59 PM
 */
trait MemcachedServiceLow extends LoggingSugar {

  protected def hosts: NonEmptyList[InetSocketAddress]
  protected def connPoolSize: Int
  protected def opTimeout: Long
  protected def connTimeout: Long

  protected lazy val client = {
    val builder = new XMemcachedClientBuilder(hosts.list.asJava)
    builder.setCommandFactory(new BinaryCommandFactory)
    builder.setSessionLocator(new ArrayMemcachedSessionLocator)
    builder.setConnectionPoolSize(connPoolSize)
    builder.setOpTimeout(opTimeout)
    builder.setConnectTimeout(connTimeout)
    builder.build
  }

  def safeReplace(key: String, exp: Int, o: Object): ThrowableValidation[Boolean] = {
    validating(client.replace(key, exp, o))
  }

  def safeDecr(key: String, by: Int): ThrowableValidation[Long] = validating {
    client.decr(key, by)
  }

  def safeIincr(key: String, by: Int): ThrowableValidation[Long] = validating {
    client.incr(key, by)
  }

  def safeAdd(key: String, exp: Int, value: Object): ThrowableValidation[Boolean] = validating {
    client.add(key, exp, value)
  }

  def safeDelete(key: String): ThrowableValidation[Boolean] = validating {
    client.delete(key)
  }

  def safeGet(key: String): ThrowableValidation[Object] = validating {
    //Aaron <aaron@stackmob.com>, 9/11/2011 - a note about this call:
    //get is a java generic method whose return type is determined by the type param, but type inference cannot
    //occur in this call because, so the method returns the Nothing type.
    //execute uses an automatic type dependent closure construction (http://www.scala-lang.org/node/138),
    //and it passes on the return type of this closure, at which point scala tries to cast Nothing
    //to Object, which throws.
    client.get[Object](key)
  }

  def safeSet(key: String, exp: Int, o: Object): ThrowableValidation[Boolean] = validating {
    client.set(key, exp, o)
  }
}
