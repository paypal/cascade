package com.paypal.stingray.common.oauth

/**
 * Created by IntelliJ IDEA.
 * User: ayakushev
 * Date: 3/7/12
 * Time: 4:49 PM
 */

class AuthorizationString(val id: String)

case class BasicAuthorizationString(override val id: String) extends AuthorizationString(id)

case class MacAuthorizationString(override val id: String, ts: String, nonce: String, mac: String, ext: String)
  extends AuthorizationString(id)

object AuthorizationString {
  
  def parseHeader(authHeader: String): Option[AuthorizationString] = {
    val authStrTokens = authHeader.trim.split(' ')
    if (authStrTokens.length < 2) {
      throw new OAuthException("Bad authorization header")
    } else {
      authStrTokens(0).trim.toLowerCase match {
        case "basic" => Option(BasicAuthorizationString(authStrTokens(1).trim))
        case "mac" => {
          val macParameters: Map[String, String] = parseMACParams(authStrTokens(1).trim)
          val id = macParameters.getOrElse("id", throw new OAuthException("key identifier not provided"))
          val ts = macParameters.getOrElse("ts", throw new OAuthException("timestamp not provided"))
          val nonce = macParameters.getOrElse("nonce", throw new OAuthException("nonce not provided"))
          val mac = macParameters.getOrElse("mac", throw new OAuthException("mac signature not provided"))
          val ext = macParameters.getOrElse("ext", "")
          Option(MacAuthorizationString(id, ts, nonce, mac, ext))
        }
        case "oauth" => None
        case _ =>  throw new OAuthException("Unknown authentication string.")
      }
    }
  }

  private val paramParser = """\s*(\w+)="?([A-Za-z0-9+/=]+)"?\s*""".r
  
  private def parseMACParams(macParam: String): Map[String, String] = {
    macParam.split(",").foldLeft(Map[String, String]())((acc, param) => {
      param match {
        case paramParser(key, value) => acc + (key -> value)
        case _ => acc
      }
    })
  }
  
}
