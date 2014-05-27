package com.paypal.stingray.akka.tests.actor

import org.specs2.specification.{SpecificationStructure, Step, Fragments}
import akka.testkit.{TestKitBase, TestKit}

/**
 * Test harness trait for Specs involving Akka Actors
 */
trait ActorSpecification extends SpecificationStructure {
  this: TestKitBase =>

  override def map(fs: => Fragments): Fragments = super.map(fs).add(Step(TestKit.shutdownActorSystem(system)))

}
