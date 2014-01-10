package com.paypal.stingray.common.stats

/**
 * Created by IntelliJ IDEA.
 *
 * Common statsd objects that are shared across all Megatron services.  Note this does *not* include API.
 * User: gary
 * Date: 8/13/13
 * Time: 3:30 PM
 */
object MegatronStats {

  class MongoStats(action: String) extends StatsDStat(s"mongo.$action")
  object MongoStatsInsert extends MongoStats("insert")
  object MongoStatsFind extends MongoStats("find")
  object MongoStatsDelete extends MongoStats("delete")
  object MongoStatsUpdate extends MongoStats("update")

  class MySqlStats(action: String) extends StatsDStat(s"mysql.$action")
  object MySqlStatsInsert extends MySqlStats("insert")
  object MySqlStatsFind extends MySqlStats("find")
  object MySqlStatsDelete extends MySqlStats("delete")
  object MySqlStatsUpdate extends MySqlStats("update")

  class RiakStats(action: String) extends StatsDStat(s"riak.$action")
  object RiakStatsInsert extends RiakStats("insert")
  object RiakStatsFind extends RiakStats("find")
  object RiakStatsDelete extends RiakStats("delete")
  object RiakStatsUpdate extends RiakStats("update")
}
