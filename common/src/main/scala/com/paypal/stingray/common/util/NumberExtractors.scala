package com.paypal.stingray.common.util

/**
 * Created by IntelliJ IDEA.
 * User: jordanrw
 * Date: 2/21/12
 * Time: 7:34 PM
 *
 * Usage:
 *
 * scala> import com.paypal.stingray.common.util.IntExtractor
 * import com.paypal.stingray.common.util.IntExtractor
 * scala> "1234" match { case IntExtractor(a) => a; case _ => 0; }
 * res0: Int = 1234
 * scala> "1234a" match { case IntExtractor(a) => a; case _ => 0; }
 * res1: Int = 0
 * scala> "1234.23" match { case IntExtractor(a) => a; case _ => 0; }
 * res2: Int = 0
 *
 */

object IntExtractor {

  def unapply(s: String): Option[Int] = {
    try {
      Some(s.toInt)
    } catch {
      case _ : java.lang.NumberFormatException => None
    }
  }

}

object LongExtractor {

  def unapply(s: String): Option[Long] = {
    try {
      Some(s.toLong)
    } catch {
      case _ : java.lang.NumberFormatException => None
    }
  }

}


