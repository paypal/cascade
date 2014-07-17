package com.paypal.stingray.http.resource

import akka.actor._
import scala.compat.Platform._
import scala.util.{Success, Try}
import spray.http._
import spray.http.StatusCodes._
import spray.http.Uri.Path
import spray.http.HttpHeaders.{RawHeader, `WWW-Authenticate`, Location}
import spray.http.HttpEntity.{Empty, NonEmpty}
import spray.http.{HttpRequest, HttpResponse}
import spray.routing.RequestContext
import com.paypal.stingray.akka.actor._
import com.paypal.stingray.common.constants.ValueConstants._
import com.paypal.stingray.http.util.HttpUtil
import scala.concurrent.duration._
import scala.reflect.ClassTag
import akka.actor.SupervisorStrategy.Escalate
import com.paypal.stingray.http.resource.HttpResourceActor.ResourceContext
import com.fasterxml.jackson.core.JsonParseException

/**
 * the actor to manage the execution of an [[com.paypal.stingray.http.resource.AbstractResourceActor]]. Create one of these per request
 */
abstract class HttpResourceActor(resourceContext: ResourceContext) extends ServiceActor {

  import HttpResourceActor._

  /*
   * Publicly overrideable
   */

  /**
   * This method will always be invoked before request processing begins. It is primarily provided for metrics tracking.
   *
   * If the method throws, an internal server error will be returned. As always, personally identifiable information should
   * never be included in exception messages.
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

  /*
   * Overrideable by subclasses
   */

  /**
   * Exception handler for creating an http error response when Status.Failure is received.
   *
   * @param exception the exception
   * @return a crafted HttpResponse from the error message
   */
  protected def handleError(exception: Exception): HttpResponse = exception match {
    case haltException: HaltException =>
      val response = addHeaderOnCode(haltException.response, Unauthorized) {
        `WWW-Authenticate`(HttpUtil.unauthorizedChallenge(request))
      }
      // If the error already has the right content type, let it through, otherwise coerce it
      val finalResponse = response.withEntity(response.entity.flatMap {
        entity: NonEmpty =>
          entity.contentType match {
            case HttpUtil.errorResponseType => entity
            case _ => HttpUtil.toJsonErrorsMap(entity.data.asString(charsetUtf8))
          }
      })
      if (finalResponse.status.intValue >= 500) {
        val statusCode = finalResponse.status.intValue
        log.warning(s"Request finished unsuccessfully with status code: $statusCode")
      }
      finalResponse
    case parseException: JsonParseException =>
      HttpResponse(BadRequest,
        HttpUtil.toJsonErrorsMap(Option(parseException.getMessage).getOrElse("")))
    case otherException: Exception =>
      HttpResponse(InternalServerError,
        HttpUtil.toJsonErrorsMap(Option(otherException.getMessage).getOrElse("")))
  }

  /*
   * Internal
   */
  case class RequestIsParsed(parsedRequest: AnyRef)
  case object BeforeExecuted
  case object ResponseContentTypeIsAcceptable

  private var pendingStep: Class[_] = HttpResourceActor.Start.getClass

  private def setNextStep[T](implicit classTag: ClassTag[T]): Unit = {
    pendingStep = classTag.runtimeClass
  }

  private val request = resourceContext.reqContext.request

  context.setReceiveTimeout(resourceContext.recvTimeout)

