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
* `ResourceDriver`
* `ResourceService`