package com.paypal.stingray.common.env

import java.net._
import scala.collection.mutable.ListBuffer
import com.paypal.stingray.common.logging.LoggingSugar

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 3/1/12
 * Time: 1:48 PM
 */

trait EnvironmentCommon extends LoggingSugar {

  private val envLogger = getLogger[EnvironmentCommon]

  lazy val getFullHostname: String = {
    try {
      val addr = InetAddress.getLocalHost
      val hostname = addr.getHostName
      if (hostname.endsWith(".local")) {
        hostname.replace(".local", ".int.mob")
      } else
        hostname
    } catch {
      case e: UnknownHostException => {
        envLogger.error("Can't get hostname")
        "hostname"
      }
    }
  }

  /**
   * Get a truncated version of the hostname, e.g. kratos.int.mob gets converted to kratos.  Used by tests
   * and the dev environment, not intended for production use.
   */
  lazy val getSubdomain: String = {
    val subdomain = for {
      hostname <- Option(getFullHostname)
      noDots <- Option(hostname.takeWhile(_ != '.'))
      noPunc <- Option(noDots.filter(c => (c != '-') && (c != '\'')))
      lower <- Option(noPunc.toLowerCase)
    } yield lower
    subdomain.orNull
  }

  /**
   * Get an IP address that can be used to address hosts in production,
   * for storage in zookeeper
   */
  lazy val getInternalIPAddress: String = {
    val addrList = ListBuffer[InetAddress]()
    var failed = true
    var tries = 0

    while (failed) {
      tries += 1
      try {
        val networkInterfaces = NetworkInterface.getNetworkInterfaces
        while (networkInterfaces.hasMoreElements) {
          val networkInterface = networkInterfaces.nextElement
          if (networkInterface.isUp) {
            val addresses = networkInterface.getInetAddresses
            while (addresses.hasMoreElements) {
              addrList += addresses.nextElement()
            }
          }
        }
        failed = false
      } catch {
        case e: SocketException => {
          if (tries >= 3) {
            envLogger.error("getInternalIPAddress failed: SocketException.  Even tried " + tries + " times")
            throw e
          }
        }
      }
    }

    val addrStrs = addrList.map(_.toString.trim.replace("/",""))
    //dev and staging internal IPs will not exist in prod as per Jay Aras, so this algorithm works fine
    addrStrs.find(_.startsWith("172.")) | {
      addrStrs.find(_.startsWith("10.")) | {
        envLogger.error("getInternalIPAddress failed: found " + addrList.size + " addresses but none started with 172. or 10.")
        throw new SocketException("No internal IP address")
      }
    }
  }
}

