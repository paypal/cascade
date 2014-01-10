package com.paypal.stingray.http.tests.resource

import com.paypal.stingray.http.resource.ResourceService
import com.paypal.stingray.http.tests.TestServiceNameComponent
import com.paypal.stingray.common.values.StaticValuesFromServiceNameComponent

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 4/4/13
 * Time: 5:17 PM
 */
trait DummyResourceService extends ResourceService with TestServiceNameComponent with StaticValuesFromServiceNameComponent {

  val dummy = new DummyResource

  override val route = {
    path("ping") {
      get {
        serve(dummy, Map())
      }
    }
  }
}
