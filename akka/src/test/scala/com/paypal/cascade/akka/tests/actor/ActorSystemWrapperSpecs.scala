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
package com.paypal.cascade.akka.tests.actor

import org.specs2._
import org.specs2.mock.Mockito
import com.paypal.cascade.akka.actor.ActorSystemWrapper
import com.paypal.cascade.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests for [[com.paypal.cascade.akka.actor.ActorSystemWrapper]]
 */
class ActorSystemWrapperSpecs
  extends SpecificationLike
  with ScalaCheck
  with Mockito { override def is = s2"""

  Initializing an ActorSystemWrapper should
    Provide an actor system                  ${Initialize.ActorSystem().ok}
    Provide an ActorRefFactory               ${Initialize.ActorRefFactory().ok}
    Provide an execution context             ${Initialize.ExecutionContext().ok}

  """

  trait Context extends CommonImmutableSpecificationContext {
    val serviceName = "testService"
    val wrapper = new ActorSystemWrapper(serviceName)
  }

  object Initialize {
    case class ActorSystem() extends Context {
      def ok = apply {
        (wrapper.system must not beNull) and
          (wrapper.system must beAnInstanceOf[akka.actor.ActorSystem])
      }
    }
    case class ActorRefFactory() extends Context {
      def ok = apply {
        (wrapper.actorRefFactory must not beNull) and
          (wrapper.actorRefFactory must beAnInstanceOf[akka.actor.ActorRefFactory]) and
          (wrapper.actorRefFactory must beEqualTo(wrapper.system))
      }
    }
    case class ExecutionContext() extends Context {
      def ok = apply {
        (wrapper.executionContext must not beNull) and
          (wrapper.executionContext must beAnInstanceOf[scala.concurrent.ExecutionContext]) and
          (wrapper.executionContext must beEqualTo(wrapper.system.dispatcher))
      }
    }
  }

}
