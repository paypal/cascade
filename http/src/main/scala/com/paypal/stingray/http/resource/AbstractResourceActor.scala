package com.paypal.stingray.http.resource

import spray.http._
import akka.actor.{Status, Actor}
import com.paypal.stingray.common.option._
import com.paypal.stingray.http.util.HttpUtil
import com.paypal.stingray.json._
import akka.event.LoggingReceive
import scala.util.{Success, Failure}
import spray.http.HttpResponse
import com.paypal.stingray.http.resource.HttpResourceActor.RequestIsProcessed

/**
 * Base class for HTTP resources built with Spray.
 *
 * See https://confluence.paypal.com/cnfl/display/stingray/AbstractResource%2C+ResourceDriver%2C+and+ResourceService
 * for more information.
 *
 * @param resourceContext Context containing information needed to service the request, such as the parent actor
 */
abstract class AbstractResourceActor(private val resourceContext: HttpResourceActor.ResourceContext) extends HttpResourceActor(resourceContext) {

  /**
   * The receive function for this resource. Should not be overridden - implement [[resourceReceive]] instead
   */
  override final def receive: Actor.Receive = LoggingReceive { super.receive orElse resourceReceive }

  /**
   * This method is overridden by the end-user to execute the requests served by this resource. The ParsedRequest object
   * will be sent to this message from ResourceActor via a tell. As an actor will be spun up for each request, it is
   * safe to store mutable state during this receive function.
   * When the request is finished, one of the provided complete methods must be called
   *
   * This receive should handle {{{ProcessRequest(_)}}} messages.
   *
   * @return The receive function to be applied when a parsed request object or other actor message is received
   */
  protected def resourceReceive: Actor.Receive

  /**
   * Complete a successful request
   * @param resp The HttpResponse to be returned
   */
  protected final def complete(resp: HttpResponse): Unit = {
    self ! RequestIsProcessed(resp, None)
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
    self ! RequestIsProcessed(resp, location.opt)
  }

  /**
   * Return an internal server error in response to a throwable
   * @param f The error to be logged
   */
  protected final def sendError(f: Throwable): Unit = {
    self ! Status.Failure(f)
  }

  /**
   * Return an error with the specified status code and error object.
   *
   * @param code The error code to return
   * @param error Error to return, will be converted to JSON
   * @tparam T Type of the error
   */
  protected final def sendErrorResponse[T : Manifest](code: StatusCode, error: T): Unit = {
    self ! Status.Failure(HaltException(code, HttpUtil.toJsonErrors(error)))
  }

  /**
   * Return an error with the specified Status code
   * @param code The error code to return
   */
  protected final def sendErrorCodeResponse(code: StatusCode): Unit = {
    self ! Status.Failure(HaltException(code))
  }

  /**
   * Return an error with the specified Status code
   * @param code The error code to return
   * @param msg Message to be returned, will be converted to JSON
   */
  protected final def sendErrorMapResponse(code: StatusCode, msg: String): Unit = {
    self ! Status.Failure(HaltException(code, HttpUtil.toJsonErrorsMap(msg)))
  }

}
