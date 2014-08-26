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
package com.paypal.cascade.http.actor

import akka.actor.{ActorRef, Props, Actor}
import spray.routing.{RoutingSettings, RejectionHandler, ExceptionHandler}
import com.paypal.cascade.http.resource.ResourceServiceComponent
import com.paypal.cascade.common.service.ServiceNameComponent
import spray.util.LoggingContext
import spray.can.Http
import akka.io.{IO => AkkaIO}
import com.paypal.cascade.http.server.SprayConfigurationComponent
import com.paypal.cascade.akka.actor.ActorSystemComponent
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
   * Convenience method to start the spray actor.
   * This should be called at startup by the application.
   *
   * @param sslEngineProvider the SSL provider to be used by this spray service. A sane default is provided. See
   *                          <a href="http://spray.io/documentation/1.2.1/spray-can/http-server/#ssl-support">
   *                            spray-can HTTP Server SSL support</a>.
   */
  def start(implicit sslEngineProvider: ServerSSLEngineProvider): Unit = {
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
