package com.paypal.stingray.common

import scalaz._
import Scalaz._

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.common.seq
 *
 * User: aaron
 * Date: 7/3/12
 * Time: 4:40 PM
 */

package object seq {

  implicit class RichSeq[T](seq: Seq[T]) {
    def get(i: Int): Option[T] = {
      if(i >= 0 && i < seq.length) {
        seq.apply(i).some
      } else {
        none
      }
    }
  }

}
