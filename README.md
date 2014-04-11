stingray-common
=================

This is a repository of common patterns, convenience objects, implicit classes, utilities, and other foundational pieces
used in projects developed by the Stingray team.

stingray-common consists of two sub-projects: common and http. To use them in a project,
include the following dependencies in your `build.sbt` or `Build.scala`:

    "com.paypal.stingray" %% "stingray-common" % commonVersion
    "com.paypal.stingray" %% "stingray-http"   % commonVersion

## common

Contains basic patterns, objects, and utilities:

- `StingrayApp` is the starting place for building executable applications. It sets up logging and MDC values.
- `CommonActor` and `ServiceActor` are skeletons for building Akka actors. They provide logging during an actor's
  lifecycle and appropriately structure unhandled messages.
- `JsonUtil` provides a utility for serializing/deserializing JSON.
- ConfigComponent provides `val config` which uses Typesafe's ConfigFactory to load configuration files. Typically this
  will be `application.conf`.
- `config` package object provides an implicit class to get configuration values. It wraps Typesafe's Config getter
  methods in Options.
- `LoggingSugar` provides easy access to SLF4J.
- Other packages, traits, and objects provide helper methods and implicit classes to, for example,
  wrap results in their proper Future/Try return type.

Useful test objects also include:

- `ActorSpecification` and `MockActor` for testing Akka actors.
- The `scalacheck` package object for extensions to Scalacheck.

## http

Contains base objects and traits for creating Spray HTTP resources:

- `AbstractResource` is a starting point for HTTP resources.
- `ResourceService` is a routing base for HTTP services.
- `StatusResponse` creates a response when the `/status` endpoint is hit, returning generic information such as the
  project name, dependencies, Git build information, etc.
- `resource` package object provides implicit classes for converting objects into Futures and Trys that can return a
  `HaltException`.
- `ActorSystemComponent` defines an implicit actor system, actor ref factory, and execution context.
- `SprayActorComponent` provides the root actor implementation used by Spray.


Useful test objects also include:

- `DummyResource` for testing request logic.
- `SprayMatchers` for confirming request/response patterns.
