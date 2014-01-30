package com.paypal.stingray.common.constants

import java.nio.charset.Charset

/**
 * Commonly used values and strings.
 */
object ValueConstants {

  /** For JSON (and many other specs), Unicode is required and UTF-8 is default. */
  val charsetUtf8: Charset = Charset.forName("UTF-8")
}
