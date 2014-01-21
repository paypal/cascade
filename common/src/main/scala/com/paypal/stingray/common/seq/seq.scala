package com.paypal.stingray.common

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
        Some(seq.apply(i))
      } else {
        None
      }
    }
  }

}
