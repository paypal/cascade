# HTTP Resources

Cascade includes a powerful system for building
[REST](http://en.wikipedia.org/wiki/Representational_state_transfer) servers
on top of Spray and Akka.

This document contains a detailed discussion of how Cascade's HTTP support
works. If you'd rather start building immediately, please see our
[getting started guide](HTTP_RESOURCE_GETTING_STARTED.md).

# Public Facing Components

Cascade's primary building block for REST servers is a **Resource**. A resource
is an actor that you write to handle an incoming HTTP request. When you define
your routes, you create a new resource actor each time the route is called.

There are a few other pieces of a Cascade HTTP server that work with
resources. They're listed below.

* `AbstractResourceActor` - this is an Akka `Actor` that you extend when you
write your resource.
* `ResourceDriver` - this is an `object` that contains a `serve` function
intended to be called inside a route to create the resource actor, run the HTTP
state machine (mentioned above), and complete the response. `serve` itself
returns a function that is compatible with `spray-routing`'s `complete`
function.
* `SprayActor` - this is an actor that starts a Spray HTTP server. It registers
your routes, adds some of its own utility routes (e.g. `/stats` and `/status`)
and sets up some configuration parameters (e.g. the Spray backlog). Use this
actor to start up your server.

# Internals

The remainder of this document describes the internals HTTP components of
Cascade.

## HttpResourceActor

This is an internal Akka `Actor` that runs a request through a state machine
for part of an HTTP request lifecycle.

For example, the state machine ensures that the `Content-Type` header on the
request is acceptable and supported before it moves on to parsing the request.
`AbstractResourceActor` extends `HttpResourceActor`, so your resource also
extends `HttpResourceActor` transitively.

## ResourceService
This is an internal `trait` that `SprayActor` (see above) mixes in.
`ResourceService` automatically adds a `/status` and `/stats` endpoint to all
`SprayActor`-based servers.

See below for details on the two endpoints

### Status
`/status` returns current build information for the project. This includes the
service name, dependencies, and Git branch and commit information.
In order to get this information, make a `GET` request to `/status` with a
`x-service-status` header in request (the value of the header doesn't matter.)

For example, after running your project locally:

```bash
curl -H "x-service-status:true" http://localhost:9090/status
```

That `curl` call returns json data that looks like the following:

```json
{
  "status":"ok",
  "service-name":"your-service",
  "dependencies":["all dependencies"],
  "git-info": {
    "branch":"develop",
    "branch-is-clean":"true",
    "commit-sha":"some-sha",
    "commit-date":"Wed Apr 16 12:01:28 PDT 2014"
  }
}
```

### Stats
`/stats` returns internal Spray monitoring information for the build. In order
to get this information, make a `GET` request to `/stats` with a
`x-service-stats` header in the request (the value of the header doesn't
matter.)

For example, after running your project locally:

```bash
curl -H "x-service-stats:true" http://localhost:9090/stats
```

That `curl` call returns json data that looks like the following:

```json
{
  "uptime":{"finite":true},
  "totalRequests":3,
  "openRequests":1,
  "maxOpenRequests":1,
  "totalConnections":3,
  "openConnections":1,
  "maxOpenConnections":1,
  "requestTimeouts":0
}
```
