/**
 * Copyright 2013-2014 PayPal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paypal.cascade.http.tests.actor

import org.specs2._
import org.specs2.mock.Mockito
import com.paypal.cascade.akka.actor.ActorSystemComponent
import com.paypal.cascade.common.tests.util.CommonImmutableSpecificationContext
import com.paypal.cascade.common.service.ServiceNameComponent
import com.paypal.cascade.http.actor._
import com.paypal.cascade.http.server._
import com.paypal.cascade.http.resource._
import spray.routing._
import spray.io.ServerSSLEngineProvider

/**
 * Tests for [[com.paypal.cascade.http.actor.SprayActorComponent]]
 */
class SprayActorComponentSpecs
  extends SpecificationLike
  with ScalaCheck
  with Mockito { override def is = s2"""

  Initializing a class which extends SprayActorComponent should
    Provide a spray actor                  ${Initialize.SprayActor().ok}

  """

  class TestActorSystem extends SprayActorComponent
    with ActorSystemComponent
    with ResourceServiceComponent
    with ServiceNameComponent
    with SprayConfigurationComponent {

    override val backlog: Int = 0
    override val port: Int = 0
    override lazy val serviceName = "http"
    override lazy val route: Route = mock[Route]
  }

  trait Context
    extends CommonImmutableSpecificationContext {
    val actorSystem = new TestActorSystem
    actorSystem.start(mock[ServerSSLEngineProvider])
    val sprayActor = actorSystem.sprayActor
  }

  object Initialize {
    case class SprayActor() extends Context {
      def ok = apply {
        (sprayActor must not beNull) and
          (sprayActor must beAnInstanceOf[akka.actor.ActorRef])
      }
    }
  }


}
