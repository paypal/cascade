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

import com.paypal.cascade.akka.actor.ActorSystemWrapper
import com.paypal.cascade.common.app.CascadeApp
import com.paypal.cascade.http.actor.SprayActor
import com.paypal.cascade.http.resource.ResourceDriver
import com.paypal.cascade.http.server.SprayConfiguration
import spray.routing.Directives._

/**
 * MyHttpServer is the entrypoint to your HTTP server.
 * Make sure to extend CascadeApp, because it sets up some
 * useful logging and exception handling logic for you.
 */
object MyHttpServer extends CascadeApp {
  val MyActorSystemWrapper = new ActorSystemWrapper("MyHttpService")

  private implicit val actorRefFactory = MyActorSystemWrapper.actorRefFactory

  val MySprayConfiguration  = SprayConfiguration("my-http-server", 8080, 5) {
    get {
      path("hello") {
        ResourceDriver.serve(MyHttpResource.apply, MyHttpResource.requestParser)
      }
    }
  }

  SprayActor.start(MyActorSystemWrapper, MySprayConfiguration)
}
