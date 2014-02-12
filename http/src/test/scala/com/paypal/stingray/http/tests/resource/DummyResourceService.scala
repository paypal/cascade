package com.paypal.stingray.http.tests.resource

import com.paypal.stingray.http.resource.ResourceService
import com.paypal.stingray.common.values.StaticValuesFromServiceNameComponent
import com.paypal.stingray.common.service.ServiceNameComponent

/**
 * A dummy resource service implementation for use with [[com.paypal.stingray.http.tests.resource.DummyResource]].
 * Only accepts requests to the "/ping" endpoint.
 */
trait DummyResourceService
  extends ResourceService
  with ServiceNameComponent
  with StaticValuesFromServiceNameComponent {

  /** This resource */
  val dummy = new DummyResource

  /** The route for this resource */
  override val route = {
    path("ping") {
      get {
        serve(dummy, Map())
      }
    }
  }

  override lazy val serviceName = "tests"

}
