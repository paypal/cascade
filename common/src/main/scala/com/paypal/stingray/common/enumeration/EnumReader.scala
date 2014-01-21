package com.paypal.stingray.common.enumeration

/**
 * com.paypal.stingray.common.enumeration
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
