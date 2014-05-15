package com.paypal.stingray.http.tests.resource

import org.specs2._
import com.paypal.stingray.http.tests.api.SprayRoutingClientComponent
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext
import spray.http.{StatusCodes, HttpMethods}
import spray.http.HttpHeaders.RawHeader
import com.paypal.stingray.json._
import akka.actor.{ActorSystem, Props, Actor}
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit

/**
 * Tests for [[com.paypal.stingray.http.resource.ResourceServiceComponent.ResourceService]]
 *
 * /stats success case is not tested because private server actor cannot start up
 * without the correct environment. Thus we only test the fail case.
 */
class ResourceServiceSpecs extends SpecificationLike with ScalaCheck { def is=s2"""

  Status endpoint should return proper build info keys                   ${Status().ok}
  Stats endpoint should return proper stats info                         ${Stats().ok}
  Stats endpoint should return failure without a proper server actor     ${Stats().fails}

"""

  val statsObject = new spray.can.server.Stats(new FiniteDuration(1L, TimeUnit.SECONDS), 1L, 1L, 1L, 1L, 1L, 1L, 1L)

  class ParentActor extends Actor {
    def receive = {
      case other => println(other)
    }
    context.actorOf(Props(new ServerActor), "listener-0")
  }
  class ServerActor extends Actor {
    def receive = {
      case spray.can.Http.GetStats => sender ! statsObject
    }
  }

  class TestEnv extends DummyResourceService with SprayRoutingClientComponent {
    override implicit lazy val system = ActorSystem()
    val serverActor = system.actorOf(Props(new ParentActor), "IO-HTTP")
  }

  class FailTestEnv extends DummyResourceService with SprayRoutingClientComponent

  class Context extends CommonImmutableSpecificationContext {
    val env = new TestEnv
    val failEnv = new FailTestEnv
  }

  case class Status() extends Context {
    def ok = {
      val matcherKeys = Seq("status", "service-name", "dependencies", "git-info")
      val res = env.sprayRoutingClient.makeRequest(HttpMethods.GET, "/status", List(RawHeader("x-service-status", "true")), None)
      val success = res.status must beEqualTo(StatusCodes.OK)
      val mappedData = res.entity.data.asString.fromJson[Map[String, Any]].get
      val keys = mappedData.map { item =>
        val (k, _) = item
        k
      }
      val entityMatches = keys must containTheSameElementsAs(matcherKeys)
      success and entityMatches
    }
  }
  case class Stats() extends Context {
    def ok = {
      val res = env.sprayRoutingClient.makeRequest(HttpMethods.GET, "/stats", List(RawHeader("x-service-stats", "true")), None)
      val correctStatus = res.status must beEqualTo(StatusCodes.OK)
      val mappedData = res.entity.data.asString.fromJson[Map[String, Any]].get
      val values = mappedData.map { item =>
        val (_, v) = item
        v
      }
      val dataMatches = values.map {
        case dur: Map[_, _] => dur must beEqualTo(Map("finite" -> true))
        case other => other must beEqualTo(1)
      }.reduceLeft { (first, second) =>
        first and second
      }
      correctStatus and dataMatches
    }
    def fails = {
      val res = failEnv.sprayRoutingClient.makeRequest(HttpMethods.GET, "/stats", List(RawHeader("x-service-stats", "true")), None)
      // server actor not started, so this throws a 500
      val success = res.status must beEqualTo(StatusCodes.InternalServerError)
      success
    }
  }
}
