package com.paypal.stingray.concurrent

import org.scalacheck._
import Gen.value

/**
 * Created by IntelliJ IDEA.
 * 
 * com.paypal.stingray.concurrent.tests
 * 
 * User: aaron
 * Date: 10/1/12
 * Time: 1:37 PM
 */

package object tests {
  def GenEmptyConcurrentHashMap[Key, Value]: Gen[ConcurrentHashMap[Key, Value]] = {
    ConcurrentHashMap[Key, Value]()
  }
}
