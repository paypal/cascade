# HTTP Resources
Cascade includes a powerful system for building
[REST](http://en.wikipedia.org/wiki/Representational_state_transfer) servers
on top of Spray and Akka.

Cascade's primary building block for REST servers is a **Resource**, which
abstracts a full HTTP request lifetime.

Resources are organized into the following logical pieces:

* `HttpResourceActor` - similar to the Spray HTTP actor, an instance of this actor gets started for each request. This
actor runs a state machine for the HTTP request it was started for. For example, the state machine starts with ensuring
that the `Content-Type` header represents an acceptable and supported content type. It then moves to parsing the request,
and so on.
* `AbstractResourceActor` - extends `HttpResourceActor` and provides convenience methods for you to write
your resource actor. When you write your resource, you extend this class.
* `ResourceDriver` - an object that contains a `serve` function that sets up an `AbstractResourceActor` and tells it
to start processing a request. `serve` itself returns a function which can be immediately be passed to 
`spray-routing`'s `complete` function.  
* `ResourceService` - a trait that you mixin to your route definitions. mixing in this trait automatically provides 
you with a `/status` and `/stats` endpoint.

## Organization

Below is the most common organization of a Cascade HTTP server. If you're building your first Cascade server,
start with this layout.

### Routes
Your HTTP server must have 1 or more routes (`GET /hello`, for example). You'll define your routes in a Cake pattern 
**module**, which will also include `SprayActorComponent`, `ActorSystemComponent`, `ServiceNameComponent`, 
`SprayConfigurationComponent`, and `ResourceServiceComponent`. Create a parent module called `ServerModule` that 
defines all of those other components and then a single child module that implements your production server. Below
is an example:

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
  //you can use spray-routing or your own logic here. Call ResourceDriver.serve(yourResource, ...) to return the 
  //RequestContext => Unit that implements your functionality. 
  override lazy val route: RequestContext => Unit = ???  
}
```

See the example server for working code. TODO: link to the file. 

### Resource
You defined the HTTP routes in your module, and you implement them in the resource. A resource is a class that 
extends `AbstractResourceActor` and defines a `resourceReceive` method.

TODO: example

### Entry Point