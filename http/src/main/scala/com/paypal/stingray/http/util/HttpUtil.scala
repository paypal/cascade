package com.paypal.stingray.http.util

import java.net.URLDecoder
import spray.http._
import scala.util.{Failure, Success, Try}
import spray.http.HttpRequest
import spray.http.HttpChallenge
import spray.http.HttpEntity._
import spray.http.HttpResponse
import StatusCodes._
import com.paypal.stingray.json._
import com.paypal.stingray.common.constants.ValueConstants.charsetUtf8

/**
 * Convenience methods for interacting with URLs and other request components.
 *
 * Methods found within [[spray.http]] objects should be preferred over these, wherever Spray objects are already
 * in use. For example, if working with a [[spray.http.Uri]], prefer to access its query string pairs using
 * [spray.http.Uri.Query$.toMap] instead of using `parseQueryStringToMap` found here
 */
object HttpUtil {
  import com.paypal.stingray.http.url.StrPair

  /** Convenience value for `utf-8` */
  val UTF_8 = "utf-8"

  val CONTENT_LANGUAGE = "Content-Language"
  val CONTENT_LANGUAGE_LC = CONTENT_LANGUAGE.toLowerCase

  /**
   * Parse a query string into a List of key-value String pairs
   *
   * @param queryString the query string to parse (without the '?')
   * @return a list of (String, String), representing each key-value pair in the query string
   */
  def parseQueryStringToPairs(queryString: String): List[StrPair] = {
    val queryStringPieces: List[String] = Option(queryString).map(_.split("&").toList).getOrElse(List())
    queryStringPieces.flatMap { piece: String =>
      piece.split("=").toList match {
        case key :: value :: Nil if (key.length > 0 && value.length > 0) => List(URLDecoder.decode(key, UTF_8) -> URLDecoder.decode(value, UTF_8))
        case _ => List()
      }
    }.toList
  }

  /**
   * Parse a query string into a Map of key-value String pairs. Ignores invalid key-value pairs in the query string
   *
   * @param queryString the query string to parse, without the leading '?'
   * @return key/value pairs mapping to the items in the query string
   */
  def parseQueryStringToMap(queryString: String): Map[String, List[String]] = {
    val pairs = parseQueryStringToPairs(queryString)

    pairs.foldLeft(Map[String, List[String]]()) { (running, current) =>
      val (curKey, curVal) = current
      if(curKey.length > 0 && curVal.length > 0) {
        val newList = running.get(curKey).map { existingList =>
          existingList ++ List(curVal)
        }.getOrElse(List(curVal))
        running ++ Map(curKey -> newList)
      } else {
        running
      }
    }
  }

  /**
   * Merge two parameter maps into one
   * @param m1 the first map
   * @param m2 the second map
   * @return a merged map containing members from both maps
   */
  def mergeParameters(m1: Map[String, List[String]],
                      m2: Map[String, List[String]]): Map[String, List[String]] = {
    val list: List[(String, List[String])] = m1.toList ++ m2.toList
    list.foldLeft(Map[String, List[String]]()) { (runningMap, currentElt) =>
      val (currentKey, currentList) = currentElt
      val newList = runningMap.get(currentKey).map { existingList =>
        existingList ++ currentList
      }.getOrElse(currentList)
      runningMap ++ Map(currentKey -> newList)
    }
  }

  /**
   * Attempt to parse the incoming request.
   * @param r the full request
   * @param data the piece of data that should be converted to the suggested type
   * @return the parsed request, or a Failure response
   */
  def parseType[T : Manifest](r: HttpRequest, data: String): Try[T] = {
    JsonUtil.fromJson[T](data)
  }

  def parseType[T : Manifest](r: HttpRequest, data: Array[Byte]): Try[T] = {
    parseType(r, new String(data, charsetUtf8))
  }

  /**
   * The message to be sent back with the `WWW-Authenticate` header when the request is
   * unauthorized. This particular form works around a known Android quirk.
   *
   * See discussion at http://stackoverflow.com/questions/6114455/
   * @return the message
   */
  def unauthorizedChallenge(req: HttpRequest): List[HttpChallenge] =
    List(HttpChallenge("OAuth", req.uri.authority.host.toString))

  /**
   * Convenience method to return an exception as a 500 Internal Error with the body being the message
   * of the exception
   */
  def errorResponse(e: Exception): HttpResponse = {
    HttpResponse(InternalServerError, e.getMessage)
  }

  /**
   * Utility method to return HttpResponse with status OK and a serialized json body via Jackson.
   * If the JSON processing is CPU intensive, it should be done in a background thread, dedicated actor, etc...
   * into an http body with the content type set
   * @param t the object to serialize
   * @tparam T the type to serialize from
   * @return an HttpResponse containing an OK StatusCode and the serialized object
   */
  def jsonOKResponse[T : Manifest](t: T): HttpResponse = {
    // TODO: convert Manifest patterns to use TypeTag, ClassTag when Jackson implements that
    HttpResponse(OK, toJsonBody(t))
  }

  /**
   * Utility method to serialize a json body via jackson.
   * If the JSON processing is CPU intensive, it should be done in a background thread, dedicated actor, etc...
   * into an http body with the content type set
   * Enforces the return of application/json as a content type, since this always serializes to json
   * @param t the object to serialize
   * @tparam T the type to serialize from
   * @return an HttpResponse containing either the desired HttpEntity, or an error entity
   */
  def toJsonBody[T : Manifest](t: T): HttpEntity = {
    // TODO: convert Manifest patterns to use TypeTag, ClassTag when Jackson implements that
    JsonUtil.toJson(t) match {
      case Success(j) => HttpEntity(ContentTypes.`application/json`, j)
      case Failure(e) => coerceError(Option(e.getMessage).getOrElse(""))
    }
  }

  val errorResponseType = ContentTypes.`application/json`

  /**
   * Used under the covers to force simple error strings into a JSON format
   * @param body the body
   * @return an HttpEntity containing an error JSON body
   */
  def coerceErrorMap(body: Array[Byte]): HttpEntity = {
    toJsonBody(Map("errors" -> List(new String(body, charsetUtf8))))
  }

  /**
   * Used under the covers to force simple error strings into a JSON format
   * @param body the body
   * @return an HttpEntity containing an error JSON body
   */
  def coerceErrorMap(body: String): HttpEntity = {
    toJsonBody(Map("errors" -> List(body)))
  }

  // TODO add docs
  def coerceError[T : Manifest](body: T): HttpEntity = {
    toJsonBody(body)
  }
}
