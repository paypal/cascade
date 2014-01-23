package com.paypal.stingray.common.values

import com.paypal.stingray.common.logging.LoggingSugar
import scala.concurrent._
import scala.concurrent.duration._

/**
 * An extension of [[com.paypal.stingray.common.values.Values]] to read asynchronously or synchronously
 * from external Values-providing services.
 *
 * DynamicValues should be preferred for values which may change during the life of the service. Implementations
 * of DynamicValues should be non-blocking and asynchronous where possible. Additionally, implementations should
 * try to cache results from a Values service lookup; for that, a default TTL is included here.
 *
 * Users of an implemented DynamicValues should strongly prefer to use asynchronous methods (those that return
 * [[scala.concurrent.Future]]) so as to avoid blocking. The synchronous, blocking `get(key)` pattern here should be
 * avoided where possible.
 *
 * On retrieval failure to a Values provider, a lookup to [[com.paypal.stingray.common.values.StaticValues]]
 * will be attmpted (if a StaticValues was passed).
 */
abstract class DynamicValues(svs: Option[StaticValues],
                             ttl: Duration = DynamicValues.DefaultTTL)
  extends Values
  with LoggingSugar {

  import DynamicValues._

  /**
   * Creates a DynamicValues tied to a given StaticValues
   * @param svs the given StaticValues
   * @return a DynamicValues
   */
  def this(svs: StaticValues) = this(Some(svs))

  /**
   * Creates a DynamicValues tied to a StaticValues with a given service name
   * @param name the service name
   * @return a DynamicValues
   */
  def this(name: String) = this(Some(new StaticValues(name)))

  private val logger = getLogger[DynamicValues]

  /**
   * Required for implementing Futures; supply with a [[scala.concurrent.ExecutionContext]]
   * @return an ExecutionContext
   */
  protected implicit def seqCtx: ExecutionContext

  /**
   * Performs the communication for an implemented DynamicValues. It is the core of the `getAsync(key)` pattern.
   * @param key the key to retrieve
   * @return a Future of the attempt, with optional value
   */
  protected def attempt(key: String): Future[Option[String]]

  /**
   * Attempt to retrieve a value for a given key
   * @param key the key to retrieve
   * @return a Future of the attempt, with optional value
   */
  def getAsync(key: String): Future[Option[String]] = {
    val dvFuture = attempt(key)
    val svOption = svs.flatMap(_.get(key))

    dvFuture.onFailure {
      case e => logger.error("Dynamic Value Server is down, defaulting to Static Values", e)
    }

    dvFuture.map { mbValue =>
      mbValue.orElse(svOption)
    }
  }

  /**
   * Retrieves a value for a given key, or throws if the retrieval times out.
   * @param key the key to retrieve
   * @return an optional String value for the given `key`
   * @throws InterruptedException if the current thread is interrupted while waiting
   * @throws TimeoutException if after waiting for the specified time `attempt(key)` is still not ready
   * @throws IllegalArgumentException if `SynchronousTimeout` is [[scala.concurrent.duration.Duration.Undefined Duration.Undefined]]
   */
  override def get(key: String): Option[String] = {
    Await.result(attempt(key), SynchronousTimeout)
  }

  /**
   * Attempts to retrieve all the values currently held by the DynamicValues service, for local access
   * @return a map of all values
   */
  def getAll: Future[Map[String, String]]
}

object DynamicValues {
  /* */
  val DefaultTTL = 1.minute
  val SynchronousTimeout = 5.seconds
}
