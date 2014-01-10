package com.paypal.stingray.http.resource

import spray.can.Http.GetStats
import spray.can.server.Stats
import spray.http._
import spray.http.ContentTypes
import spray.http.HttpEntity
import spray.routing._
import StatusCodes._
import com.paypal.stingray.common.json.JSONUtil._
import com.paypal.stingray.common.service.ServiceNameComponent
import com.paypal.stingray.common.values.{StaticValuesComponent, BuildStaticValues}
import com.paypal.stingray.http.server.StatusResponse
import scalaz.std.string._
import scalaz.syntax.std.boolean._
import scalaz.syntax.std.option._
import scalaz.Validation
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import net.liftweb.json.scalaz.JsonScalaz._
import scala.concurrent.duration._
import akka.actor.ActorSelection
import akka.pattern.ask

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 4/12/13
 * Time: 12:20 PM
 */
trait ResourceService extends HttpService with ResourceDriver {
  this: StaticValuesComponent with ServiceNameComponent =>

  protected implicit def ec: ExecutionContext

  protected lazy val bsvs = new BuildStaticValues(svs)

  def route: Route

  private lazy val statusResponse = toJSON(StatusResponse.getStatusResponse(bsvs, serviceName)).toWireFormat

  private lazy val statusRoute: Route = (path("status") & headerValue(_.is("x-stackmob-status").option(()))) { _ =>
    complete(HttpResponse(OK, HttpEntity(ContentTypes.`application/json`, statusResponse)))
  }

  private lazy val serverActor: ActorSelection = actorRefFactory.actorSelection("/user/IO-HTTP/listener-0")

  private lazy val statsRoute: Route = (path("stats") & headerValue(_.is("x-stackmob-stats").option(()))) { _ =>
    (ctx: RequestContext) => {
      import com.paypal.stingray.http.resource.StatsHelper._
      (serverActor ? GetStats)(1.second).mapTo[Stats].onComplete {
        case Success(stats) => ctx.complete(
          HttpResponse(OK, HttpEntity(ContentTypes.`application/json`, toJSON(stats).toWireFormat))
        )
        case Failure(t) => ctx.complete(
          HttpResponse(InternalServerError, HttpEntity(ContentTypes.`application/json`, toJSON(Map("errors" -> List(~Option(t.getMessage)))).toWireFormat))
        )
      }
    }
  }

  protected lazy val unsealedFullRoute = statusRoute ~ statsRoute ~ route

  lazy val fullRoute = sealRoute(unsealedFullRoute)

  /**
   * Run the request on this resource, first applying a rewrite
   */
  def serveWithRewrite[ParsedRequest, AuthInfo, PostBody, PutBody](resource: Resource[ParsedRequest, AuthInfo, PostBody, PutBody])(rewrite: HttpRequest => Validation[Throwable, (HttpRequest, Map[String, String])]): RequestContext => Unit = { ctx: RequestContext =>
    rewrite(ctx.request).map { case (request, pathParts) =>
      serve(resource, pathParts)(ctx.copy(request = request))
    } valueOr { e =>
      ctx.complete(HttpResponse(BadRequest, HttpEntity(ContentTypes.`application/json`, e.getMessage)))
    }
  }

  /**
   * Run the request on this resource
   */
  def serve[ParsedRequest, AuthInfo, PostBody, PutBody](resource: Resource[ParsedRequest, AuthInfo, PostBody, PutBody], pathParts: Map[String, String] = Map()): RequestContext => Unit = { ctx: RequestContext =>
    ctx.complete(serveSync(ctx.request, resource, pathParts))
  }

}
