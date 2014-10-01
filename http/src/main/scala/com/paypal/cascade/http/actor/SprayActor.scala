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

import akka.actor.{Props, Actor}
import spray.routing.{Route, RoutingSettings, RejectionHandler, ExceptionHandler}
import com.paypal.cascade.http.resource.ResourceService
import spray.util.LoggingContext
import spray.can.Http
import akka.io.{IO => AkkaIO}
import com.paypal.cascade.http.server.SprayConfiguration
import com.paypal.cascade.akka.actor.ActorSystemWrapper
import spray.io.ServerSSLEngineProvider

/**
 * Implementation
 * The root actor implementation used by spray
 */
class SprayActor(override val config: SprayConfiguration,
                 override val actorSystemWrapper: ActorSystemWrapper) extends Actor with ResourceService {
  //lifting implicits so we can pass them explicitly to runRoute below
  private val exceptionHandler = implicitly[ExceptionHandler]
  private val rejectionHandler = implicitly[RejectionHandler]
  private val routingSettings = implicitly[RoutingSettings]

  override val actorRefFactory = context

  override def receive: Actor.Receive = {
    val loggingContext: LoggingContext = implicitly[LoggingContext]
    runRoute(fullRoute)(exceptionHandler, rejectionHandler, context, routingSettings, loggingContext)
  }
}

//companion object for creating the spray actor
object SprayActor {
  /**
   * Convenience method to start the spray actor.
   * This should be called at startup by the application.
   *
   * @param sslEngineProvider the SSL provider to be used by this spray service. A sane default is provided. See
   *                          <a href="http://spray.io/documentation/1.2.1/spray-can/http-server/#ssl-support">
   *                            spray-can HTTP Server SSL support</a>.
   */
  def start(systemWrapper: ActorSystemWrapper,
            config: SprayConfiguration)
           (implicit sslEngineProvider: ServerSSLEngineProvider): Unit = {
    //used for AkkaIO(...)
    implicit val actorSystem = systemWrapper.system

    val sprayActorProps = Props(new SprayActor(config, systemWrapper))
    val sprayActor = systemWrapper.system.actorOf(sprayActorProps)

    AkkaIO(Http) ! Http.Bind(sprayActor, interface = "0.0.0.0", port = config.port, backlog = config.backlog)
  }
}