  //crash on unhandled exceptions
  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ => Escalate
    }

  override def receive: Actor.Receive = { // scalastyle:ignore cyclomatic.complexity scalastyle:ignore method.length

    //begin processing the request. check if content type is supported and response content type is acceptable
    case Start =>
      setNextStep[BeforeExecuted.type]
      self ! Try {
        before(request.method)
        BeforeExecuted
      }.orFailure

    case BeforeExecuted =>
      setNextStep[ResponseContentTypeIsAcceptable.type]
      self ! ensureContentTypeSupportedAndAcceptable.map { _ =>
        ResponseContentTypeIsAcceptable
      }.orFailure

    //the response content type is acceptable, now parse the request
    case ResponseContentTypeIsAcceptable =>
      setNextStep[RequestIsParsed]
      self ! resourceContext.reqParser.apply(request).map { p =>
        RequestIsParsed(p)
      }.orFailure

    //the request has been parsed, now process the request
    case RequestIsParsed(p) =>
      setNextStep[RequestIsProcessed]
      //account for extremely long processing times
      context.setReceiveTimeout(resourceContext.processRecvTimeout)
      self ! ProcessRequest(p)

    //the request has been processed, now construct the response, send it to the spray context, send it to the returnActor, and stop
    case RequestIsProcessed(resp, mbLocation) =>
      setNextStep[HttpResponse]
      context.setReceiveTimeout(resourceContext.recvTimeout)
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

      self ! finalResponse

    //we got a response to return (either through successful processing or an error handling),
    //so return it to the spray context and return actor and then stop
    case r: HttpResponse =>
      Try {
        after(r)
      }.recover {
        case t: Throwable => log.error(t, "An error occurred executing after()")
      }
      resourceContext.reqContext.complete(r)
      resourceContext.mbReturnActor.foreach { returnActor =>
        returnActor ! r
      }
      context.stop(self)

    //there was an error somewhere along the way, so translate it to an HttpResponse (using handleError),
    //send the exception to returnActor and stop
    case s @ Status.Failure(t) =>
      setNextStep[HttpResponse]
      log.warning("Unexpected request error: {} , cause: {}, trace: {}", t.getMessage, t.getCause, t.getStackTrace.mkString("", EOL, EOL))
      t match {
        case e: Exception => {
          val respFromError = handleError(e)
          val respPlusHeaders = respFromError.withHeaders(addLanguageHeader(responseLanguage, respFromError.headers))
          self ! respPlusHeaders
        }
        case t: Throwable => throw t
      }

    //the actor didn't receive a message before the current ReceiveTimeout
    case ReceiveTimeout =>
      val timeoutMillis = if (pendingStep == classOf[RequestIsProcessed]) {
        resourceContext.processRecvTimeout.toMillis
      } else { resourceContext.recvTimeout.toMillis }
      log.error(
        s"$self didn't receive message within $timeoutMillis milliseconds of the last one. next expected message was ${pendingStep.getName}")
      self ! HttpResponse(StatusCodes.ServiceUnavailable)
  }

  /**
   * Continues execution if this resource supports the content type sent in the request,
   * and can respond in a format the the requester can accept, or halts
   * @return a Try containing the acceptable content type found, or a failure
   */
  private def ensureContentTypeSupportedAndAcceptable: Try[ContentType] = {
    val supported = request.entity match {
      case Empty => Success(())
      case NonEmpty(ct, _) => acceptableContentTypes.contains(ct).orHaltWithT(UnsupportedMediaType)
    }
    supported.flatMap(_ =>
      request.acceptableContentType(List(responseContentType)).orHaltWithT(NotAcceptable))
  }

  /**
   * Given a matching HTTP response code, add the given header to that response
   * @param response the initial response
   * @param status the response status code
   * @param header the header to conditionally add
   * @return a possibly modified response
   */
  private def addHeaderOnCode(response: HttpResponse, status: StatusCode)
                             (header: => HttpHeader): HttpResponse = {
    if(response.status == status) {
      response.withHeaders(header :: response.headers)
    } else {
      response
    }
  }

  /**
   * Adds a `Content-Language` header to the current header list if the given `responseLanguage` is not None, and the
   * given `headers` list does not yet have a `Content-Language` header set
   * @param responseLanguage the value to assign the `Content-Language` header, or None, if not required
   * @param headers the current list of headers
   * @return augmented list of `HttpHeader` object, or the same list as `response.headers` if no modifications needed
   */
  private def addLanguageHeader(responseLanguage: Option[Language], headers: List[HttpHeader]) : List[HttpHeader] = {
    responseLanguage match {
      case Some(lang) =>
        if (headers.exists(_.lowercaseName == HttpUtil.CONTENT_LANGUAGE_LC)) {
          headers
        } else {
          RawHeader(HttpUtil.CONTENT_LANGUAGE, lang.toString()) :: headers
        }
      case None => headers
    }
  }

}

object HttpResourceActor {

  /**
   * ResourceContext contains all information needed to start an AbstractResourceActor
   * @param reqContext the spray [[spray.routing.RequestContext]] for this request
   * @param reqParser the function to parse the request into a valid scala type
   * @param mbReturnActor the actor to send the successful [[spray.http.HttpResponse]] or the failed [[java.lang.Throwable]].
   *                      optional - pass None to not do this
   * @param recvTimeout the longest time this actor will wait for any step (except the request processsing) to complete.
   *                    if this actor doesn't execute a step in time, it immediately fails and sends an [[spray.http.HttpResponse]] indicating the error to the
   *                    context and return actor.
   * @param processRecvTimeout the longest time this actor will wait for `reqProcessor` to complete
   */
  case class ResourceContext(reqContext: RequestContext,
                             reqParser: RequestParser,
                             mbReturnActor: Option[ActorRef] = None,
                             recvTimeout: FiniteDuration = HttpResourceActor.defaultRecvTimeout,
                             processRecvTimeout: FiniteDuration = HttpResourceActor.defaultProcessRecvTimeout)

  //requests
  /**
   * Sent to AbstractResourceActor to indicate that a request should be processed
   * @param req The parsed request to process
   */
  case class ProcessRequest(req: Any)

  //responses
  case class RequestIsProcessed(response: HttpResponse, mbLocation: Option[String])

  /**
   * the function that parses an [[spray.http.HttpRequest]] into a type, or fails
   */
  type RequestParser = HttpRequest => Try[AnyRef]

  /**
   * the only message to send each [[com.paypal.stingray.http.resource.HttpResourceActor]]. it begins processing the
   * [[com.paypal.stingray.http.resource.AbstractResourceActor]] that it contains
   */
  object Start

  /**
   * the default receive timeout for most steps in ResourceActor
   */
  val defaultRecvTimeout = 500.milliseconds

  /**
   * the receive timeout for the process function step in ResourceActor
   */
  val defaultProcessRecvTimeout = 4.seconds

  /**
   * create the [[akka.actor.Props]] for a new [[com.paypal.stingray.http.resource.HttpResourceActor]]
   * @param resourceActorProps function for creating props for an actor which will handle the request
   * @param reqContext the [[com.paypal.stingray.http.resource.HttpResourceActor.ResourceContext]] to pass to the
   *                   [[com.paypal.stingray.http.resource.HttpResourceActor]]
   * @param reqParser the parser function to pass to the [[com.paypal.stingray.http.resource.HttpResourceActor]]
   * @param mbResponseActor the optional actor to pass to the [[com.paypal.stingray.http.resource.HttpResourceActor]]
   * @return the new [[akka.actor.Props]]
   */
  def props(resourceActorProps: ResourceContext => AbstractResourceActor,
                           reqContext: RequestContext,
                           reqParser: RequestParser,
                           mbResponseActor: Option[ActorRef],
                           recvTimeout: FiniteDuration = defaultRecvTimeout,
                           processRecvTimeout: FiniteDuration = defaultProcessRecvTimeout): Props = {
    Props.apply(resourceActorProps(ResourceContext(reqContext, reqParser, mbResponseActor, recvTimeout, processRecvTimeout)))
      .withMailbox("single-consumer-mailbox")
  }

}
