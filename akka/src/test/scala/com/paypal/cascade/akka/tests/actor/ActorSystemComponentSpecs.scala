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
package com.paypal.cascade.akka.tests.actor

import org.specs2._
import org.specs2.mock.Mockito
import com.paypal.cascade.akka.actor.ActorSystemComponent
import com.paypal.cascade.common.service.ServiceNameComponent
import com.paypal.cascade.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests for [[com.paypal.cascade.akka.actor.ActorSystemComponent]]
 */
class ActorSystemComponentSpecs
  extends SpecificationLike
  with ScalaCheck
  with Mockito { override def is = s2"""

  Initializing a class which extends ActorSystemComponent should
    Provide an actor system                  ${Initialize.ActorSystem().ok}
    Provide an ActorRefFactory               ${Initialize.ActorRefFactory().ok}
    Provide an execution context             ${Initialize.ExecutionContext().ok}

  """

  class TestActorSystem extends ActorSystemComponent with ServiceNameComponent {
    override lazy val serviceName = "akka"
  }

  trait Context
    extends CommonImmutableSpecificationContext {
    val actorSystem = new TestActorSystem
  }

  object Initialize {
    case class ActorSystem() extends Context {
      def ok = this {
        (actorSystem.system must not beNull) and
          (actorSystem.system must beAnInstanceOf[akka.actor.ActorSystem])
      }
    }
    case class ActorRefFactory() extends Context {
      def ok = this {
        (actorSystem.actorRefFactory must not beNull) and
          (actorSystem.actorRefFactory must beAnInstanceOf[akka.actor.ActorRefFactory]) and
          (actorSystem.actorRefFactory must beEqualTo(actorSystem.system))
      }
    }
    case class ExecutionContext() extends Context {
      def ok = this {
        (actorSystem.ec must not beNull) and
          (actorSystem.ec must beAnInstanceOf[scala.concurrent.ExecutionContext]) and
          (actorSystem.ec must beEqualTo(actorSystem.system.dispatcher))
      }
    }
  }

}
