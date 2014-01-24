package com.paypal.stingray.common.logging

import org.slf4j.{Logger, LoggerFactory}
import scala.reflect.ClassTag

/**
 * Convenience methods for interacting with [[org.slf4j.Logger]] and other SLF4J objects.
 */

trait LoggingSugar {

  /**
   * Retrieves a [[org.slf4j.Logger]] object for the given object type `T`. Allows you to write code such as
   * {{{ getLogger[Foo] }}} in place of lengthier code such as {{{ LoggerFactory.getLogger(classOf[Foo]) }}}.
   */
  def getLogger[T <: AnyRef](implicit classTag: ClassTag[T]): Logger = {
    LoggerFactory.getLogger(classTag.runtimeClass)
  }

}
