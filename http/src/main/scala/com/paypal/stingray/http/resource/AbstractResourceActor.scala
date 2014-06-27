package com.paypal.stingray.http.resource

import spray.http._
import akka.actor.{Status, Actor, ActorRef}
import com.paypal.stingray.common.option._
import com.paypal.stingray.http.resource.HttpResourceActor._
import spray.http.HttpResponse
import spray.http.Language
import com.paypal.stingray.akka.actor.ServiceActor
import com.paypal.stingray.http.util.HttpUtil
import com.paypal.stingray.json._
import scala.util.{Failure, Success}

/**
 * Base class for HTTP resources built with Spray.
 *
 * See https://confluence.paypal.com/cnfl/display/stingray/AbstractResource%2C+ResourceDriver%2C+and+ResourceService
 * for more information.
 *
 * @param resourceContext Context containing information needed to service the request, such as the parent actor
 */
abstract class AbstractResourceActor(private val resourceContext: HttpResourceActor.ResourceContext) extends ServiceActor {

  /**
   * The receive function for this resource. Should not be overridden - implement [[resourceReceive]] instead
   */
  override final def receive: Actor.Receive = defaultReceive orElse resourceReceive orElse errorCatching

  /**
   * This method is overridden by the end-user to execute the requests served by this resource. The ParsedRequest object
   * will be sent to this message from ResourceActor via a tell. As an actor will be spun up for each request, it is
   * safe to store mutable state during this receive function.
   * When the request is finished, one of the provided complete methods must be called
   *
   * @return The receive function to be applied when a parsed request object or other actor message is received
   */
  protected def resourceReceive: Actor.Receive

  private def defaultReceive: Actor.Receive = {
    case CheckSupportedFormats =>
      resourceContext.httpActor ! SupportedFormats(acceptableContentTypes,
        responseContentType,
        responseLanguage)
  }

  private def errorCatching: Actor.Receive = {
    case failed: Status.Failure =>
      log.error("Error serving request", failed)
      resourceContext.httpActor ! failed
      context.stop(self)
  }

  /**
   * Complete a successful request
   * @param resp The HttpResponse to be returned
   */
  protected final def complete(resp: HttpResponse): Unit = {
    resourceContext.httpActor ! RequestIsProcessed(resp, None)
    context.stop(self)
  }

  /**
   * Complete a successful request with a JSON response
   * @param code Status code to return
   * @param response Object to be serialized in the response
   * @tparam T Type of the object to be returned
   */
  protected final def completeToJSON[T](code: StatusCode, response: T): Unit = {
    response.toJson match {
      case Success(jsonStr) => complete(HttpResponse(code, jsonStr))
      case Failure(_) => sendErrorResponse(StatusCodes.InternalServerError, "Could not write response to json")
    }
  }

  /**
   * Complete a successful request with a JSON response
   * @param code Status code to return
   * @param response Object to be serialized in the response
   * @param location Value for the HTTP Location header
   * @tparam T Type of the object to be returned
   */
  protected final def completeToJSON[T](code: StatusCode, response: T, location: String): Unit = {
    response.toJson match {
      case Success(jsonStr) => complete(HttpResponse(code, jsonStr), location)
      case Failure(_) => sendErrorResponse(StatusCodes.InternalServerError, "Could not write response to json")
    }
  }

  /**
   * Complete a successful request
   * @param resp The HttpResponse to be returned
   * @param location Value for the HTTP location header
   */
  protected final def complete(resp: HttpResponse, location: String): Unit = {
    resourceContext.httpActor ! RequestIsProcessed(resp, location.opt)
    context.stop(self)
  }

  /**
   * Return an internal server error in response to a throwable
   * @param f The error to be logged
   */
  protected final def sendError(f: Throwable): Unit = {
    resourceContext.httpActor ! Status.Failure(f)
    context.stop(self)
  }

  // TODO add docs
  protected final def sendErrorResponse[T : Manifest](code: StatusCode, error: T): Unit = {
    resourceContext.httpActor ! Status.Failure(HaltException(code, HttpUtil.coerceError(error)))
  }

  /**
   * Return an error with the specified Status code
   * @param code The error code to return
   */
  protected final def sendErrorResponseCode(code: StatusCode): Unit = {
    resourceContext.httpActor ! Status.Failure(HaltException(code))
    context.stop(self)
  }

  /**
   * Return an error with the specified Status code
   * @param code The error code to return
   * @param msg Message to be returned, will be converted to JSON
   */
  protected final def sendErrorResponseMap(code: StatusCode, msg: String): Unit = {
    resourceContext.httpActor ! Status.Failure(HaltException(code, HttpUtil.coerceErrorMap(msg)))
    context.stop(self)
  }

  /**
   * A list of content types that that this server can accept, by default `application/json`.
   * These will be matched against the `Content-Type` header of incoming requests.
   * @return a list of content types
   */
  val acceptableContentTypes: List[ContentType] = List(ContentTypes.`application/json`)

  /**
   * The content type that this server provides, by default `application/json`
   * @return a list of content types
   */
  val responseContentType: ContentType = ContentTypes.`application/json`

  /**
   * The language of the data in the response, to for the Content-Language header
   *
   * @return a spray.http.Language value in an Option, or None, if the Content-Language header
   *         does not need to be set for this resource
   */
  val responseLanguage: Option[Language] = Option(Language("en", "US"))

}
