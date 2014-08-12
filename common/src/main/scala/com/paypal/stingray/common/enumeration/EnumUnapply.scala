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
package com.paypal.stingray.common.enumeration

/**
 * Allows pattern matching on String values that correspond to [[com.paypal.stingray.common.enumeration.Enumeration]]
 * subtypes. Note that the type returned by the extractor is the general sealed trait `T`, not an Enumeration instance.
 *
 * {{{
 *   scala> "SOMETYPE" match { case AnEnumeration(a) => a; case _ => throw new Exception("fail!") }
 *   res0: com.project.AnEnumeration = SOMETYPE
 *   scala> "OTHERTYPE" match { case AnEnumeration(a) => a; case _ => throw new Exception("fail!") }
 *   res1: com.project.AnEnumeration = OTHERTYPE
 *   scala> "not a type" match { case AnEnumeration(a) => a; case _ => throw new Exception("fail!") }
 *   java.lang.Exception: fail!
 * }}}
 */
trait EnumUnapply[T <: Enumeration] {

  /**
   * Allows pattern matching on String values that correspond to Enumeration subtypes
   * @param s the String to try to convert
   * @param reader implicitly, the [[com.paypal.stingray.common.enumeration.EnumReader]] to use for conversion
   * @return optionally, an Enumeration subtype corresponding to the input String
   */
  def unapply(s: String)(implicit reader: EnumReader[T]): Option[T] = reader.read(s)
}
