package com.paypal.stingray.http.resource

import akka.actor._
import akka.pattern.pipe
import scala.util.{Success, Try}
import scala.concurrent.{ExecutionContext, Future}
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

/**
 * the actor to manage the execution of an [[AbstractResource]]. create one of these per request
 * @param resource the resource to execute
 * @param reqContext the spray [[RequestContext]] for this request
 * @param reqParser the function to parse the request into a valid scala type
 * @param reqProcessor the function to process the actual request
 * @param mbReturnActor the actor to send the successful [[HttpResponse]] or the failed [[Throwable]]. optional - pass None to not do this
 * @param recvTimeout the longest time this actor will wait for any step (except the request processsing) to complete.
 *                    if this actor doesn't execute a step in time, it immediately fails and sends an [[HttpResponse]] indicating the error to the
 *                    context and return actor.
 * @param processRecvTimeout the longest time this actor will wait for `reqProcessor` to complete
 * @tparam AuthInfo the authorization info type that [[AbstractResource]] uses
 * @tparam ParsedRequest the type that the request gets parsed into
 */
class ResourceActor[AuthInfo, ParsedRequest](resource: AbstractResource[AuthInfo],
                                             reqContext: RequestContext,
                                             reqParser: ResourceActor.RequestParser[ParsedRequest],
                                             reqProcessor: ResourceActor.RequestProcessor[ParsedRequest],
                                             mbReturnActor: Option[ActorRef],
                                             recvTimeout: Duration = ResourceActor.defaultRecvTimeout,
                                             processRecvTimeout: Duration = ResourceActor.processRecvTimeout) extends ServiceActor {

  import context.dispatcher
  import ResourceActor._

  case class MessageIsSupported(a: HttpRequest)
  case class RequestIsParsed(p: ParsedRequest)
  case class ContentTypeIsSupported(p: ParsedRequest)
  case class ResponseContentTypeIsAcceptable(p: ParsedRequest)
  case class RequestIsAuthorized(p: ParsedRequest)
  case class RequestIsProcessed(response: HttpResponse, mbLocation: Option[String])

  private var pendingStep: Class[_] = ResourceActor.Start.getClass
  private def setNextStep[T](implicit classTag: ClassTag[T]): Unit = {
    pendingStep = classTag.runtimeClass
  }

  private val request = reqContext.request

  log.debug(s"started $self with request $request and resource ${resource.getClass.getSimpleName}")

  context.setReceiveTimeout(recvTimeout)

  override def receive: Actor.Receive = {

    //begin processing the request
    case Start =>
      self ! ensureMethodSupported(resource, request.method).map { _ =>
        MessageIsSupported(request)
      }.orFailure
      setNextStep[MessageIsSupported]

    //the HTTP method is supported, now parse the request
    case MessageIsSupported(a) =>
      self ! reqParser(a).map { p =>
        RequestIsParsed(p)
      }.orFailure
      setNextStep[RequestIsParsed]

    //the request has been parsed, now check if the content type is supported
    case RequestIsParsed(p) =>
      self ! ensureContentTypeSupported(resource, request).map { _ =>
        ContentTypeIsSupported(p)
      }.orFailure
      setNextStep[ContentTypeIsSupported]

    //the content type is supported, now check if the response content type is acceptable
    case ContentTypeIsSupported(p) =>
      self ! ensureResponseContentTypeAcceptable(resource, request).map { _ =>
        ResponseContentTypeIsAcceptable(p)
      }.orFailure
      setNextStep[ResponseContentTypeIsAcceptable]

    //the response content type is acceptable, now check if the request is authorized
    case ResponseContentTypeIsAcceptable(p) =>
      ensureAuthorized(resource, request).map { _ =>
        RequestIsAuthorized(p)
      }.recover(handleErrorPF).pipeTo(self)
      setNextStep[RequestIsAuthorized]

    //the request is authorized, now process the request
    case RequestIsAuthorized(p) =>
      //account for extremely long processing times
      context.setReceiveTimeout(processRecvTimeout)
      reqProcessor.apply(p).map { case (response, mbLocation) =>
        RequestIsProcessed(response, mbLocation)
      }.recover(handleErrorPF).pipeTo(self)
      setNextStep[RequestIsProcessed]

    //the request has been processed, now construct the response, send it to the spray context, send it to the returnActor, and stop
    case RequestIsProcessed(resp, mbLocation) =>
      context.setReceiveTimeout(recvTimeout)
      val responseWithLocation = addHeaderOnCode(resp, Created) {
        // if an `X-Forwarded-Proto` header exists, read the scheme from that; else, preserve what was given to us
        val newScheme = request.headers.find(_.name == "X-Forwarded-Proto").map(_.value).getOrElse(request.uri.scheme)

        // if we created something, `location` will have more information to append to the response path
        val newPath = Path(request.uri.path.toString + mbLocation.map("/" + _).getOrElse(""))

        // copy the request uri, replacing scheme and path as needed, and return a `Location` header with the new uri
        val newUri = request.uri.copy(scheme = newScheme, path = newPath)
        Location(newUri)
      }
      val headers = addLanguageHeader(resource.responseLanguage, responseWithLocation.headers)
      // Just force the request to the right content type

      val finalResponse: HttpResponse = responseWithLocation.withHeadersAndEntity(headers, responseWithLocation.entity.flatMap { entity: NonEmpty =>
        HttpEntity(resource.responseContentType, entity.data)
      })

      self ! finalResponse

      setNextStep[HttpResponse]


    //we got a response to return (either through successful processing or an error handling), so return it to the spray context and return actor and then stop
    case r: HttpResponse =>
      reqContext.complete(r)
      mbReturnActor.foreach { returnActor =>
        returnActor ! r
      }
      context.stop(self)

    //there was an error somewhere along the way, so translate it to an HttpResponse (using handleError), send the exception to returnActor and stop
    case s @ Status.Failure(t) =>
      log.error(t, s"Unexpected error: request: $request error: ${t.getMessage}")
      t match {
        case e: Exception => self ! handleError(e)
        case t: Throwable =>
          throw t
      }
      setNextStep[HttpResponse]

    //the actor didn't receive a method before startTimeout
    case ReceiveTimeout =>
      log.error(s"$self didn't receive a next message within $recvTimeout of the last one. next expected message was ${pendingStep.getName}")
      self ! HttpResponse(StatusCodes.ServiceUnavailable)
  }

  /**
   * Continues execution if this method is supported, or halts
   * @param resource this resource
   * @param method the method sent
   * @return an empty Try
   */
  private def ensureMethodSupported(resource: AbstractResource[_],
                                    method: HttpMethod): Try[Unit] = {
    resource.supportedHttpMethods.contains(method).orHaltWithT(MethodNotAllowed)
  }

  /**
   * Continues execution and yields an `AuthInfo` if this method is authorized, or halts
   * @param resource this resource
   * @param request the request
   * @tparam AI the `AuthInfo` type
   * @return a Future containing an `AuthInfo` object, or a failure
   */
  private def ensureAuthorized[AI](resource: AbstractResource[AI],
                                   request: HttpRequest)
                                  (implicit ctx: ExecutionContext): Future[AI] = {
    for {
      authInfoOpt <- resource.isAuthorized(request)
      authInfo <- authInfoOpt.orHaltWith(Unauthorized)
    } yield authInfo
  }

  /**
   * Continues execution if this resource supports the content type sent in the request, or halts
   * @param resource this resource
   * @param request the request
   * @return an empty Try
   */
  private def ensureContentTypeSupported(resource: AbstractResource[_],
                                         request: HttpRequest): Try[Unit] = {
    request.entity match {
      case Empty => Success()
      case NonEmpty(ct, _) => resource.acceptableContentTypes.contains(ct).orHaltWithT(UnsupportedMediaType)
    }
  }

  /**
   * Continues execution if this resource can respond in a format that the requester can accept, or halts
   * @param resource this resource
   * @param request the request
   * @return a Try containing the acceptable content type found, or a failure
   */
  private def ensureResponseContentTypeAcceptable(resource: AbstractResource[_],
                                                  request: HttpRequest): Try[ContentType] = {
    request.acceptableContentType(List(resource.responseContentType)).orHaltWithT(NotAcceptable)
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

  private val handleErrorPF: PartialFunction[Throwable, HttpResponse] = {
    case e: Exception => handleError(e)
  }
  private def handleError(exception: Exception) = {
    exception match {
      case h: HaltException =>
        val response = addHeaderOnCode(h.response, Unauthorized) {
          `WWW-Authenticate`(resource.unauthorizedChallenge(request))
        }
        val headers = addLanguageHeader(resource.responseLanguage, response.headers)
        // If the error already has the right content type, let it through, otherwise coerce it
        val finalResponse = response.withHeadersAndEntity(headers, response.entity.flatMap {
          entity: NonEmpty =>
            entity.contentType match {
              case resource.responseContentType => entity
              case _ => resource.coerceError(entity.data.toByteArray)
            }
        })
        if (finalResponse.status.intValue >= 500) {
          log.warning(s"Request finished unsuccessfully: request: $request response: $finalResponse")
        }
        finalResponse
      case otherException =>
        log.error(s"Unexpected error: request: $request error: ${otherException.getMessage}", otherException)
        HttpResponse(InternalServerError,
          resource.coerceError(Option(otherException.getMessage).getOrElse("").getBytes(charsetUtf8)),
          addLanguageHeader(resource.responseLanguage, Nil))
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

object ResourceActor {
  /**
   * the function that parses an [[HttpRequest]] into a type, or fails
   * @tparam T the type to parse the request into
   */
  type RequestParser[T] = HttpRequest => Try[T]

  /**
   * the function to process the request and output a result future. the return of this function is a tuple.
   * the first element is the [[HttpResponse]] to return to the client.
   * the second element is the (optional) value of the location header to return to the client.
   * @tparam T the type to process
   */
  type RequestProcessor[T] = T => Future[(HttpResponse, Option[String])]

  /**
   * the only message to send each [[ResourceActor]]. it begins processing the [[AbstractResource]] that it contains
   */
  object Start

  /**
   * the default receive timeout for most steps in ResourceActor
   */
  val defaultRecvTimeout = 250.milliseconds

  /**
   * the receive timeout for the process function step in ResourceActor
   */
  val processRecvTimeout = 2.seconds

  val dispatcherName = "resource-actor-dispatcher"

  /**
   * create the [[Props]] for a new [[ResourceActor]]
   * @param resource the resource to pass to the [[ResourceActor]]
   * @param reqContext the [[RequestContext]] to pass to the [[ResourceActor]]
   * @param reqParser the parser function to pass to the [[ResourceActor]]
   * @param reqProcessor the processor function to pass to the [[ResourceActor]]
   * @param mbResponseActor the optional actor to pass to the [[ResourceActor]]
   * @tparam AuthInfo the authorization info type for [[AbstractResource]]
   * @tparam ParsedRequest the type of the parsed request
   * @return the new [[Props]]
   */
  def props[AuthInfo, ParsedRequest](resource: AbstractResource[AuthInfo],
                                     reqContext: RequestContext,
                                     reqParser: ResourceActor.RequestParser[ParsedRequest],
                                     reqProcessor: ResourceActor.RequestProcessor[ParsedRequest],
                                     mbResponseActor: Option[ActorRef],
                                     recvTimeout: Duration = defaultRecvTimeout): Props = {
    Props.apply(new ResourceActor(resource, reqContext, reqParser, reqProcessor, mbResponseActor, recvTimeout))
      .withDispatcher(dispatcherName)
      .withMailbox("single-consumer-mailbox")
  }

}
