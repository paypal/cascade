package com.paypal.stingray.http.resource

import akka.actor._
import akka.pattern.pipe
import scala.util.{Success, Try}
import scala.concurrent.{ExecutionContext, Future}
import spray.http._
import spray.http.StatusCodes._
import spray.http.Uri.Path
import spray.http.HttpHeaders.{`WWW-Authenticate`, Location}
import spray.http.HttpEntity.{Empty, NonEmpty}
import spray.http.{HttpRequest, HttpResponse}
import spray.routing.RequestContext
import com.paypal.stingray.common.actor._
import com.paypal.stingray.common.constants.ValueConstants._
import com.paypal.stingray.common.option._

class ResourceActor[AuthInfo, ParsedRequest](resource: AbstractResource[AuthInfo],
                                             reqContext: RequestContext,
                                             reqParser: ResourceActor.RequestParser[ParsedRequest],
                                             reqProcessor: ResourceActor.RequestProcessor[ParsedRequest],
                                             mbReturnActor: Option[ActorRef]) extends ServiceActor {

  import context.dispatcher
  import ResourceActor._

  sealed trait ResourceMessage
  case class MessageIsSupported(a: HttpRequest) extends ResourceMessage
  case class RequestIsParsed(p: ParsedRequest) extends ResourceMessage
  case class ContentTypeIsSupported(p: ParsedRequest) extends ResourceMessage
  case class ResponseContentTypeIsAcceptable(p: ParsedRequest) extends ResourceMessage
  case class RequestIsAuthorized(p: ParsedRequest) extends ResourceMessage
  case class RequestIsProcessed(response: HttpResponse, mbLocation: Option[String]) extends ResourceMessage

  private val request = reqContext.request

  log.debug(s"started $self with request $request and resource ${resource.getClass.getSimpleName}")

  override def receive: Actor.Receive = {

    //begin processing the request
    case Start =>
      self ! ensureMethodSupported(resource, request.method).map { _ =>
        MessageIsSupported(request)
      }.orFailure

    //the HTTP method is supported, now parse the request
    case MessageIsSupported(a) =>
      self ! reqParser(a).map { p =>
        RequestIsParsed(p)
      }.orFailure

    //the request has been parsed, now check if the content type is supported
    case RequestIsParsed(p) =>
      self ! ensureContentTypeSupported(resource, request).map { _ =>
        ContentTypeIsSupported(p)
      }.orFailure

    //the content type is supported, now check if the response content type is acceptable
    case ContentTypeIsSupported(p) =>
      self ! ensureResponseContentTypeAcceptable(resource, request).map { _ =>
        ResponseContentTypeIsAcceptable(p)
      }.orFailure

    //the response content type is acceptable, now check if the request is authorized
    case ResponseContentTypeIsAcceptable(p) =>
      ensureAuthorized(resource, request).map { _ =>
        RequestIsAuthorized(p)
      }.recover(handleError).pipeTo(self)

    //the request is authorized, now process the request
    case RequestIsAuthorized(p) =>
      reqProcessor.apply(p).map { case (response, mbLocation) =>
        RequestIsProcessed(response, mbLocation)
      }.recover(handleError).pipeTo(self)

    //the request has been processed, now construct the response, send it to the spray context, send it to the returnActor, and stop
    case RequestIsProcessed(resp, mbLocation) =>
      val responseWithLocation = addHeaderOnCode(resp, Created) {
        // if an `X-Forwarded-Proto` header exists, read the scheme from that; else, preserve what was given to us
        val newScheme = request.headers.find(_.name == "X-Forwarded-Proto").map(_.value).getOrElse(request.uri.scheme)

        // if we created something, `location` will have more information to append to the response path
        val newPath = Path(request.uri.path.toString + mbLocation.map("/" + _).getOrElse(""))

        // copy the request uri, replacing scheme and path as needed, and return a `Location` header with the new uri
        val newUri = request.uri.copy(scheme = newScheme, path = newPath)
        Location(newUri)
      }
      // Just force the request to the right content type
      val entity = responseWithLocation.entity.flatMap { entity: NonEmpty =>
        HttpEntity(resource.responseContentType, entity.data)
      }
      self ! responseWithLocation.withEntity(entity)


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
      //TODO: handleError is a partial function. should we care if we pass a throwable that it doesn't cover?
      self ! handleError(t)
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

  private def handleError: PartialFunction[Throwable, HttpResponse] = {
    case e: HaltException =>
      val response = addHeaderOnCode(e.response, Unauthorized) {
        `WWW-Authenticate`(resource.unauthorizedChallenge(request))
      }
      // If the error already has the right content type, let it through, otherwise coerce it
      val finalResponse = response.withEntity(response.entity.flatMap { entity: NonEmpty =>
        entity.contentType match {
          case resource.responseContentType => entity
          case _ => resource.coerceError(entity.data.toByteArray)
        }
      })
      if (finalResponse.status.intValue >= 500) {
        log.warning(s"Request finished unsuccessfully: request: $request response: $finalResponse")
      }
      finalResponse
    case e: Exception =>
      log.error(s"Unexpected error: request: $request error: ${e.getMessage}", e)
      HttpResponse(InternalServerError, resource.coerceError(Option(e.getMessage).getOrElse("").getBytes(charsetUtf8)))
  }

}

object ResourceActor {
  type RequestParser[T] = HttpRequest => Try[T]
  type RequestProcessor[T] = T => Future[(HttpResponse, Option[String])]

  object Start

  def props[AuthInfo, ParsedRequest](resource: AbstractResource[AuthInfo],
                                     reqContext: RequestContext,
                                     reqParser: ResourceActor.RequestParser[ParsedRequest],
                                     reqProcessor: ResourceActor.RequestProcessor[ParsedRequest],
                                     mbResponseActor: Option[ActorRef]) = {
    Props.apply(new ResourceActor(resource, reqContext, reqParser, reqProcessor, mbResponseActor))
  }

}
