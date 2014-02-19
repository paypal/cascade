package com.paypal.stingray.http.actor

import akka.actor.{Props, Actor}
import spray.routing.{RoutingSettings, RejectionHandler, ExceptionHandler, HttpService}
import com.paypal.stingray.http.resource.ResourceService
import com.paypal.stingray.common.service.ServiceNameComponent
import spray.util.LoggingContext

/**
 * Provides the root actor implementation used by spray
 */
trait SprayActorComponent {
  this: ActorSystemComponent with ResourceService with ServiceNameComponent =>

  /**
   * Service Provided
   * This is the actor which will serve spray requests
   */
  lazy val sprayActor = {
    sys.addShutdownHook(system.shutdown())
    system.actorOf(SprayActor.props, serviceName)
  }

  //lifting implicits
  //Why are these here? Because implicit scoping is not bringing them into the inner class here
  private val exceptionHandler = implicitly[ExceptionHandler]
  private val rejectionHandler = implicitly[RejectionHandler]
  private val routingSettings = implicitly[RoutingSettings]

  /**
   * Implementation
   * The root actor implementation used by spray
   */
  protected class SprayActor extends Actor with HttpService {
    override val actorRefFactory = context
    override def receive = {
      val loggingContext: LoggingContext = implicitly[LoggingContext]
      runRoute(fullRoute)(exceptionHandler, rejectionHandler, context, routingSettings, loggingContext)
    }
  }

  //companion object for props
  private object SprayActor {
    val props = Props[SprayActor]
  }
}
