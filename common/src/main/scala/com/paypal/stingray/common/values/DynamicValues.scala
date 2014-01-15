package com.paypal.stingray.common.values

import com.paypal.stingray.common.logging.LoggingSugar
import scala.concurrent._
import scala.concurrent.duration._

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 3/13/13
 * Time: 6:05 PM
 */
abstract class DynamicValues(svs: Option[StaticValues],
                             ttl: Int = DynamicValues.DefaultTTL)
  extends Values
  with LoggingSugar {

  import DynamicValues._

  def this(svs: StaticValues) = this(Some(svs))
  def this(name: String) = this(Some(new StaticValues(name)))

  private val logger = getLogger[DynamicValues]
  protected implicit def seqCtx: ExecutionContext

  protected def attempt(key: String): Future[Option[String]]

  // async pattern
  def getAsync(key: String): Future[Option[String]] = {
    val dvFuture = attempt(key)
    val svOption = svs.flatMap(_.get(key))

    dvFuture.onFailure {
      case e => logger.error("Dynamic Value Server is down, defaulting to Static Values", e)
    }

    dvFuture.map { mbValue =>
      mbValue.orElse(svOption)
    }
  }

  // synchronous pattern
  override def get(key: String): Option[String] = {
    Await.result(attempt(key), SynchronousTimeout)
  }

  def set(key: String, value: String): Future[Unit]

  def delete(key: String): Future[Boolean]

  def getAll: Future[Map[String, String]]
}

object DynamicValues {
  val DefaultTTL = 60000
  val SynchronousTimeout = 5.seconds
}
