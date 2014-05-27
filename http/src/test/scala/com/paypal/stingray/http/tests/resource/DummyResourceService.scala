package com.paypal.stingray.http.tests.resource

import com.paypal.stingray.common.service.ServiceNameComponent
import spray.routing.Directives._
import com.paypal.stingray.http.resource.ResourceHttpActor
import scala.concurrent.Future
import spray.http.{HttpRequest, HttpResponse}
import scala.util.Success
import com.paypal.stingray.http.resource.{ResourceServiceComponent, ResourceDriver}
import com.paypal.stingray.akka.actor.ActorSystemComponent

/**
 * A dummy resource service implementation for use with [[com.paypal.stingray.http.tests.resource.DummyResource]].
 * Only accepts requests to the "/ping" endpoint.
 */
trait DummyResourceService
  extends ServiceNameComponent
  with ResourceServiceComponent
  with ActorSystemComponent {

  /** This resource */
  val dummy = new DummyResource

  val processRequest: ResourceHttpActor.RequestProcessor[Unit] = { _: Unit =>
    Future.successful {
      HttpResponse() -> None
    }
  }

  val parseRequest: ResourceHttpActor.RequestParser[Unit] = { _ : HttpRequest =>
    Success(())
  }

  /** The route for this resource */
  override val route = {
    path("ping") {
      get {
        ResourceDriver.serve[Unit, Unit](dummy, processRequest, parseRequest)
      }
    }
  }

  override lazy val serviceName = "tests"

}
