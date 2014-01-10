package com.paypal.stingray.services.cache

import com.paypal.stingray.common.logging.LoggingSugar
import com.paypal.stingray.common.option._
import java.net.InetSocketAddress
import com.paypal.stingray.common.validation._
import com.paypal.stingray.common.util.casts._
import scalaz._
import Scalaz._
import org.apache.commons.codec.digest.DigestUtils
import net.rubyeye.xmemcached.MemcachedClient

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 6/27/11
 * Time: 11:16 AM
 */

class MemcachedService(mbHostStrings: Option[NonEmptyList[String]],
                       port: Int,
                       override protected val connPoolSize: Int,
                       override protected val opTimeout: Long = MemcachedClient.DEFAULT_OP_TIMEOUT,
                       override protected val connTimeout: Long = MemcachedClient.DEFAULT_CONNECT_TIMEOUT)
  extends LoggingSugar with MemcachedServiceLow {

  private lazy val logger = getLogger[MemcachedService]

  override protected def hosts = {
    val addresses = try {
      mbHostStrings.orThrow(new IllegalArgumentException("Invalid or unknown memcached host(s)"))
    } catch {
      case e: IllegalArgumentException => fatalError(e.getMessage, e.some); throw e
    }

    addresses.map { hostString: String =>
      new InetSocketAddress(hostString, port)
    }
  }

  private def fatalError(errorMsg: String, t: Option[Throwable] = none[Throwable]) {
    t.foreach { _ =>
      logger.error(errorMsg, t)
    }

    logger.error("\n" +
      "\n====================================================================================================" +
      "\nFatal Memcached Error: " + errorMsg + " Exiting..." +
      "\n====================================================================================================\n")

    System.exit(0)
  }

  @throws(classOf[MemcachedException])
  def replace(key: String, exp: Int, o: Object): Boolean = execute {
    safeReplace(key, exp, o)
  }

  @throws(classOf[MemcachedException])
  def decr(key: String, by: Int): Long = execute {
    validating(client.decr(key, by))
  }

  @throws(classOf[MemcachedException])
  def incr(key: String, by: Int): Long = execute {
    validating(client.incr(key, by))
  }

  @throws(classOf[MemcachedException])
  def add(key: String, exp: Int, value: Object): Boolean = execute {
    safeAdd(key, exp, value)
  }

  @throws(classOf[MemcachedException])
  def delete(key: String): Boolean = execute {
    safeDelete(key)
  }

  @throws(classOf[MemcachedException])
  def get(key: String): Object = execute {
    //Aaron <aaron@stackmob.com>, 9/11/2011 - a note about this call:
    //get is a java generic method whose return type is determined by the type param, but type inference cannot
    //occur in this call because, so the method returns the Nothing type.
    //execute uses an automatic type dependent closure construction (http://www.scala-lang.org/node/138),
    //and it passes on the return type of this closure, at which point scala tries to cast Nothing
    //to Object, which throws.
    safeGet(key)
  }

  @throws(classOf[MemcachedException])
  def getAs[T : Manifest](key: String): Option[T] = execute {
    safeGet(key).map { valueAsObject =>
      valueAsObject.cast[T]
    }
  }

  @throws(classOf[MemcachedException])
  def getOrElseAs[T : Manifest](key: String)(f: => T): T = execute {
    safeGet(key).map { valueAsObject =>
      valueAsObject.cast[T] | f
    }
  }

  @throws(classOf[MemcachedException])
  def set(key: String, exp: Int, o: Object): Boolean = execute {
    safeSet(key, exp, o)
  }

  @throws(classOf[MemcachedException])
  def shutdown() {
    execute {
      validating(client.shutdown())
    }
  }

  @throws(classOf[MemcachedException])
  private def execute[T](op: => ThrowableValidation[T]): T = {
    op.getOrThrow {
      case e => {
        logger.error("MemcachedService error: %s".format(e.getMessage), e)
        new MemcachedException(e.getMessage, e)
      }
    }
  }

}

object MemcachedService {
  def createKey(prefix: String, keys: String*): String = {
    md5Hex(prefix, keys)
  }

  private def md5Hex(prefix: String, objs: Seq[String]): String = {
    val s = objs.foldLeft(new StringBuilder)((a, b) => a.append(b.toString))
    DigestUtils.md5Hex(prefix + s.toString)
  }
}

class MemcachedException(message: String, cause: Throwable) extends Exception(message: String, cause: Throwable)
