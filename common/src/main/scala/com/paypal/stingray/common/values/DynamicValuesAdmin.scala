package com.paypal.stingray.common.values

import scala.concurrent.duration.Duration
import scala.concurrent.Future

/**
 * An extension of [[com.paypal.stingray.common.values.DynamicValues]] that allows for setting and deleting values,
 * in addition to retrieval. This class should only be extended when write access is necessary; in most cases,
 * implementations of DynamicValues should be the preferred method of interaction with these values.
 *
 * The admonishments found in DynamicValuues to use asynchronous accessors still apply here.
 */
abstract class DynamicValuesAdmin(svs: Option[StaticValues],
                                  ttl: Duration = DynamicValues.DefaultTTL)
  extends DynamicValues(svs, ttl) {

  /**
   * Creates a DynamicValuesAdmin tied to a given StaticValues
   * @param svs the given StaticValues
   * @return a DynamicValuesAdmin
   */
  def this(svs: StaticValues) = this(Some(svs))

  /**
   * Creates a DynamicValuesAdmin tied to a StaticValues with a given service name
   * @param name the service name
   * @return a DynamicValuesAdmin
   */
  def this(name: String) = this(Some(new StaticValues(name)))

  /**
   * Attempts to set a value for a given key
   * @param key the key to set
   * @param value the value
   * @return a Future containing the operation
   */
  def set(key: String, value: String): Future[Unit]

  /**
   * Attempts to delete a value for a given key
   * @param key the key to delete
   * @return a Future containing the operation, with a Boolean value representing whether the delete was successful
   */
  def delete(key: String): Future[Boolean]

}
