package com.paypal.stingray.http.resource

import spray.http._
import scala.concurrent._
import org.slf4j.LoggerFactory
import akka.actor.{Status, Actor, ActorRef}
import scala.util.Failure
import com.paypal.stingray.common.option._
import spray.http.StatusCodes._
import spray.http.HttpRequest
import spray.http.HttpResponse
import spray.http.Language
import scala.util.Failure
import com.paypal.stingray.http.resource.ResourceHttpActor._
import spray.http.HttpRequest
import spray.http.HttpResponse
import com.paypal.stingray.http.resource.ResourceHttpActor.RequestIsProcessed
import spray.http.Language
import com.paypal.stingray.http.resource.ResourceHttpActor.CheckSupportedFormats

/**
 * Base class for HTTP resources built with Spray.
 *
 * See https://confluence.paypal.com/cnfl/display/stingray/AbstractResource%2C+ResourceDriver%2C+and+ResourceService
 * for more information.
 *
 * @param requestContext The ResourceActor which started this actor
 */
abstract class AbstractResourceActor(private val requestContext: ActorRef) extends Actor {

  /**
   * The receive function for this resource. Should not be overridden - implement [[processRequest]] instead
   */
  override final def receive: Actor.Receive = processRequest orElse defaultReceive

  /**
   * This method is overridden by the end-user to execute the requests served by this resource. The ParsedRequest object
   * will be sent to this message from ResourceActor via a tell. As an actor will be spun up for each request, it is
   * safe to store mutable state during this receive function.
   * When the request is finished, [[complete]] must be called
   * @return The receive function to be applied when a parsed request object or other actor message is received
   */
  protected def processRequest: PartialFunction[Any, Unit]

  private def defaultReceive: PartialFunction[Any, Unit] = {
    case CheckSupportedFormats() =>
      requestContext ! SupportedFormats(acceptableContentTypes,
        responseContentType,
        responseLanguage)
    case failed: Status.Failure =>
      logger.error("Error serving request", failed)
      requestContext ! failed
      context.stop(self)
    case other =>
      logger.error(s"Unhandled message: $other")
      requestContext ! Status.Failure(new IllegalStateException("Unhandled message to resource actor"))
      context.stop(self)
  }

  protected def complete(resp: HttpResponse): Unit = {
    requestContext ! RequestIsProcessed(resp, None)
    context.stop(self)
  }

  protected def complete(resp: HttpResponse, location: String): Unit = {
    requestContext ! RequestIsProcessed(resp, location.opt)
    context.stop(self)
  }

  protected def error(f: Exception): Unit = {
    requestContext ! Status.Failure(f)
    context.stop(self)
  }

  protected lazy val logger = LoggerFactory.getLogger(this.getClass)

  /**
   * Determines the AuthInfo for a given request, if authorized
   * @param r the parsed request
   * @return true iff the request is authorized to continue
   */
  def isAuthorized(r: HttpRequest): Boolean

  /**
   * A list of content types that that this server can accept, by default `application/json`.
   * These will be matched against the `Content-Type` header of incoming requests.
   * @return a list of content types
   */
  lazy val acceptableContentTypes: List[ContentType] = List(ContentTypes.`application/json`)

  /**
   * The content type that this server provides, by default `application/json`
   * @return a list of content types
   */
  lazy val responseContentType: ContentType = ContentTypes.`application/json`

  /**
   * The language of the data in the response, to for the Content-Language header
   *
   * @return a spray.http.Language value in an Option, or None, if the Content-Language header
   *         does not need to be set for this resource
   */
  lazy val responseLanguage: Option[Language] = Option(Language("en", "US"))

}
