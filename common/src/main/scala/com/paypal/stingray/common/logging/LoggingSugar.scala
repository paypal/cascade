package com.paypal.stingray.common.logging

import org.slf4j.{Logger, LoggerFactory}

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 11/16/11
 * Time: 11:41 PM
 */

trait LoggingSugar {

  /**
   * This is just a convenience method so you can type:
   *
   * getLogger[Foo]
   *
   * ...rather than...
   *
   * LoggerFactory.getLogger(classOf[Foo])
   *
   */
  def getLogger[T <: AnyRef](implicit manifest: Manifest[T]): Logger = {
    LoggerFactory.getLogger(manifest.runtimeClass.asInstanceOf[Class[T]])
  }

  /**
   * Log the exception with the specified log function. then throw the exception
   */
  def logAndThrow(e: Throwable)(logFn: (String, Throwable) => Unit): Nothing = {
    logFn(e.getMessage, e)
    throw e
  }

}
