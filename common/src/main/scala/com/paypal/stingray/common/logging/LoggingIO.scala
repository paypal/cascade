package com.paypal.stingray.common.logging

import scalaz.effect._
import scalaz.syntax.monad._
import org.slf4j.Logger

/**
 * Created by IntelliJ IDEA.
 * User: jordanrw
 * Date: 1/27/12
 * Time: 11:27 PM
 */

trait LoggingIO {

  protected def logger: Logger

  def logDebugIO(msg: String): IO[Unit] = logger.debug(msg).pure[IO]

  def logInfoIO(msg: String): IO[Unit] = logger.info(msg).pure[IO]

  def logWarnIO(msg: String, exception: Throwable): IO[Unit] = logger.warn(msg, exception).pure[IO]

  def logErrorIO(msg: String, exception: Throwable): IO[Unit] = logger.error(msg, exception).pure[IO]

  def logErrorIO(msg: String): IO[Unit] = logger.error(msg).pure[IO]

}
