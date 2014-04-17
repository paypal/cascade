package com.paypal.stingray.akka.tests.actor

import org.specs2._
import org.specs2.mock.Mockito
import com.paypal.stingray.akka.actor.ActorSystemComponent
import com.paypal.stingray.common.service.ServiceNameComponent
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests for [[com.paypal.stingray.akka.actor.ActorSystemComponent]]
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
