package com.paypal.stingray.http.client

import com.stackmob.newman._
import spray.http.HttpHeaders.{`Content-Type`, `Accept-Charset`, Accept}
import spray.http.{ContentTypes, HttpCharsets, MediaTypes}
import com.paypal.stingray.common.logging.LoggingSugar
import com.paypal.stingray.http.headers._
import java.nio.charset.Charset
import com.stackmob.newman.Constants._
import java.net.{URL, InetAddress}
import com.twitter.util.Duration
import java.util.concurrent.TimeUnit
import com.stackmob.newman.dsl._
import com.paypal.stingray.common.stats.{StatsDStat, StatsD}

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.http.client
 *
 * User: aaron
 * Date: 7/2/13
 * Time: 4:54 PM
 */
trait BaseClientCommon extends LoggingSugar {

  protected val acceptHeader: Header = Accept(MediaTypes.`application/json`).toNewmanHeader
  protected val acceptCharsetHeader: Header = `Accept-Charset`(HttpCharsets.`UTF-8`).toNewmanHeader
  protected val contentTypeHeader: Header = `Content-Type`(ContentTypes.`application/json`).toNewmanHeader

  protected lazy val defaultStat: StatsDStat = new StatsDStat(s"$targetService.client.timing") {}


  /**
   * the service that this client talks to
   * @return the service name
   */
  def targetService: String

  /**
   * the http protocol over which this service communicates
   * @return http or https
   */
  def protocol: Protocol

  /**
   * the host on which this service listens
   * @return the hostname
   */
  def host: String

  /**
   * the port on which this service listens
   * @return the port
   */
  def port: Int
  /**
   * the http client that should be used to talk to this service
   * @return
   */
  def httpClient: HttpClient

  /**
   * the charset that should be used to encode/decode http bodies
   * @return the charset
   */
  def charset: Charset = UTF8Charset

  protected lazy val currentHost = InetAddress.getLocalHost.toString

  /**
   * the time after which a warning is issued
   */
  protected lazy val warningExecutionTime = Duration.fromTimeUnit(1, TimeUnit.SECONDS)

  /**
   * the time after which an error is issued
   * TODO: cancel the network request after this time, and do a fail over action
   */
  protected lazy val failureExecutionTime = Duration.fromTimeUnit(2, TimeUnit.SECONDS)

  /**
   * the time after which all requests log a debug statement
   */
  protected lazy val debugExecutionTime = Duration.fromTimeUnit(500, TimeUnit.MILLISECONDS)

  //implicits for various newman operations, such as the DSL, etc...
  //the ctor arguments are not marked as implicit b/c various ppl who know more about
  //the scala language & compiler than me (Aaron, 6/6/2012) say that it's bad practice to do that
  protected implicit val useHttpClient = httpClient
  protected implicit val useCharset = charset

  /**
   * generate a URL from a list of path elements and a list of query key/value pairs
   * @param path the path elements
   * @param queryStringElts the query string elements
   * @return the generated URL
   */
  protected def genURL(path: List[String], queryStringElts: List[(String, String)] = Nil): URL = {
    QueryStringBuilder(protocol, host, port, Path(path), queryStringElts)
  }

  /**
   * generate a query-string-less URL from path elements
   * @param path the path elements
   * @return the generated URL
   */
  protected def genURL(path: String*): URL = genURL(path.toList)

  protected def statsD: StatsD
}
