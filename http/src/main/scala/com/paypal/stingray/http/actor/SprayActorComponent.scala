package com.paypal.stingray.http.actor

import akka.actor.{ActorRef, Props, Actor}
import spray.routing.{RoutingSettings, RejectionHandler, ExceptionHandler}
import com.paypal.stingray.http.resource.ResourceServiceComponent
import com.paypal.stingray.common.service.ServiceNameComponent
import spray.util.LoggingContext
import spray.can.Http
import akka.io.{IO => AkkaIO}
import com.paypal.stingray.http.server.SprayConfigurationComponent
import com.paypal.stingray.akka.actor.ActorSystemComponent
import spray.io.ServerSSLEngineProvider

/**
 * Provides the root actor implementation used by spray
 */
trait SprayActorComponent {
  this: ActorSystemComponent
    with ResourceServiceComponent
    with ServiceNameComponent
    with SprayConfigurationComponent =>

  /**
   * Service Provided
   * This is the actor which will serve spray requests
   */
  lazy val sprayActor: ActorRef = system.actorOf(SprayActor.props, serviceName)

  /**
   * Convenience method to start the spray actor
   * This should be called at startup by the application
   */
  def start(implicit sslEngineProvider: ServerSSLEngineProvider) {
    AkkaIO(Http) ! Http.Bind(sprayActor, interface = "0.0.0.0", port = port, backlog = backlog)
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
  protected class SprayActor extends Actor with ResourceService {
    override val actorRefFactory = context
    override def receive: Actor.Receive = {
      val loggingContext: LoggingContext = implicitly[LoggingContext]
      runRoute(fullRoute)(exceptionHandler, rejectionHandler, context, routingSettings, loggingContext)
    }
  }

  //companion object for props
  private object SprayActor {
    val props = Props(new SprayActor)
  }

}
