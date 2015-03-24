# Cascade

![Cascade logo](doc/cascade.png)

[![Build Status](https://travis-ci.org/paypal/cascade.png?branch=develop)](https://travis-ci.org/paypal/cascade)
[![Codacy Badge](https://www.codacy.com/project/badge/d722bb8e8a8847bd9a4497908368bd4e)](https://www.codacy.com/public/paypal/cascade)

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
"com.paypal" %% "cascade-$projectName" % "{{version}}"
```

For example, to use the Akka library:

```scala
"com.paypal" %% "cascade-akka" % "{{version}}"
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

[View the ScalaDocs](https://paypal.github.io/cascade/api/{{version}}/index.html#com.paypal.cascade.package)

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
[Scala 2.11.6](http://scala-lang.org/download/) and
[SBT 0.13.8](http://www.scala-sbt.org/download.html) to build and run this
project.

## Publishing to Sonatype OSS

This section is for Cascade core contributors only.

The following should be done once prior to attempting to release a new version of Cascade.

1. Create an account at http://issues.sonatype.org
2. Request publish access at https://issues.sonatype.org/browse/OSSRH-11183
3. Create an account at http://oss.sonatype.org
4. Create a user token:
 - Login into Sonatype OSS with the credentials from the previous step
 - Open up your profile
 - Select user token from the profile settings dropdown
 - Click access user token
5. Create ```~/.sbt/0.13/sonatype.sbt``` using the user token from the previous step:

  ```scala
  credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", <username>, <password>)
  ```

6. If you haven't done so previously, open sbt in Cascade to create and publish a PGP key pair using these commands:
  - ```set pgpReadOnly := false```
  - ```pgp-cmd gen-key```. Take note of the email address you set. You'll use it in the next command.
  - ```pgp-cmd send-key $EMAILADDR hkp://keyserver.ubuntu.com```
  - See http://www.scala-sbt.org/sbt-pgp/usage.html for more information
7. Close sbt in Cascade

## Releasing A New Version of Cascade

This section is for Cascade core contributors only.

All releases must be done from a release branch that merges into master.

1. Complete the steps in the "Publishing to Sonatype OSS" section above
2. Create a `release/$RELEASENAME` branch
3. [Open a pull request](https://github.com/paypal/cascade/compare) merging your branch from (2) into `master`
4. Perform the release:
  - Set the CHANGELOG_MSG and CHANGELOG_AUTHOR environment variables to work around an issue with sbt 0.13.6+
  - ```sbt "release cross with-defaults"```
5. Go to http://oss.sonatype.org and login
6. Go to “Staging Repositories” (on left side)
7. Find your repo (at the bottom) 
8. Click close
9. Click release
10. Merge your PR from (3), then merge `release/$RELEASENAME` into `develop`

{{auto-gen}}
