package com.paypal.stingray.http.actor

import akka.actor.Actor
import spray.routing.HttpService
import com.paypal.stingray.http.resource.ResourceService

/**
 * Provides the root actor implementation used by spray
 */
class RootActorComponent {
  this: ResourceService =>

  /**
   * The root actor implementation used by spray
   */
  protected class RootActor extends Actor with HttpService {
    override val actorRefFactory = context
    override def receive = runRoute(fullRoute)
  }
}
