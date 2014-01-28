package com.paypal.stingray.http.tests

import com.paypal.stingray.common.service.ServiceNameComponent

/**
 * Service name to use for test services
 */
trait TestServiceNameComponent extends ServiceNameComponent {
  override lazy val serviceName = "tests"
}
