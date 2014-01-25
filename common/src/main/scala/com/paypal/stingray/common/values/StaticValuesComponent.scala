package com.paypal.stingray.common.values

import com.paypal.stingray.common.service.ServiceNameComponent

/**
 * Trait used to mix-in a [[com.paypal.stingray.common.values.StaticValues]] instance
 */
trait StaticValuesComponent {

  /** the StaticValues instance */
  val svs: StaticValues
}

/**
 * Trait used to mix-in a [[com.paypal.stingray.common.values.StaticValues]] that is instantiated using
 * the service's name as defined in its [[com.paypal.stingray.common.service.ServiceNameComponent]]
 */
trait StaticValuesFromServiceNameComponent extends StaticValuesComponent {
  this: ServiceNameComponent =>

  /** the StaticValues instance */
  override lazy val svs = new StaticValues(serviceName)
}
