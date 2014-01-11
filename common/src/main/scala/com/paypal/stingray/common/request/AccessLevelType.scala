package com.paypal.stingray.common.request

import com.paypal.stingray.common.enumeration._


/**
 * Created by drapp on 7/16/13.
 * Currently just CRUD methods, but might be extended to other wacky things one day
 */
sealed abstract class AccessLevelType extends Enumeration

object AccessLevelType extends EnumUnapply[AccessLevelType] {

  object NotAllowed extends AccessLevelType {
    val stringVal = "notallowed"
  }

  object PrivateKey extends AccessLevelType {
    val stringVal = "privatekey"
  }

  object Owner extends AccessLevelType {
    val stringVal = "owner"
  }

  object StaticAccessList extends AccessLevelType {
    val stringVal = "staticaccesslist"
  }

  object FieldAccessList extends AccessLevelType {
    val stringVal = "fieldaccesslist"
  }

  object OwnerAccessList extends AccessLevelType {
    val stringVal = "owneraccesslist"
  }

  object AllUsers extends AccessLevelType {
    val stringVal = "allusers"
  }

  object Open extends AccessLevelType {
    val stringVal = "open"
  }

  implicit val schemaActionRead: EnumReader[AccessLevelType] = new EnumReader[AccessLevelType] {
    override def read(s: String): Option[AccessLevelType] = s.toLowerCase match {
      case NotAllowed.stringVal => Some(NotAllowed)
      case PrivateKey.stringVal => Some(PrivateKey)
      case Owner.stringVal => Some(Owner)
      case StaticAccessList.stringVal => Some(StaticAccessList)
      case FieldAccessList.stringVal => Some(FieldAccessList)
      case OwnerAccessList.stringVal => Some(OwnerAccessList)
      case AllUsers.stringVal => Some(AllUsers)
      case Open.stringVal => Some(Open)
      case _ => None
    }
  }

}
