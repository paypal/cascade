/**
 * Copyright 2013-2015 PayPal
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

import java.util.concurrent.TimeUnit

import spray.can.server.ServerSettings

import scala.concurrent.Future

import akka.actor.{Actor, Props}
import akka.io.{IO => AkkaIO}
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http
import spray.io.ServerSSLEngineProvider
import spray.routing.{ExceptionHandler, RejectionHandler, RoutingSettings}
import spray.util.LoggingContext

import com.paypal.cascade.akka.actor.ActorSystemWrapper
import com.paypal.cascade.http.resource.ResourceService
import com.paypal.cascade.http.server.SprayConfiguration

/**
 * The root actor implementation used by spray
 */
class SprayActor(override val sprayConfig: SprayConfiguration,
                 override val actorSystemWrapper: ActorSystemWrapper) extends Actor with ResourceService {
  //lifting implicits so we can pass them explicitly to runRoute below
  private[this] val exceptionHandler = implicitly[ExceptionHandler]
  private[this] val rejectionHandler = implicitly[RejectionHandler]
  private[this] val routingSettings = implicitly[RoutingSettings]

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
   * @return a Future[Any] that will contain Http.Bound if the server successfully started.
   *         see http://spray.io/documentation/1.2.2/spray-can/http-server/ under
   *         "starting and stopping" for details on failure modes
   */
  def start(systemWrapper: ActorSystemWrapper,
            sprayConfig: SprayConfiguration,
            serverSettings: Option[ServerSettings] = None)
           (implicit sslEngineProvider: ServerSSLEngineProvider,
            timeout: Timeout): Future[Http.Event] = {
    //used for AkkaIO(...)
    implicit val actorSystem = systemWrapper.system

    val sprayActorProps = Props(new SprayActor(sprayConfig, systemWrapper))
    val sprayActor = systemWrapper.system.actorOf(sprayActorProps)
    val bindMsg = Http.Bind(sprayActor,
      interface = "0.0.0.0",
      port = sprayConfig.port,
      backlog = sprayConfig.backlog,
      settings = serverSettings)
    (AkkaIO(Http) ? bindMsg).mapTo[Http.Event]
  }
}
