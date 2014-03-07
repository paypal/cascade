package com.paypal.stingray.http.tests.actor

import org.specs2._
import com.paypal.stingray.http.actor._
import com.paypal.stingray.http.server._
import com.paypal.stingray.http.resource._
import com.paypal.stingray.common.service.ServiceNameComponent
import com.paypal.stingray.common.values._
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext
import spray.routing.Route
import org.specs2.mock.Mockito

/**
 * Tests for [[com.paypal.stingray.http.actor.ActorSystemComponent]]
 */
class ActorSystemComponentSpecs
  extends SpecificationLike
  with ScalaCheck
  with Mockito { def is = s2"""

  Initializing a class which extends ActorSystemComponent should
    Provide an actor system                  ${Initialize.ActorSystem().ok}
    Provide an ActorRefFactory               ${Initialize.ActorRefFactory().ok}
    Provide an execution context             ${Initialize.ExecutionContext().ok}

"""

  trait Context
    extends CommonImmutableSpecificationContext {

    class TestActorSystem extends ActorSystemComponent
      with ResourceServiceComponent
      with ServiceNameComponent
      with SprayConfigurationComponent
      with StaticValuesFromServiceNameComponent {

      override val backlog: Int = 0
      override val port: Int = 0
      override lazy val serviceName = "http"
      override lazy val route: Route = mock[Route]
    }

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
