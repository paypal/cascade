package com.paypal.stingray.http.server.exception

import com.paypal.stingray.common.option._

/**
 * Base type for Exceptions thrown by services. Implementations can be used to wrap other exceptions,
 * or can be thrown as exceptions themselves.
 *
 * @param message what caused this exception
 * @param throwable optionally, another exception that is caught and wrapped here
 */
abstract class ServiceException(message: String, throwable: Option[Throwable] = none[Throwable])
  extends Exception(message, throwable.orNull)
