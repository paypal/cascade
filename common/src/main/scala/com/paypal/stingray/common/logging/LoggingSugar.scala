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
   *
   * Akka-based actors using this trait must have the following defined in their `application.conf`:
   *
   * {{{
   *   akka {
   *     loggers = ["akka.event.slf4j.Slf4jLogger"]
   *   }
   * }}}
   *
   * This provides a hook from SLF4J to the Akka event stream, for asynchronous logging.
   * See http://doc.akka.io/docs/akka/2.2.0/scala/logging.html for more information.
   */
  def getLogger[T <: AnyRef](implicit classTag: ClassTag[T]): Logger = {
    LoggerFactory.getLogger(classTag.runtimeClass)
  }

}
