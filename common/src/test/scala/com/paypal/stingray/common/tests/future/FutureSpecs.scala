package com.paypal.stingray.common.tests.future

import org.specs2._
import scala.concurrent.ExecutionContext
import com.paypal.stingray.common.future._
import scala.concurrent.Future
import com.paypal.stingray.common.logging.LoggingSugar

/**
 * Tests implicit classes in [[com.paypal.stingray.common.future]]
 */
class FutureSpecs extends Specification with ScalaCheck with LoggingSugar { def is=s2"""

  mapFailure:

    Converts Throwable to different type via full function                    ${FutureMapFailure().fullFuncSuccess}
    Converts Throwable to different type via partial function                 ${FutureMapFailure().pfSuccess}

"""
  implicit val ec: ExecutionContext = sequentialExecutionContext(getLogger[FutureSpecs])
  case class CustomException(message: String) extends Exception(message)

  case class FutureMapFailure() {
    def fullFuncSuccess = {
      val f = Future[Unit] { throw new Exception("fail") }
      val mapped = f.mapFailure { e =>
        new CustomException(e.getMessage)
      }
      mapped.toTry must beAFailedTry[Unit].withThrowable[CustomException]
    }
    def pfSuccess = {
      val f = Future[Unit] { throw new Exception("fail") }
      val mapped = f.mapFailure[CustomException] {
        case e: Exception => new CustomException(e.getMessage)
      }
      mapped.toTry must beAFailedTry[Unit].withThrowable[CustomException]
    }
  }

}
