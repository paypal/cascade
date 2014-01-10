package com.paypal.stingray.common

import scalaz._
import Scalaz._

/**
 * Created by IntelliJ IDEA.
 * User: jordanrw
 * Date: 1/12/12
 * Time: 6:36 PM
 */

package object lenses {

  /**
   * MapLensW provides additional functionality similar to that
   * already provided by scalaz.Lens.MapLens (https://github.com/scalaz/scalaz/blob/master/core/src/main/scala/scalaz/Lens.scala)
   *
   * like MapLens, MapLensW also enriches lenses of type Lens[S, Map[K, V]] -- lenses which view a Map[K, V] in value of type S
   */

  implicit class RichMapLens[S, K, V](lens: Lens[S, Map[K, V]]) {

    /**
     * Conditionally add to the map viewed through this lens
     *
     * If <code>Option[V]</code> is <code>Some[V]</code> the state is modified by adding
     * the (K, V) pair to the map, otherwise the state is returned unmodified
     *
     * The result of this operation is a State[S, Map[K, V]]
     *
     * <code>
     * case class Wrapper(map: Map[String, String])
     * val wrapperMap: Lens[Wrapper, Map[String, String]] = Lens(_.map, (w, m) => w.copy(map = m))
     *
     * (for {
     *   newMap <- wrapperMap +=? (("key", "some".some))
     * } yield newMap) ! Wrapper(Map("a" -> "b")) // returns Map("a" -> "b", "key" -> "some")
     *
     * (for {
     *   newMap <- wrapperMap +=? (("key", none))
     * } yield newMap) ! Wrapper(Map("a" -> "b") // returns Map("a" -> "b")
     * </code>
     */
    def +=?(elem: (K, Option[V])): State[S, Map[K, V]] = lens.mods(m => elem._2.cata(v => m + (elem._1 -> v), m))

    /**
     * Conditionally add to the map viewed through this lens
     */
    def +=?(elem: (K, V), bool: Boolean): State[S, Map[K, V]] = lens.mods(m => if (bool) m + elem else m)

    /**
     * Conditionally remove from the map viewed through this lens
     */
    def -=?(k: K, bool: Boolean): State[S, Map[K, V]] = lens.mods(m => if (bool) (m - k) else m)
  }

}
