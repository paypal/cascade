package com.paypal.stingray.common.db.mongo

import com.mongodb._
import com.paypal.stingray.common.values.StaticValues
import com.paypal.stingray.common.constants.ValueConstants._
import com.stackmob.core.DatastoreException
import scala.collection.convert.Wrappers.JConcurrentMapWrapper
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap
import scalaz._
import Scalaz._

class MongoFactory(svs: StaticValues) {

  private lazy val mongoClientOptions = {
    val builder = new MongoClientOptions.Builder
    builder.autoConnectRetry(svs.getBool("mongo.autoConnectRetry") | true)
    builder.maxAutoConnectRetryTime(~svs.getInt("mongo.maxAutoConnectRetryTime"))
    builder.connectTimeout(svs.getInt("mongo.connectTimeout") | 10000)
    builder.socketTimeout(~svs.getInt("mongo.socketTimeout"))
    builder.maxWaitTime(svs.getInt("mongo.maxWaitTime") | 3000)
    builder.threadsAllowedToBlockForConnectionMultiplier(svs.getInt("mongo.threadsAllowedToBlockForConnectionMultiplier") | 5)
    builder.connectionsPerHost(svs.getInt("mongo.connectionsPerHost") | 200)
    builder.readPreference(if (svs.getBool("mongo.readFromSecondary") | true) {
      ReadPreference.secondaryPreferred
    } else {
      ReadPreference.primaryPreferred
    })
    builder.writeConcern(WriteConcern.SAFE)
    builder.build()
  }

  def getConnection: Mongo = {
    try {
      get(svs.get(MongoHost) | "localhost")
    } catch {
      case e: ConnectException => throw new DatastoreException(e.getMessage, e)
      case e: UnknownHostException => throw new DatastoreException(e.getMessage, e)
      case e: MongoException => throw new DatastoreException(e.getMessage, e)
    }
  }

  def getRawAnalyticsConnection: Mongo = {
    try {
      get(svs.get(MongoAnalyticsHost) | "localhost")
    } catch {
      case e: ConnectException => throw new DatastoreException(e.getMessage, e)
      case e: UnknownHostException => throw new DatastoreException(e.getMessage, e)
      case e: MongoException => throw new DatastoreException(e.getMessage, e)
    }
  }

  def getAggregatedAnalyticsDB: DB = {
    get(svs.getOrDie(MongoAggregatedAnalyticsHost)).getDB(svs.getOrDie(MongoAggregatedAnalyticsDB))
  }

  def getAnalyticsDB: DB = {
    get(svs.getOrDie(MongoAnalyticsHost)).getDB(svs.getOrDie(MongoAnalyticsDB))
  }

  def get(host: String): Mongo = {
    MongoFactory.cachedConnections.getOrElseUpdate(host, new MongoClient(host, mongoClientOptions))
  }

}

private object MongoFactory {
  lazy val cachedConnections = new JConcurrentMapWrapper(new ConcurrentHashMap[String, Mongo]())
}
