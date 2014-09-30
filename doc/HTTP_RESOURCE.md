# HTTP Resources

Cascade includes a powerful system for building
[REST](http://en.wikipedia.org/wiki/Representational_state_transfer) servers
on top of Spray and Akka.

This document contains a detailed discussion of how Cascade's HTTP support
works. If you'd rather start building immediately, please see our
[getting started guide](HTTP_RESOURCE_GETTING_STARTED.md).

## Introduction

Cascade's primary building block for REST servers is a **Resource**, which
abstracts a full HTTP request lifetime.

Resources are organized into the following logical pieces:

* `HttpResourceActor` - similar to the Spray HTTP actor, an instance of this
actor gets started for each request. This
actor runs a state machine for the HTTP request it was started for. For example,
the state machine starts with ensuring
that the `Content-Type` header represents an acceptable and supported content
type. It then moves to parsing the request,
and so on.
* `AbstractResourceActor` - extends `HttpResourceActor` and provides convenience
methods for you to write
your resource actor. When you write your resource, you extend this class.
* `ResourceDriver` - an object that contains a `serve` function that sets up an
`AbstractResourceActor` and tells it to start processing a request. `serve`
itself returns a function which can be immediately be passed to
`spray-routing`'s `complete` function.
* `ResourceService` - a trait that you mixin to your route definitions. mixing
in this trait automatically provides you with a `/status` and `/stats` endpoint.
See below for more details.

# ResourceService
`ResourceService` automatically adds two routes to your defined routes:

- `/status`
- `/stats`

## Status
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

## Stats
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
