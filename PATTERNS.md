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

Here's a complete example of a component that provides access to a networked
key/value database:

```scala
trait KVServiceComponent {

    //Dependencies
    this: KVServiceDiscoveryComponent =>

    //Service Provided
    val kvService: KVService

    //Interface Provided

    /**
     * KVService is an interface that provides access to a key/value storage
     * system. Implementations of this interface may choose their own storage
     * backends and storage semantics (e.g. consistency guarantees, etc...).
     * Each implementation is required to specify those parameters.
     */
    trait KVService {
        /**
         * Get a value for a key, or None if the key doesn't exist
         *
         * @param key the key to get the value for
         * @return Some(value) if the key/value pair existed, None otherwise
         */
        def get(key: String): Option[Array[Byte]]

        /**
         * Set the value for a key.
         *
         * @param key the key part of the key/value pair
         * @param val the value part of the key/value pair
         */
        def set(key: String, value: Array[Byte])
    }

    //Implementation

    /**
     * MemcachedKVServiceImpl is a KVService implementation that uses
     * Memcached as its backing store. All writes go to exactly one node
     * (it uses the service discovery mechanism to find the correct node)
     * and are fully consistent.
     */
    class MemcachedKVServiceImpl extends KVService {
        /**
         * getConnectionForKey retrieves the appropriate Node for the given key,
         * using the KVServiceDiscovery dependency.
         *
         * @param key the key to use to locate the Node
         * @return the node for the given key
         */
        private def getNodeForKey(key: String): Node = {
            kvServiceDiscovery.getHostForKey(key)
        }

        /**
         * get retrieves the value for the given key
         *
         * @param key the key whose value to look up
         * @return Some(value) if they key/value pair was found, None otherwise
         */
        override def get(key: String): Option[Array[Byte]] = {
            getConnectionForKey(key).get(key)
        }

        /**
         * set creates or overwrites the value for the given key
         *
         * @param key the key whose value to create or overwrite
         * @param value the value to create or overwrite
         */
        override def set(key: String, value: Array[Byte]) {
            getConnectionForKey(key).set(key, val)
        }
    }
}
```

A few notes:

1. All dependencies are listed only as self-types
2. Only modules (see below) may extend components.
3. Names should be consistent. The component for `$INTERFACE` should be
`${INTERFACE}Component`. The implementation `$IMPL` (under `//Implementation`)
for `$INTERFACE` should be `${IMPL}${NAME}Impl`, and the service provided
(under `//Service Provided`) should be `${iNTERFACE}` (note the lowercase first
character.)
4. Generally components are referred to as `Service`s, since they perform one
single action.
5. Only functionality that has dependencies need to be in components. A set of
`String` utilities, for example, need not be.

## Modules

Modules are the top level unit to define what dependencies to inject. Here's
an example of a top level module to define the dependencies to use `KVService`
in production:

```scala

object ServerModule {

}
```

## More

This guide has most of the required reference material for using the Cake
pattern. See [CAKE.md](CAKE.md) for extensive details.
