# Writing an HTTP Server

This document will walk you through building an HTTP server on top of Spray,
Akka and Cascade. All of the code examples are based on our
[working HTTP server example](/examples/src/main/scala/com/paypal/cascade/examples/http/resource/)

## Routes
Your HTTP server must have 1 or more routes (`GET /hello`, for example). You'll
define your routes in a Cake pattern **module**, which will also include
`SprayActorComponent`, `ActorSystemComponent`, `ServiceNameComponent`,
`SprayConfigurationComponent`, and `ResourceServiceComponent`.

Create a parent module called `ServerModule` that defines all of those other
components and then a single child module that implements your production server.

You'll define your routes in your child module(s), and each route should
call `ResourceDriver.serve(...)`.

Example:

```scala
trait ServerModule
  extends SprayActorComponent
  with ActorSystemComponent
  with ServiceNameComponent
  with SprayConfigurationComponent
  with ResourceServiceComponent

object ProductionServerModule extends ServerModule {
  override lazy val serviceName = ???
  override lazy val port = ???
  override lazy val backlog = ???

  //you can use spray-routing or your own logic here. Call
  //ResourceDriver.serve(yourResource, ...) to return the
  //RequestContext => Unit that implements your functionality.
  override lazy val route: RequestContext => Unit = get {
    path("hello") {
      ResourceDriver.serve({ resourceContext =>
        new MyResource(resourceContext)
      }, { req =>
        parseRequest(req)
      })
    }
  }
}
```

See the [example `ServerModule`](/examples/src/main/scala/com/paypal/cascade/examples/http/resource/MyHttpServerModule.scala)
for working code.

## Resource
You defined the HTTP routes in your module, and you implement them in the
resource. A resource is a class that extends `AbstractResourceActor` and
defines a `resourceReceive` method, which should accept `ProcessRequest`
messages to process requests. The values inside those messages will be the
parsed values that `ResourceDriver` produced (see below). Example:

```scala
class MyResource(ctx: ResourceContext) extends AbstractResourceActor(ctx) {
    override def resourceReceive = {
        //processing a request that was not parsed at all
        case ProcessRequest(req: HttpRequest) => completeToJSON(StatusCodes.OK, "hello world!")
        //processing a request that was parsed by the ResourceDriver
        case ProcessRequest(req: GetHelloWorld) => completeToJSON(StatusCodes.OK, "hello world 2!")
    }
}
```

## Entry Point

The last and simplest part will bind to the right port and run the server when
the JVM starts. That code simply looks like the following:

```scala
object MyHttpServer extends CascadeApp {
  ProductionServerModule.start

}
```

A few notes:

- Make sure your `object` extends `CascadeApp`.
- The `ProductionServerModule.start` method is defined on `SprayActorComponent`,
which is mixed in by `ServerModule`.
