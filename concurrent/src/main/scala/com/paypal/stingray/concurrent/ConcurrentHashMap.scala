package com.paypal.stingray.concurrent

import scala.collection.mutable.{HashMap => MutableHashMap}
import scala.collection.concurrent.{Map => ScalaConcurrentMap}
import com.paypal.stingray.common.option._

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.common.concurrent
 *
 * User: aaron
 * Date: 9/7/12
 * Time: 5:33 PM
 */

/**
 * a hash map that can be accessed concurrently
 * @tparam Key the key type
 * @tparam Value the value type
 *
 */
abstract class ConcurrentHashMap[Key: Equal, Value: Equal] extends ScalaConcurrentMap[Key, Value] {

  protected val map = MutableHashMap[Key, Value]()

  /**
   * get a value from the map, by key
   * @param key the key whose value to get
   * @return Some(currentValue) if the key existed, None if the key didn't exist
   */
  override def get(key: Key): Option[Value] = map.synchronized(map.get(key))

  /**
   * equivalent to get(key).isDefined
   * @param key the key to check
   * @return true if the key exists, false otherwise
   */
  def exists(key: Key): Boolean = get(key).isDefined

  /**
   * set a key in the map
   * @param key the key to set
   * @param value the value to set
   * @return true if the key had a value before the set, false otherwise
   */
  def set(key: Key)(value: => Value): Boolean = map.synchronized(map.put(key, value).isDefined)

  /**
   * alias for set(kv._1, io(kv._2))
   */
  override def +=(kv: (Key, Value)): this.type = {
    set(kv._1)(kv._2)
    this
  }

  /**
   * remove a key-value pair only if it exists and its value satisfies a predicate
   * @param key the key for the key-value pair to remove
   * @param valuePred the predicate for the key's value
   * @return Some(value) if the key existed and its value satisfied a predicate, None otherwise.
   *         if Some(value) is returned, the key-value pair was removed
   */
  def removeIf(key: Key)(valuePred: Value => Boolean): Option[Value] = map.synchronized {
    map.get(key).flatMap { value =>
      valuePred(value).option {
        map -= key
        value
      }
    }
  }

  /**
   * remove a key-value pair if the existing value is equal to value
   * @param key the key to remove
   * @param value the value whose equality to check
   * @return true if the key-value pair was removed, false otherwise
   */
  override def remove(key: Key, value: Value): Boolean = (removeIf(key)(existing => existing === value)).isDefined

  /**
   * remove a key-value pair if it exists
   * @param key the key to remove
   * @return the ConcurrentHashMap that resulted from the key-value pair removal
   */
  def -=(key: Key): this.type = {
    removeIf(key)(_ => true)
    this
  }

  /**
   * set a value in the map if one was present before
   * @param key the key to set
   * @param value the value to set if there was previously a value assigned for the given key
   * @return Some(oldValue) if oldValue was assigned to key, None otherwise.
   *         if Some is returned, the new value was set
   */
  def setIfPresent(key: Key)(value: => Value): Option[Value] = setIf(key, value)(_ => true)

  /**
   * set a value only if none existed before
   * @param key the key to set
   * @param value the value to set
   * @return Some(oldValue) if a value already existed, None otherwise.
   *         if None is returned, the new value was set
   */
  def setIfAbsent(key: Key)(value: => Value): Option[Value] = map.synchronized {
    map.get(key).sideEffectNone(map.put(key, value))
  }

  /**
   * alias for setIfAbsent
   */
  override def putIfAbsent(key: Key, value: Value): Option[Value] = setIfAbsent(key)(value)

  /**
   * set a value only if one existed and a predicate holds true on that value
   * @param key the key to set
   * @param value the value to set
   * @param pred the predicate to run on the value, if one exists
   * @return Some(oldValue) if it existed, and pred(oldValue) returned true, None otherwise.
   *         if Some(oldValue) is returend, the new value was set
   */
  def setIf(key: Key, value: => Value)(pred: Value => Boolean): Option[Value] = map.synchronized {
    map.get(key).flatMap { oldValue =>
      pred(oldValue).option {
        map.put(key, value)
        oldValue
      }
    }
  }

  /**
   * alias for setIfPresent(key, newValue)
   */
  override def replace(key: Key, newValue: Value): Option[Value] = setIfPresent(key)(newValue)

  /**
   * remove a key-value pair if it exists and the value is equal to oldValue
   * @param key the key whose value to replace
   * @param oldValue the value to expect
   * @param newValue the value to set if appropriate
   * @return true if the replacement happened, false otherwise
   */
  override def replace(key: Key, oldValue: Value, newValue: Value): Boolean = (setIf(key, newValue) { existing =>
    existing === oldValue
  }).isDefined

  /**
   * create an iterator representing a copy of the current state of this ConcurrentHashMap.
   * future changes to this ConcurrentHashMap will not be reflected in the returned iterator
   * @return an iterator representing a copy of the current state of this ConcurrentHashMap
   */
  def iterator: Iterator[(Key, Value)] = map.synchronized(map.toSeq.iterator)
}

object ConcurrentHashMap {
  def apply[Key: Equal, Value: Equal](initialPairs: (Key, Value)*): ConcurrentHashMap[Key, Value] = apply(initialPairs.toTraversable.toMap)
  def apply[Key: Equal, Value: Equal](initialMap: Map[Key, Value] = Map()): ConcurrentHashMap[Key, Value] = new ConcurrentHashMap[Key, Value] {
    override protected val map = {
      val m = MutableHashMap[Key, Value]()
      for((key, value) <- initialMap) {
        m += (key -> value)
      }
      m
    }
  }
}
