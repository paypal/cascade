package com.paypal.stingray.concurrent.tests.actor

import org.specs2._
import com.paypal.stingray.concurrent.actor._
import scalaz._
import com.paypal.stingray.common.logging.LoggingSugar

/**
 * com.stackmob.tests.actors
 *
 * Copyright 2011 StackMob
 *
 * User: aaron
 * Date: 11/30/11
 * Time: 10:48 AM
 */

class ActorSpecs extends Specification with LoggingSugar { def is =
  "A StackMobActor should"                                                                                              ^
    "accept messages and compute the result in the background"                                                          ! context().delayedComputation ^
    "return a promise for the result of a valid input"                                                                  ! context().validInput ^
    "return a promise that contains an exception from the computation"                                                  ! context().inputThrows ^
    "return a promise that contains a valid value computed inside another actor"                                        ! context().validInputCalculatedInSecondActor ^
                                                                                                                        end

  private val logger = getLogger[ActorSpecs]

  case class context() {
    private final val sleepTimeMS = 2000
    private final val outsideDomain = 3
    private final val invalid = 2
    private final val valid = 1
    private def outputFunction(i:Int) = i + 1
    class InvalidInputException extends Exception

    private def newDelayedActor = actor[Int, Int] {
      case i:Int => {
        Thread.sleep(sleepTimeMS)
        outputFunction(i)
      }
    }

    private def newNormalActor = actor[Int, Int] {
      case i: Int if i == invalid && i != outsideDomain => throw new InvalidInputException
      case i: Int if i != outsideDomain => outputFunction(i)
    }

    private class ActorCallsDelayedActor extends Actor[Int, Int] {
      private val otherActor = newDelayedActor
      override def responder(i: Int) = {
        (otherActor ! i).get match {
          case Success(i:Int) => i
          case Failure(t:Throwable) => throw t
        }
      }
    }

    def delayedComputation = {
      logger.debug("delayedComputation")
      val a = newDelayedActor
      val startTime = System.currentTimeMillis()
      val endTime = System.currentTimeMillis()
      val elapsed = endTime - startTime
      val sleepTimeMSAsLong:Long = sleepTimeMS

      (a ! valid).get.toEither must beRight.like {
        case i => (elapsed must beGreaterThanOrEqualTo(0L)) and (elapsed must beLessThan(sleepTimeMSAsLong)) and
          (i must beEqualTo(outputFunction(valid)))
      }
    }

    def validInput = {
      logger.debug("validInput")
      (newNormalActor ! valid).get.toEither must beRight.like {
        case i => i must beEqualTo(outputFunction(valid))
      }
    }

    def inputThrows = {
      logger.debug("inputThrows")
      (newNormalActor ! invalid).get.toEither must beLeft.like {
        case t => t must beAnInstanceOf[InvalidInputException]
      }
    }

    def validInputCalculatedInSecondActor = {
      logger.debug("validInputCalculatedInSecondActor")
      val a = new ActorCallsDelayedActor
      (a ! valid).get.toEither must beRight.like {
        case i => i must beEqualTo(outputFunction(valid))
      }
    }

  }

}
