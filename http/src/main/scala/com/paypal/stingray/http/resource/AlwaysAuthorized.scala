package com.paypal.stingray.http.resource

import scala.concurrent.Future
import com.paypal.stingray.common.option._
import spray.http.HttpRequest

/**
 * Mix this into an [[com.paypal.stingray.http.resource.AbstractResourceActor]] implementation,
 * and set AuthInfo to [[com.paypal.stingray.http.resource.NoAuth]] to always authorize requests.
 */
trait AlwaysAuthorized {

  /**
   * Always returns authorized
   * @param p the request
   * @return authorized
   */
  def isAuthorized(p: HttpRequest): Future[Option[NoAuth]] = ().some.continue

}
