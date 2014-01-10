package com.paypal.stingray.common.deploymentapi.metadata

import com.paypal.stingray.common.enumeration._
import scalaz._
import scalaz.Equal._

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 1/30/12
 * Time: 12:13 PM
 */

sealed abstract class DeploymentSource extends Enumeration

object DeploymentSource {

  object GITHUB_CALLBACK extends DeploymentSource {
    override val stringVal = "GITHUB_CALLBACK"
  }

  object GITHUB_MANUAL extends DeploymentSource {
    override val stringVal = "GITHUB_MANUAL"
  }

  object STANDALONE extends DeploymentSource {
    override val stringVal = "STANDALONE"
  }

  object JENKINS extends DeploymentSource {
    override val stringVal = "JENKINS"
  }

  implicit val deploymentSourceRead: EnumReader[DeploymentSource] = new EnumReader[DeploymentSource] {
    override def read(s: String): Option[DeploymentSource] = s.toUpperCase match {
      case GITHUB_CALLBACK.stringVal => Some(GITHUB_CALLBACK)
      case GITHUB_MANUAL.stringVal => Some(GITHUB_MANUAL)
      case STANDALONE.stringVal => Some(STANDALONE)
      case JENKINS.stringVal => Some(JENKINS)
      case _ => None
    }
  }

  implicit val deploymentSourceEqual: Equal[DeploymentSource] = equalA

}
