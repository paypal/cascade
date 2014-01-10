package com.paypal.stingray.common.request

import scalaz._
import Scalaz._
import com.paypal.stingray.common.option._



/**
 * Represents restrictions on access to a schema as set by the developer. Can be applied to individual http verbs
 */

sealed abstract class AccessLevel {
  protected def levelType: AccessLevelType
  protected def parameters: List[String]
  def allows(auth: Authorization): Boolean
  val strVal = toString
  lazy val toTypeAndParams = (levelType, parameters)
}

object AccessLevel {

  // This verb is totally cut off from everything
  case object NotAllowed extends AccessLevel {
    def levelType = AccessLevelType.NotAllowed
    def parameters: List[String] = List()
    def allows(auth: Authorization): Boolean = false
  }

  // This verb is only available via the oauth1 private key. Prior to oauth2 this was the only setting.
  // Now it remains for grandfathering and for admin tools.
  case object PrivateKey extends AccessLevel {
    def levelType = AccessLevelType.PrivateKey
    def parameters: List[String] = List()
    def allows(auth: Authorization): Boolean = auth.level == Authorization.PRIVATE_KEY_LVL
  }

  // Objects in this schema can only be accessed/modified by their owner, as found in their sm_owner field.
  // This makes no sense for POST
  case object Owner extends AccessLevel {
    def levelType = AccessLevelType.Owner
    def parameters: List[String] = List()
    def allows(auth: Authorization): Boolean = auth.level >= Authorization.USER_AUTH_LVL
  }

  // References a field in a specific object that must be a relation to users
  // Useful for defining a set of admin users
  case class StaticAccessList(aclSchema: String, instance: String, field: String) extends AccessLevel {
    def levelType = AccessLevelType.StaticAccessList
    def parameters: List[String] = List(aclSchema, instance, field)
    def allows(auth: Authorization): Boolean = auth.level >= Authorization.USER_AUTH_LVL
  }

  object StaticAccessList {
    val regex = "StaticAccessList\\(([^,]*),([^,]*),([^,]*)\\)".r
  }

  // References a field in the object being accessed, which must be a relation to a user object.
  // That field is then treated like the owner. Useful for restricting a conversation to its members
  case class FieldAccessList(field: String) extends AccessLevel {
    def levelType = AccessLevelType.FieldAccessList
    def parameters: List[String] = List(field)
    def allows(auth: Authorization): Boolean = auth.level >= Authorization.USER_AUTH_LVL
  }

  object FieldAccessList {
    val regex = "FieldAccessList\\(([^,]*)\\)".r
  }

  // References a field in the user object of the owner of the object being accessed, which must be a
  // relation to a user object. That field is then treated like the owner. Useful for restricting a
  // wall post to the friends of its owner
  case class OwnerAccessList(userSchema: String, field: String) extends AccessLevel {
    def levelType = AccessLevelType.OwnerAccessList
    def parameters: List[String] = List(userSchema, field)
    def allows(auth: Authorization): Boolean = auth.level >= Authorization.USER_AUTH_LVL
  }

  object OwnerAccessList {
    val regex = "OwnerAccessList\\(([^,]*),([^,]*)\\)".r
  }

  // This verb is only available to requests authenticated as coming from an oauth2 user
  case object AllUsers extends AccessLevel {
    def levelType = AccessLevelType.AllUsers
    def parameters: List[String] = List()
    def allows(auth: Authorization): Boolean = auth.level >= Authorization.USER_AUTH_LVL
  }

  // This verb can be used by absolutely anyone
  case object Open extends AccessLevel {
    def levelType = AccessLevelType.Open
    def parameters: List[String] = List()
    def allows(auth: Authorization): Boolean = true
  }

  @throws(classOf[InvalidAccessLevelException])
  def fromString(str: String): AccessLevel = {
    readString(str) orThrow new InvalidAccessLevelException(str)
  }

  def readString(str: String): Option[AccessLevel] = {
    str.some collect {
      case Open.strVal => Open
      case AllUsers.strVal => AllUsers
      case Owner.strVal => Owner
      case StaticAccessList.regex(schema, instance, user) => StaticAccessList(schema, instance, user)
      case FieldAccessList.regex(fieldName) => FieldAccessList(fieldName)
      case OwnerAccessList.regex(userSchema, fieldName) => OwnerAccessList(userSchema, fieldName)
      case PrivateKey.strVal => PrivateKey
      case NotAllowed.strVal => NotAllowed
    }

  }

  def fromType(accessLevelType: AccessLevelType, params: List[String]): Option[AccessLevel] = (accessLevelType, params).some collect {
    case (AccessLevelType.Open, Nil) => AccessLevel.Open
    case (AccessLevelType.AllUsers, Nil) => AccessLevel.AllUsers
    case (AccessLevelType.Owner, Nil) => AccessLevel.Owner
    case (AccessLevelType.StaticAccessList, aclSchema :: instance :: field :: Nil) => AccessLevel.StaticAccessList(aclSchema, instance, field)
    case (AccessLevelType.FieldAccessList, field :: Nil) => AccessLevel.FieldAccessList(field)
    case (AccessLevelType.OwnerAccessList, userSchema :: field :: Nil) => AccessLevel.OwnerAccessList(userSchema, field)
    case (AccessLevelType.PrivateKey, Nil) => AccessLevel.PrivateKey
    case (AccessLevelType.NotAllowed, Nil) => AccessLevel.NotAllowed
  }

}

class InvalidAccessLevelException(s: String) extends Exception(s + " is not a valid access level")
