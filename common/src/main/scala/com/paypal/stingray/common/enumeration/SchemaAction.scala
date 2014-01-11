package com.paypal.stingray.common.enumeration

import com.paypal.stingray.common.enumeration._
import com.paypal.stingray.common.request.AccessLevel


/**
 * Created by drapp on 7/16/13.
 * Currently just CRUD methods, but might be extended to other wacky things one day
 */
sealed abstract class SchemaAction extends Enumeration

object SchemaAction extends EnumUnapply[SchemaAction] {

  object GET extends SchemaAction {
    val stringVal = "get"
  }

  object PUT extends SchemaAction {
    val stringVal = "put"
  }

  object POST extends SchemaAction {
    val stringVal = "post"
  }

  object DELETE extends SchemaAction {
    val stringVal = "delete"
  }

  implicit val schemaActionRead: EnumReader[SchemaAction] = new EnumReader[SchemaAction] {
    override def read(s: String): Option[SchemaAction] = s.toLowerCase match {
      case GET.stringVal => Some(GET)
      case PUT.stringVal => Some(PUT)
      case POST.stringVal => Some(POST)
      case DELETE.stringVal => Some(DELETE)
      case _ => None
    }
  }

}
