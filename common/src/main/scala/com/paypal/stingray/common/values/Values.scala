package com.paypal.stingray.common.values

import scalaz.Monad
import com.paypal.stingray.common.validation._
import com.paypal.stingray.common.enumeration._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import scala.language.higherKinds

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 3/13/13
 * Time: 5:27 PM
 * Given a means of producing a string inside a container, this trait provides convenience methods for turning
 * it into various other formats. It can be treated as a bare string, comma separated list, or parsed into json
 *
 * {{{Values}}} operates on a type constructor {{{C[_]}}}. Its get function returns a {{{C[Option[String]]}}}, and
 * functions that call get use the abstract {{{Monad[C]}}} to access and do operations on the {{{Option[String]}}}
 * inside the {{{C}}}
 */
trait Values[C[_]] {

  protected implicit def monad: Monad[C]

  def get(key: String): C[Option[String]]

  /**
   * non-json comma separate list, because people are lazy
   */
  def getSimpleList(key: String): C[Option[List[String]]] = {
    monad.map(get(key)) { mbValue =>
      mbValue.map(_.split(",").toList)
    }
  }

  def getInt(key: String): C[Option[Int]] = {
    monad.map(get(key)) { mbValue =>
      mbValue.flatMap(s => validating(s.toInt).toOption)
    }
  }

  def getLong(key: String): C[Option[Long]] = {
    monad.map(get(key)) { mbValue =>
      mbValue.flatMap(s => validating(s.toLong).toOption)
    }
  }

  def getEnum[T <: Enumeration : EnumReader](key: String): C[Option[T]] = {
    monad.map(get(key)) { mbValue =>
      mbValue.flatMap { enumString: String =>
        enumString.readEnum[T]
      }
    }
  }

  def getJSON[T: JSONR](key: String): C[Option[T]] = {
    monad.map(get(key)) { mbValue =>
      for {
        s <- mbValue
        json <- validating(parse(s)).toOption
        t <- fromJSON[T](json).toOption
      } yield t
    }
  }

  def getBool(key: String): C[Option[Boolean]] = {
    monad.map(get(key)) { mbValue =>
      mbValue.flatMap { boolString: String =>
        validating(boolString.toBoolean).toOption
      }
    }
  }
}
