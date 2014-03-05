stingray-common
=================

This is a repository of common patterns, convenience objects, utilities, and other foundational pieces used in
projects developed by the Stingray team.

To use them in a project, include the following dependencies in your `build.sbt` or `Build.scala`:

    "com.paypal.stingray" %% "stingray-common" % commonVersion
    "com.paypal.stingray" %% "stingray-http"   % commonVersion

## common

Contains basic patterns, objects, and utilities, including:

- `CommonActor` and `ServiceActor` for building Akka actors
- `StingrayApp` for building executable applications
- `JsonUtil` for serializing/deserializing JSON
- `StaticValues` and `DynamicValues` for configuration data
- `LoggingSugar` for easy access to SLF4J

Useful test objects also include:

- `ActorSpecification` and `MockActor` for testing Akka actors
- The `scalacheck` package object for extensions to Scalacheck

## http

Contains base objects and traits for creating Spray HTTP resources, including:

- `AbstractResource` as a starting point for HTTP resources
- `ResourceService` as a routing base for HTTP services

Useful test objects also include:

- `DummyResource` for testing request logic
- `SprayMatchers` for confirming request/response patterns
