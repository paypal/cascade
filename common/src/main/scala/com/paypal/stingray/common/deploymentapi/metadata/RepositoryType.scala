package com.paypal.stingray.common.deploymentapi.metadata

import com.paypal.stingray.common.enumeration._

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 1/30/12
 * Time: 12:12 PM
 */

// an enumeration representing the type of repository (ie: HTML5 Custom code, etc...)
sealed abstract class RepositoryType extends Enumeration {
  def validBuildTypes:Set[BuildType]
}

object RepositoryType extends EnumUnapply[RepositoryType] {

  object HTML5 extends RepositoryType {
    override val stringVal = "HTML5"
    val validBuildTypes = Set[BuildType](BuildType.PHONEGAP, BuildType.BROWSER)
  }

  object CC extends RepositoryType {
    override val stringVal = "CC"
    val validBuildTypes = Set[BuildType]()
  }

  implicit val repositoryTypeReader: EnumReader[RepositoryType] = new EnumReader[RepositoryType] {
    override def read(s: String): Option[RepositoryType] = s.toUpperCase match {
      case HTML5.stringVal => Some(HTML5)
      case CC.stringVal => Some(CC)
      case _ => None
    }
  }

}
