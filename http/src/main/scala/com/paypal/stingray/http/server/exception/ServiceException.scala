package com.paypal.stingray.http.server.exception

import com.paypal.stingray.common.option._

/**
 * Created with IntelliJ IDEA.
 * User: taylor
 * Date: 10/9/12
 * Time: 3:31 PM
 */

class ServiceException(message: String, throwable: Option[Throwable] = none[Throwable])
  extends Exception(message, throwable.orNull)

object ServiceException extends ThrowableJson[ServiceException]

trait ThrowableJson[T <: Throwable] {

  val errorRootJSONKey = "errors"

}
