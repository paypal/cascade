package com.paypal.stingray.common.util

import scala.reflect._

/*
 * Utility classes for class casting.
 *
 * {{{
 *    import com.paypal.stingray.util.casts._
 *
 *   // `.cast[T]` casts an object to type T, returning an Option[T] which is some iff the object may be cast, none otherwise
 *   def castToString(obj: Obj): String =
 *     obj.cast[String].getOrElse(throw new ClassCastException("not a String")
 *
 *   // `.cast[T]` on an Option[A] returns an Option[T] which is Some[T] containing the casted contents of the original
 *   // option iff those contents may be cast to a T, None otherwise.
 *   def castOptionToString(opt: Option[_]): String =
 *     opt.cast[String].getOrElse(throw new ClassCastException("not a String"))
 *
 *   // `.cast[T]` on an Array[A] returns a Array[T] containing only the elements of the original array
 *   // which may be cast to a T
 *   def findStringsInArray(arr: Array[_]): Array[String] =
 *     arr.cast[String]
 *
 *   // `.cast[T]` on a Traversable[A] returns a Traversable[T] containing only the elements of the original traverable
 *   // which may be cast to a T
 *   def findStringsInTraversable(tr: Traversable[_]): Traversable[String] =
 *     tr.cast[String]
 *
 *   // `.cast[T]` on a List[A] returns a List[T] containing only the elements of the original list
 *   // which may be cast to a T
 *   def findStringsInList(li: List[_]): List[String] =
 *     li.cast[String]
 * }}}
 */

object casts {

  /**
   * Wrapper to allow any class to be cast, based on a Manifest for that class
   * @param any the class to be cast
   */
  implicit class CastableAny(any: Any) {

    /**
     * Cast the wrapped object as type `T`, optionally returning the newly cast object if the cast was successful
     * @param target the manifest of type `T`
     * @tparam T the type to which this wrapped object will be cast
     * @return optionally, the newly cast object, or None if the cast was unsuccessful
     */
    def cast[T](implicit target: ClassTag[T]): Option[T] = {
      Option(any).flatMap { _ =>
        val source = any match {
          case _: Boolean => classTag[Boolean]
          case _: Byte => classTag[Byte]
          case _: Char => classTag[Char]
          case _: Short => classTag[Short]
          case _: Int => classTag[Int]
          case _: Long => classTag[Long]
          case _: Float => classTag[Float]
          case _: Double => classTag[Double]
          case _ => ClassTag(any.getClass)
        }
        if (target.runtimeClass.isAssignableFrom(source.runtimeClass)) {
          Some(any.asInstanceOf[T])
        } else {
          None
        }
      }
    }
    def castIf[T](pred: T => Boolean)(implicit m: Manifest[T]): Option[T] = {
      cast[T].flatMap(c => if(pred(c)) Some(c) else None)
    }
  }

  implicit class CastableOption(opt: Option[_]) {
    def cast[T : ClassTag]: Option[T] = {
      opt.flatMap(_.cast[T])
    }
    def castIf[T : ClassTag](pred: T => Boolean): Option[T] = {
      cast[T].flatMap(c => if(pred(c)) Some(c) else None)
    }
  }

  implicit class CastableTraversable(traversable: Traversable[_]) {
    def cast[T : ClassTag]: Traversable[T] = {
      traversable.flatMap(_.cast[T])
    }
    def castIf[T : ClassTag](pred: T => Boolean): Traversable[T] = {
      cast[T].flatMap(c => if(pred(c)) Some(c) else None)
    }
  }

  implicit class CastableList(list: List[_]) {
    def cast[T : ClassTag]: List[T] = {
      list.flatMap(_.cast[T])
    }
    def castIf[T : ClassTag](pred: T => Boolean): List[T] = {
      cast[T].flatMap(c => if(pred(c)) Some(c) else None)
    }
  }

  implicit class CastableArray(array: Array[_]) {
    def cast[T : ClassTag]: Array[T] = {
      array.flatMap(_.cast[T])
    }
    def castIf[T : ClassTag](pred: T => Boolean): Array[T] = {
      cast[T].flatMap(c => if(pred(c)) Some(c) else None)
    }
  }

}
