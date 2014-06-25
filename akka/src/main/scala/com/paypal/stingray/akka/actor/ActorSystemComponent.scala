package com.paypal.stingray.akka.actor

import akka.actor.{ActorRefFactory, ActorSystem}
import com.paypal.stingray.common.logging._
import com.paypal.stingray.common.service.ServiceNameComponent
import scala.concurrent.ExecutionContext

/**
 * Provides the root actor which supervises other actors and handles spray http requests
 */
trait ActorSystemComponent {
  //Dependencies
  self: ServiceNameComponent =>

  //Implicits provided
  implicit lazy val system = {
    val newSystem = ActorSystem(serviceName)
    sys.addShutdownHook {
      newSystem.shutdown()
      flushAllLogs()
    }
    newSystem
  }

  implicit lazy val actorRefFactory: ActorRefFactory = system
  implicit lazy val ec: ExecutionContext = system.dispatcher

}
