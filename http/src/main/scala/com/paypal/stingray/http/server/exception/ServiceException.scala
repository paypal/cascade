package com.paypal.stingray.http.server.exception

import net.liftweb.json.JsonAST._
import net.liftweb.json.scalaz.JsonScalaz._
import scalaz._
import Scalaz._

/**
 * Created with IntelliJ IDEA.
 * User: taylor
 * Date: 10/9/12
 * Time: 3:31 PM
 */

class ServiceException(message: String, throwable: Option[Throwable] = none[Throwable]) extends Exception(message, throwable.orNull)

object ServiceException extends ThrowableJson[ServiceException]

trait ThrowableJson[T <: Throwable] {

  val errorRootJSONKey = "errors"

  implicit val throwableJSONW = new JSONW[T] {
    override def write(e: T): JValue = {
      JString(e.getMessage)
    }
  }

}
