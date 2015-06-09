package com.paypal.cascade.http.util

import java.nio.charset.StandardCharsets._

import spray.http.HttpEntity.NonEmpty
import spray.http.HttpHeaders.{RawHeader, `WWW-Authenticate`}
import spray.http.StatusCodes._
import spray.http._

import com.paypal.cascade.common.logging.LoggingSugar
import com.paypal.cascade.http.resource.HaltException

trait HttpErrorResponeHandler extends LoggingSugar {

  private[this] val log = getLogger[HttpErrorResponeHandler]

  /**
   * Creates an appropriate HttpResponse for a given exception, request, and response language.
   *
   * @param exception the exception
   * @param request the request
   * @param responseLanguage option of language to make the response
   * @return a crafted HttpResponse from the error message
   */
  def createErrorResponse(exception: Exception, request: HttpRequest, responseLanguage: Option[Language]): HttpResponse = {
    val resp = exception match {
      case haltException: HaltException =>
        val response = addHeaderOnCode(haltException.response, Unauthorized) {
          `WWW-Authenticate`(HttpUtil.unauthorizedChallenge(request))
        }
        // If the error already has the right content type, let it through, otherwise coerce it
        val finalResponse = response.withEntity(response.entity.flatMap {
          entity: NonEmpty =>
            entity.contentType match {
              case HttpUtil.errorResponseType => entity
              case _ => HttpUtil.toJsonBody(entity.data.asString(UTF_8))
            }
        })
        if (finalResponse.status.intValue >= 500) {
          val statusCode = finalResponse.status.intValue
          log.warn(s"Request finished unsuccessfully with status code: $statusCode")
        }
        finalResponse
      case otherException: Exception =>
        HttpResponse(InternalServerError, HttpUtil.toJsonBody(s"Error in request execution: ${otherException.getClass.getSimpleName}"))
    }
    resp.withHeaders(addLanguageHeader(responseLanguage, resp.headers))
  }

  private[cascade] def addHeaderOnCode(response: HttpResponse, status: StatusCode)
                                      (header: => HttpHeader): HttpResponse = {
    if(response.status == status) {
      response.withHeaders(header :: response.headers)
    } else {
      response
    }
  }

  private[cascade] def addLanguageHeader(responseLanguage: Option[Language], headers: List[HttpHeader]) : List[HttpHeader] = {
    responseLanguage match {
      case Some(lang) =>
        if (headers.exists(_.lowercaseName == HttpUtil.CONTENT_LANGUAGE_LC)) {
          headers
        } else {
          RawHeader(HttpUtil.CONTENT_LANGUAGE, lang.toString) :: headers
        }
      case None => headers
    }
  }
}
