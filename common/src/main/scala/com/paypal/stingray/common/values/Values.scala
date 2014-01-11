package com.paypal.stingray.common.values

import com.paypal.stingray.common.enumeration._
import net.liftweb.json._
import scala.language.higherKinds
import scala.concurrent.Future

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

  def get(key: String): Future[Option[String]]

  /**
   * non-json comma separate list, because people are lazy
   */
  def getSimpleList(key: String): Future[Option[List[String]]] = {
    monad.map(get(key)) { mbValue =>
      mbValue.map(_.split(",").toList)
    }
  }

  def getInt(key: String): Future[Option[Int]] = {
    monad.map(get(key)) { mbValue =>
      mbValue.flatMap(s => validating(s.toInt).toOption)
    }
  }

  def getLong(key: String): Future[Option[Long]] = {
    monad.map(get(key)) { mbValue =>
      mbValue.flatMap(s => validating(s.toLong).toOption)
    }
  }

  def getEnum[T <: Enumeration : EnumReader](key: String): Future[Option[T]] = {
    monad.map(get(key)) { mbValue =>
      mbValue.flatMap { enumString: String =>
        enumString.readEnum[T]
      }
    }
  }

  def getJSON[T: JSONR](key: String): Future[Option[T]] = {
    monad.map(get(key)) { mbValue =>
      for {
        s <- mbValue
        json <- validating(parse(s)).toOption
        t <- fromJSON[T](json).toOption
      } yield t
    }
  }

  def getBool(key: String): Future[Option[Boolean]] = {
    monad.map(get(key)) { mbValue =>
      mbValue.flatMap { boolString: String =>
        validating(boolString.toBoolean).toOption
      }
    }
  }
}
