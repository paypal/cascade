package com.paypal.stingray.services

import com.rabbitmq.client._
import com.rabbitmq.client.QueueingConsumer.Delivery
import com.paypal.stingray.common.json.JSONUtil
import com.paypal.stingray.common.logging.LoggingSugar
import com.paypal.stingray.common.validation._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import com.paypal.stingray.common.json.jsonscalaz._
import org.slf4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 11/20/12
 * Time: 4:42 PM
 */
abstract class SimpleRabbitMQService[T: JSON] extends LoggingSugar {
  def logger: Logger
  def hosts: List[String]
  def exchange: String
  def topic: String
  def queueName: String
  def readTimeout: Int
  def basicQos: Option[Int] = None

  class ReloadingChannel {

    private val factory: ConnectionFactory = new ConnectionFactory
    private var channel: Channel = newChannel

    def ensureOpen(): Channel = {
      if(!channel.isOpen) {
        channel = newChannel
      }
      channel
    }

    def newChannel: Channel = {
      val addresses: Array[Address] = hosts.map(Address.parseAddress).toArray
      val connection = factory.newConnection(addresses)
      val createdChannel = connection.createChannel()
      createdChannel.exchangeDeclare(exchange, "topic")

      //durable, non-exclusive, non-autodelete
      createdChannel.queueDeclare(queueName, true, false, false, null)
      createdChannel.queueBind(queueName, exchange, topic)

      // set # of messages assigned per worker
      basicQos.foreach(createdChannel.basicQos(_))

      createdChannel
    }

  }

  private lazy val localChannel = new ReloadingChannel

  def enqueue(message: T): ThrowableValidation[Unit] = {
    enqueue(message, localChannel)
  }

  def enqueue(message: T, channel: ReloadingChannel): ThrowableValidation[Unit] = {
    logger.debug("enqueuing message onto rabbit queue %s".format(queueName.toString))
    validating {
      channel.ensureOpen().basicPublish(exchange, topic, null,
        JSONUtil.prepare(toJSON(message)).getBytes("UTF-8"))
    }
  }

  /**
   * Dequeue a message. Returns none if there is none.
   * @param handler determine whether the dequeue was a success and either ack or nack the queue.
   *                Called with the same object that gets returned. default is success
   * @return the dequeued object
   */
  private def dequeue(reader: Reader)(handler: (T => Boolean) = { _ => true }): Option[T] = {
    for {
      delivery <- reader.getNextDelivery(readTimeout)
      message <- {
        asT(delivery)
      }
      _ <- {
        logger.debug("dequeueing message from rabbit queue %s".format(queueName))
        if(handler(message)) reader.basicAck(delivery) else reader.basicNack(delivery)
      }
    } yield message
  }

  private def asT(del: Delivery): Option[T] = {
    (for {
      json <- validating {parse(new String(del.getBody, "UTF-8"))} mapFailure { error =>
        logger.error("Invalid json in RabbitMQ " + error.getMessage)
      }
      message <- fromJSON[T](json).mapFailure { errors =>
        logger.error("Invalid json in RabbitMQ: " + new String(del.getBody, "UTF-8") + " " + JSONUtil.prepare(toJSON(errors)))
      }
    } yield {
      message
    }).toOption
  }

  val defaultDequeueCount = 10


  def queueLength: Int = localChannel.ensureOpen().queueDeclarePassive(queueName).getMessageCount

  def isEmpty: Boolean = queueLength === 0

  protected def dequeueRunnable(process: T => ThrowableValidation[Unit]) = new QueuePollerRunnable {

    private lazy val reader = new Reader(new ReloadingChannel, queueName)

    var running = true
    def run() {
      logger.debug("QueuePoller starting")
      while (running) {
        try {
          dequeue(reader)({ msg =>
            process(msg).mapFailure({ e =>
              logger.error("Queue processing function returned an error", e)
            }).isSuccess
          })
        } catch {
          case e: Throwable => logger.error("Dequeue thread crashed, restarting", e)
        }
      }
    }

    def stop() {
      running = false
    }
  }

  private class Reader(channel: ReloadingChannel, queueName: String) {
    private var consumer: QueueingConsumer = newConsumer()
    basicConsume()

    def newConsumer() = new QueueingConsumer(channel.ensureOpen())

    def basicConsume() = channel.ensureOpen().basicConsume(queueName, false, consumer)

    def basicAck(delivery: Delivery) =  channel.ensureOpen().basicAck(delivery.getEnvelope.getDeliveryTag, false).some
    def basicNack(delivery: Delivery) =  channel.ensureOpen().basicNack(delivery.getEnvelope.getDeliveryTag, false, false).some

    def getNextDelivery(readTimeout: Int): Option[Delivery] = {
      //when an rmq node goes down, the channel still claims to be open, but this gets thrown
      try {
        Option(consumer.nextDelivery(readTimeout))
      } catch {
        case t: Throwable => {
          //reset the queue, return none as if there was nothing to retrieve and let it pick up next poll
          try {
            logger.error("dequeue failed: %s".format(t.getMessage), t)
            consumer = newConsumer()
            basicConsume()
            None
          } catch {
            case t: Throwable => {
              logger.error("RABBITMQ RECONNECT FAILED: ALL RABBITS MAY BE DOWN. PANIC?", t)
              None
            }
          }
        }
      }
    }
  }

}

trait QueuePollerRunnable extends Runnable {
  def stop()
}

