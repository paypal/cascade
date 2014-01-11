package com.paypal.stingray.common.tests.json

import org.specs2.Specification
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import com.paypal.stingray.common.json.JSONUtil

/**
 * User: Will Palmeri
 * Date: 4/10/13
 */
class JSONUtilSpecs extends Specification { override def is =
  "JSONUtilSpecs".title                                                                               ^
  """
  JSONUtil provides json ser/deser using lift-json
  """                                                                                                 ^
  "Serialize a JValue correctly using OptimizedPrepare"                                               ! optimizedPrepare().ok ^
  "Serialize a JValue using OptimizedPrepare should be faster than compact(prepare())"                ! optimizedPrepare().faster ^
  "Serialize an empty array"                                                                          ! optimizedPrepare().emptyArray ^
  "Serialize an empty object"                                                                         ! optimizedPrepare().emptyObj ^
  "Serialize a JObject with JNothing fields"                                                          ! optimizedPrepare().nothingObj ^
  "Serialize a JArray with JNothing fields"                                                           ! optimizedPrepare().nothingArray ^
                                                                                                      end

  trait context extends CommonImmutableSpecificationContext {

    def liftPrepare(json: JValue): String = {
      compact(render(json))
    }


    lazy val bigJValue = {
      def appendN(json: JObject, count: Int): JObject = {
        if (count == 0) json else json ~ appendN(json, count - 1)
      }

      appendN(completeJValue, 1000)
    }

    val completeJValue: JObject = {
      ("string" -> "value") ~
      ("number" -> 1) ~
      ("bool" -> true) ~
      ("obj" -> (("k1" -> "v1") ~ ("k2" -> "v2"))) ~
      ("arr" -> List(1,2,3))
    }
  }

  /**
   * results with bigJValue size 100 and 1000 serializations:
   * optTime 3174 unoptTime 6426
   *
   * with bigJValue size 1000 and 100 serializations:
   * optTime 430 unoptTime 996
   *
   * with bigJValue size 1000 and 1000 serializations:
   * optTime 3396 unoptTime 7725
   *
   * optimizedPrepare about 2.2x faster!!
   */

  case class optimizedPrepare() extends context {
    def ok = apply {
      JSONUtil.prepare(completeJValue) must beEqualTo(liftPrepare(completeJValue))
    }

    def faster = apply {
      val count = 100
      val unoptTime = time(() => liftPrepare(bigJValue), count)
      val optTime = time(() => JSONUtil.prepare(bigJValue), count)
      optTime must beLessThan(unoptTime)
    }

    def emptyArray = apply {
     val empty = JObject(List(JField("arr", JArray(List()))))
      JSONUtil.prepare(empty) must beEqualTo(liftPrepare(empty))
    }

    def emptyObj = apply {
     val empty = JObject(List[JField]())
      JSONUtil.prepare(empty) must beEqualTo(liftPrepare(empty))
    }

    def nothingObj = apply {
      val nothing = JObject(List(JField("k1", JNothing), JField("k2", JNothing)))
      JSONUtil.prepare(nothing) must beEqualTo(liftPrepare(nothing))
    }

    def nothingArray = apply {
      val nothing = JObject(List(JField("arr", JArray(List(JObject(List(JField("k1", JNothing), JField("k2", JNothing))))))))
      JSONUtil.prepare(nothing) must beEqualTo(liftPrepare(nothing))
    }

    private def time[T](f: () => T, count: Int): Int = {
      val st = System.currentTimeMillis()
      for (i <- 1 to count) {
        f()
      }
      (System.currentTimeMillis() - st).toInt
    }
  }

}
