package com.paypal.stingray.http.server

/**
 * This component provides configuration information for a spray service
 */
trait SprayConfigurationComponent {

  //configuration variables provided below

  /**
   * The port the spray service should listen on
   */
  val port: Int

  /**
   * Number of backlogged connections spray should allow before resetting connections
   */
  val backlog: Int

}
