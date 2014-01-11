package com.paypal.stingray.common.stats

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 8/9/13
 * Time: 1:55 PM
 */
trait StatsDComponent {

  implicit def statsD: StatsD
}

