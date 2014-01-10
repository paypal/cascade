package com.paypal.stingray.http.resource

import scalaz._
import Scalaz._
import net.liftweb.json.scalaz.JsonScalaz._
import net.liftweb.json._
import com.paypal.stingray.common.json.JSONUtil._
import com.paypal.stingray.common.validation._
import spray.http._
import spray.http.HttpMethods._
import spray.http.HttpResponse
import StatusCodes._
import com.paypal.stingray.common.logging.LoggingSugar
import scala.concurrent._
import org.slf4j.LoggerFactory

/**
 *
 * @tparam ParsedRequest A representation of the request as this resource sees it. This should contain all the data from the request
 *                       needed by this resource to produce the response (except the body). Use the type HttpRequest and mix in
 *                       NoParsing to skip parsing
 * @tparam AuthInfo a structure for information gained during authorization. Use the type NoAuth and mixin AlwaysAuthorized to skip
 *                  authorization
 * @tparam PostBody the class to serialize the POST body to. Use the type NoBody if the resource doesn't do POST, or doesn't use a body
 * @tparam PutBody the class to serialize the PUT body to. Use the type NoBody if the resource doesn't do PUT, or doesn't use a body
 */
abstract class SimpleResource[ParsedRequest, AuthInfo, PostBody: JSONR, PutBody: JSONR] extends Resource[ParsedRequest, AuthInfo, PostBody, PutBody] with LoggingSugar {

  override protected lazy val logger = LoggerFactory.getLogger(this.getClass)

  override def parsePostBody(r: HttpRequest): Future[Option[PostBody]] = {
    fromJsonBody[PostBody](r)
  }

  override def parsePutBody(r: HttpRequest): Future[Option[PutBody]] = {
    fromJsonBody[PutBody](r)
  }
}
