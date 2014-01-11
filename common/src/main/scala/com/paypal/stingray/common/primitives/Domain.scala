package com.paypal.stingray.common.translatable.primitives

import org.codehaus.jackson.annotate.JsonCreator
import com.paypal.stingray.common.annotate.AnnotationHelpers._
import scala.beans.BeanProperty
import scala.util.control.Exception._

class Domain @JsonCreator() (@ScalaJsonProperty("domain") @BeanProperty val domain: String)
  extends AbstractString(domain) {
  override def hashCode: Int = Option(value) match {
    case Some(hc) => hc.hashCode
    case None => 0
  }
}

object Domain extends AbstractStringCommon {

  val MAX_LENGTH = 32

  /**
   * Updated to better match RFC 2396.  Underscores are not allowed, hyphens are okay.
   * Add our own restriction for no uppercase letters.
   * See: http://stackoverflow.com/questions/106179/regular-expression-to-match-hostname-or-ip-address
   */
  def isValid(input: String): Boolean = {
    catching(classOf[InvalidDomainException]).withTry(assertValid(input)).isSuccess
  }

  private def assertValid(input: String) {
    try {
      assertValid(input, "^([a-z0-9]|[a-z0-9][-a-z0-9]*[a-z0-9])$",
        "must be all lowercase letters or numbers, " + "hyphens allowed. Domain was " + input, 3, MAX_LENGTH)
    } catch {
      case e: InvalidStringException => throw new InvalidDomainException("Domain " + input + " " + e.getMessage)
    }
  }

  @throws(classOf[InvalidDomainException])
  def fromString(input: String): Domain = {
    assertValid(input)
    new Domain(input)
  }

  def none: Domain = null

}

class InvalidDomainException(msg: String) extends Exception(msg)
