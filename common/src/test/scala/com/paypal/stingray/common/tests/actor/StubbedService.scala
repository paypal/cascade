package com.stackmob.tests.common.actor

import akka.actor.{ActorSystem, ActorRef}
import akka.pattern.pipe
import akka.testkit.{CallingThreadDispatcher, TestActor, TestProbe}
import scalaz.{Failure, Success, Validation}
import concurrent.Future

/**
 * Created with IntelliJ IDEA.
 * User: taylor
 * Date: 7/31/13
 * Time: 5:06 PM
 */

trait StubbedService {

  implicit def actorSystem: ActorSystem

  protected def stubbedService[T](expect: PartialFunction[Any, T]): ActorRef = {
    val probe = TestProbe()
    probe.setAutoPilot(new TestActor.AutoPilot {
      def invalidMessage(msg: Any): T = throw new IllegalStateException(s"Invalid message sent $msg")
      override def run(sender: ActorRef, msg: Any): TestActor.AutoPilot = {
        implicit val ec = actorSystem.dispatchers.lookup(CallingThreadDispatcher.Id)
        Validation.fromTryCatch(expect.applyOrElse(msg, invalidMessage)) match {
          case Success(response) => Future.successful(response) pipeTo sender
          case Failure(error) => Future.failed(error) pipeTo sender
        }
        TestActor.KeepRunning
      }
    })
    probe.ref
  }

}
