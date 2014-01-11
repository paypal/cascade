package com.paypal.stingray.common.env

import com.paypal.stingray.common.enumeration._

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 1/9/12
 * Time: 6:16 PM
 */

sealed abstract class EnvironmentType extends Enumeration

/**
 * EnvironmentType represents the application's environment.
 *   API Version > 0 => prod
 *   API Version < 1 => dev
 */
object EnvironmentType extends EnumUnapply[EnvironmentType] {

  object DEV extends EnvironmentType {
    val stringVal = "dev"
  }
  object PROD extends EnvironmentType {
    val stringVal = "prod"
  }

  implicit val environmentTypeRead: EnumReader[EnvironmentType] = new EnumReader[EnvironmentType] {
    override def read(s: String): Option[EnvironmentType] = s.toLowerCase match {
      case DEV.stringVal => Some(DEV)
      case PROD.stringVal => Some(PROD)
      case _ => None
    }
  }

}
