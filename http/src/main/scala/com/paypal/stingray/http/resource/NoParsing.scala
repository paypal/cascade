package com.paypal.stingray.http.resource

import scalaz._
import Scalaz._
import spray.http.HttpRequest
import scala.concurrent.Future

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 4/26/13
 * Time: 1:45 PM
 */
trait NoParsing {

  def parseRequest(r: HttpRequest, pathParts: Map[String, String]): Future[HttpRequest] = r.continue

}
