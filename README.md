stingray-common
=================

This is a repository of common patterns, convenience objects, implicit classes, utilities, and other foundational pieces
used in projects developed by the Stingray team.

stingray-common consists of several sub-projects. The current projects are:

* stingray-common
* stingray-http
* stingray-akka
* stingray-json

To include any/all of the following in your project,
add the dependency in your `build.sbt` or `Build.scala` in the following format:

    "com.paypal.stingray" %% "project-name" % "root-common-version"


The following is an overview of what is included in each sub-project:

## common

Contains basic patterns, objects, and utilities for any project:

- `StingrayApp` is the starting place for building executable applications. It sets up logging and MDC values.
- `LoggingSugar` provides easy access to SLF4J.
- `trys` package object contains implicit classes/methods to convert Try objects to Either and Future objects.
- `option` package object contains implicit classes/methods to wrap any object in an Option.

Useful test objects include:

- `ActorSpecification` and `MockActor` for testing Akka actors.
- The `scalacheck` package object for extensions to Scalacheck.

## http

Contains base objects and traits for creating Spray HTTP resources:

- `AbstractResource` is a starting point for HTTP resources.
- `ResourceDriver` provides an implementation of a basic HTTP request handling pipeline.
- `ResourceService` is a routing base for HTTP services.
- `resource` package object contains implicit classes for converting objects into Futures and Trys that can return a
  `HaltException`.
- `SprayActorComponent` provides the root actor implementation used by Spray.
- `SprayConfigurationComponent` defines basic config for a Spray service.
- `url` package object contains methods to break a query parameter list into a list or map.
- `HttpUtil` pacakage object contains convenience methods for interacting with URLs.


http also provides two endpoints for projects that use it, implemented in the `ResourceServiceComponent`:

- `/status` returns current build information for the project. This includes the service name, dependencies, and Git branch and commit information.
  Must include the `x-service-status` header in request. For example, running user-platform-serv locally:

        curl -H "x-service-status:true" "http://localhost:9090/status

  returns something like

        {
          "status":"ok",
          "service-name":"userplatformserv",
          "dependencies":["all dependencies"],
          "git-info":{"branch":"develop","branch-is-clean":"true",
          "commit-sha":"ae86509970ed3d19bf578b6f6b66e00ca696adf0",
          "commit-date":"Wed Apr 16 12:01:28 PDT 2014"}
        }

- `/stats` returns internal Spray monitoring information for the build.
  Must include the `x-service-stats` reader in request, For example, running user-platform-serv locally:

        curl -H "x-service-stats:true" "http://localhost:9090/stats

  returns something like

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

Useful test objects include:

- `DummyResource` for testing request logic.
- `SprayMatchers` for confirming request/response patterns.

## akka

- `CommonActor` and `ServiceActor` are skeletons for building Akka actors. They provide logging during an actor's
  lifecycle and appropriately structure unhandled messages.
- `ActorSystemComponent` defines an implicit actor system, actor ref factory, and execution context.
- `config` package object provides an implicit class to get configuration values. It wraps Typesafe's Config getter
    methods in Options.
- `ConfigComponent` provides `val config` which uses Typesafe's ConfigFactory to load configuration files. Typically this
  will be `application.conf`.

## json

- `json` package object provides a utility for serializing/deserializing JSON. It uses the `JsonUtil` object.


