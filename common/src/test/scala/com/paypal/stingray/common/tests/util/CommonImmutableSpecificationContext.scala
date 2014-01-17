package com.paypal.stingray.common.tests.util

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 3/28/12
 * Time: 5:36 PM
 */

import org.specs2.specification._
import org.specs2.execute.{Failure => SpecsFailure, Result => SpecsResult, AsResult}
import com.paypal.stingray.common.logging.LoggingSugar

trait CommonImmutableSpecificationContext extends Around with LoggingSugar {

  private lazy val logger = getLogger[CommonImmutableSpecificationContext]

  def before() { }

  def after() { }

  override def around[T : AsResult](t: => T): SpecsResult = {
    try {
      before()
      AsResult(t)
    } catch {
      case e: Throwable => {
        logger.error(e.getMessage, e)
        throw e
      }
    } finally {
      after()
    }
  }

  protected def logAndFail(t: Throwable): SpecsResult = {
    logger.warn(t.getMessage, t)
    SpecsFailure(s"failed with exception ${t.getClass.getCanonicalName} (${t.getMessage})")
  }

}
