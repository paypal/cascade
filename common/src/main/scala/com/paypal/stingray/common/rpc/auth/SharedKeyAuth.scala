package com.paypal.stingray.common.rpc.auth

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.common.rpc.auth
 *
 * User: aaron
 * Date: 1/25/12
 * Time: 3:30 PM
 */

trait SharedKeyAuth {
  def sharedKey:String
  def authHeaderPair:(String, String) = (SharedKeyAuth.AuthHeaderName, sharedKey)
}

object SharedKeyAuth {
  val AuthHeaderName = "X-StackMob-InternalAuth"
}
