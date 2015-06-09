/**
 * Copyright 2013-2015 PayPal
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

import java.nio.charset.StandardCharsets.UTF_8

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

import akka.actor.SupervisorStrategy.Escalate
import akka.actor._
import spray.http.HttpEntity.{Empty, NonEmpty}
import spray.http.HttpHeaders.{Location, RawHeader, `WWW-Authenticate`}
import spray.http.StatusCodes._
import spray.http.Uri.Path
import spray.http.{HttpRequest, HttpResponse, _}
import spray.routing.RequestContext

import com.paypal.cascade.akka.actor._
import com.paypal.cascade.http.resource.HttpResourceActor._
import com.paypal.cascade.http.util.{HttpErrorResponeHandler, HttpUtil}

/**
 * the actor to manage the execution of an [[com.paypal.cascade.http.resource.AbstractResourceActor]]. Create one of these per request
 */
private[http] abstract class HttpResourceActor(resourceContext: ResourceContext) extends ServiceActor with HttpErrorResponeHandler {

  /**
   * This method will always be invoked before request processing begins. It is primarily provided for metrics tracking.
   *
   * If the method throws, an error response will be returned. The response will be determined by
   * [[com.paypal.cascade.http.resource.HttpResourceActor#createErrorResponse createErrorResponse]].
   * As always, personally identifiable information should never be included in exception messages.
   * @param method The Http method of the request in question
   */
  def before(method: HttpMethod): Unit = {}

  /**
   * This method will always be invoked after request processing is finished.
   *
   * If the method throws, the error will be logged and the given response will still be returned. As always, personally
   * identifiable information should never be included in exception messages.
   * @param resp The response to be returned to the client
   */
  def after(resp: HttpResponse): Unit = {}

  /**
   * A list of content types that that this server can accept, by default `application/json`.
   * These will be matched against the `Content-Type` header of incoming requests.
   * @return a list of content types
   */
  val acceptableContentTypes: List[ContentType] = List(ContentTypes.`application/json`, ContentTypes.`application/json`.withoutDefinedCharset)

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

  import context.dispatcher // used for cancellable below
  /**
   * A scheduled task which times the request out after the
   * [[com.paypal.cascade.http.resource.HttpResourceActor.ResourceContext.resourceTimeout resourceTimeout]].
   * See the Akka <a href="http://doc.akka.io/docs/akka/2.3.9/scala/scheduler.html">Scheduler</a> documentation.
   *
   * @return a Cancellable which will run after the context timeout unless it is cancelled via its cancel method.
   */
  protected final lazy val timeoutCancellable: Cancellable = {
    context.system.scheduler.scheduleOnce(resourceContext.resourceTimeout, self, RequestTimedOut)
  }

  /**
   * Creates an appropriate HttpResponse for a given exception.
   *
   * @param exception the exception
   * @return a crafted HttpResponse from the error message
   */
  protected def createErrorResponse(exception: Exception): HttpResponse = {
    createErrorResponse(exception, request, responseLanguage)
  }

  /**
   * Creates an appropriate error message for when a request could not be parsed.
   * @param t the throwable which caused the error
   * @return a message describing the parsing error
   */
  protected def parseErrorMessage(t: Throwable): String = s"Unable to parse request: ${t.getClass.getSimpleName}"

  /*
   * Internal
   */
  private[this] val request = resourceContext.reqContext.request

  /** crash on unhandled exceptions */
  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ => Escalate
    }

  /**
   * There was an error somewhere along the way, so translate it to an HttpResponse (using createErrorResponse),
   * send the exception to returnActor and stop.
   * @param t the error that occurred
   * @throws Throwable if t is not of type `Exception`
   */
  @throws[Throwable]
  private[http] final def handleRequestError(t: Throwable): Unit = {
    t match {
      case e: Exception => completeRequest(createErrorResponse(e, request, responseLanguage))
      case t: Throwable => throw t
    }
  }

  override def receive: Actor.Receive = { // scalastyle:ignore cyclomatic.complexity scalastyle:ignore method.length

    /*
     * begin processing the request:
     *   a) check if the content type is supported and response content type is acceptable
     *   b) next parse the request
     *   c) then process the request
     */
    case Start =>
      timeoutCancellable // initialize the timeout event
      val processRequest = for {
        _ <- logFailure("before()", before(request.method))
        _ <- ensureContentTypeSupportedAndAcceptable
        req <- resourceContext.reqParser(request).orHaltWithMessage(BadRequest)(parseErrorMessage).map(ProcessRequest)
      } yield req
      processRequest match {
        case Success(proc) => self ! proc
        case Failure(t) => handleRequestError(t)
      }

    //the request has been processed, now construct the response, send it to the spray context, send it to the returnActor, and stop
    case RequestIsProcessed(resp, mbLocation) =>
      timeoutCancellable.cancel() // preemptively cancel the timeout to reduce dead letters
      val responseWithLocation = addHeaderOnCode(resp, Created) {
        // if an `X-Forwarded-Proto` header exists, read the scheme from that; else, preserve what was given to us
        val newScheme = request.headers.find(_.name == "X-Forwarded-Proto") match {
          case Some(hdr) => hdr.value
          case None => request.uri.scheme
        }

        // if we created something, `location` will have more information to append to the response path
        val finalLocation = mbLocation match {
          case Some(loc) => s"/$loc"
          case None => ""
        }
        val newPath = Path(request.uri.path.toString + finalLocation)

        // copy the request uri, replacing scheme and path as needed, and return a `Location` header with the new uri
        val newUri = request.uri.copy(scheme = newScheme, path = newPath)
        Location(newUri)
      }
      val headers = addLanguageHeader(responseLanguage, responseWithLocation.headers)
      // Just force the request to the right content type

      val finalResponse: HttpResponse = responseWithLocation.withHeadersAndEntity(headers, responseWithLocation.entity.flatMap {
        entity: NonEmpty =>
          HttpEntity(responseContentType, entity.data)
      })

      completeRequest(finalResponse)

    //the actor didn't complete the request before the request timeout
    case RequestTimedOut =>
      log.error(s"Did not complete request within ${resourceContext.resourceTimeout}.")
      completeRequest(createErrorResponse(HaltException(StatusCodes.ServiceUnavailable)))
  }

  private[this] def logFailure[T](method: String, fun: => T): Try[T] = {
    val attempt = Try(fun)
    attempt match {
      case Failure(fail) =>
        log.error(fail, s"An error occurred executing $method")
        attempt
      case _ => attempt
    }
  }

  /**
   * The only proper way to finish the request and kill this actor.
   * @param r the response to send
   */
  private[resource] def completeRequest(r: HttpResponse): Unit = {
    timeoutCancellable.cancel() // we are about to stop the actor, so cancel to avoid a dead letter
    logFailure("after()", after(r))
    resourceContext.reqContext.complete(r)
    resourceContext.mbReturnActor.foreach { returnActor =>
      returnActor ! r
    }
    context.stop(self)
  }

  /**
   * Continues execution if this resource supports the content type sent in the request,
   * and can respond in a format the the requester can accept, or halts
   * @return a Try containing the acceptable content type found, or a failure
   */
  private[this] def ensureContentTypeSupportedAndAcceptable: Try[ContentType] = {
    val supported = request.entity match {
      case Empty => Success(())
      case NonEmpty(ct, _) => acceptableContentTypes.contains(ct).orHaltWithT(UnsupportedMediaType)
    }
    supported.flatMap(_ =>
      request.acceptableContentType(List(responseContentType)).orHaltWithT(NotAcceptable))
  }

}

