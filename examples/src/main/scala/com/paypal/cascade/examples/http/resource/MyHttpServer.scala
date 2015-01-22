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

import java.util.concurrent.TimeUnit

import com.paypal.cascade.akka.actor.ActorSystemWrapper
import com.paypal.cascade.common.app.CascadeApp
import com.paypal.cascade.http.actor.SprayActor
import com.paypal.cascade.http.resource.ResourceDriver
import com.paypal.cascade.http.server.SprayConfiguration
import spray.routing.Directives._
import akka.util.Timeout

/**
 * MyHttpServer is the entrypoint to your HTTP server.
 * Make sure to extend CascadeApp, because it sets up some
 * useful logging and exception handling logic for you.
 */
object MyHttpServer extends CascadeApp {
  val systemWrapper = new ActorSystemWrapper("MyHttpService")

  val timeoutSecs = 10
  val port = 8080
  val backlog = 5

  private implicit val actorRefFactory = systemWrapper.actorRefFactory
  private implicit val timeout = new Timeout(timeoutSecs, TimeUnit.SECONDS)
  val config  = SprayConfiguration("my-http-server", port, backlog) {
    get {
      path("hello") {
        ResourceDriver.serve(MyHttpResource.apply, MyHttpResource.requestParser)
      }
    }
  }

  SprayActor.start(systemWrapper, config)
}
