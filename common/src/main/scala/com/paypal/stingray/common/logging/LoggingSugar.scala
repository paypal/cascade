package com.paypal.stingray.common.logging

import org.slf4j.{Logger, LoggerFactory}
import scala.reflect.ClassTag

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
  def getLogger[T <: AnyRef](implicit classTag: ClassTag[T]): Logger = {
    LoggerFactory.getLogger(classTag.runtimeClass)
  }

}
