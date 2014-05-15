
# 0.8.1 05/15/14 released by mvaznaian
* Move RichConfigOption to common, improve test coverage, bump build-utils and add doc settings

# 0.8.0 05/02/14 released by Taylor
* Shutdown on unhandled exceptions in the main thread.
* Remove service from MDC.

# 0.7.0 04/30/14 released by Taylor
* Adds NoConcurrentExecutionContext.

# 0.6.1 04/24/14 released by Taylor
* Fix logging

# 0.5.0 04/21/14 released by Aaron Schlesinger
* Making the ResourceDriver into an Akka Actor, to reduce the amount of operations on Futures

# 0.4.0 04/16/14 released by Taylor
* Added stingray-akka subproject
* Added stingray-json subproject
* stingray-common now only depends on SLF4J and Logback
* Cleaned up additional dependencies

# 0.3.0 04/10/14 released by mvaznaian
* Scala 2.10.4, ConfigComponent, Content-Language header, additional implicit defs, http specs

# 0.2.0 03/25/14 released by mvaznaian
* Remove Static and Dynamic Values in favor of Typesafe config, update status endpoint response

# 0.1.9 03/20/14 released by awharris
* spray 1.3.1, akka 2.3.0, other resource driver improvements

# 0.1.8 03/07/14 released by Alex
* Changes to resourceDriver

# 0.1.7 02/25/14 released by mpoconnell
* Fix for SprayActor
Simplified ResourceDriver

# 0.1.6 02/25/14 released by mpoconnell
* Resource Driver fixes, actor system fixes (Actually)

# 0.1.5 02/25/14 released by mpoconnell
* Changes to ResourceService and ResourceDriver
Actor System changes

# 0.1.4 02/18/14 released by awharris
* configurable Await timeouts for SprayMatchers requests, ActorSpecification RichFuture methods

# 0.1.3 02/11/14 released by awharris
* New patterns for actors (TryOrFailure, EitherOrFailure)

# 0.1.2 02/06/14 released by awharris
* Removed unnecessary JsonUtil methods, removed scala-reflect dependency

# 0.1.1 01/31/14 released by awharris
* Release with parameters

# 0.1.0 01/30/14 released by awharris
* First
