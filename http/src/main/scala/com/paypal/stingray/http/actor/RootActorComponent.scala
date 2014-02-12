package com.paypal.stingray.http.actor

import akka.actor.Actor
import spray.routing.HttpService
import com.paypal.stingray.http.resource.ResourceService

/**
 * Created by ayakushev on 1/28/14.
 */

trait RootActorComponent {
  this: ResourceService =>

  class RootActor extends Actor with HttpService {
    override val actorRefFactory = context
//    implicitly[RoutingSettings](RoutingSettings.default)
//    implicitly[ExceptionHandler](ExceptionHandler.default)
    override def receive = runRoute(fullRoute)
  }
}
