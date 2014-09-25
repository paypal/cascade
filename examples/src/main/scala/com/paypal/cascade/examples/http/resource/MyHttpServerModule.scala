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
package com.paypal.cascade.examples.http.resource

import com.paypal.cascade.akka.actor.ActorSystemComponent
import com.paypal.cascade.common.service.ServiceNameComponent
import com.paypal.cascade.http.actor.SprayActorComponent
import com.paypal.cascade.http.resource.{ResourceDriver, ResourceServiceComponent}
import com.paypal.cascade.http.server.SprayConfigurationComponent
import spray.routing.Directives._

/**
 * ServerModule is the generic setup configuration for your app.
 * As you see below, MyHttpServerModule is the production configuration for your app.
 * In the future, you may extend this to implement a different production configuration,
 * various testing configurations, or some other configuration.
 */
trait ServerModule
  extends SprayActorComponent
  with ActorSystemComponent
  with ServiceNameComponent
  with SprayConfigurationComponent
  with ResourceServiceComponent

/**
 * This is the default production configuration for your application.
 * As you can see, it has some static configuration values (which you might take from a
 * configuration file or other configuration system on startup)  and the endpoints for your HTTP server.
 * Make sure all values in this object are marked lazy, to avoid trait initialization issues
 */
object MyHttpServerModule extends ServerModule {
  /**
   * the service name for this HTTP server. This service name and some other
   * server status and health information will be exposed in a special endpoint by
   * default. To see that data, do a GET on the /status endpoint with the "x-service-status" header
   */
  override lazy val serviceName = "my-http-server"
  /**
   * the port that this HTTP server should bind to
   */
  override lazy val port = 8080
  /**
   * the number of connections spray should accept in its backlog before resetting connections.
   * this value helps to throttle connections in the event that you have many concurrently.
   */
  override lazy val backlog = 5
  /**
   * the HTTP endpoints that you want to expose. In this case, we are serving only GET /hello.
   */
  override lazy val route = get {
    path("hello") {
      ResourceDriver.serve(MyHttpResource.apply, MyHttpResource.requestParser)
    }
  }

}
