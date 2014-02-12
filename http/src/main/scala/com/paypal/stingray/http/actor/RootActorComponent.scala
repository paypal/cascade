package com.paypal.stingray.http.actor

import akka.actor.Actor
import spray.routing.HttpService
import com.paypal.stingray.http.resource.ResourceService

/**
 * Created by Miles O'Connell.
 *
 * 2/11/14
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
