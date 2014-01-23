package com.paypal.stingray.common.constants

import java.nio.charset.Charset

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 3/12/13
 * Time: 12:28 PM
 */
object ValueConstants {
  val StingrayEnvironment = "stingray.environment"
  val ClusterName = "cluster.name"
  val BuildTag = "build_name"

  val charsetUtf8 = Charset.forName("UTF-8")
}
