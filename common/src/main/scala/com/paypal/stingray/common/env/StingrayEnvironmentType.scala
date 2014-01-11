package com.paypal.stingray.common.env

import com.paypal.stingray.common.enumeration._

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 8/24/12
 * Time: 11:21 AM
 */

sealed abstract class StingrayEnvironmentType extends Enumeration

/**
 * StackMobEnvironmentType represents the StackMob environment.
 *   Rackspace => PRODUCTION
 *   Townsend => STAGING
 *   Local => DEVELOPMENT
 */
object StingrayEnvironmentType extends EnumUnapply[StingrayEnvironmentType] {

  object DEVELOPMENT extends StingrayEnvironmentType {
    val stringVal = "dev"
  }

  object STAGING extends StingrayEnvironmentType {
    val stringVal = "staging"
  }

  object PRODUCTION extends StingrayEnvironmentType {
    val stringVal = "prod"
  }

  implicit val stingrayEnvTypeRead: EnumReader[StingrayEnvironmentType] = new EnumReader[StingrayEnvironmentType] {
    override def read(s: String): Option[StingrayEnvironmentType] = s.toLowerCase match {
      case DEVELOPMENT.stringVal => Some(DEVELOPMENT)
      case STAGING.stringVal => Some(STAGING)
      case PRODUCTION.stringVal => Some(PRODUCTION)
      case _ => None
    }
  }
}
