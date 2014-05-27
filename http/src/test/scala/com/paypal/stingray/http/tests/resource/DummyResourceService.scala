package com.paypal.stingray.http.tests.resource

import com.paypal.stingray.common.service.ServiceNameComponent
import spray.routing.Directives._
import com.paypal.stingray.http.resource.{AbstractResourceActor, ResourceHttpActor, ResourceServiceComponent, ResourceDriver}
import scala.concurrent.Future
import spray.http.{HttpRequest, HttpResponse}
import scala.util.{Try, Success}
import com.paypal.stingray.akka.actor.ActorSystemComponent
import akka.actor.{ActorRef, Props}
import com.paypal.stingray.http.tests.resource.DummyResource.GetRequest
import com.paypal.stingray.common.trys._

/**
 * A dummy resource service implementation for use with [[com.paypal.stingray.http.tests.resource.DummyResource]].
 * Only accepts requests to the "/ping" endpoint.
 */
trait DummyResourceService
  extends ServiceNameComponent
  with ResourceServiceComponent
  with ActorSystemComponent {

  /** This resource */
  val dummy: ActorRef => AbstractResourceActor = new DummyResource(_)

  val parseRequest: ResourceHttpActor.RequestParser[GetRequest] = { _ : HttpRequest =>
    Try (GetRequest("bar"))
  }

  /** The route for this resource */
  override val route = {
    path("ping") {
      get {
        ResourceDriver.serve(dummy, parseRequest)
      }
    }
  }

  override lazy val serviceName = "tests"

}
