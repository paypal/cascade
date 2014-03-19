package com.paypal.stingray.common.tests.properties

import org.specs2._
import org.specs2.mock.Mockito
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext
import com.paypal.stingray.common.properties.BuildProperties

/**
 * Tests [[com.paypal.stingray.common.properties.BuildProperties]]
 */
class BuildPropertiesSpecs extends Specification with Mockito { override def is = s2"""

  BuildProperties loads the build.properties files.

  Get should:
    get the value when the path exists      ${GetValue().ok}

"""
  trait Context extends CommonImmutableSpecificationContext {
    // Nothing for now
  }

  case class GetValue() extends Context {
    def ok = apply {
      val bp = spy(new BuildProperties)
      bp.get("some.property") returns Some("somevalue")
      bp.get("some.property") must beSome("somevalue")
    }
  }

}
