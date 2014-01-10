package com.paypal.stingray.http.resource

import spray.can.server.Stats
import net.liftweb.json.scalaz.JsonScalaz._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._

/**
 * Created by taylor on 9/11/13.
 */

object StatsHelper {
  implicit val statsJSONW = new JSONW[Stats] {
    override def write(stats: Stats): JValue = {
      ("uptime" -> stats.uptime.toString) ~
      ("total-requests" -> stats.totalRequests) ~
      ("open-requests" -> stats.openRequests) ~
      ("max-open-requests" -> stats.maxOpenRequests) ~
      ("total-connections" -> stats.totalConnections) ~
      ("open-connections" -> stats.openConnections) ~
      ("max-open-connections" -> stats.maxOpenConnections) ~
      ("request-timeouts" -> stats.requestTimeouts)
    }
  }
}
