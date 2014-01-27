package com.paypal.stingray.http.resource

import scala.concurrent.Future
import com.paypal.stingray.common.option._

/**
 * Mix this into an [[com.paypal.stingray.http.resource.AbstractResource]] implementation,
 * and set AuthInfo to [[com.paypal.stingray.http.resource.NoAuth]] to always authorize requests.
 */
trait AlwaysAuthorized[T] {

  /**
   * Always returns authorized
   * @param p the request
   * @return authorized
   */
  def isAuthorized(p: T): Future[Option[NoAuth]] = ().some.continue

}
