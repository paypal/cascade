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
package com.paypal.stingray.akka.actor

import akka.actor.{ActorRefFactory, ActorSystem}
import com.paypal.stingray.common.logging._
import com.paypal.stingray.common.service.ServiceNameComponent
import scala.concurrent.ExecutionContext

/**
 * Provides the root actor which supervises other actors and handles spray http requests.
 */
trait ActorSystemComponent {
  //Dependencies
  self: ServiceNameComponent =>

  /**
   * The default ActorSystem.
   * All components that depend on this one automatically inherit this.
   */
  implicit lazy val system = {
    val newSystem = ActorSystem(serviceName)
    sys.addShutdownHook {
      newSystem.shutdown()
      flushAllLogs()
    }
    newSystem
  }

  /**
   * The default [[akka.actor.ActorRefFactory]] (comes from system).
   * All components that depend on this one automatically inherit this.
   */
  implicit lazy val actorRefFactory: ActorRefFactory = system

  /**
   * The default [[scala.concurrent.ExecutionContext]] (comes from system).
   * All components that depend on this one automatically inherit this.
   */
  implicit lazy val ec: ExecutionContext = system.dispatcher

}
