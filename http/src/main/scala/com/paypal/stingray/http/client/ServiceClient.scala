package com.paypal.stingray.http.client

import com.paypal.stingray.common.validation._
import java.nio.charset.Charset
import com.stackmob.newman.{SprayHttpClient, HttpClient, ApacheHttpClient}
import com.stackmob.newman.Constants._
import com.stackmob.newman.dsl._
import scala.concurrent.Await
import com.stackmob.newman.response.HttpResponse
import com.paypal.stingray.http.request.InternalRequest
import scala.concurrent.duration.Duration
import com.paypal.stingray.common.stats.StatsD
import akka.actor.ActorSystem

/**
 * A client capable of accepting proxied InternalRequests. Can be hooked up to optimus as a top level service
 * User: drapp
 * Date: 11/18/12
 * Time: 10:13 PM
 */

class ServiceClient(override val targetService: String,
                    override val protocol: Protocol,
                    override val host: String,
                    override val port: Int,
                    override val httpClient: HttpClient = new SprayHttpClient(ActorSystem("service-client")),
                    override val charset: Charset = UTF8Charset)
                   (implicit override val statsD: StatsD)
  extends BaseClient(targetService, protocol, host, port, httpClient, charset) {

  private lazy val asyncClient = new AsyncServiceClient(targetService, protocol, host, port, httpClient, charset)

  /**
   * send an {{{InternalRequest}}} to an internal service
   * @param req the request to send
   * @param dur the maximum amount of time to wait for the service to return
   * @return the response, wrapped in an IO
   */
  def sendRequest(req: InternalRequest,
                  dur: Duration = Duration.Inf): ThrowableValidation[HttpResponse] = {
    Await.result(asyncClient.sendRequest(req), dur)
  }
}
