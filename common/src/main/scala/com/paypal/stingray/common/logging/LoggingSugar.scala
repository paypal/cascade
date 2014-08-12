/**
 * Copyright 2013-2014 PayPal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
