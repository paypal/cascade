# The Cake Pattern

[PATTERNS.md](PATTERNS.md) briefly describes the Cake pattern. Since this
document shows thorough details and examples of the pattern in action,
please make sure to read the patterns document first.

# Component

Here's a complete example of a component that provides access to a networked
key/value database:

```scala
trait KVServiceComponent {

    //Dependencies
    this: KVServiceDiscoveryComponent =>

    //Service Provided. This should usually be the default implementation.
    //if you leave it abstract, it can be a val (singleton) or def (factory).
    //otherwise it should be a lazy val (singleton) or def (factory)
    lazy val kvService: KVService = new MemcachedServiceKVServiceImpl

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

## Module

Modules are the top level unit to define what dependencies to inject.

Below is an example of a module to define the dependencies for `KVService` in
production:

```scala
object ServerModule
    extends KVServiceComponent
    with KVServiceDiscoveryComponent
    with App {

    //this is the method that starts your server. do whatever
    //initialization you need in this object. Since it extends App, it's
    //the entrypoint to your applicaiton
    startServing()
}
```

And here's an example of a module for use with testing:

```scala
object TestModule
    extends KVServiceComponent
    with KVServiceDiscoveryComponent {

    //here's where we stub out the kvService from KVServiceComponent
    override val kvService = new InMemoryKVService

    //here's where we stub out the kvServiceDiscovery from KVServiceDiscoveryComponent
    override val kvServiceDiscovery = new InMemoryKVServiceDiscovery
}
```
