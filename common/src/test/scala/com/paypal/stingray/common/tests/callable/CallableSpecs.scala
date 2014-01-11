package com.paypal.stingray.common.tests.callable

import org.specs2.Specification
import org.specs2.execute.{Result => SpecsResult}
import java.util.concurrent.Callable

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.common.tests.callable
 *
 * User: aaron
 * Date: 8/15/12
 * Time: 2:35 PM
 */

class CallableSpecs extends Specification { def is =
  "CallableSpecs".title                                                                                                 ^
  "the callable package in stingray-common is used to make creating and manipulating Callables easier"                  ^
  "the callable function should"                                                                                        ^
    "create the appropriate callable"                                                                                   ! callableFunctionSucceeds ^
                                                                                                                        end ^
  "the callByNameToCallable implicit should"                                                                            ^
    "create the appropriate callable"                                                                                   ! callableImplicitSucceeds ^
                                                                                                                        end
  private def fn: Int = 22

  private def callableFunctionSucceeds: SpecsResult = {
    import com.paypal.stingray.common.callable.callable
    callable(fn).call() must beEqualTo(fn)
  }

  private def callableImplicitSucceeds: SpecsResult = {
    import com.paypal.stingray.common.callable.callByNameToCallable
    val c: Callable[Int] = fn
    c.call must beEqualTo(fn)
  }
}
