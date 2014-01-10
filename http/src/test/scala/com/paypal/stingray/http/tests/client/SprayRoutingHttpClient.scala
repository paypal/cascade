package com.paypal.stingray.http.tests.client

import scalaz._
import Scalaz._
import com.stackmob.newman._
import com.stackmob.newman.request._
import com.stackmob.newman.response.{HttpResponse => NewmanResponse, HttpResponseCode}
import java.net.URL
import com.paypal.stingray.http.resource.ResourceService
import spray.http._
import spray.http.HttpEntity._
import spray.http.{HttpRequest => SprayHttpRequest}
import akka.actor.{ActorSystem, Actor}
import parser.HttpParser
import spray.http.HttpHeaders.RawHeader
import spray.http.HttpResponse
import akka.testkit.TestActorRef
import java.util.concurrent.{TimeUnit, CountDownLatch}
import com.paypal.stingray.common.option._
import com.stackmob.tests.common.actor.StubbedService
import spray.routing.RequestContext
import com.paypal.stingray.http.tests.TestServiceNameComponent
import com.paypal.stingray.common.values.StaticValuesFromServiceNameComponent
import scala.concurrent.Future

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 4/2/13
 * Time: 5:32 PM
 */

trait SprayRoutingHttpClient extends HttpClient with StubbedService with TestServiceNameComponent with StaticValuesFromServiceNameComponent {
  this: ResourceService =>

  implicit def actorSystem: ActorSystem

  // A stupid little actor that just waits for a response
  class RequestRunner extends Actor {
    val latch: CountDownLatch = new CountDownLatch(1)
    var response: Option[HttpResponse] = none

    def runRequest(m: HttpMethod, u: URL, h: Headers, body: Option[RawBody] = none): NewmanResponse = {
      val (errors, parsedHeaders: List[HttpHeader]) = HttpParser.parseHeaders(h.list.map(x => RawHeader(x._1, x._2)))
      if (errors.length > 0) throw new IllegalStateException("Invalid headers: " + errors.map(_.formatPretty).mkString("\n"))
      val req = SprayHttpRequest(method = m, uri = u.toString, headers = parsedHeaders)
      val reqWithBody = body some { b => req.withEntity(HttpEntity(ContentTypes.`application/json`, new String(b, "UTF-8"))) } none req
      fullRoute(RequestContext(reqWithBody, self, req.uri.path))
      latch.await(10, TimeUnit.SECONDS)
      val resp = response orThrow new IllegalStateException("Request timed out")
      val cTypeHeader = resp.entity.some.collect {
        case NonEmpty(cType, _) => "Content-Type" -> cType.value
      }
      val headers = cTypeHeader ++ resp.headers.map(header => (header.name, header.value))
      NewmanResponse(HttpResponseCode.fromInt(resp.status.intValue).get, headers.toList.toNel, resp.entity.data.toByteArray)
    }

    def receive = {
      case resp: HttpResponse => {
        response = resp.some
        latch.countDown()
      }
    }
  }

  def onRequestRunner(f: RequestRunner => NewmanResponse): Future[NewmanResponse] = {
    Future.successful {
      f(TestActorRef(new RequestRunner).underlyingActor)
    }
  }

  override def get(u: URL, h: Headers) = GetRequest(u, h) {
    onRequestRunner(_.runRequest(HttpMethods.GET, u, h))
  }

  override def post(u: URL, h: Headers, b: RawBody) = PostRequest(u, h, b) {
    onRequestRunner(_.runRequest(HttpMethods.POST, u, h, b.some))
  }

  override def put(u: URL, h: Headers, b: RawBody) = PutRequest(u, h, b) {
    onRequestRunner(_.runRequest(HttpMethods.PUT, u, h, b.some))
  }

  override def delete(u: URL, h: Headers) = DeleteRequest(u, h) {
    onRequestRunner(_.runRequest(HttpMethods.DELETE, u, h))
  }

  override def head(u: URL, h: Headers) = HeadRequest(u, h) {
    onRequestRunner(_.runRequest(HttpMethods.HEAD, u, h))
  }

}
