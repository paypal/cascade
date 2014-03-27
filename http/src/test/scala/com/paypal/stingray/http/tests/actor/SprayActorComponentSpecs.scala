package com.paypal.stingray.http.tests.actor

import org.specs2._
import org.specs2.mock.Mockito
import com.paypal.stingray.http.actor._
import com.paypal.stingray.http.server._
import com.paypal.stingray.http.resource._
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext
import com.paypal.stingray.common.service.ServiceNameComponent
import spray.routing._

/**
 * Tests for [[com.paypal.stingray.http.actor.SprayActorComponent]]
 */
class SprayActorComponentSpecs
  extends SpecificationLike
  with ScalaCheck
  with Mockito { override def is = s2"""

  Initializing a class which extends SprayActorComponent should
    Provide a spray actor                  ${Initialize.SprayActor().ok}

"""

  trait Context
    extends CommonImmutableSpecificationContext {

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

    val actorSystem = new TestActorSystem
    actorSystem.start()
    val sprayActor = actorSystem.sprayActor
  }

  object Initialize {
    case class SprayActor() extends Context {
      def ok = this {
        (sprayActor must not beNull) and
          (sprayActor must beAnInstanceOf[akka.actor.ActorRef])
      }
    }
  }


}
