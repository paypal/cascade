/**
 * Copyright 2013-2015 PayPal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paypal.cascade.common.tests.future

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.specs2._

import com.paypal.cascade.common.future._

/**
 * Tests implicit classes in [[com.paypal.cascade.common.future]]
 */
class FutureSpecs extends Specification with ScalaCheck { def is=s2"""

  mapFailure:

    Converts Throwable to different type via full function                    ${FutureMapFailure().fullFuncSuccess}
    Converts Throwable to different type via new explicate throwable          ${FutureMapFailure().newThrowableSuccess}
    Converts Throwable to different type via partial function                 ${FutureMapFailure().pfSuccess}

  toUnit:
    Converts a successful Future[T] to Future[Unit]                           ${FutureToUnit().successful}
    Converts a failed Future[T] to Future[Unit]                               ${FutureToUnit().failed}

"""

  case class CustomException(message: String) extends Exception(message)

  case class FutureMapFailure() {
    def fullFuncSuccess = {
      val f = Future[Unit] { throw new Exception("fail") }
      val mapped = f.mapFailure { e =>
        new CustomException(e.getMessage)
      }
      mapped.toTry must beAFailedTry[Unit].withThrowable[CustomException]
    }
    def newThrowableSuccess = {
      val f = Future[Unit] { throw new Exception("fail") }
      val mapped = f.mapFailure(new CustomException("custom fail"))
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

  case class FutureToUnit() {
    def successful = {
      val fut = Future.successful("This is a Future[String]")
      val unitFut = fut.toUnit
      unitFut.toTry must beASuccessfulTry[Unit]
    }
    def failed = {
      val fut = Future.failed[String](new CustomException("This is a failed Future[String]"))
      val unitFut = fut.toUnit
      unitFut.toTry must beAFailedTry[Unit].withThrowable[CustomException]
    }
  }

}
