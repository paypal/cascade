package com.paypal.stingray.common.enumeration

/**
 * com.paypal.stingray.common.enumeration
 *
 * Copyright 2012 StackMob
 *
 * User: aaron
 * Date: 1/10/12
 * Time: 1:49 PM
 *
 * This is a fully type safe enumeration framework. It takes more setup & code than the built-in Scala Enumeration class,
 * but it will pay off in 2 major ways:
 *
 * 1. you get to decide what happens when a conversion from a string to enum value fails
 * 2. you get compiler warnings when you miss a match on one or more enum values (ie: non-exhaustive match error)
 *
 * these benefits are worth the relatively small bit of extra code per enumeration.
 *
 * How to use this to create enumerations of your own:
 *
 * 1. create your enumeration:
 * <code>
 *   package my.package
 *
 *   //you must include this to avoid extending build-in scala enumerations
 *   import com.paypal.stingray.common.enumeration._
 *
 *   //make sure this is sealed and an abstract class, not a trait
 *   sealed abstract class MyEnum extends Enumeration
 *   object MyEnumValue1 extends MyEnum {
 *       override lazy val stringVal = "my_enum_value_1"
 *   }
 *   object MyEnumValue2 extends MyEnum {
 *       override lazy val stringVal = "my_enum_value_2"
 *   }
 *
 *   //create an EnumReader that knows how to convert strings into a MyEnum if the lowercase of the string
 *   //matches the lowercase of the enum string value value
 *   implicit val reader: EnumReader[MyEnum] = lowerEnumReader(MyEnumValue1, MyEnumValue2)
 *
 * </code>
 *
 * 2. import your enum & the Enumeration implicits where you want to use the enum:
 * <code>
 *   package my.package
 *   import com.paypal.stingray.common.enumeration._
 * </code>
 *
 * 3. use your enumeration & the implicits you imported to convert strings to your enum values:
 * <code>
 *   "my_enum_value_1".readEnum[MyEnum] match {
 *       case Some(MyEnumValue1) => doStuffForMyEnumValue1()
 *       case Some(MyEnumValue2) => doStuffForMyEnumValue2()
 *       //you will get warnings for non-exhaustive matches if you have more MyEnum subclasses than these
 *       case None => doStuffForNoEnum()
 *   }
 * </code>
 */

trait EnumReader[T] {
  def read(s: String): Option[T]

  //same as read, except throws
  def withName(s: String): T = read(s) match {
    case Some(v) => v
    case None => throw new EnumerationException(s)
  }
}

/**
 * By mixing this into your enum you are able to pattern match on string values
 * that you want to convert to enum values (iif they are valid). For example,
 * see com.paypal.stingray.common.deploymentapi.metadata.RepositoryType has this trait mixed in
 * and can be used as such:
 *
 * Note: the type returned by the extractor is the general sealed trait T, not the
 * enum instances themselves
 *
 * scala> "HTML5" match { case RepositoryType(a) => a; case _ => throw new Exception("fail!") }
 * res0: com.paypal.stingray.common.deploymentapi.metadata.RepositoryType = HTML5
 * scala> "CC" match { case RepositoryType(a) => a; case _ => throw new Exception("fail!") }
 * res1: com.paypal.stingray.common.deploymentapi.metadata.RepositoryType = CC
 * scala> "a" match { case RepositoryType(a) => a; case _ => throw new Exception("fail!") }
 * java.lang.Exception: fail!
 */
trait EnumUnapply[T <: Enumeration] {
  def unapply(s: String)(implicit reader: EnumReader[T]): Option[T] = reader.read(s)
}
