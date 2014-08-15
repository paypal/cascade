# Patterns
Although the libraries in Cascade are loosely coupled from each other, they all
follow some patterns that we've found useful. Each pattern listed here has
helped us write code that is more readable, better organized or both.

This document is a reference to patterns that we use in Cascade. Hopefully it
helps you understand the code.

# Cake
Cake is a simple pattern we use for organizing Scala code so that we can easily
(and simply) test it. We slightly adapted the pattern outlined in
[Jonas Bonér's Blog Post](http://jonasboner.com/2008/10/06/real-world-scala-dependency-injection-di/)
to our needs. It allows us to stub out complex functionality (often
functionality that does I/O) so we can write unit or integration tests without
needing complex external infrastructure (like a database).

## Dependency Injection
Many in the Scala community talk about the Cake pattern as dependency injection
and in our use case, they're right. We use this pattern to achieve dependency
injection in our code.

### Cake vs. DI Frameworks
There are multiple [dependency](https://code.google.com/p/google-guice/)
[injection](http://projects.spring.io/spring-framework/)
[frameworks](http://square.github.io/dagger/) in the Java ecosystem already,
and we decided to use Cake instead for two major reasons. We want:

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
We follows these rules for naming:

1. `object`s, `class`es and `trait`s are camel cased starting with an uppercase
letter.
2. `def`s, `val`s, `var`s and `lazy val`s are camel cased starting with a
lowercase letter.
3. `implicit class` names start with `Rich`.
4. `package object` names are short and lower case.
5. `package object`s are in their own file named `${package}.scala`.

# Errors
We prefer to use
[`Try`](`](http://www.scala-lang.org/api/current/#scala.util.Try) where
possible to pass errors to the caller of a `def`. When the exception does not
matter to the caller, we use
[`Option`](http://www.scala-lang.org/api/current/#scala.Option).

We do, however, throw exceptions in a few cases:

1. We throw from
[Akka `Actor`](http://doc.akka.io/docs/akka/2.3.5/scala/actors.html)s so that
the supervisor can decide what to do.
2. We throw from `def`s that have a `@throws` annotation on them. We often do
this when we override Java methods that have a thrown exception on them.

Generally, we try to minimize throwing exceptions in favor of `Try` and
`Option`, and `throw` only when we have a good reason.
