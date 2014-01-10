package com.paypal.stingray.http.request

import scalaz._
import Scalaz._
import InternalRequest._
import com.stackmob.newman.HttpClient
import java.net.URL
import com.stackmob.newman.{Headers, Header}
import com.paypal.stingray.common.primitives._
import com.paypal.stingray.common.option._
import com.paypal.stingray.common.validation._
import com.stackmob.newman.request.{HttpRequest => NewmanRequest}
import com.paypal.stingray.common.env.EnvironmentType
import com.paypal.stingray.common.request.Authorization
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import spray.http._
import spray.http.HttpEntity._
import HttpMethods._
import com.paypal.stingray.http.headers._
import com.paypal.stingray.http.headers.InternalHeaders._

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 4/18/13
 * Time: 4:48 PM
 * InternalRequest is a request that has been validated by optimus and includes the important metadata. Downstream services can
 * expect this format regardless of what auth the request used or where it came from.
 */
case class InternalRequest(underlying: HttpRequest,
                           modules: Modules,
                           appId: AppId,
                           apiVersionNumber: APIVersionNumber,
                           authorization: Authorization) {

  def envType: EnvironmentType = apiVersionNumber.toEnvType

  def hasModule(moduleId: ModuleId): Boolean = modules.list.exists(_.toString === moduleId.toString)

  /**
   * convert this InternalRequest to a Newman HttpRequest. note that client is not implicit
   * because we choose to force the caller to think in explicit terms about which HttpClient to
   * use in this conversion instead of relying on implicit scope.
   * @param client the HttpClient with which to create the resultant HttpRequest
   * @return the resultant HttpRequest, or an error indicating why the HttpRequest could not be created
   */
  def toNewmanRequest(client: HttpClient): Validation[InternalRequestToHttpRequestError, NewmanRequest] = {
    val url = new URL(underlying.uri.toString())
    val baseHeaders: List[(String, String)] = underlying.headers.filterNot(_.is(HttpHeaders.`Content-Type`.lowercaseName)).toList.map(_.toNewmanHeader)

    val (contentType, body) = underlying.entity match {
      case NonEmpty(cType, data) => (cType.value.some, data.toByteArray)
      case Empty => (None, Array[Byte]())
    }

    val headers: Headers = (contentType.map((HttpHeaders.`Content-Type`.name, _)).toList ::: baseHeaders ::: internalHeaderList(modules, appId, apiVersionNumber, authorization)).toNel

    underlying.method match {
      case GET => client.get(url, headers).success
      case POST => client.post(url, headers, body).success
      case PUT => client.put(url, headers, body).success
      case DELETE => client.delete(url, headers).success
      case HEAD => client.head(url, headers).success
      case m => (MethodNotSupportedError(m): InternalRequestToHttpRequestError).fail
    }
  }
}

object InternalRequest {

  def apply(req: HttpRequest): Validation[HttpRequestToInternalRequestError, InternalRequest] = for {
  // parse modules header
    modules <- for {
      hdr <- req.headers.find(_.lowercaseName === `X-StackMob-Internal-Data-Modules`.lowercaseName).toSuccess(ModulesHeaderNotFoundError())
      hdrValue <- (hdr.value.isEmpty ? "[]" | hdr.value).success
      hdrJson <- validating(parse(hdrValue)).mapFailure(_ => MalformedModulesHeaderError(hdr.value))
      ids <- fromJSON[List[String]](hdrJson).mapFailure {_ => MalformedModulesHeaderError(hdr.value)}
      moduleIds <- ids.map(ModuleId.fromString).sequence[Option, ModuleId].toSuccess(MalformedModulesHeaderError(hdr.value))
    } yield moduleIds.toNel

    // parse app id header
    appIdStr <- req.headers.find(_.lowercaseName === `X-StackMob-Internal-Data-AppID`.lowercaseName).map(_.value).toSuccess(AppIDHeaderNotFoundError)
    appId <- AppId.fromString(appIdStr).toSuccess(AppIDMalformedError(appIdStr))

    // parse client environment header
    versionStr <- req.headers.find(_.lowercaseName === `X-StackMob-Internal-Data-API-Version`.lowercaseName).map(_.value).toSuccess(APIVersionHeaderNotFoundError)
    apiVersion <- APIVersionNumber.fromString(versionStr).toSuccess(APIVersionHeaderMalformedError(versionStr))

    //parse the authorization header
    authStr <- req.headers.find(_.lowercaseName === `X-StackMob-Internal-Data-Authorization`.lowercaseName).map(_.value).toSuccess(AuthrorizationHeaderNotFoundError)
    auth <- Authorization.readString(authStr).success[HttpRequestToInternalRequestError]

  } yield InternalRequest(req, modules, appId, apiVersion, auth)

  def internalHeaders(modules: Modules, appId: AppId, version: APIVersionNumber, auth: Authorization): Headers = {
    Headers(internalHeaderList(modules, appId, version, auth))
  }

  protected def internalHeaderList(modules: Modules, appId: AppId, version: APIVersionNumber, auth: Authorization): List[Header] = {
    List(`X-StackMob-Internal-Data-Modules`(modules),
    `X-StackMob-Internal-Data-AppID`(appId),
    `X-StackMob-Internal-Data-API-Version`(version),
    `X-StackMob-Internal-Data-Authorization`(auth)).map(_.toNewmanHeader)
  }

  type Modules = Option[NonEmptyList[ModuleId]]
  val EmptyModules = none[Modules]

}