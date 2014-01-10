package com.paypal.stingray.concurrent

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.concurrent.actor
 *
 * User: aaron
 * Date: 10/4/12
 * Time: 3:55 PM
 */
package object actor {
  def actor[In, Out](f: In => Out): Actor[In, Out] = Actor(f)
}
