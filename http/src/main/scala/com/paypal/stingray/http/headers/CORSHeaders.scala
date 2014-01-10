package com.paypal.stingray.http.headers

import spray.http._
import HttpHeaders._

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 4/18/13
 * Time: 11:25 PM
 */
object CORSHeaders {



  object Origin {
    lazy val lowercaseName = name.toLowerCase
    lazy val name = "Origin"
  }

  case class Origin(host: String) extends BaseHeader {
    override lazy val name = Origin.name
    override lazy val lowercaseName = Origin.lowercaseName
    override lazy val value = host
  }

  object `Access-Control-Request-Method` {
    lazy val lowercaseName = name.toLowerCase
    lazy val name = "Access-Control-Request-Method"
  }

  case class `Access-Control-Request-Method`(method:  HttpMethod) extends BaseHeader {
    override lazy val name = `Access-Control-Request-Method`.name
    override lazy val lowercaseName = `Access-Control-Request-Method`.lowercaseName
    override lazy val value = method.value
  }

  object `Access-Control-Request-Headers` {
    lazy val lowercaseName = name.toLowerCase
    lazy val name = "Access-Control-Request-Headers"
  }

  case class `Access-Control-Request-Headers`(headers: List[String]) extends BaseHeader {
    override lazy val name = `Access-Control-Request-Headers`.name
    override lazy val lowercaseName = `Access-Control-Request-Headers`.lowercaseName
    override lazy val value = headers.mkString(", ")
  }

  object `Access-Control-Allow-Origin` {
    lazy val lowercaseName = name.toLowerCase
    lazy val name = "Access-Control-Allow-Origin"
  }

  case class `Access-Control-Allow-Origin`(host: String) extends BaseHeader {
    override lazy val name = `Access-Control-Allow-Origin`.name
    override lazy val lowercaseName = `Access-Control-Allow-Origin`.lowercaseName
    override lazy val value = host
  }

  object `Access-Control-Allow-Methods` {
    lazy val lowercaseName = name.toLowerCase
    lazy val name = "Access-Control-Allow-Methods"
  }

  case class `Access-Control-Allow-Methods`(methods: List[HttpMethod]) extends BaseHeader {
    override lazy val name = `Access-Control-Allow-Methods`.name
    override lazy val lowercaseName = `Access-Control-Allow-Methods`.lowercaseName
    override lazy val value = methods.map(_.value).mkString(", ")
  }

  object `Access-Control-Allow-Headers` {
    lazy val lowercaseName = name.toLowerCase
    lazy val name = "Access-Control-Allow-Headers"
  }

  case class `Access-Control-Allow-Headers`(host: String) extends BaseHeader {
    override lazy val name = `Access-Control-Allow-Headers`.name
    override lazy val lowercaseName = `Access-Control-Allow-Headers`.lowercaseName
    override lazy val value = host
  }

  object `Access-Control-Allow-Credentials` {
    lazy val lowercaseName = name.toLowerCase
    lazy val name = "Access-Control-Allow-Credentials"
  }

  case class `Access-Control-Allow-Credentials`(allow: Boolean) extends BaseHeader {
    override lazy val name = `Access-Control-Allow-Credentials`.name
    override lazy val lowercaseName = `Access-Control-Allow-Credentials`.lowercaseName
    override lazy val value = allow.toString
  }

  val `access-control-expose-headers` = "Access-Control-Expose-Headers"

  object `Access-Control-Expose-Headers` {
    lazy val lowercaseName = name.toLowerCase
    lazy val name = "Access-Control-Expose-Headers"
  }

	case class `Access-Control-Expose-Headers`(headerNames: List[String]) extends BaseHeader {
    override lazy val name = `Access-Control-Expose-Headers`.name
    override lazy val lowercaseName = `Access-Control-Expose-Headers`.lowercaseName
    override lazy val value = headerNames.mkString(" ").toString
  }

  object `Access-Control-Max-Age` {
    lazy val lowercaseName = name.toLowerCase
    lazy val name = "Access-Control-Max-Age"
  }

  case class `Access-Control-Max-Age`(seconds: Int) extends BaseHeader {
    override lazy val name = `Access-Control-Max-Age`.name
    override lazy val lowercaseName = `Access-Control-Max-Age`.lowercaseName
    override lazy val value = seconds.toString
  }

}
