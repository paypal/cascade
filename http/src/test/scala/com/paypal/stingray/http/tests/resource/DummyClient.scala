package com.paypal.stingray.http.tests.resource

import com.stackmob.newman.{HttpClient, ApacheHttpClient}
import com.stackmob.newman.dsl._
import com.stackmob.newman.Constants.UTF8Charset
import java.nio.charset.Charset
import scalaz._
import Scalaz._
import com.paypal.stingray.http.client._
import com.paypal.stingray.common.stats.StatsD
import com.stackmob.tests.common.stats.DummyStatsD
import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration._
import com.paypal.stingray.concurrent.future.sequentialExecutionContext
import com.paypal.stingray.common.logging.LoggingSugar

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 4/4/13
 * Time: 9:05 PM
 */
class DummyClient(override val protocol: Protocol = http,
                  override val host: String = "localhost",
                  override val port: Int = 0,
                  override val statsD: StatsD = new DummyStatsD(),
                  override val httpClient: HttpClient = new ApacheHttpClient,
                  override val charset: Charset = UTF8Charset)
  extends BaseClient("dummy", protocol, host, port, httpClient, charset)(statsD) with LoggingSugar {

  private lazy val logger = getLogger[DummyClient]
  private implicit lazy val seqCtx: ExecutionContext = sequentialExecutionContext(logger)

  def ping: Validation[Throwable, String] = {
    lazy val fut = GET(genURL(List("ping"), List(("foo", "bar"))))
      .addHeaders(("Accept", "text/plain"))
      .apply.map { resp =>
        resp.bodyString().success[Throwable]
      }
    Await.result(fut, 1.second)
  }
}
