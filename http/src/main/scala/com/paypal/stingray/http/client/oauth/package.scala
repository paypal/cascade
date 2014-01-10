package com.paypal.stingray.http.client

import com.stackmob.newman.dsl._
import com.stackmob.newman.request._
import org.scribe.builder.api._
import org.scribe.model.{Verb, OAuthRequest, Token}
import org.scribe.builder.ServiceBuilder
import org.scribe.oauth.OAuthService
import java.net.URL
import org.apache.commons.codec.binary.Base64
import com.paypal.stingray.common.either._
import scala.collection.JavaConverters._

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.http.client.oauth
 *
 * User: aaron
 * Date: 1/3/13
 * Time: 5:49 PM
 */
package object oauth {

  type OAuthErrorEither[T] = Either[OAuthError, T]

  sealed trait OAuthError {
    def description: String
  }

  abstract class HttpRequestTypeNotAvailableError(reqType: HttpRequestType) extends OAuthError {
    override lazy val description = "%s isn't an available request type".format(reqType.stringVal)
  }
  object HttpRequestTypeNotAvailableError {
    def apply(r: HttpRequestType): HttpRequestTypeNotAvailableError = new HttpRequestTypeNotAvailableError(r) {}
  }

  abstract class SigningError(details: String) extends OAuthError {
    override lazy val description = details
  }
  object SigningError {
    def apply(s: String): SigningError = new SigningError(s){}
  }

  private case class TwoLeggedOAuth() extends DefaultApi10a {
    override def getAccessTokenEndpoint: String = ""
    override def getRequestTokenEndpoint: String = ""
    override def getAuthorizationUrl(arg0: Token): String = ""
  }

  val AuthHeaderName: String = "Authorization"
  val BasicAuthHeaderType: String = "Basic"

  implicit class RichBuilder(builder: Builder) {
    private lazy val existingHttpRequest = builder.toRequest
    private lazy val url: URL = existingHttpRequest.url
    private lazy val httpRequestType: HttpRequestType = existingHttpRequest.requestType
    private lazy val verbEither: OAuthErrorEither[Verb] = httpRequestType match {
      case HttpRequestType.GET => Verb.GET.toRight
      case HttpRequestType.POST => Verb.POST.toRight
      case HttpRequestType.PUT => Verb.PUT.toRight
      case HttpRequestType.DELETE => Verb.DELETE.toRight
      case HttpRequestType.HEAD => HttpRequestTypeNotAvailableError(httpRequestType).toLeft
    }

    private def buildService(pub: String, priv: String): OAuthErrorEither[OAuthService] = {
      val apiClass: Class[_ <: Api] = classOf[TwoLeggedOAuth]
      new ServiceBuilder().provider(apiClass).apiKey(pub).apiSecret(priv).build.toRight
    }

    def addTwoLeggedOAuthHeader(pub: String, priv: String): OAuthErrorEither[Builder] = for {
      svc <- buildService(pub, priv).right
      emptyToken <- (new Token("", "")).toRight.right
      verb <- verbEither.right
      request <- (new OAuthRequest(verb, url.toString)).toRight.right
      signedRequest <- {
        svc.signRequest(emptyToken, request)
        request.toRight[OAuthError]
      }.right
      headers <- signedRequest.getHeaders.toRight.right
      authHeaderVal <- (headers.asScala.get(AuthHeaderName) match {
        case Some(h) => h.toRight
        case None => SigningError("no auth header found").toLeft
      }).right
      authHeader <- (AuthHeaderName -> authHeaderVal).toRight.right
    } yield {
      builder.addHeaders(authHeader)
    }

    def addBasicAuthHeader(username: String, password: String): Builder = {
      val headerValue = Base64.encodeBase64String("%s %s:%s".format(BasicAuthHeaderType, username, password).getBytes("UTF-8"))
      builder.addHeaders(AuthHeaderName -> headerValue)
    }
  }

}
