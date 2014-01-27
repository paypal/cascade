package com.paypal.stingray.common.service

/**
 * Base trait for named services. See [[com.paypal.stingray.common.values.StaticValues]] for an example.
 */
trait ServiceNameComponent {

  /** The name of this service */
  val serviceName: String
}
