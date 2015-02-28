/**
 * Copyright 2013-2014 PayPal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paypal.cascade.http.resource

import scala.util.{Failure, Success}

import akka.actor.{Actor, Status}
import spray.http.{HttpResponse, _}

import com.paypal.cascade.common.option._
import com.paypal.cascade.http.resource.HttpResourceActor.RequestIsProcessed
import com.paypal.cascade.http.util.HttpUtil
import com.paypal.cascade.json._

/**
 * Base class for HTTP resources built with Spray.
 * @param resourceContext Context containing information needed to service the request, such as the parent actor
 */
abstract class AbstractResourceActor(private val resourceContext: HttpResourceActor.ResourceContext)
  extends HttpResourceActor(resourceContext) {

  /**
   * The receive function for this resource. Should not be overridden - implement [[resourceReceive]] instead
   */
  override final def receive: Actor.Receive = {
    // needs extra type inference assistance to prevent a lint error
    super.receive.orElse[Any, Unit](resourceReceive).orElse[Any, Unit](failureReceive)
  }

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
   * Receive Status.Failure last, so that clients can receive on it.
   */
  private def failureReceive: Actor.Receive = {
    case s @ Status.Failure(t: Throwable) =>
      handleRequestError(t)
  }

  /**
   * There was an error somewhere along the way, so translate it to an HttpResponse (using createErrorResponse),
   * send the exception to returnActor and stop.
   * @param t the error that occurred
   */
  private def handleRequestError(t: Throwable): Unit = {
    t match {
      case e: Exception => completeRequest(createErrorResponse(e))
      case t: Throwable => throw t
    }
  }

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
    response.toJson.orErrorWithMessage(t => s"Could not write response to json: ${t.getClass.getSimpleName}") match {
      case Success(jsonStr) => complete(HttpResponse(code, jsonStr))
      case Failure(t) => handleRequestError(t)
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
    response.toJson.orErrorWithMessage(t => s"Could not write response to json: ${t.getClass.getSimpleName}") match {
      case Success(jsonStr) => complete(HttpResponse(code, jsonStr), location)
      case Failure(t) => handleRequestError(t)
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
   * Return an internal server error in response to a throwable. The type of the throwable should have a case in
   * [[com.paypal.cascade.http.resource.HttpResourceActor#createErrorResponse createErrorResponse]].
   * @param f The error to be used for server response.
   */
  protected final def sendError(f: Throwable): Unit = {
    handleRequestError(f)
  }

  /**
   * Return an error with the specified status code and error object.
   *
   * @param code The error code to return
   * @param error Error to return, will be converted to JSON
   * @tparam T Type of the error
   */
  protected final def sendErrorResponse[T : Manifest](code: StatusCode, error: T): Unit = {
    handleRequestError(HaltException(code, HttpUtil.toJsonBody(error)))
  }

  /**
   * Return an error with the specified Status code
   * @param code The error code to return
   */
  protected final def sendErrorCodeResponse(code: StatusCode): Unit = {
    handleRequestError(HaltException(code))
  }

}
