# Cascade

![Cascade logo](doc/cascade.png)

[![Build Status](https://travis-ci.org/paypal/cascade.png?branch=develop)](https://travis-ci.org/paypal/cascade)

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

# Getting Started

To use Cascade libraries in your project, simply add a dependency to your
build system. In an SBT project, add the following to your `build.sbt` or
`Build.scala` file:

```scala
"com.paypal" %% "cascade-$projectName" % "0.1.4"
```

For example, to use the Akka library:

```scala
"com.paypal" %% "cascade-akka" % "0.1.4"
```

If you're starting a new project, we recommend using SBT along with
[Horizon](https://github.com/paypal/horizon)

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
[PATTERNS.md](doc/PATTERNS.md) describes them in detail.

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

# The Libraries

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

The `http` library has base objects and traits for creating Spray HTTP servers.
This library is intended to complement the functionality that Spray and Akka
already have. Please see our [getting started guide](doc/HTTP_RESOURCE_GETTING_STARTED.md)
and [detailed documentation](doc/HTTP_RESOURCE.md) for more.

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


# Contributing

As normal, if you want to contribute to Cascade, we gladly accept pull requests.
Simply fork this repository, make your changes, and submit a PR to us.
If you are fixing an issue in your PR, please make sure to write `Fixes #123`
in the description, so that the issue is closed after your PR gets merged.

**Note:** If you want to modify this file (`README.md`) in your PR, edit
[`Readme-Template.md`](Readme-Template.md) and then run `genReadme` in SBT.

## Development

If you have [Vagrant](http://vagrantup.com), simply `vagrant up` in this
project to get a VM with all the necessary tools for development. When inside
the VM, the sources for this project are inside `/vagrant`, and they're synched
with your host machine. Edit your code on the host machine and build/run it
inside the VM.

If you don't have Vagrant, you'll need
[Scala 2.11.4](http://scala-lang.org/download/) and
[SBT 0.13.6](http://www.scala-sbt.org/download.html) to build and run this
project.

## Releasing A New Version of Cascade

This section is for Cascade core contributors only.

All releases must be done from a release branch that merges into master.
Below are steps:

1. create a `release/$RELEASENAME` branch
2. [open a pull request](https://github.com/paypal/cascade/compare) merging
your branch from (1) into `master`, and get it
3. follow steps below to publish to Sonatype OSS
4. merge your PR from (2), then merge `master` into `develop`
  - `git checkout master; git pull origin master`
  - `git checkout develop; git merge --no-ff master`
5. [create a new release](https://github.com/paypal/cascade/releases/new) with
a tag on your branch from (1)
6. delete your branch from (1)
  - `git checkout develop; git branch -D release/$RELEASENAME`

## Publishing to Sonatype OSS

This section is for Cascade core contributors only.

The following should be done for step #3 in the above "Releasing A New Version
of Cascade" section. They publish a Cascade artifact to OSS Sonatype and then
to Maven Central.

1. Follow http://www.scala-sbt.org/sbt-pgp/usage.html to create and
publish a PGP Key Pair
  - Make sure you have a `~/.sbt/0.13/plugins/gpg.sbt` file
  - add `addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.3”)` to your
  `gpg.sbt` file
2. Open SBT in Cascade, then run these commands:
  - `set pgpReadOnly := false`
  - `pgp-cmd gen-key`. Take note of the email address you set. You'll use
  it in the next command
  - `pgp-cmd send-key $EMAILADDR hkp://keyserver.ubuntu.com`
3. Close SBT in Cascade, then run these commands:
  - `sbt -Dchangelog.author=“…” -Dchangelog.msg=“…” release cross with-defaults`
  - `sbt publishSigned`
4. Go to https://oss.sonatype.org. Click "Release" and then click "Close"

{{auto-gen}}
