package com.paypal.stingray.common.tests.stats

import com.paypal.stingray.common.values.StaticValuesComponent
import com.paypal.stingray.common.stats.{StatsDComponent, StatsD}

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 9/17/13
 * Time: 11:02 AM
 */
trait DummyStatsDComponent extends StatsDComponent {

  implicit def statsD: StatsD = new DummyStatsD

}
