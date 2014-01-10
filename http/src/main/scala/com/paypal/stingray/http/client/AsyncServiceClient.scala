package com.paypal.stingray.http.client

import com.stackmob.newman.dsl.Protocol
import com.stackmob.newman.{SprayHttpClient, HttpClient}
import java.nio.charset.Charset
import com.stackmob.newman.Constants._
import com.paypal.stingray.http.request._
import com.stackmob.newman.request.HttpRequest
import com.stackmob.newman.response.HttpResponse
import java.lang.Exception
import scalaz.Scalaz._
import spray.http.Uri.{Host, Authority}
import scala.concurrent.Future
import com.paypal.stingray.common.validation.ThrowableValidation
import com.paypal.stingray.concurrent.future.sequentialExecutionContext
import com.paypal.stingray.common.logging.LoggingSugar
import com.paypal.stingray.common.stats.StatsD
import akka.actor.ActorSystem

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.http.client
 *
 * User: aaron
 * Date: 6/27/13
 * Time: 6:23 PM
 */
class AsyncServiceClient(override val targetService: String,
                         override val protocol: Protocol,
                         override val host: String,
                         override val port: Int,
                         override val httpClient: HttpClient = new SprayHttpClient(ActorSystem("service-client")),
                         override val charset: Charset = UTF8Charset)(implicit override val statsD: StatsD)
  extends AsyncBaseClient(targetService, protocol, host, port, httpClient, charset)
  with LoggingSugar {

  private lazy val logger = getLogger[AsyncServiceClient]

  implicit private val seqCtx = sequentialExecutionContext(logger)

  import AsyncServiceClient._

  private def toSprayException(error: InternalRequestToHttpRequestError): Throwable = {
    InternalRequestToHttpRequestException(error)
  }

  def sendRequest(req: InternalRequest): Future[ThrowableValidation[HttpResponse]] = {
    val url = genURL(Nil)
    val newReq = req.copy(underlying = req.underlying.copy(uri = req.underlying.uri.copy(authority = Authority(Host(url.getHost), url.getPort))))
    newReq.toNewmanRequest(httpClient).fold(
      succ = { req: HttpRequest =>
        req.apply.map { resp =>
          resp.success[Throwable]
        }
      },
      fail = { t =>
        val ex = toSprayException(t)
        Future.successful(ex.fail[HttpResponse])
      }
    )
  }
}

object AsyncServiceClient {
  private[AsyncServiceClient] case class InternalRequestToHttpRequestException(error: InternalRequestToHttpRequestError) extends Exception(error.reason)
}
