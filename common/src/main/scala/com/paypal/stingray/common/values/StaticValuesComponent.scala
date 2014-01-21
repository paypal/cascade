package com.paypal.stingray.common.values

import com.paypal.stingray.common.service.ServiceNameComponent

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 8/9/13
 * Time: 1:51 PM
 */
trait StaticValuesComponent {

  val svs: StaticValues
}

trait StaticValuesFromServiceNameComponent extends StaticValuesComponent {
  this: ServiceNameComponent =>

  override lazy val svs = new StaticValues(serviceName)
}
