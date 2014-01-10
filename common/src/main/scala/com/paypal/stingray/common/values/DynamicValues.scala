package com.paypal.stingray.common.values

import scalaz._
import Scalaz._
import com.paypal.stingray.common.logging.LoggingSugar
import scala.concurrent._

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 3/13/13
 * Time: 6:05 PM
 */
abstract class DynamicValues(svs: Option[StaticValues], ttl: Int = DynamicValues.DefaultTTL) extends Values[Future] with LoggingSugar {

  def this(svs: StaticValues) = this(svs.some)
  def this(name: String) = this(new StaticValues(name).some)

  private val logger = getLogger[DynamicValues]
  protected implicit def seqCtx: ExecutionContext
  protected implicit def monad: Monad[Future]

  protected def attempt(key: String): Future[Option[String]]

  override def get(key: String): Future[Option[String]] = {
    val dvFuture = attempt(key)
    val svOption = svs.flatMap(_.get(key))

    dvFuture.onFailure {
      case e => logger.error("Dynamic Value Server is down, defaulting to Static Values", e)
    }

    dvFuture.map { mbValue =>
      mbValue.orElse(svOption)
    }
  }

  def set(key: String, value: String): Future[Unit]

  def delete(key: String): Future[Boolean]

  def getAll: Future[Map[String, String]]
}

object DynamicValues {
  val DefaultTTL = 60000
}
