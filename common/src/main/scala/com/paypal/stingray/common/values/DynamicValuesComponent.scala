package com.paypal.stingray.common.values

/**
 * Trait used to mix-in a [[com.paypal.stingray.common.values.DynamicValues]] instance
 */
trait DynamicValuesComponent {

  /** the DynamicValues instance */
  val dvs: DynamicValues

}
