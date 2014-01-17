package com.paypal.stingray.http.resource

import scala.concurrent.Future
import com.paypal.stingray.common.option._

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 4/26/13
 * Time: 1:16 PM
 * Mix this into a SimpleResource with AuthInfo=NoAuth to allways authorize requests
 */
trait AlwaysAuthorized[T] {

  def isAuthorized(p: T): Future[Option[NoAuth]] = ().some.continue

}
