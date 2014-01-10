package com.paypal.stingray.http.client

import com.paypal.stingray.common.logging.LoggingSugar
import com.paypal.stingray.concurrent.future.sequentialExecutionContext
import com.paypal.stingray.common.stats.{StatsD, StatsDStat}
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import java.sql.Time
import scala.compat.Platform
import java.util.concurrent.TimeUnit
import com.paypal.stingray.common.either._

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.http.client
 *
 * User: aaron
 * Date: 7/2/13
 * Time: 7:31 PM
 */
object Instrumentation extends LoggingSugar {
  private[this] lazy val logger = getLogger[Instrumentation.type]

  implicit private val seqCtx = sequentialExecutionContext(logger)

  /**
   * instrument the given promise
   * @param i the promise to instrument. this method will begin executing the promise (by calling {{{unsafePerformIO}}}) after it begins timing
   * @param targetService the target service that {{{i}}} is talking to
   * @param currentHost this host
   * @param failureDuration the maximum time allowed before the operation is considered a failure
   * @param warningDuration the maximum time allowed before the operation is logged as a warning
   * @param debugDuration the maximum time allowed before the operation is debug logged
   * @param fn the function to transform a throwable into a failure type
   * @tparam Fail the type of the failure to be returned
   * @tparam Succ the type of the success to be returned
   * @return an instrumented Future
   */
  private[client] def apply[Fail, Succ](i: => Future[Either[Fail, Succ]],
                                        targetService: String,
                                        currentHost: String,
                                        failureDuration: Duration,
                                        warningDuration: Duration,
                                        debugDuration: Duration,
                                        stat: StatsDStat)
                                       (fn: Throwable => Fail)
                                       (implicit c:StatsD): Future[Either[Fail, Succ]] = {
    val orig: Future[Either[Fail, Succ]] = {
      for {
        start <- Future.successful(Platform.currentTime)
        op <- stat.timeFuture(i)
        end <- Future.successful(Platform.currentTime)
        opDuration <- Future.successful(Duration(end - start, TimeUnit.MILLISECONDS))
        shouldReportFailure <- Future.successful(opDuration > failureDuration)
        shouldReportWarning <- Future.successful(opDuration > warningDuration)
        shouldReportSuccess <- Future.successful(opDuration > debugDuration)
        _ <- Future.successful {
          if (shouldReportFailure) {
            logger.error(s"client failure to service $targetService from host $currentHost: ${opDuration.toMillis} ms to execute (failures are ${failureDuration.toMillis} or over)")
          } else if(shouldReportWarning) {
            logger.warn(s"client warning to service $targetService from host $currentHost. ${opDuration.toMillis} ms to execute (warnings are ${warningDuration.toMillis} ms or over)")
          } else if (shouldReportSuccess) {
            logger.debug(s"client success to service $targetService from host $currentHost. ${opDuration.toMillis}")
          } else {
            ()
          }
        }
      } yield {
        op
      }
    }

    orig.recover {
      case t => {
        logger.warn(t.getMessage, t)
        fn(t).toLeft
      }
    }
  }

}
