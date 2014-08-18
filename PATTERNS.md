# Patterns
The libraries in Cascade, while loosely coupled from each other,
follow similar design patterns. These patterns were chosen by the
Cascade development team to ensure code is readable and better organized.

This document serves as a reference for patterns used in Cascade.

# Cake
Cake is a pattern used for organizing Scala code as well as dependency injection,
allowing implementations to be easily swapped out so testing is simple. The original pattern
is outlined in [Jonas Bonér's Blog Post](http://jonasboner.com/2008/10/06/real-world-scala-dependency-injection-di/),
and has been slightly adapted to fit our needs. It allows us to stub out complex functionality (often
functionality that handles I/O) so we can write unit or integration tests without
needing a complex external infrastructure (like a database).

### Cake vs. DI Frameworks
Many in the Scala community use the Cake pattern for dependency injection, as does this project.
There are multiple dependency injection frameworks ([Guice](https://code.google.com/p/google-guice/),
[Spring](http://projects.spring.io/spring-framework/),
[Dagger](http://square.github.io/dagger/)) in the Java ecosystem already.
Cake was decided to be used for two major reasons. In Cascade, we want:

1. To do DI at _compile time_ instead of runtime.
2. To do DI with the fewest number of external dependncies.

Since Cake only uses Scala language features, we get both #1 and #2 for free.

## Components
A component in Cake is a synonym for a class that has dependencies and provides
some functionality. A component is a Scala `trait` that contains:

1. An interface for one piece of functionality (written in a Scala `trait`)
2. One "real" implementation of the `trait` from (1)
3. Zero or more "fake" (e.g. stub) implementations of the `trait` from (1)
4. An abstract `val` (singleton) or `def` (factory) for the interface from (1)

## Modules
Modules are top level `object`s or `class`es that configure all dependencies.
They are the only thing that ever inherits (with `extends` and `with`) a
component, and may override the `val` or `def` from (4) above as appropriate.

Most projects have a single `object` module for the main entry point (e.g.
`object MyServer extends App with MyServiceComponent`) and 1 or more modules
for test configuration (e.g. stubbing out the database driver).

Please see [CAKEPATTERN.md](CAKEPATTERN.md) for thorough details and examples.

# Naming
The following naming conventions were chosen by the Cascade development team:

1. `object`s, `class`es and `trait`s are camel cased starting with an uppercase
letter.
2. `def`s, `val`s, `var`s and `lazy val`s are camel cased starting with a
lowercase letter.
3. `implicit class` names start with `Rich`.
4. `package object` names are short and lower case.
5. `package object`s are in their own file named `${package}.scala`. We do this
so that they're easier to find on the filesystem, in GitHub, etc...

# Errors
The following error handling conventions were chosen by the Cascade development team:

Where possible, use [`Try`](http://www.scala-lang.org/api/current/#scala.util.Try) where
possible to pass errors to the caller of a `def`. When the exception does not
matter to the caller, use
[`Option`](http://www.scala-lang.org/api/current/#scala.Option).

However, throw exceptions in a few cases:

1. Throw from
[Akka `Actor`](http://doc.akka.io/docs/akka/2.3.5/scala/actors.html)s so that
the supervisor can decide what to do.
2. Throw from `def`s that have a `@throws` annotation on them. We often do
this when we override Java methods that have a thrown exception on them.

Generally, try to minimize throwing exceptions in favor of `Try` and
`Option`, and `throw` only when there is a good reason.
