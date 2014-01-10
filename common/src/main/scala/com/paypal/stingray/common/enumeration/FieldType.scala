package com.paypal.stingray.common.enumeration

import scalaz._
import scalaz.Equal._
import Scalaz._
import com.paypal.stingray.common.enumeration.FieldType.NoneType

/**
 * User: will
 * Date: 7/8/13
 */

sealed abstract class FieldType extends Enumeration {
  def toOption: Option[FieldType] = this match {
    case NoneType => None
    case _ => this.some
  }
}

object FieldType extends EnumUnapply[FieldType] {
  //core field types
  object SubObjectType                   extends FieldType { val stringVal = "subobject" }
  object StringType                      extends FieldType { val stringVal = "string" }
  object NumberType                      extends FieldType { val stringVal = "number"}
  object IntType                         extends FieldType { val stringVal = "int" }
  object DoubleType                      extends FieldType { val stringVal = "double" }
  object BoolType                        extends FieldType { val stringVal = "bool" }
  object ArrayType                       extends FieldType { val stringVal = "array" }
  //parametererized field types
  object DateTimeType                    extends FieldType { val stringVal = "datetime" }
  object UTCTimeType                     extends FieldType { val stringVal = "utcmillis" }
  object UserNameType                    extends FieldType { val stringVal = "username" }
  object PasswordType                    extends FieldType { val stringVal = "password" }
  object ForgotPasswordEmailType         extends FieldType { val stringVal = "forgotpemail" }
  object CreatorNameRefType              extends FieldType { val stringVal = "creatorref" }
  object RelationType                    extends FieldType { val stringVal = "relationship" }
  object GeopointType                    extends FieldType { val stringVal = "geopoint" }
  object BinaryType                      extends FieldType { val stringVal = "binary" }
  object PrimaryKeyType                  extends FieldType { val stringVal = "pk" }
  //invalid type
  object NoneType                        extends FieldType { val stringVal = "_none" }

  implicit val fieldEqual: Equal[FieldType] = equalA

  implicit val fieldTypeRead: EnumReader[FieldType] = new EnumReader[FieldType] {
    override def read(s: String): Option[FieldType] = s.toLowerCase.some.collect {
      case SubObjectType.stringVal             => SubObjectType
      case StringType.stringVal                => StringType
      case NumberType.stringVal                => NumberType
      case IntType.stringVal                   => IntType
      case DoubleType.stringVal                => DoubleType
      case BoolType.stringVal                  => BoolType
      case ArrayType.stringVal                 => ArrayType
      case DateTimeType.stringVal              => DateTimeType
      case UTCTimeType.stringVal               => UTCTimeType
      case UserNameType.stringVal              => UserNameType
      case PasswordType.stringVal              => PasswordType
      case ForgotPasswordEmailType.stringVal   => ForgotPasswordEmailType
      case CreatorNameRefType.stringVal        => CreatorNameRefType
      case RelationType.stringVal              => RelationType
      case GeopointType.stringVal              => GeopointType
      case BinaryType.stringVal                => BinaryType
      case PrimaryKeyType.stringVal            => PrimaryKeyType
      case NoneType.stringVal                  => NoneType
    }
  }
}
