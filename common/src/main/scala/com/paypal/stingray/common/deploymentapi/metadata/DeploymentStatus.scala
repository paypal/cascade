package com.paypal.stingray.common.deploymentapi.metadata

import com.paypal.stingray.common.enumeration._

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 1/30/12
 * Time: 12:11 PM
 */

sealed abstract class DeploymentStatus extends Enumeration

object DeploymentStatus {

  object RUNNING extends DeploymentStatus {
    override val stringVal = "RUNNING"
  }

  object FINISHED extends DeploymentStatus {
    override val stringVal = "FINISHED"
  }

  object BUILDING extends DeploymentStatus {
    override val stringVal = "BUILDING"
  }

  object ERROR extends DeploymentStatus {
    override val stringVal = "ERROR"
  }

  implicit val deploymentStatusReader: EnumReader[DeploymentStatus] = new EnumReader[DeploymentStatus] {
    override def read(s: String): Option[DeploymentStatus] = s.toUpperCase match {
      case RUNNING.stringVal => Some(RUNNING)
      case FINISHED.stringVal => Some(FINISHED)
      case ERROR.stringVal => Some(ERROR)
      case BUILDING.stringVal => Some(BUILDING)
      case _ => None
    }
  }

}
