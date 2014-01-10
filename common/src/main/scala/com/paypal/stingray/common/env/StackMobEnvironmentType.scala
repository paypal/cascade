package com.paypal.stingray.common.env

import com.paypal.stingray.common.enumeration._
import scalaz._
import scalaz.Equal._

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 8/24/12
 * Time: 11:21 AM
 */

sealed abstract class StackMobEnvironmentType extends Enumeration

/**
 * StackMobEnvironmentType represents the StackMob environment.
 *   Rackspace => PRODUCTION
 *   Townsend => STAGING
 *   Local => DEVELOPMENT
 */
object StackMobEnvironmentType extends EnumUnapply[StackMobEnvironmentType] {

  object DEVELOPMENT extends StackMobEnvironmentType {
    val stringVal = "dev"
  }

  object STAGING extends StackMobEnvironmentType {
    val stringVal = "staging"
  }

  object PRODUCTION extends StackMobEnvironmentType {
    val stringVal = "prod"
  }

  implicit val stackmobEnvTypeRead: EnumReader[StackMobEnvironmentType] = new EnumReader[StackMobEnvironmentType] {
    override def read(s: String): Option[StackMobEnvironmentType] = s.toLowerCase match {
      case DEVELOPMENT.stringVal => Some(DEVELOPMENT)
      case STAGING.stringVal => Some(STAGING)
      case PRODUCTION.stringVal => Some(PRODUCTION)
      case _ => None
    }
  }

  implicit val stackmobEnvTypeEqual: Equal[StackMobEnvironmentType] = equalA

}
