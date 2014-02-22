package com.paypal.stingray.common.tests.values

import com.paypal.stingray.common.values._
import org.specs2._
import akka.testkit._
import akka.actor._
import scala.concurrent._
import com.paypal.stingray.common.tests.actor.ActorSpecification

/**
 * Tests [[com.paypal.stingray.common.values.DynamicValues]]
 */
class DynamicValuesSpecs extends TestKit(ActorSystem()) with ActorSpecification with SpecificationLike { def is = s2"""

  Retrieving dynamic value asyncronously works ${GetAsync().ok}
  Failed retrieval looks in static values file
  If key in neither file, ...
  getAll
  get ${Get().ok}

"""

  class ExtendedDV extends DynamicValues(None) {

    override implicit val seqCtx = synchronousExecution

    override def attempt(key: String): Future[Option[String]] = {
      Future.successful {
        Some(key)
      }
    }

    override def getAll: Future[Map[String, String]] = {
      Future.successful {
        Map[String, String]()
      }
    }

  }

  case class GetAsync() {
    def ok = {
      val dv = new ExtendedDV
      val value = dv.getAsync("anything").mapTo[Option[String]].toTry
      value must beSuccessfulTry[Option[String]].withValue(Some("anything"))
    }
  }

  case class Get() {
    def ok = {
      val dv = new ExtendedDV
      val value = dv.get("anything")
      value must beSome[String].like {
        case v => v must beEqualTo("anything")
      }
    }
  }

}
