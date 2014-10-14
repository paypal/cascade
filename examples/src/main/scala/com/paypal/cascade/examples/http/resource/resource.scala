package com.paypal.cascade.examples.http

import com.paypal.cascade.akka.actor.ActorSystemWrapper
import com.paypal.cascade.http.resource.ResourceDriver
import com.paypal.cascade.http.server.SprayConfiguration
import spray.routing.Directives._

package object resource {
  val MyActorSystemWrapper = new ActorSystemWrapper("MyHttpService")
  private implicit val actorRefFactory = MyActorSystemWrapper.actorRefFactory

  val MySprayConfiguration  = SprayConfiguration("my-http-server", 8080, 5) {
    get {
      path("hello") {
        ResourceDriver.serve(MyHttpResource.apply, MyHttpResource.requestParser)
      }
    }
  }
}