object HttpResourceActor {
  /* requests */
  /**
   * Contains all information needed to start an HttpResourceActor.
   * @param reqContext the spray `spray.routing.RequestContext` for this request
   * @param reqParser the function to parse the request into a valid scala type
   * @param mbReturnActor the actor to send the successful [[spray.http.HttpResponse]] or the failed `java.lang.Throwable`.
   *                      optional - pass None to not do this
   * @param resourceTimeout the time after which the request will time out, from the start of the request (i.e. when
   *                        the resource actor receives [[com.paypal.cascade.http.resource.HttpResourceActor.Start Start]].)
   *                        Not the same as spray's timeout. This can be specified on a per-request basis and is more granular.
   */
  case class ResourceContext(reqContext: RequestContext,
                             reqParser: RequestParser,
                             mbReturnActor: Option[ActorRef] = None,
                             resourceTimeout: FiniteDuration = HttpResourceActor.defaultResourceTimeout)

  /**
   * the function that parses an [[spray.http.HttpRequest]] into a type, or fails
   */
  type RequestParser = HttpRequest => Try[AnyRef]

  /**
   * Sent to AbstractResourceActor to indicate that a request should be processed
   * @param req The parsed request to process
   */
  case class ProcessRequest(req: Any)

  /* responses */
  /**
   * Used to notify the resource actor that the server has processed the request and can complete it
   * @param response the response to send
   * @param mbLocation optional location for the returned resource if something was created
   */
  private[resource] case class RequestIsProcessed(response: HttpResponse, mbLocation: Option[String])

  /**
   * the only message to send each `com.paypal.cascade.http.resource.HttpResourceActor`. it begins processing the
   * [[com.paypal.cascade.http.resource.AbstractResourceActor]] that it contains
   */
  private[http] object Start

  /**
   * Signals that the request timed out.
   */
  private[HttpResourceActor] case object RequestTimedOut

  /**
   * The default timeout for a request which is not completed in time. Not the same as spray's timeout.
   */
  val defaultResourceTimeout = 4000.milliseconds

  /**
   * create the `akka.actor.Props` for a new `com.paypal.cascade.http.resource.HttpResourceActor`
   * @param resourceActorProps function for creating props for an actor which will handle the request
   * @param reqContext the [[com.paypal.cascade.http.resource.HttpResourceActor.ResourceContext]] to pass to the
   *                   `com.paypal.cascade.http.resource.HttpResourceActor`
   * @param reqParser the parser function to pass to the `com.paypal.cascade.http.resource.HttpResourceActor`
   * @param mbResponseActor the optional actor to pass to the `com.paypal.cascade.http.resource.HttpResourceActor`
   * @param resourceTimeout the amount of time until the request times out after it has been started. Not the same as
   *                        spray's timeout. This can be specified on a per-request basis and is more granular.
   * @return the new `akka.actor.Props`
   */
  def props(resourceActorProps: ResourceContext => AbstractResourceActor,
            reqContext: RequestContext,
            reqParser: RequestParser,
            mbResponseActor: Option[ActorRef],
            resourceTimeout: FiniteDuration = defaultResourceTimeout): Props = {
    Props(resourceActorProps(ResourceContext(reqContext, reqParser, mbResponseActor, resourceTimeout)))
      .withMailbox("single-consumer-mailbox")
  }

}
