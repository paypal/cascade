package com.paypal.stingray.http.resource

import spray.can.Http.GetStats
import spray.can.server.Stats
import spray.http._
import spray.http.ContentTypes
import spray.http.HttpEntity
import spray.http.StatusCodes._
import spray.routing._
import com.paypal.stingray.common.option._
import com.paypal.stingray.common.service.ServiceNameComponent
import com.paypal.stingray.common.values.{StaticValuesComponent, BuildStaticValues}
import com.paypal.stingray.common.json._
import com.paypal.stingray.http.server.StatusResponse
import scala.concurrent.ExecutionContext
import scala.util.{Try, Failure, Success}
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

  private lazy val statusResponse = StatusResponse.getStatusResponse(bsvs, serviceName)

  private lazy val statusError = "{status:error}"

  private lazy val statusRoute: Route = path("status") { _ =>
    val statusRespJson = JsonUtil.toJson(statusResponse).getOrElse(statusError)
    complete(HttpResponse(OK, HttpEntity(ContentTypes.`application/json`, statusRespJson)))
  }

  private lazy val serverActor: ActorSelection = actorRefFactory.actorSelection("/user/IO-HTTP/listener-0")

  private lazy val statsError = "{stats:error}"

  private lazy val statsRoute: Route = path("stats") { _ =>
    (ctx: RequestContext) => {
      (serverActor ? GetStats)(1.second).mapTo[Stats].onComplete {
        case Success(stats) => {
          val statsRespJson = JsonUtil.toJson(stats).getOrElse(statsError)
          ctx.complete(HttpResponse(OK, HttpEntity(ContentTypes.`application/json`, statsRespJson)))
        }
        case Failure(t) => {
          val statsFailureJson = JsonUtil.toJson(Map("errors" -> List(Option(t.getMessage).getOrElse("")))).getOrElse(statsError)
          ctx.complete(HttpResponse(InternalServerError, HttpEntity(ContentTypes.`application/json`, statsFailureJson)))
        }
      }
    }
  }

  protected lazy val unsealedFullRoute = statusRoute ~ statsRoute ~ route

  lazy val fullRoute = sealRoute(unsealedFullRoute)

  /**
   * Run the request on this resource, first applying a rewrite
   */
  def serveWithRewrite[ParsedRequest, AuthInfo, PostBody, PutBody]
  (resource: Resource[ParsedRequest, AuthInfo, PostBody, PutBody])
  (rewrite: HttpRequest => Try[(HttpRequest, Map[String, String])]): RequestContext => Unit = { ctx: RequestContext =>
    rewrite(ctx.request).map { case (request, pathParts) =>
      serve(resource, pathParts)(ctx.copy(request = request))
    }.recover {
      case e: Throwable =>
        ctx.complete(HttpResponse(BadRequest, HttpEntity(ContentTypes.`application/json`, e.getMessage)))
    }
  }

  /**
   * Run the request on this resource
   */
  def serve[ParsedRequest, AuthInfo, PostBody, PutBody]
  (resource: Resource[ParsedRequest, AuthInfo, PostBody, PutBody],
   pathParts: Map[String, String] = Map()): RequestContext => Unit = { ctx: RequestContext =>
    ctx.complete(serveSync(ctx.request, resource, pathParts))
  }

}
