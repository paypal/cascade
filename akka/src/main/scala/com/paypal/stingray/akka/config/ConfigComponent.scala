package com.paypal.stingray.akka.config

import com.typesafe.config.ConfigFactory

/**
 * Simple component which provides access to a configuration file
 */
trait ConfigComponent {

  /* Loads application.conf */
  lazy val config = ConfigFactory.load()

}
