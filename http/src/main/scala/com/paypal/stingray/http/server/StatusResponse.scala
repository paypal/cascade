package com.paypal.stingray.http.server

import com.paypal.stingray.common.constants.CommonConstants
import com.paypal.stingray.common.logging.LoggingSugar
import com.paypal.stingray.http.headers._
import StatusResponse._
import org.slf4j.LoggerFactory
import com.paypal.stingray.common.values.{StaticValues, BuildStaticValues}


/**
 * Created by drapp on 6/28/13.
 */
case class StatusResponse(status: String,
                          serviceName: String,
                          buildTag: String,
                          dependencies: List[String])

object StatusResponse {

  private lazy val logger = LoggerFactory.getLogger(getClass)

  private[http] val statusJSONKey = "status"
  private[http] val serviceNameJSONKey = "service-name"
  private[http] val buildTagJSONKey = "build-tag"
  private[http] val dependenciesJSONKey = "dependencies"

  // TODO: JSON reader/writer

  def getStatusResponse(svs: StaticValues, serviceName: String): StatusResponse = {
    val partialStatus = svs.get("build.tag").map { buildTag =>
      StatusResponse("ok", serviceName, buildTag, _: List[String])
    }.getOrElse {
      StatusResponse("error", serviceName, "unknown", _: List[String])
    }
    val dependencies = svs.get("service.dependencies").map(_.split(",")).getOrElse(Array())
    val status = partialStatus(dependencies.toList)
    logger.debug(s"Status Response - status: ${status.status}, build tag: ${status.buildTag}, dependencies: ${dependencies.mkString(",")}")
    status
  }

}

