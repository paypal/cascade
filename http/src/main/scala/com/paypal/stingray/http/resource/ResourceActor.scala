package com.paypal.stingray.http.resource

import akka.actor._
import spray.http.HttpEntity
import spray.http.StatusCodes._
import com.paypal.stingray.common.actor._
import akka.pattern.pipe
import scala.util.Try
import scala.concurrent.Future
import spray.http.Uri.Path
import spray.http.HttpHeaders.{`WWW-Authenticate`, Location}
import com.paypal.stingray.common.constants.ValueConstants._
import spray.http.HttpRequest
import spray.routing.RequestContext
import spray.http.HttpEntity.NonEmpty
import spray.http.HttpResponse

class ResourceActor[AuthInfo, ParsedRequest](resource: AbstractResource[AuthInfo],
                                             reqContext: RequestContext,
                                             reqParser: ResourceActor.RequestParser[ParsedRequest],
                                             reqProcessor: ResourceActor.RequestProcessor[ParsedRequest],
                                             mbReturnActor: Option[ActorRef]) extends ServiceActor {

  import context.dispatcher

  import ResourceActor._

  private def handleError: PartialFunction[Throwable, HttpResponse] = {
    case e: HaltException =>
      val response = ResourceDriver.addHeaderOnCode(e.response, Unauthorized) {
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

  private val request = reqContext.request

  case class MessageIsSupported(a: HttpRequest)
  case class RequestIsParsed(p: ParsedRequest)
  case class ContentTypeIsSupported(p: ParsedRequest)
  case class ResponseContentTypeIsAcceptable(p: ParsedRequest)
  case class RequestIsAuthorized(p: ParsedRequest)
  case class RequestIsProcessed(response: HttpResponse, mbLocation: Option[String])

  override def receive = {

    //begin processing the request
    case Start =>
      self ! ResourceDriver.ensureMethodSupported(resource, request.method).map { a =>
          MessageIsSupported(request)
      }.orFailure

    //the HTTP method is supported, now parse the request
    case MessageIsSupported(a) =>
      self ! reqParser(a).map { p =>
        RequestIsParsed(p)
      }.orFailure

    //the request has been parsed, now check if the content type is supported
    case RequestIsParsed(p) =>
      self ! ResourceDriver.ensureContentTypeSupported(resource, request).map { a =>
        ContentTypeIsSupported(p)
      }.orFailure

    //the content type is supported, now check if the response content type is acceptable
    case ContentTypeIsSupported(p) =>
      self ! ResourceDriver.ensureResponseContentTypeAcceptable(resource, request).map { a =>
        ResponseContentTypeIsAcceptable(p)
      }.orFailure

    //the response content type is acceptable, now check if the request is authorized
    case ResponseContentTypeIsAcceptable(p) =>
      ResourceDriver.ensureAuthorized(resource, request).map { a =>
        RequestIsAuthorized(p)
      }.pipeTo(self)

    //the request is authorized, now process the request
    case RequestIsAuthorized(p) =>
      reqProcessor.apply(p).map { tup =>
        RequestIsProcessed(tup._1, tup._2)
      }.recover(handleError).pipeTo(self)

    //the request has been processed, now construct the response, send it to the spray context, send it to the returnActor, and stop
    case RequestIsProcessed(resp, mbLocation) =>
      val responseWithLocation = ResourceDriver.addHeaderOnCode(resp, Created) {
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
      val finalResp = responseWithLocation.withEntity(entity)
      reqContext.complete(finalResp)
      mbReturnActor.foreach { returnActor =>
        returnActor ! finalResp
      }
      context.stop(self)

    //there was an error somewhere along the way, so translate it to an HttpResponse (using handleError), send the exception to returnActor and stop
    case s@ Status.Failure(t) =>
      log.error(t, s"Unexpected error: request: $request error: ${t.getMessage}")
      reqContext.complete(handleError.apply(t))
      mbReturnActor.foreach { returnActor =>
        returnActor ! s
      }
      context.stop(self)
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