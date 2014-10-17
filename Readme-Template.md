# Cascade

Cascade is a collection of libraries that implement common patterns,
convenience objects, implicit classes, utilities, and other foundational pieces
used in Scala applications and servers at PayPal. The libraries herein are
carefully designed to:

1. Work well with Scala and the Typesafe libraries.
2. Work independently from the others.
3. Be well defined in their functionality.
4. Minimize the number of external dependencies.
5. Related to (4) - use the features of the Scala standard library before
building their own.

# Usage

The libraries live in separate sub-projects in this repository:

* [common](common/) - utilities to help build any Scala program.
* [http](http/) - building blocks for building [Spray](http://spray.io) servers.
* [akka](akka/) - building blocks for building [Akka](http://akka.io) systems.
* [json](json/) - utilities to encode/decode JSON.

As mentioned in (2) above, these libraries are designed to work independently.
Although they have internal dependencies on each other (e.g. many libraries
depend on `common`), you can mix and match which libraries you use.

The libraries in Cascade all follow some similar patterns.
[PATTERNS.md](PATTERNS.md) describes them in detail.

Current Version: {{version}}

[View the ScalaDocs](https://paypal.github.io/Cascade/api/{{version}}/index.html#com.paypal.cascade.package)

# Dependencies

To use Cascade libraries in your project, simply add a dependency to your
build system. In an SBT project, add the following to your `build.sbt` or
`Build.scala` file:

```scala
"com.paypal" %% "cascade-$projectName" % "{{version}}"
```

For example, to use the Akka library:

```scala
"com.paypal" %% "cascade-akka" % "{{version}}"
```

If you're starting a new project, we recommend using SBT along with
[Horizon](https://github.com/paypal/horizon)

# Library Details

## common

Basic patterns, objects, and utilities for any project:

- `CascadeApp` is the starting place for building executable applications. It sets up logging and MDC values.
- `LoggingSugar` provides easy access to SLF4J.
- `trys` package object contains implicit classes/methods to convert Try objects to Either and Future objects.
- `option` package object contains implicit classes/methods to wrap any object in an Option.
- `casts` package object contains implicit classes/methods to cast objects to the class type provided.

Useful test objects include:

- `ActorSpecification` and `MockActor` for testing Akka actors.
- The `scalacheck` package object for extensions to Scalacheck.

## http

Base objects and traits for creating Spray HTTP resources:

- `AbstractResource` is a starting point for HTTP resources.
- `ResourceActor` provides an implementation of a basic HTTP request handling pipeline.
- `ResourceDriver` spawns a ResourceActor, which should happen per request.
- `ResourceService` is a routing base for HTTP services.
- `resource` package object contains implicit classes for converting objects into Futures and Trys that return an exception on error.
- `SprayActorComponent` provides the root actor implementation used by Spray.
- `SprayConfigurationComponent` defines basic config for a Spray service.
- `url` package object contains methods to break a query parameter list into a list or map.
- `HttpUtil` pacakage object contains convenience methods for interacting with URLs.


http also provides two endpoints for projects that use it, implemented in the `ResourceServiceComponent`:

- `/status` returns current build information for the project. This includes the service name, dependencies, and Git branch and commit information.
  Must include the `x-service-status` header in request. For example, after running your project locally:

        curl -H "x-service-status:true" "http://localhost:9090/status

  returns something like

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

- `/stats` returns internal Spray monitoring information for the build.
  Must include the `x-service-stats` header in request, For example, after running your project locally:

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
- `SprayRoutingClient` for use in integration tests to test the full service stack, including Spray routes.
  Provides the `makeRequest` methodfor interacting with a Spray service as if via HTTP, using the declared routes.

## akka

- `CommonActor` and `ServiceActor` are skeletons for building Akka actors. They provide logging during an actor's
  lifecycle and appropriately structure unhandled messages.
- `ActorSystemComponent` defines an implicit actor system, actor ref factory, and execution context.
- `config` package object provides an implicit class to get configuration values. It wraps Typesafe's Config getter
    methods in Options.
- `ConfigComponent` provides `val config` which uses Typesafe's ConfigFactory to load configuration files. Typically this
  will be `application.conf`.

## json

Simple functionality for fast JSON encoding/decoding that uses [Jackson](https://github.com/FasterXML/jackson)
under the hood.

- The `JsonUtil` object provides two functions to do encoding and decoding:
    - `toJson(value: Any): Try[String]`
    - `fromJson[T: Manifest](json: String): Try[T]``
- The `json` package object provides implicit classes to wrap `JsonUtil.toJson`
and `JsonUtil.fromJson`. If you `import com.paypal.cascade.json._` you can
decode a `String` using `.fromJson[T]` and you can encode an `Any`
(any type) using `.toJson`.


# Development

If you have [Vagrant](http://vagrantup.com), simply `vagrant up` in this project to get a VM with all the necessary
tools for development. When inside the VM, the sources for this project are inside `/vagrant`, and they're
synched with your host machine. Edit your code on the host machine and build/run it inside the VM.

If you don't have Vagrant, you'll need [Scala 2.11.2](http://scala-lang.org/download/) and
[SBT 0.13.5](http://www.scala-sbt.org/download.html) to build and run this project.

# Releasing A New Version
When it's time to release a new version of Cascade, use the SBT console to do
so.

## Preliminary Steps
You only have to do these steps once to set up your computer or VM.

### Sonatype Credentials File
Make sure you have a `~/.sbt/0.13/sonatype.sbt` file that looks like this:

```scala
credentials += Credentials("Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  "$YourUsername",
  "$YourPassword")
```

`$YourUsername` and `$YourPassword` need to be the same as your authentication
information at [oss.sonatype.org](https://oss.sonatype.org).

### GPG Key Generation
Open up an SBT console in the Cascade repo, then do these steps:

- `set pgpReadOnly := false`
- `pgp-cmd gen-key`
- `pgp-cmd send-key $name hkp://keyserver.ubuntu.com`
  - `$name` is the name of the key you set in step 2.

## Publishing And Closing A New Version

- `publishSigned`
  - If it prompts for a GPG password, use the one you set when you ran
  `pgp-cmd gen-key`.
- `sonatypeRelease`
  - If there are multiple staged repositories opened, go to
  [the sonatype UI](https://oss.sonatype.org) and find yours. When you do,
  record the name and `sonatypeRelease $your_repo`.

{{auto-gen}}
