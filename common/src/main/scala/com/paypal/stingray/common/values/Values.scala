package com.paypal.stingray.common.values

import com.paypal.stingray.common.enumeration._
import scala.language.higherKinds
import scala.util.Try

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 3/13/13
 * Time: 5:27 PM
 * Given a means of producing a string inside a container, this trait provides convenience methods for turning
 * it into various other formats. It can be treated as a bare string, comma separated list, or parsed into json
 *
 * {{{Values}}} operates on a type constructor {{{Future[_]}}}. Its get function returns a {{{Future[Option[String]]}}}, and
 * functions that call get use the abstract {{{Monad[C]}}} to access and do operations on the {{{Option[String]}}}
 * inside the {{{C}}}
 */
trait Values {

  def get(key: String): Option[String]

  /**
   * non-json comma separate list, because people are lazy
   */
  def getSimpleList(key: String): Option[List[String]] = {
    get(key).map { value =>
      value.split(",").toList
    }
  }

  def getInt(key: String): Option[Int] = {
    get(key).flatMap { value =>
      Try(value.toInt).toOption
    }
  }

  def getLong(key: String): Option[Long] = {
    get(key).flatMap { value =>
      Try(value.toLong).toOption
    }
  }

  def getEnum[T <: Enumeration : EnumReader](key: String): Option[T] = {
    get(key).flatMap { value =>
      value.readEnum[T]
    }
  }

  def getBool(key: String): Option[Boolean] = {
    get(key).flatMap { value =>
      Try(value.toBoolean).toOption
    }
  }
}
