package com.paypal.stingray.http.actor

import akka.actor.{Props, Actor, ActorRefFactory, ActorSystem}
import com.paypal.stingray.common.service.ServiceNameComponent
import scala.concurrent.ExecutionContext
import com.paypal.stingray.http.resource.ResourceService
import spray.routing.HttpService

/**
 * This component provides the root actor system for a spray service
 */
trait RootActorSystemComponent {
  //Dependencies
  self: ResourceService with ServiceNameComponent with RootActorComponent =>

  //Implicits provided
  implicit lazy val system = ActorSystem(serviceName)
  implicit lazy val actorRefFactory: ActorRefFactory = system
  implicit lazy val ec: ExecutionContext = system.dispatcher

  /**
   * The root actor which supervises other actors and handles spray http requests
   */
  //Service Provided
  lazy val service = {
    sys.addShutdownHook(system.shutdown())
    system.actorOf(rootActorProps, serviceName)
  }

  private val rootActorProps = Props[RootActor](new RootActor)
}
