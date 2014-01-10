package com.paypal.stingray.common.logging

import scalaz._
import Scalaz._
import com.paypal.stingray.common.validation._
import com.paypal.stingray.common.api.DeployState
import java.util.Date
import java.util.regex.Pattern
import com.paypal.stingray.common.primitives._
import com.paypal.stingray.common.db.mongo.MongoFactory
import com.paypal.stingray.common.db.mongo.MongoConstants._
import net.liftweb.json._
import java.text.SimpleDateFormat
import com.paypal.stingray.common.constants.CommonConstants._
import com.mongodb._
import com.paypal.stingray.common.json.JSONUtil._
import scala.collection.JavaConverters._
import ch.qos.logback.classic.spi.{ThrowableProxyUtil, LoggingEvent}
import ch.qos.logback.classic.Level
import com.paypal.stingray.common.constants.ValueConstants._
import com.paypal.stingray.common.values.StaticValues
import com.paypal.stingray.common.constants.ValueConstants

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 9/30/11
 * Time: 1:02 PM
 */

class LoggerDatastoreService(svs: StaticValues) {

  private lazy val mongoFactory = new MongoFactory(svs)

  private val formats = new DefaultFormats {
    override def dateFormatter: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  }

  def logSetup(clientId: ClientId, appId: AppId, deployState: DeployState) {
    getOrCreateCappedCollection(clientId, appId, deployState)
  }

  def logCleanup(clientId: ClientId) {
    getDBForClient(clientId).dropDatabase()
  }

  def logEvent(event: LoggingEvent) {
    val mdc = event.getMDCPropertyMap
    val clientId = mdc.get(MDC_CLIENT_ID).toLong
    val appId = mdc.get(MDC_APP_ID).toLong
    val version = mdc.get(MDC_VERSION).toInt
    val deployState = if (version > 0) DeployState.Deployed else DeployState.Sandbox
    val dbCol = getOrCreateCappedCollection(ClientId(clientId), AppId(appId), deployState)
    dbCol.insert(getLogForEvent(event))
  }

  def query(clientId: ClientId,
                     appId: AppId,
                     deployState: DeployState,
                     minLevel: Level = Level.TRACE,
                     since: Option[Date] = none,
                     ascendingDate: Boolean = false,
                     filter: Option[Pattern] = none,
                     skip: Int = 0,
                     limit: Int = 0,
                     tail: Boolean = false): Iterator[Log] = {
    val dbCol = getOrCreateCappedCollection(clientId, appId, deployState)
    val builder = BasicDBObjectBuilder.start()

    deployState match {
      case DeployState.Sandbox => builder.add(LoggerDatastoreService.VERSION, 0)
      case DeployState.Deployed => builder.add(LoggerDatastoreService.VERSION, new BasicDBObject(GTE, 1))
    }

    if (minLevel != Level.TRACE) {
      builder.add(LoggerDatastoreService.LEVEL, new BasicDBObject(GTE, minLevel.levelInt))
    }

    since foreach { s =>
      builder.add(LoggerDatastoreService.TIMESTAMP, new BasicDBObject(GTE, formats.dateFormat.format(s)))
    }

    filter foreach { f =>
      val regex = new BasicDBList()
      regex.add(new BasicDBObject(LoggerDatastoreService.MESSAGE, f))
      regex.add(new BasicDBObject(LoggerDatastoreService.EXCEPTION, f))
      builder.add(OR, regex)
    }

    var cursor = dbCol.find(builder.get()).skip(skip).limit(limit)

    if (!ascendingDate && !tail) {
      cursor = cursor.sort(new BasicDBObject(NATURAL, -1))
    }

    if (tail) {
      cursor = cursor.addOption(Bytes.QUERYOPTION_TAILABLE)
    }

    for (r <- cursor.iterator().asScala) yield deserialize[Log](r.toString, formats)
  }

  private def getLogForEvent(event: LoggingEvent): DBObject = {
    val mdc = event.getMDCPropertyMap

    val builder = BasicDBObjectBuilder.start().
      add(LoggerDatastoreService.TIMESTAMP, formats.dateFormat.format(new Date(event.getTimeStamp))).
      add(LoggerDatastoreService.MESSAGE, event.getFormattedMessage).
      add(LoggerDatastoreService.LEVEL, event.getLevel.levelInt).
      add(LoggerDatastoreService.LOGGER, event.getLoggerName).
      add(LoggerDatastoreService.APP_ID, mdc.get(MDC_APP_ID).toLong).
      add(LoggerDatastoreService.CLIENT_ID, mdc.get(MDC_CLIENT_ID).toLong).
      add(LoggerDatastoreService.APP_NAME, mdc.get(MDC_APP_NAME)).
      add(LoggerDatastoreService.CLIENT_NAME, mdc.get(MDC_DOMAIN_NAME)).
      add(LoggerDatastoreService.VERSION, mdc.get(MDC_VERSION).toInt)

    Option(event.getThrowableProxy).foreach { _ =>
      builder.add(LoggerDatastoreService.EXCEPTION, ThrowableProxyUtil.asString(event.getThrowableProxy))
    }

    builder.get
  }

  private def getDBForClient(clientId: ClientId): DB = {
    val mongo = mongoFactory.get(svs.getOrDie(ValueConstants.MongoLoggingHost))
    mongo.getDB(getDatabaseName(clientId))
  }

  private def getOrCreateCappedCollection(clientId: ClientId, appId: AppId, deployState: DeployState): DBCollection = {
    val db = getDBForClient(clientId)
    val size = MongoCappedSize
    val builder = BasicDBObjectBuilder.start().add("capped", true).add("size", size).add("autoIndexId", true)

    validating {
      db.createCollection(getCollectionName(appId), builder.get)
    } mapFailureOrThrow {
      case e: MongoException => // capped collection already exists
    }

    db.getCollection(getCollectionName(appId))
  }

  private def getDatabaseName(clientId: ClientId): String = {
    "%s-%s".format(clientId.toString, MongoLoggingDBSuffix)
  }

  private def getCollectionName(appId: AppId): String = {
    "%s-%s".format(appId.toString, MongoLoggingColSuffix)
  }
}

object LoggerDatastoreService {

  val TIMESTAMP = "ts"
  val MESSAGE = "msg"
  val LEVEL = "lvl"
  val LOGGER = "log"
  val APP_ID = "aid"
  val CLIENT_ID = "cid"
  val APP_NAME = "a"
  val CLIENT_NAME = "c"
  val VERSION = "v"
  val EXCEPTION = "ex"

}

case class Log(msg: String, lvl: Int, log: String, aid: AppId, cid: ClientId, a: String, c: String, v: APIVersionNumber, ex: Option[String], ts: Date)

