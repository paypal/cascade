/**
 * Copyright 2013-2015 PayPal
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

import java.util.concurrent.TimeUnit
import akka.util.Timeout
import com.paypal.cascade.akka.actor.ActorSystemWrapper
import org.specs2._
import org.specs2.mock.Mockito
import com.paypal.cascade.common.tests.util.CommonImmutableSpecificationContext
import com.paypal.cascade.http.actor._
import com.paypal.cascade.http.server._
import spray.can.server.ServerSettings
import spray.routing._
import spray.io.ServerSSLEngineProvider
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Tests for [[com.paypal.cascade.http.actor.SprayActor]]
 */
class SprayActorSpecs
  extends SpecificationLike
  with ScalaCheck
  with Mockito { override def is = s2"""

  Calling SprayActor.start should succeed ${Initialize().ok}

  """

  trait Context extends CommonImmutableSpecificationContext {
    val backlog: Int = 0
    val port: Int = 0
    val serviceName = "http"
    val route = mock[Route]

    val wrapper = new ActorSystemWrapper(serviceName)
    val config = new SprayConfiguration(serviceName, port, backlog, route)
    implicit val timeout = Timeout(10, TimeUnit.SECONDS)
  }

  case class Initialize() extends Context {
    import spray.can._
    def ok() = apply {
      // Ensure there are no exceptions on startup.
      // The Http bind makes a tell to the Tcp bind, using the
      // server settings bind-timeout value as the receive timeout.
      // It's overridden to avoid to allow extra time to bind during specs.
      val defaultSettings = ServerSettings(wrapper.system)
      val overriddenBindTimeoutSettings = defaultSettings.copy(timeouts = defaultSettings.timeouts.copy(bindTimeout = timeout.duration))
      val fut = SprayActor.start(wrapper, config, Option(overriddenBindTimeoutSettings))(mock[ServerSSLEngineProvider], timeout)
      Await.result(fut, timeout.duration) must beAnInstanceOf[Http.Bound]
    }
  }
}
