package com.paypal.stingray.common.enumeration

/**
 * User: will
 * Date: 7/9/13
 */
sealed abstract class RelationType extends Enumeration
object RelationType extends EnumUnapply[RelationType] {
  object One2One extends RelationType { val stringVal = "one2one" }
  object One2Many extends RelationType { val stringVal = "one2many" }
  object Many2One extends RelationType { val stringVal = "many2one" }
  object Many2Many extends RelationType { val stringVal = "many2many" }

  implicit val relationTypeRead: EnumReader[RelationType] = new EnumReader[RelationType] {
    override def read(s: String): Option[RelationType] = Some(s.toLowerCase).collect {
      case One2One.stringVal => One2One
      case One2Many.stringVal => One2Many
      case Many2One.stringVal => Many2One
      case Many2Many.stringVal => Many2Many
    }
  }
}