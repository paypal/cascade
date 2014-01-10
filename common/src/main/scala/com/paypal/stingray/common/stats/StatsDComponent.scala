package com.paypal.stingray.common.stats

import scalaz._
import Scalaz._
import com.paypal.stingray.common.values.StaticValuesComponent
import com.paypal.stingray.common.constants.ValueConstants._

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 8/9/13
 * Time: 1:55 PM
 */
trait StatsDComponent {

  implicit def statsD: StatsD
}

trait StatsDFromStaticValuesComponent extends StatsDComponent {
  this: StaticValuesComponent =>

  lazy val statsDHost = svs.get(StatsdHost) | "geary"
  lazy val statsDPort = svs.getInt(StatsdPort) | 8125

  override lazy val statsD: StatsD = new StatsDCommon(statsDHost, statsDPort, svs)
}

