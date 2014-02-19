package com.paypal.stingray.http.actor

import akka.actor.{Props, ActorRefFactory, ActorSystem}
import com.paypal.stingray.common.service.ServiceNameComponent
import scala.concurrent.ExecutionContext
import com.paypal.stingray.http.resource.ResourceService

/**
 * Provides the root actor which supervises other actors and handles spray http requests
 */
trait ActorSystemComponent {
  //Dependencies
  self: ResourceService with ServiceNameComponent =>

  //Implicits provided
  implicit lazy val system = ActorSystem(serviceName)
  implicit lazy val actorRefFactory: ActorRefFactory = system
  implicit lazy val ec: ExecutionContext = system.dispatcher

}
