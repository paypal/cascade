package com.paypal.stingray.http.actor

import akka.actor.{ActorRefFactory, ActorSystem}
import com.paypal.stingray.common.service.ServiceNameComponent
import scala.concurrent.ExecutionContext

/**
 * Created by ayakushev on 1/28/14.
 */
trait RootActorSystemComponent {
  self: ServiceNameComponent with RootActorPropsComponent =>

  implicit lazy val system = ActorSystem(serviceName)
  implicit lazy val actorRefFactory: ActorRefFactory = system
  implicit lazy val ec: ExecutionContext = system.dispatcher
  lazy val service = {
    sys.addShutdownHook(system.shutdown())
    system.actorOf(rootActorProps, serviceName)
  }
}
