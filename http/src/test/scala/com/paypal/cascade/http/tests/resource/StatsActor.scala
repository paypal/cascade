/**
 * Copyright 2013-2015 PayPal
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
package com.paypal.cascade.http.tests.resource

import java.util.concurrent.TimeUnit

import akka.actor.{ActorLogging, Actor}
import spray.can.Http
import spray.can.server.Stats

import scala.concurrent.duration.FiniteDuration

class StatsActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case Http.GetStats =>
      log.debug(s"received GetStats message from ${sender()}")
      sender ! StatsActor.statsObject
  }
}

object StatsActor {
  val statsObject = Stats(new FiniteDuration(1L, TimeUnit.SECONDS), 1L, 1L, 1L, 1L, 1L, 1L, 1L)
}
