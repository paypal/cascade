package com.paypal.stingray.common

/**
 * Convenience methods and implicit wrappers for working with [[scala.collection.Seq]]
 */
package object seq {

  /**
   * Implicit wrapper for [[scala.collection.Seq]] objects
   * @param seq the sequence to wrap
   * @tparam T the type of values in the sequence
   */
  implicit class RichSeq[T](seq: Seq[T]) {

    /**
     * An accessor interface to sequences, because `seq(i)` and `seq.apply(i)` are hard, apparently
     * @param i the index in the sequence to retrieve
     * @return the value at index `i`, or None
     */
    def get(i: Int): Option[T] = {
      if(i >= 0 && i < seq.length) {
        Some(seq.apply(i))
      } else {
        None
      }
    }
  }

}
