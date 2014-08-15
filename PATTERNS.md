# Patterns

Although the libraries in Cascade are loosely coupled from each other, they all
follow some patterns that we've found useful. Each pattern listed here has
helped us write code that is more readable, better organized or both.

# Cake

Cake is a simple pattern for organizing Scala code and nothing more. We slightly
adapted the pattern outlined in
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
