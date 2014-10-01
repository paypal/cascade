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
package com.paypal.cascade.akka.actor

import akka.actor.{ActorRefFactory, ActorSystem}
import com.paypal.cascade.common.logging._
import scala.concurrent.ExecutionContext

/**
 * Provides the root actor which supervises other actors and handles spray http
 * requests. Use this wrapper as input anywhere you need access to an `ActorSystem`, `ActorRefFactory`, or
 * `ExecutionContext`. Since an app generally has one `ActorSystem`, treat this wrapper like a singleton -
 * create it on app startup and pass it to everywhere you need it.
 */

class ActorSystemWrapper(serviceName: String) {

  /**
   * the [[akka.actor.ActorSystem]] that this wrapper wraps.
   * the system will be configured to shut down gracefully on JVM shutdown.
   */
  lazy val system = {
    val s = ActorSystem(serviceName)
    sys.addShutdownHook {
      s.shutdown()
      flushAllLogs()
    }
    s
  }

  /**
   * The default [[akka.actor.ActorRefFactory]]
   * comes from the implicit `system`.
   */
  lazy val actorRefFactory: ActorRefFactory = system

  /**
   * The default [[scala.concurrent.ExecutionContext]].
   * comes from the implicit `system`.
   */
  lazy val executionContext: ExecutionContext = system.dispatcher
}

