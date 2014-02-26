package com.paypal.stingray.common.tests.values

import com.paypal.stingray.common.values._
import org.specs2._
import akka.testkit._
import akka.actor._
import scala.concurrent._
import com.paypal.stingray.common.tests.actor.ActorSpecification
import org.scalacheck.Prop.{Exception => PropException, _}
import org.scalacheck.Arbitrary._

/**
 * Tests [[com.paypal.stingray.common.values.DynamicValues]]
 */
class DynamicValuesSpecs extends TestKit(ActorSystem()) with ActorSpecification with SpecificationLike with ScalaCheck { def is = s2"""

  DynamicValues is an implementation of com.paypal.stingray.common.values.Values
  to read asynchronously from external properties files.

  Retrieving dynamic value async works              ${GetAsync().successCase}
  Failed dv lookup defaults to static values file   ${GetAsync().defaultToSV}
  When attempt fails, returns exception             ${GetAsync().failureCase}
  getAll                                            ${GetAll().ok}
  get                                               ${Get().ok}

"""

  private val defaultAttempt = { key: String =>
    Future.successful {
      Some(key)
    }
  }
  private class DummyDV(svs: StaticValues = StaticValues.defaultValues)
                        (attemptFn: String => Future[Option[String]] = defaultAttempt) extends DynamicValues(svs) {

    override implicit val seqCtx = synchronousExecution

    override def attempt(key: String) = attemptFn(key)

    override def getAll: Future[Map[String, String]] = {
      Future.successful {
        Map[String, String]()
      }
    }

  }

  case class GetAsync() {
    def successCase = forAll(arbitrary[String]) { str =>
      val dv = new DummyDV()()
      val value = dv.getAsync(str).mapTo[Option[String]].toTry
      value must beSuccessfulTry[Option[String]].withValue(Some(str))
    }
    def defaultToSV = {
      val url = getClass.getClassLoader.getResource("test.properties")
      val sv = new StaticValues(url)
      val dv = new DummyDV(sv)({ key: String =>
        Future.successful(Option.empty[String])
      })
      val value = dv.getAsync("some.property").mapTo[Option[String]].toTry
      value must beSuccessfulTry[Option[String]].withValue(Some("somevalue"))
    }
    def failureCase = {
      val dv = new DummyDV()({ key: String =>
        Future.failed { new Exception("foo") }
      })
      val value = dv.getAsync("some.property").mapTo[Option[String]].toTry
      value must beFailedTry[Option[String]].withThrowable[Exception]("foo")
    }
  }

  case class GetAll() {
    def ok = {
      val dv = new DummyDV()()
      val value = dv.getAll.mapTo[Map[String, String]].toTry
      value must beSuccessfulTry[Map[String, String]]
    }
  }

  case class Get() {
    def ok = forAll (arbitrary[String]) { str =>
      val dv = new DummyDV()()
      val value = dv.get(str)
      value must beSome[String].like {
        case v => v must beEqualTo(str)
      }
    }
  }

}
