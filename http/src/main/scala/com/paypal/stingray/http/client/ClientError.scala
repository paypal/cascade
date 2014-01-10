package com.paypal.stingray.http.client

import scalaz.Scalaz._

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.http.client
 *
 * User: aaron
 * Date: 7/17/13
 * Time: 4:07 PM
 */

/**
 * The root of the error hierarchy that client methods should return.
 */
sealed trait ClientError {

  def toString: String

  def fold[X](inputError: ClientInputError => X,
              throwableError: ClientThrowableError => X,
              httpError: ClientHTTPError => X): X = {
    this match {
      case i @ ClientInputError(_) => inputError(i)
      case t @ ClientThrowableError(_) => throwableError(t)
      case h @ ClientHTTPError(_, _, _) => httpError(h)
    }
  }
}

/**
 * A client error representing invalid input.
 *
 * @param message the error message
 */
case class ClientInputError(message: String) extends ClientError {
  override def toString: String = {
    s"Client input error: message = $message"
  }
}

/**
 * A client error representing an exception.
 *
 * @param throwable the exception
 */
case class ClientThrowableError(throwable: Throwable) extends ClientError {
  override def toString: String = {
    s"Client error: message = ${throwable.getMessage}, cause = ${~Option(throwable.getCause).map(_.getMessage)}"
  }
}

/**
 * A client error representing an HTTP error response.
 *
 * @param code the http response code
 * @param body the http response body
 * @param message the error message
 */
case class ClientHTTPError(code: Int, body: Option[String], message: Option[String]) extends ClientError {
  override def toString: String = {
    s"Client HTTP error: code = $code, body = ${~body}, message = ${~message}"
  }
}
