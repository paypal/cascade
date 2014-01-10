package com.paypal.stingray.common

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.common.util
 *
 * User: aaron
 * Date: 2/14/13
 * Time: 5:29 PM
 *
 * A utility to convert to/from storage sizes
 */
package object util {
  sealed trait StorageSize {
    private val doubleMultiplier = 1024.toDouble
    protected def bytes: Long
    protected def inKilobytes: Double = bytes / doubleMultiplier
    protected def inMegabytes: Double = inKilobytes / doubleMultiplier
    protected def inGigabytes: Double = inMegabytes / doubleMultiplier
    protected def inTerabytes: Double = inMegabytes / doubleMultiplier
  }

  object StorageSize {
    private val multiplier = 1024
    def fromBytes(numBytes: Long): StorageSize = new StorageSize {
      override lazy val bytes = numBytes
    }
    def fromKilobytes(numKilobytes: Long): StorageSize = new StorageSize {
      override lazy val bytes: Long = numKilobytes * multiplier
    }
    def fromMegabytes(numMegabytes: Long): StorageSize = new StorageSize {
      override lazy val bytes: Long = numMegabytes * multiplier * multiplier
    }
    def fromGigabytes(numGigabytes: Long): StorageSize = new StorageSize {
      override lazy val bytes: Long = numGigabytes * multiplier * multiplier * multiplier
    }
    def fromTerabytes(numTerabytes: Long): StorageSize = new StorageSize {
      override lazy val bytes: Long = numTerabytes * multiplier * multiplier * multiplier * multiplier
    }
  }
}
