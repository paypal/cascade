package com.paypal.stingray.common.deploymentapi.metadata

import com.paypal.stingray.common.enumeration._

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.common.deploymentapi.metadata
 *
 * User: aaron
 * Date: 2/27/12
 * Time: 12:43 PM
 */

// an enum representing how to build a given repository (ie: Phonegap for html5, maven for custom code, etc...)
sealed abstract class BuildType extends Enumeration

object BuildType extends EnumUnapply[BuildType] {

  // indicates that the code should be sent to PhoneGap build to produce an Android, iOS, etc... binary
  object PHONEGAP extends BuildType {
    override val stringVal = "PHONEGAP"
  }

  // indicates that no further modifications or build steps should be made on the code, and
  // it should be deployed appropriately according to the repository type
  object BROWSER extends BuildType {
    override val stringVal = "BROWSER"
  }

  // indicates that Maven should be used to build custom code.
  // only to be used with the RepositoryType of CC
  object MAVEN extends BuildType {
    override val stringVal = "MAVEN"
  }

  implicit val buildTypeReader: EnumReader[BuildType] = new EnumReader[BuildType] {
    override def read(s: String): Option[BuildType] = s.toUpperCase match {
      case PHONEGAP.stringVal => Some(PHONEGAP)
      case BROWSER.stringVal => Some(BROWSER)
      case _ => None
    }
  }

}
