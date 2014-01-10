package com.paypal.stingray.http

import scalaz.Validation
import com.paypal.stingray.common.validation._
import scala.concurrent.Future
import scala.util.Try

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.http.client
 *
 * User: aaron
 * Date: 2/4/13
 * Time: 4:14 PM
 */
package object client {

  /**
   * a {{{Try}}} inside a Scala Future, with a failure type subclassed from {{{scala.util.Failure}}}
   * @tparam Success the success type
   */
  type FutureTry[Success] = Future[Try[Success]]

  /**
   * an {{{Either}}} inside a Scala Future, right-biased
   * @tparam Failure the left type, idiomatically a Failure
   * @tparam Success the right type, idiomatically a Success
   */
  type FutureEither[Failure, Success] = Future[Either[Failure, Success]]
}
