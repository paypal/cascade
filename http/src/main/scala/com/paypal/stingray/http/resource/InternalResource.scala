package com.paypal.stingray.http.resource

import net.liftweb.json._
import JsonDSL._
import scalaz.JsonScalaz._
import spray.http.{HttpResponse, HttpRequest}
import com.paypal.stingray.common.logging.LoggingSugar
import spray.http.StatusCodes._
import org.slf4j.LoggerFactory
import concurrent.Future
import com.paypal.stingray.http.resource._
import com.paypal.stingray.http.request.InternalRequest

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 5/10/13
 * Time: 4:20 PM
 * A reesource specialized to accept InternalRequests and never bother with auth
 */
abstract class InternalResource[ParsedRequest, AuthInfo, PostBody: JSONR, PutBody: JSONR] extends SimpleResource[ParsedRequest, AuthInfo, PostBody, PutBody] with LoggingSugar {

  override protected lazy val logger = LoggerFactory.getLogger(this.getClass)

  def parseRequest(r: InternalRequest, pathParts: Map[String, String]): Future[ParsedRequest]

  override def parseRequest(r: HttpRequest, pathParts: Map[String, String]): Future[ParsedRequest] = for {
    internalReq <- InternalRequest(r).orHalt { e =>
      logger.info("client submitted malformed request %s".format(e.reason))
      HttpResponse(BadRequest, "client submitted malformed request %s".format(e.reason))
    }
    req <- parseRequest(internalReq, pathParts)
  } yield req

}
