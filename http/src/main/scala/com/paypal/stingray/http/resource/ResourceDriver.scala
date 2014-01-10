package com.paypal.stingray.http.resource

import scalaz._
import Scalaz._
import spray.http._
import spray.http.HttpEntity._
import HttpMethods._
import StatusCodes._
import HttpHeaders._
import com.paypal.stingray.common.logging.LoggingSugar
import scala.concurrent.Future
import com.paypal.stingray.common.string._

trait ResourceDriver extends LoggingSugar {

  protected lazy val logger = getLogger[ResourceDriver]

  def ensureAvailable(resource: Resource[_, _, _, _]) = {
    import resource.context
    resource.available.orHaltWith(ServiceUnavailable)
  }
  def ensureMethodSupported(resource: Resource[_, _, _, _], method: HttpMethod) = {
    import resource.context
    resource.supportedHttpMethods.contains(method).orHaltWith(MethodNotAllowed)
  }

  def parseBody[T](request: HttpRequest, method: HttpMethod)(f: HttpRequest => Future[Option[T]]) = {
    if(request.method == method) f(request)
    else none[T].continue
  }

  def ensureAuthorized[PR, AI](resource: Resource[PR, AI, _, _], parsedRequest: PR) = {
    import resource.context
    for {
      authInfoOpt <- resource.isAuthorized(parsedRequest)
      authInfo <- authInfoOpt.orHaltWith(Unauthorized)
    } yield authInfo
  }

  def ensureNotForbidden[PR, AI](resource: Resource[PR, AI, _, _], parsedRequest: PR, authInfo: AI) = {
    import resource.context
    for {
      isForbidden <- resource.isForbidden(parsedRequest, authInfo)
      _ <- (!isForbidden).orHaltWith(Forbidden)
    } yield ()
  }

  def ensureContentTypeSupported(resource: Resource[_, _, _, _], request: HttpRequest) = {
    request.entity match {
      case Empty => ().continue
      case NonEmpty(ct, _) => resource.acceptableContentTypes.contains(ct).orHaltWith(UnsupportedMediaType)
    }
  }

  def ensureResponseContentTypeAcceptable(resource: Resource[_, _, _, _], request: HttpRequest) = {
    import resource.context
    request.acceptableContentType(List(resource.responseContentType)).orHaltWith(NotAcceptable)
  }

  def addHeaderOnCode(response: HttpResponse, status: StatusCode)(header: => HttpHeader) = {
    if(response.status == status) {
      response.withHeaders(header :: response.headers)
    } else {
      response
    }

  }

  def serveSync[ParsedRequest, AuthInfo, PostBody, PutBody](request: HttpRequest,
                                                            resource: Resource[ParsedRequest, AuthInfo, PostBody, PutBody],
                                                            pathParts: Map[String, String]): Future[HttpResponse] = {

    import resource.context

    (for {
      _ <- ensureAvailable(resource)
      _ <- ensureMethodSupported(resource, request.method)

      parsedRequest <- resource.parseRequest(request, pathParts)
      postBody <- parseBody(request, POST)(resource.parsePostBody)
      putBody <- parseBody(request, PUT)(resource.parsePutBody)

      authInfo <- ensureAuthorized(resource, parsedRequest)
      _ <- ensureNotForbidden(resource, parsedRequest, authInfo)
      _ <- ensureContentTypeSupported(resource, request)
      _ <- ensureResponseContentTypeAcceptable(resource, request)

      (httpResponse, location) <- request.method match {
        case GET => resource.doGet(parsedRequest, authInfo).map((_, none[String]))
        case HEAD => resource.doHead(parsedRequest, authInfo).map((_, none[String]))
        case PUT => {
          putBody.orError().flatMap { body =>
            resource.doPut(parsedRequest, authInfo, body).map((_, none[String]))
          }
        }
        case POST => {
          postBody.orError().flatMap { body =>
            resource.doPostAsCreate(parsedRequest, authInfo, body)
          }
        }
        case DELETE => resource.doDelete(parsedRequest, authInfo).map((_, none[String]))
        case OPTIONS => resource.doOptions(parsedRequest, authInfo).map((_, none[String]))
        case _ => halt(MethodNotAllowed)
      }
    } yield {
      val responseWithLocation = addHeaderOnCode(httpResponse, Created) {
        val scheme = request.headers.find(_.name === "X-Forwarded-Proto").map(_.value) | "http"
        Location(Uri("%s://%s:%s%s".format(scheme, request.uri.authority.host, request.uri.authority.port, request.uri.path) + ~location.map("/" + _)))
      }
      // Just force the request to the right content type
      responseWithLocation.withEntity(responseWithLocation.entity.flatMap((entity: NonEmpty) => HttpEntity(resource.responseContentType, entity.data)))
    }).recover {
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
          logger.warn(s"Request finished unsuccessfully: request: $request response: $finalResponse")
        }
        finalResponse
      case t: Throwable => {
        logger.error(s"Unexpected error: request: $request error: ${t.getMessage}", t)
        HttpResponse(InternalServerError, resource.coerceError((~Option(t.getMessage)).getBytesUTF8))
      }

    }

  }

}
