package com.paypal.stingray.http.tests.resource

import scala.concurrent.ExecutionContext
import org.specs2.Specification
import spray.testkit.Specs2RouteTest
import java.util.concurrent.Executors
import akka.dispatch.ExecutionContexts
import com.paypal.stingray.http.tests.client.SprayRoutingHttpClient

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 4/4/13
 * Time: 5:35 PM
 */
class DummyClientSpecs extends Specification with Specs2RouteTest { override def is =
  "DummyClientSpecs".title                                                                                              ^
    """
    Tests the stackmob-resource framework, along with its integration with Newman clients for specs
    """                                                                                                                 ^
    "a simple ping should work"                                                                                         ! Test().succeeds ^
                                                                                                                        end

  case class Test() extends Context {
    def succeeds = client.ping.toEither must beRight.like {
      case s => s must beEqualTo("pong")
    }
  }

  class DummyHttpClient extends SprayRoutingHttpClient with DummyResourceService {
    override lazy val actorRefFactory = system
    override lazy val actorSystem = system
    override lazy val ec: ExecutionContext = ExecutionContexts.fromExecutor(Executors.newCachedThreadPool())
  }

  trait Context {
    val client = new DummyClient(httpClient = new DummyHttpClient)
  }

}
