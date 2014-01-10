package com.paypal.stingray.common.db

import com.paypal.stingray.common.enumeration._

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 3/7/12
 * Time: 12:31 PM
 */

sealed abstract class DatabaseType extends Enumeration {
  def getDbName: String = stringVal
}

object DatabaseType {

  def getAll: List[DatabaseType] = List(StackMob)

  object StackMob extends DatabaseType {
    override val stringVal = "stackmob"
  }

  implicit val databaseTypeReader: EnumReader[DatabaseType] = new EnumReader[DatabaseType] {
    override def read(s: String): Option[DatabaseType] = s.toLowerCase match {
      case StackMob.stringVal => Some(StackMob)
      case _ => None
    }
  }

}
