# Writing an HTTP Server

This document will walk you through building an HTTP server on top of Spray,
Akka and Cascade. All of the code examples are based on our
[working HTTP server example](/examples/src/main/scala/com/paypal/cascade/examples/http/resource/).

For more details on how Cascade's HTTP support works, see
[HTTP_RESOURCE.md](HTTP_RESOURCE.md).

## Resources
A resource implements handlers for incoming requests. Each resource is a class
that extends `AbstractResourceActor` (which eventually extends Akka `Actor`),
that implements a `resourceReceive` method.

`resourceReceive` is similar to the `receive` in Akka. It's a `PartialFunction`
that can accept multiple messages. Each message represents an individual
request, and all messages will be wrapped in a `ProcessRequest` case class.

Below is an example resource.

```scala
class MyResource(ctx: ResourceContext) extends AbstractResourceActor(ctx) {
  override def resourceReceive = {
    //processing a request that was not parsed at all.
    case ProcessRequest(req: HttpRequest) =>
      completeToJSON(StatusCodes.OK, "hello world!")
    //processing a request that was parsed by the ResourceDriver
    case ProcessRequest(req: GetHelloWorld) =>
      completeToJSON(StatusCodes.OK, "hello world 2!")
  }
}

object MyResource {
  //this is a convenience method used as a a parameter in
  //ResourceDriver.serve, which you'll see later.
  def apply(ctx: ResourceContext): MyResource = {
    new MyResource(ctx)
  }
}
```

A few notes:

- Resources don't define routes to bind to, nor do they couple tightly with
the functionality to parse a request into a Scala type. Cascade makes routes,
parsing and resources orthogonal on purpose. You can take advantage of that
orthogonality in a lot of ways. Here are a few ideas:
  - re-use routes for different servers (e.g. a dev and a prod server)
  - switch parsers at runtime (e.g. for different request body encodings)
  - switch resources at runtime (e.g. to do dependency injection)
- You can create a new resource instance for each incoming request or reuse a
resource across requests. We recommend the former so that a single resource
actor can safely store state for a single request. Also, we only built support
for the first pattern in `ResourceDriver` (see below.)

## Routes & Startup
As mentioned in the last section, your HTTP server has a set of routes that
execute your resources when a request matches a route.

You define your routes and spray server configs at the same time inside a
`SprayConfiguration` class.

As mentioned above, we recommend that you call `ResourceDriver.serve(...)`
inside each route, to create a new resource actor for each request. That
pattern follows what Spray does for HTTP handler actors.

As you'll see in the example below, `ResourceDriver.serve(...)` takes in a
resource construction function and a request parsing function. Your resource
can have multiple handlers, so you can use one resource actor with multiple
different parsers. Your server will still work as long as the resource works
with the different parsed bodies you send to it.

```scala
object MyServer extends CascadeApp {
  val svcName = "sample-service"
  val cfg = SprayConfiguration(serviceName = svcName, port = 8080, backlog = 5) {
    //This is where you put your routes. We recommend using spray-routing here,
    //but you can also write your own routing code that returns a
    //spray.routing.Route.
    get {
      path("hello") {
        ResourceDriver.serve(MyResource.apply, parseRequest)
      }
    }
  }
  val sysWrapper = new ActorSystemWrapper(svcName)
  //this is what actually starts your server
  SprayActor.start(sysWrapper, cfg)
}
```

One final note - make sure your `MyServer` object extends `CascadeApp`. Doing
so will set up some global logging and exception handling features that work
best with Spray/Akka servers.

See the
[example `ServerModule`](/examples/src/main/scala/com/paypal/cascade/examples/http/resource/MyHttpServerModule.scala)
for working code.
