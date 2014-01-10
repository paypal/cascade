package com.paypal.stingray.http.tests

import com.paypal.stingray.common.service.ServiceNameComponent

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 8/9/13
 * Time: 3:38 PM
 */
trait TestServiceNameComponent extends ServiceNameComponent {
  override lazy val serviceName = "tests"
}
