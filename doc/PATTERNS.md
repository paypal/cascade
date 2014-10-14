# Patterns
The libraries in Cascade are loosely coupled from each other but share some familiar
design patterns. We use each pattern to ensure Cascade code is readable and organized.

This document serves as a reference for patterns used in Cascade.

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
