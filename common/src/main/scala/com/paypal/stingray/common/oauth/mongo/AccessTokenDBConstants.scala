package com.paypal.stingray.common.oauth.mongo

import com.paypal.stingray.common.db.mongo.MongoConstants

/**
 * Created with IntelliJ IDEA.
 * User: ayakushev
 * Date: 5/6/13
 * Time: 6:05 PM
 */
object AccessTokenDBConstants {

  //do not change these values
  val tokenField = MongoConstants.ID //also MAC ID
  val userField = "user"
  val schemaField = "schema"
  val expiryTimeField = "expiryTime"
  val macKeyField = "macKey"
  val timestampDeltaField = "tsDel"
  val tokenTypeField = "type"

  val oauth2AccessTokensColl = "oauth2_access_tokens"

}
